package ru.demo.messenger.chats.single.future;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import android.util.LongSparseArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import ru.demo.domain.message.MessageDataSource;
import ru.demo.messenger.chats.single.future.message.ForwardFutureMessage;
import ru.demo.messenger.chats.single.future.message.FutureMessage;
import ru.demo.messenger.chats.single.future.message.InvalidFutureMessage;
import ru.demo.messenger.chats.single.future.message.ReplyFutureMessage;
import ru.demo.messenger.chats.single.future.message.StickerFutureMessage;
import ru.demo.messenger.chats.single.future.message.TextFutureMessage;
import ru.demo.messenger.data.message.MessageModel;
import ru.demo.messenger.internal.di.Injection;
import rx.Single;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

import static ru.demo.messenger.chats.single.future.ReceivedMessageForBroadcast.ofError;
import static ru.demo.messenger.chats.single.future.ReceivedMessageForBroadcast.ofSuccessfulSend;

public class FutureMessagesService extends Service {

    public static final String ACTION_SEND_MESSAGE = "ru.demo.messenger.action.send_message";

    public static final String EXTRA_CHAT_MESSAGE = "extra.CHAT_MESSAGE";

    private static final long REPEAT_AWAKE_DELAY = TimeUnit.SECONDS.toMillis(5);

    private CompositeSubscription subscriptions;

    private FutureMessageStorage futureMessageStorage;
    private FutureMessageStorage.ChangeDataListener changeDataListener;

    private List<Long> executedChatId;

    // TODO: ET 12.12.16 Replace handler with retryWhen method on source observable
    private Handler repeatAwakeHandler;
    private Runnable repeatAwakeRunnable;

    private SendTextMessage sendTextMessage;
    private SendForwardMessage sendForwardMessage;
    private SendReplyMessage sendReplyMessage;
    private SendStickerMessage sendStickerMessage;

    public static Intent getIntent(Context context) {
        return new Intent(context, FutureMessagesService.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final MessageDataSource messageDataSource = Injection.provideMessageDataSource(this);
        final SendMessageHelper sendMessageHelper = new SendMessageHelper(Injection.provideUploadFiles());
        sendTextMessage = new SendTextMessage(messageDataSource, sendMessageHelper);
        sendForwardMessage = new SendForwardMessage(messageDataSource);
        sendReplyMessage = new SendReplyMessage(messageDataSource, sendMessageHelper);
        sendStickerMessage = new SendStickerMessage(messageDataSource);

        subscriptions = new CompositeSubscription();

        executedChatId = Collections.synchronizedList(new ArrayList<>());

        futureMessageStorage = FutureMessageStorage.getInstance();
        changeDataListener = new FutureMessageStorage.ChangeDataListener() {
            @Override
            public void onMessageAdded(FutureMessage message) {
                awakeExecutor();
            }

            @Override
            public void onMessageRemoved(long chatId) {

            }

            @Override
            public void onDroppedAll() {
                subscriptions.clear();
            }
        };
        futureMessageStorage.addChangeDataListener(changeDataListener);

        repeatAwakeHandler = new Handler();
        repeatAwakeRunnable = this::awakeExecutor;
    }

    private synchronized void awakeExecutor() {
        final LongSparseArray<List<FutureMessage>> allChats = futureMessageStorage.getAllChats();
        for (int i = 0; i < allChats.size(); i++) {
            final long key = allChats.keyAt(i);
            if (!executedChatId.contains(key)) {
                final List<FutureMessage> futureMessages = allChats.valueAt(i);
                if (futureMessages.size() > 0) {
                    executedChatId.add(key);
                    sendMessage(futureMessages.get(0));
                }
            }
        }
    }

    private void sendMessage(FutureMessage message) {
        subscriptions.add(
                createSendRequest(message)
                        .subscribeOn(Schedulers.io())
                        .subscribe(chatMessage -> {
                                    executedChatId.remove(message.getChatId());
                                    futureMessageStorage.remove(message.getChatId());

                                    awakeExecutor();

                                    sendMessageEvent(ofSuccessfulSend(chatMessage));
                                },
                                throwable -> {
                                    executedChatId.remove(message.getChatId());

                                    if (throwable instanceof InvalidFutureMessage) {
                                        futureMessageStorage.remove(message.getChatId());
                                        sendMessageEvent(ofError(message.getChatId()));
                                    } else {
                                        startDelayedAwake();
                                    }
                                }
                        )
        );
    }

    private Single<MessageModel> createSendRequest(FutureMessage message) {
        if (message instanceof ForwardFutureMessage) {
            return sendForwardMessage.execute((ForwardFutureMessage) message);
        } else if (message instanceof ReplyFutureMessage) {
            return sendReplyMessage.execute((ReplyFutureMessage) message);
        } else if (message instanceof TextFutureMessage) {
            return sendTextMessage.execute((TextFutureMessage) message);
        } else if (message instanceof StickerFutureMessage) {
            return sendStickerMessage.execute((StickerFutureMessage) message);
        } else {
            return Single.error(new InvalidFutureMessage());
        }
    }

    private void sendMessageEvent(ReceivedMessageForBroadcast event) {
        final Intent data = new Intent(ACTION_SEND_MESSAGE);
        data.putExtra(EXTRA_CHAT_MESSAGE, event);
        sendBroadcast(data);
    }

    private void startDelayedAwake() {
        // TODO: ET 12.12.16 need pause delayed awake when device gonna sleep - prevent device wake up
        // stop repeating when device gonna sleep. Resume when device waking up.
        // or stop repeating when no connection. Resume when connection is on
        repeatAwakeHandler.removeCallbacks(repeatAwakeRunnable);
        repeatAwakeHandler.postDelayed(repeatAwakeRunnable, REPEAT_AWAKE_DELAY);
    }

    @Override
    public void onDestroy() {
        repeatAwakeHandler.removeCallbacks(repeatAwakeRunnable);
        subscriptions.clear();
        futureMessageStorage.removeChangeDataListener(changeDataListener);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
