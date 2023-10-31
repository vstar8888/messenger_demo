package ru.demo.messenger.chats.single.info;

import android.content.Context;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import biz.growapp.base.loading.BaseLoadingView;
import biz.growapp.base.loading.BasePresenter;
import ru.demo.messenger.data.message.MessageInfoUser;
import ru.demo.messenger.data.message.MessageModel;
import ru.demo.messenger.network.BaseSubscriber;
import ru.demo.messenger.network.DownloadFileHelper;
import ru.demo.messenger.network.RequestManager;
import ru.demo.messenger.network.response.MessageInfoResponse;
import ru.demo.messenger.network.response.base.ServerError;
import ru.demo.messenger.network.services.MessagesService;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

class MessageInfoPresenter extends BasePresenter<MessageInfoPresenter.View> {

    interface View extends BaseLoadingView {
        void onMessageInfoLoaded(List<Object> usersDelivered,
                                 List<Object> usersIsRead,
                                 MessageModel message
        );
    }

    private final MessagesService messagesService;
    private final MessageModel message;
    private final DownloadFileHelper downloadFileHelper;

    MessageInfoPresenter(Context context, @NonNull View view, MessageModel message) {
        super(view);
        messagesService = RequestManager.createService(MessagesService.class);
        this.message = message;
        downloadFileHelper = new DownloadFileHelper(context);
    }

    public MessageModel getMessage() {
        return message;
    }

    void getMessageInfo() {
        subscriptions.add(
                messagesService.getMessageInfo(message.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BaseSubscriber<MessageInfoResponse>() {
                            @Override
                            public void onNext(MessageInfoResponse result) {
                                List<Object> usersDelivered = new ArrayList<>();
                                List<Object> usersIsRead = new ArrayList<>();
                                for (int i = 0; i < result.messageInfoUsers.size(); i++) {
                                    MessageInfoUser user = result.messageInfoUsers.get(i);
                                    if (InfoReadStatus.ISREAD.equals(user.getStatus())) {
                                        usersIsRead.add(user);
                                    } else {
                                        usersDelivered.add(user);
                                    }
                                }
                                getView().onMessageInfoLoaded(usersDelivered, usersIsRead, message);
                                getView().switchToMain(true);
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().switchToError(true, error.getMessage());
                            }
                        })
        );
    }


    void downloadFile(Context context, String sourceUrl, String filename) {
        subscriptions.add(downloadFileHelper.downloadFile(context, sourceUrl, filename)
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {}, throwable -> {})
        );
    }

}
