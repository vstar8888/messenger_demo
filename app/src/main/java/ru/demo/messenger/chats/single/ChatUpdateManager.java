package ru.demo.messenger.chats.single;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import ru.demo.messenger.network.BaseSubscriber;
import ru.demo.messenger.network.RequestManager;
import ru.demo.messenger.network.response.ChatList;
import ru.demo.messenger.network.response.base.ServerError;
import ru.demo.messenger.network.services.MessagesService;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ChatUpdateManager {

    private final MessagesService messagesService;
    private boolean isRequestActive;
    private List<ChatUpdateListenerHolder> updateHolders;

    public interface Listener {
        void onNext(ChatList result, boolean needOnlyParticipantsUpdate);
        void onError(ServerError error);
    }

    public ChatUpdateManager() {
        messagesService = RequestManager.createService(MessagesService.class);
        isRequestActive = false;
        updateHolders = new ArrayList<>();
    }

    public void addListener(long chatId, Listener listener) {
        updateHolders.add(
                new ChatUpdateListenerHolder(chatId, listener)
        );
    }

    public void removeListener(Listener listener) {
        for (int i = 0; i < updateHolders.size(); i++) {
            ChatUpdateListenerHolder updateHolder = updateHolders.get(i);
            if (updateHolder.listener.equals(listener)) {
                updateHolders.remove(i);
            }
        }
    }

    public @Nullable Subscription getChatById(long chatId, boolean needOnlyParticipantsUpdate) {
        if (isRequestActive) {
            return null;
        }
        isRequestActive = true;
        return messagesService.getChat(chatId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doAfterTerminate(() -> isRequestActive = false)
                .subscribe(new BaseSubscriber<ChatList>() {
                    @Override
                    public void onNext(ChatList result) {
                        for (int i = 0; i < updateHolders.size(); i++) {
                            ChatUpdateListenerHolder updateHolder = updateHolders.get(i);
                            if (updateHolder.chatId == chatId) {
                                updateHolder.listener.onNext(result, needOnlyParticipantsUpdate);
                            }
                        }
                    }

                    @Override
                    public void onError(ServerError error) {
                        for (int i = 0; i < updateHolders.size(); i++) {
                            ChatUpdateListenerHolder updateHolder = updateHolders.get(i);
                            if (updateHolder.chatId == chatId) {
                                updateHolder.listener.onError(error);
                            }
                        }
                    }
                });
    }

}
