package ru.demo.messenger.chats.single.settings.rename;

import androidx.annotation.NonNull;

import biz.growapp.base.loading.BaseLoadingView;
import biz.growapp.base.loading.BasePresenter;
import ru.demo.messenger.data.chat.ChatModel;
import ru.demo.messenger.network.BaseSubscriber;
import ru.demo.messenger.network.RequestManager;
import ru.demo.messenger.network.response.base.BaseResponse;
import ru.demo.messenger.network.response.base.ServerError;
import ru.demo.messenger.network.services.MessagesService;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

class RenameChatPresenter extends BasePresenter<RenameChatPresenter.View> {

    interface View extends BaseLoadingView {
        void onChatRenamed(String name);
    }

    private final MessagesService messagesService;

    RenameChatPresenter(@NonNull View view) {
        super(view);
        messagesService = RequestManager.createService(MessagesService.class);
    }

    void renameChat(ChatModel chat, String name) {
        getView().switchToLoading(true);
        subscriptions.add(
                messagesService.renameChat(name, chat.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BaseSubscriber<BaseResponse>() {
                            @Override
                            public void onNext(BaseResponse result) {
                                chat.setTitle(name);
                                getView().onChatRenamed(name);
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().switchToError(true, error.getMessage());
                            }
                        }));
    }

}