package ru.demo.messenger.chats.list;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.List;

import biz.growapp.base.loading.BaseLoadingView;
import biz.growapp.base.loading.BasePresenter;
import retrofit2.Response;
import ru.demo.messenger.Consts;
import ru.demo.messenger.MainApp;
import ru.demo.messenger.chats.single.MessageReadStatus;
import ru.demo.messenger.data.chat.ChatModel;
import ru.demo.messenger.data.chat.MessageReadEvent;
import ru.demo.messenger.data.message.MessageModel;
import ru.demo.messenger.data.user.UserModel;
import ru.demo.messenger.main.MainActivity;
import ru.demo.messenger.network.BaseSubscriber;
import ru.demo.messenger.network.RequestManager;
import ru.demo.messenger.network.response.ChatList;
import ru.demo.messenger.network.response.base.BaseResponse;
import ru.demo.messenger.network.response.base.ServerError;
import ru.demo.messenger.network.services.MessagesService;
import ru.demo.messenger.people.OnlineUsersHolder;
import ru.demo.messenger.utils.Prefs;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

class ChatsPresenter extends BasePresenter<ChatsPresenter.View> {

    public interface View extends BaseLoadingView {
        void onChatsLoaded(List<ChatModel> chats);

        void addOrUpdateChat(ChatModel chatModel);

        void onAddChatClick(MainActivity.ActionClickEvent event);

        void onReceiveMessage(MessageModel message);

        void onMessageRead(long chatId, long messageId);

        void onUserOnlineStatusChanged(OnlineUsersHolder.UserStatusUpdate status);

        void onMessageDelivered(long chatId, @MessageModel.ReadStatus String status);

        void onChatRemoved(ChatModel chatModel);

        void updateChatPhoto(long chatId, String photo);
    }

    private final MessagesService messagesService;
    private boolean isNoMoreData = false;
    private String loadMoreUrl;
    private boolean firstLoaded = true;
    private final long selfUserId;

    private Subscription addChatClickSubscription;
    private Subscription changeOnlineStatusSubscription;


    ChatsPresenter(@NonNull View view) {
        super(view);
        this.messagesService = RequestManager.createService(MessagesService.class);
        subscribeAddChatClick();
        selfUserId = Prefs.get().getLong(Consts.Prefs.USER_ID, 0);
    }

    private void subscribeAddChatClick() {
        addChatClickSubscription =
                MainApp.globalBus.observeEvents(MainActivity.ActionClickEvent.class)
                        .observeOn(AndroidSchedulers.mainThread())
                        .filter(event ->
                                MainActivity.TAB_POSITION_MESSAGE == event.getTabPosition())
                        .subscribe(event -> getView().onAddChatClick(event));
    }

    private void subscribeToReceiveMessage() {
        subscriptions.add(
                MainApp.globalBus.observeEvents(MessageModel.class)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(message -> getView().onReceiveMessage(message))
        );
    }

    private void subscribeToDeliveringMessage() {
        subscriptions.add(
                MainApp.globalBus.observeEvents(MessageReadEvent.class)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(message -> getView().onMessageDelivered(message.getChatId(),
                                message.getStatus()))
        );
    }

    private void subscribeToReadingMessage() {
        subscriptions.add(
                MainApp.globalBus.observeEvents(MessageReadStatus.class)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(event -> getView().onMessageRead(event.getChatId(),
                                event.getMessageId()))
        );
    }

    private void subscribeToUpdateChatPhoto() {
        subscriptions.add(
                MainApp.globalBus.observeEvents(UpdateChatPhotoEvent.class)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(event ->
                                getView().updateChatPhoto(event.getChatId(), event.getPhoto())
                        )
        );
    }

    private void resetPagination() {
        isNoMoreData = false;
        loadMoreUrl = null;
    }

    void resetChats() {
        subscriptions.clear();
        firstLoaded = true;
        resetPagination();
        getActiveChats();
    }

    void getActiveChats() {
        if (isNoMoreData) {
            getView().onChatsLoaded(Collections.emptyList());
            return;
        }
        if (loadMoreUrl == null) {
            subscribe(messagesService.getActiveChats());
        } else {
            subscribe(messagesService.getChats(loadMoreUrl));
        }
    }

    private void subscribe(Observable<Response<ChatList>> chats) {
        subscriptions.add(
                chats.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BaseSubscriber<ChatList>() {
                            @Override
                            public void onNext(ChatList result) {
                                loadMoreUrl = result.load_more_url;
                                if (loadMoreUrl == null) {
                                    isNoMoreData = true;
                                }
                                getView().onChatsLoaded(result.chats);

                                if (firstLoaded) {
                                    firstLoaded = false;
                                    subscribeToReceiveMessage();
                                    subscribeToDeliveringMessage();
                                    subscribeToReadingMessage();
                                    subscribeToUpdateChatPhoto();
                                }
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().switchToError(true, error.getMessage());
                            }
                        })
        );
    }

    void getChatById(long chatId) {
        subscriptions.add(
                messagesService.getChat(chatId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BaseSubscriber<ChatList>() {
                            @Override
                            public void onNext(ChatList result) {
                                if (result.chats == null || result.chats.isEmpty()) {
                                    return;
                                }
                                getView().addOrUpdateChat(result.chats.get(0));
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().switchToError(true, error.getMessage());
                            }
                        })
        );
    }

    void removeChat(ChatModel chatModel) {
        getView().switchToLoading(true);
        subscriptions.add(
                messagesService.deleteChain(chatModel.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doAfterTerminate(() -> getView().switchToMain(true))
                        .subscribe(new BaseSubscriber<BaseResponse>() {
                            @Override
                            public void onNext(BaseResponse result) {
                                getView().onChatRemoved(chatModel);
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().showErrorDialog(error.getMessage());
                            }
                        })
        );
    }

    void leaveGroup(ChatModel chat) {
        getView().switchToLoading(true);
        subscriptions.add(
                messagesService.leaveGroup(chat.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doAfterTerminate(() -> getView().switchToMain(true))
                        .subscribe(new BaseSubscriber<BaseResponse>() {
                            @Override
                            public void onNext(BaseResponse result) {
                                chat.setInChain(false);
                                removeChatUser(chat, selfUserId);
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().showErrorDialog(error.getMessage());
                            }
                        })
        );
    }

    private void removeChatUser(ChatModel chat, long userId) {
        for (int i = chat.getUsers().size() - 1; i >= 0; i--) {
            UserModel user = chat.getUsers().get(i);
            if (user.getId() == userId) {
                chat.getUsers().remove(i);
                return;
            }
        }
    }

    void onResume() {
        changeOnlineStatusSubscription = MainApp.globalBus
                .observeEvents(OnlineUsersHolder.UserStatusUpdate.class)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(status -> getView().onUserOnlineStatusChanged(status));
    }

    void onPause() {
        changeOnlineStatusSubscription.unsubscribe();
    }

    @Override
    public void onDestroyView() {
        resetPagination();
        addChatClickSubscription.unsubscribe();
        super.onDestroyView();
    }

}
