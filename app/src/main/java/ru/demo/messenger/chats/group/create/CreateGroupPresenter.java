package ru.demo.messenger.chats.group.create;

import android.content.Context;
import androidx.annotation.NonNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import biz.growapp.base.loading.BaseLoadingView;
import biz.growapp.base.loading.BasePresenter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import ru.demo.messenger.Consts;
import ru.demo.messenger.MainApp;
import ru.demo.messenger.chats.list.UpdateChatPhotoEvent;
import ru.demo.messenger.data.chat.ChatModel;
import ru.demo.messenger.data.user.UserModel;
import ru.demo.messenger.network.BaseSubscriber;
import ru.demo.messenger.network.RequestManager;
import ru.demo.messenger.network.response.ChatAvatarResponse;
import ru.demo.messenger.network.response.ChatResponse;
import ru.demo.messenger.network.response.UserSingle;
import ru.demo.messenger.network.response.base.ServerError;
import ru.demo.messenger.network.services.MessagesService;
import ru.demo.messenger.network.services.UserService;
import ru.demo.messenger.utils.BitmapUtils;
import ru.demo.messenger.utils.Prefs;
import rx.Observable;
import rx.Single;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

class CreateGroupPresenter extends BasePresenter<CreateGroupPresenter.CreateGroupView> {

    interface CreateGroupView extends BaseLoadingView {
        void onUserLoaded(UserModel user);

        void onChatCreated(ChatModel chat);

        void onPrepareChatCompleted(ChatModel chat);
    }

    private static final MediaType MEDIA_TYPE_IMAGE = MediaType.parse("image/jpeg");
    private final MessagesService messagesService;
    private final UserService userService;

    private final long myUserId;


    CreateGroupPresenter(@NonNull CreateGroupView view) {
        super(view);
        this.messagesService = RequestManager.createService(MessagesService.class);
        this.userService = RequestManager.createService(UserService.class);

        this.myUserId = Prefs.get().getLong(Consts.Prefs.USER_ID, 0L);
    }

    Single<File> compressAndRotate(@NonNull Context context, File original) {
        return BitmapUtils.compressImage(context, original)
                .subscribeOn(Schedulers.computation())
                .flatMap(compressedFile -> BitmapUtils.fixOrientation(context, compressedFile, original.getAbsolutePath()));
    }

    void createChat(List<UserModel> users, String groupName, String message) {
        String payload = UUID.randomUUID().toString();
        subscriptions.add(
                messagesService.createGroupChat(message, getUserIds(users), null, payload, groupName)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BaseSubscriber<ChatResponse>() {
                            @Override
                            public void onNext(ChatResponse result) {
                                getView().onChatCreated(result.chat);
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().switchToError(true, error.getMessage());
                            }
                        }));
    }

    void changeChatAvatar(File avatarFile, ChatModel chat) {
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
                        .subscribe(new BaseSubscriber<ChatAvatarResponse>() {
                            @Override
                            public void onNext(ChatAvatarResponse result) {
                                chat.setPhoto(result.chatAvatar);
                                getView().onPrepareChatCompleted(chat);
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().switchToError(true, error.getMessage());
                            }
                        }));
    }

    void getSelfUserForCreateChat() {
        final UserModel user = Prefs.load(Consts.Prefs.USER_DATA, UserModel.class);
        if (user == null) {
            getUser(myUserId);
        } else {
            getView().onUserLoaded(user);
        }
    }

    private void getUser(long userId) {
        subscriptions.add(
                userService.getUserById(userId)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BaseSubscriber<UserSingle>() {
                            @Override
                            public void onNext(UserSingle result) {
                                if (result.user.getId() == myUserId) {
                                    Prefs.save(Consts.Prefs.USER_DATA, result.user);
                                }
                                getView().onUserLoaded(result.user);
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().switchToError(true, error.getMessage());
                            }
                        }));
    }

    private List<Long> getUserIds(List<UserModel> users) {
        final ArrayList<Long> userIds = new ArrayList<>(users.size());
        for (int i = 0; i < users.size(); i++) {
            userIds.add(users.get(i).getId());
        }
        return userIds;
    }

    void sendUpdateChatEvent(ChatModel chat) {
        MainApp.globalBus.send(new UpdateChatPhotoEvent(chat.getId(), chat.getPhoto()));
    }

}
