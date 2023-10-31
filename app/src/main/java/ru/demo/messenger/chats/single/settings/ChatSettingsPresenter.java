package ru.demo.messenger.chats.single.settings;

import android.content.Context;
import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.growapp.base.loading.BaseLoadingView;
import biz.growapp.base.loading.BasePresenter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import ru.demo.messenger.Consts;
import ru.demo.messenger.MainApp;
import ru.demo.messenger.chats.single.ChatUpdateManager;
import ru.demo.messenger.data.chat.ChatModel;
import ru.demo.messenger.data.chat.LastChatMessage;
import ru.demo.messenger.data.message.MessageModel;
import ru.demo.messenger.data.user.UserModel;
import ru.demo.messenger.network.BaseSubscriber;
import ru.demo.messenger.network.RequestManager;
import ru.demo.messenger.network.response.ChatAvatarResponse;
import ru.demo.messenger.network.response.ChatList;
import ru.demo.messenger.network.response.MessageResponse;
import ru.demo.messenger.network.response.base.BaseResponse;
import ru.demo.messenger.network.response.base.ServerError;
import ru.demo.messenger.network.services.MessagesService;
import ru.demo.messenger.utils.BitmapUtils;
import ru.demo.messenger.utils.Prefs;
import rx.Observable;
import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

class ChatSettingsPresenter extends BasePresenter<ChatSettingsPresenter.View> {

    interface View extends BaseLoadingView {
        void onUsersAdded(ArrayList<UserModel> users);
        void onUserRemoved(int position);
        void onUserRemovedFromAdmins(int position);
        void setUserAsAdmin(int position);
        void onGroupLeaved();
        void setGroupAvatar(String chatAvatar);
        void showViewLoadingDialog();
        void dismissViewLoadingDialog();
        void updateUsers(List<Long> prevAdminsList, List<Long> newAdminsList, List<UserModel> users);
        void updateToolbarParticipantsAvatar(ChatModel chat);
        void removeMe(long selfUserId);
        void addMe();
        void setMeAsAdmin();
        void onChatRemoved();
    }

    private static final MediaType MEDIA_TYPE_IMAGE = MediaType.parse("image/jpeg");
    private final MessagesService messagesService;
    private ChatModel chat;
    private final long selfUserId;
    private ChatUpdateManager chatUpdateManager;

    ChatSettingsPresenter(@NonNull View view, ChatModel chat, ChatUpdateManager chatUpdateManager) {
        super(view);
        this.chat = chat;
        this.chatUpdateManager = chatUpdateManager;
        chatUpdateManager.addListener(chat.getId(), chatUpdateListener);
        messagesService = RequestManager.createService(MessagesService.class);
        selfUserId = Prefs.get().getLong(Consts.Prefs.USER_ID, 0);
        subscribeToReceiveMessage(selfUserId);
    }

    ChatModel getChat() {
        return chat;
    }

    boolean isUserAdmin(long userId) {
        return chat.getAdminsList().contains(userId);
    }

    boolean isMeAdmin() {
        return chat.isInChain() && isUserAdmin(selfUserId);
    }

    public long getSelfUserId() {
        return selfUserId;
    }

    private void updateLastMessage(MessageModel message) {
        chat.setLastMessage(new LastChatMessage(message));
    }

    private void removeChatUser(long userId) {
        for (int i = chat.getUsers().size() - 1; i >= 0; i--) {
            UserModel user = chat.getUsers().get(i);
            if (user.getId() == userId) {
                chat.getUsers().remove(i);
                return;
            }
        }
    }

    void addUsersToGroup(ArrayList<UserModel> users) {
        ArrayList<Long> userIds = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            userIds.add(users.get(i).getId());
        }
        getView().showViewLoadingDialog();
        subscriptions.add(
                messagesService.addParticipient(chat.getId(), userIds)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doAfterTerminate(() -> getView().dismissViewLoadingDialog())
                        .subscribe(new BaseSubscriber<MessageResponse>() {
                            @Override
                            public void onNext(MessageResponse result) {
                                updateLastMessage(result.message);
                                chat.getUsers().addAll(users);
                                getView().onUsersAdded(users);
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().switchToError(true, error.getMessage());
                            }
                        })
        );
    }

    void removeUserFromGroup(long userId, int position) {
        getView().showViewLoadingDialog();
        subscriptions.add(
                messagesService.removeParticipient(chat.getId(), Collections.singletonList(userId))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doAfterTerminate(() -> getView().dismissViewLoadingDialog())
                        .subscribe(new BaseSubscriber<MessageResponse>() {
                            @Override
                            public void onNext(MessageResponse result) {
                                updateLastMessage(result.message);
                                removeChatUser(userId);
                                getView().onUserRemoved(position);
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().switchToError(true, error.getMessage());
                            }
                        })
        );
    }

    void addToChainAdmins(long userId, int position) {
        getView().showViewLoadingDialog();
        subscriptions.add(
                messagesService.addToChainAdmins(chat.getId(), Collections.singletonList(userId))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doAfterTerminate(() -> getView().dismissViewLoadingDialog())
                        .subscribe(new BaseSubscriber<BaseResponse>() {
                            @Override
                            public void onNext(BaseResponse result) {
                                chat.getAdminsList().add(userId);
                                getView().setUserAsAdmin(position);
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().switchToError(true, error.getMessage());
                            }
                        })
        );
    }

    void removeFromChainAdmins(long userId, int position) {
        getView().showViewLoadingDialog();
        subscriptions.add(
                messagesService.removeFromChainAdmins(chat.getId(), Collections.singletonList(userId))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doAfterTerminate(() -> getView().dismissViewLoadingDialog())
                        .subscribe(new BaseSubscriber<BaseResponse>() {
                            @Override
                            public void onNext(BaseResponse result) {
                                chat.getAdminsList().remove(userId);
                                getView().onUserRemovedFromAdmins(position);
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().switchToError(true, error.getMessage());
                            }
                        })
        );
    }

    void leaveGroup() {
        getView().showViewLoadingDialog();
        subscriptions.add(
                messagesService.leaveGroup(chat.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doAfterTerminate(() -> getView().dismissViewLoadingDialog())
                        .subscribe(new BaseSubscriber<BaseResponse>() {
                            @Override
                            public void onNext(BaseResponse result) {
                                chat.setInChain(false);
                                removeChatUser(selfUserId);
                                getView().onGroupLeaved();
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().switchToError(true, error.getMessage());
                            }
                        })
        );
    }

    Single<File> compressAndRotate(@NonNull Context context, File original) {
        return BitmapUtils.compressImage(context, original)
                .subscribeOn(Schedulers.computation())
                .flatMap(compressedFile -> BitmapUtils.fixOrientation(context, compressedFile, original.getAbsolutePath()));
    }

    void changeChatAvatar(File avatarFile) {
        getView().showViewLoadingDialog();
        subscriptions.add(
                Observable.just(avatarFile)
                        .subscribeOn(Schedulers.computation())
                        .map(file -> MultipartBody.Part.createFormData("file", file.getName(),
                                RequestBody.create(MEDIA_TYPE_IMAGE, file))
                        )
                        .observeOn(Schedulers.io())
                        .flatMap(body -> messagesService.uploadChatAvatar(body, chat.getId()))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doAfterTerminate(() -> getView().dismissViewLoadingDialog())
                        .subscribe(new BaseSubscriber<ChatAvatarResponse>() {
                            @Override
                            public void onNext(ChatAvatarResponse result) {
                                chat.setPhoto(result.chatAvatar);
                                getView().setGroupAvatar(result.chatAvatar);
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().switchToError(true, error.getMessage());
                            }
                        }));
    }

    private void getChatById(long chatId) {
        Subscription getChatById = chatUpdateManager.getChatById(chatId, true);
        if (getChatById != null) {
            subscriptions.add(getChatById);
        }
    }

    private ChatUpdateManager.Listener chatUpdateListener = new ChatUpdateManager.Listener() {
        @Override
        public void onNext(ChatList result, boolean needOnlyParticipantsUpdate) {
            if (result.chats == null || result.chats.isEmpty()) {
                return;
            }
            List<Long> prevAdmins = new ArrayList<>();
            prevAdmins.addAll(chat.getAdminsList());
            chat = result.chats.get(0);
            getView().updateUsers(prevAdmins, chat.getAdminsList(), chat.getUsers());
            getView().updateToolbarParticipantsAvatar(chat);
        }

        @Override
        public void onError(ServerError error) {}
    };

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        chatUpdateManager.removeListener(chatUpdateListener);
    }

    private void subscribeToReceiveMessage(long selfUserId) {
        subscriptions.add(
                MainApp.globalBus.observeEvents(MessageModel.class)
                        .observeOn(AndroidSchedulers.mainThread())
                        .filter(message ->
                                message.getChatId() == chat.getId()
                        )
                        .subscribe(message -> {
                            if (MessageModel.MessageType.YOU_NEW_ADMIN.equals(message.getMessageType())) {
                                if (!chat.getAdminsList().contains(selfUserId)) {
                                    chat.getAdminsList().add(selfUserId);
                                }
                                getView().setMeAsAdmin();
                            }
                            if (message.isMeRemovedMessageType()) {
                                chat.setInChain(false);
                                chat.getAdminsList().remove(selfUserId);
                                getView().removeMe(selfUserId);
                            } else {
                                chat.setInChain(true);
                                getView().addMe();
                            }
                            if (MessageModel.MessageType.REMOVE_PARTICIPANT.equals(message.getMessageType()) ||
                                    MessageModel.MessageType.ADD_PARTICIPANT.equals(message.getMessageType())) {
                                getChatById(chat.getId());
                            }
                        })
        );
    }

    void removeChat() {
        getView().switchToLoading(true);
        subscriptions.add(
                messagesService.deleteChain(chat.getId())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doAfterTerminate(() -> getView().switchToMain(true))
                        .subscribe(new BaseSubscriber<BaseResponse>() {
                            @Override
                            public void onNext(BaseResponse result) {
                                chat = null;
                                getView().onChatRemoved();
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().showErrorDialog(error.getMessage());
                            }
                        })
        );
    }
}
