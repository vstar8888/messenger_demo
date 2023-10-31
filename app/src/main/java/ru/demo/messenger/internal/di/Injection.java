package ru.demo.messenger.internal.di;

import android.content.Context;
import androidx.annotation.NonNull;

import com.google.gson.Gson;

import java.lang.ref.WeakReference;

import ru.demo.data.RxSchedulers;
import ru.demo.data.auth.AuthRepository;
import ru.demo.data.device.PhotoRepository;
import ru.demo.data.feedback.FeedbackRepository;
import ru.demo.data.files.FilesNetworkApi;
import ru.demo.data.message.FrescoSmallCacheFetcher;
import ru.demo.data.message.MessageRepository;
import ru.demo.data.notifications.NotificationsRepository;
import ru.demo.data.user.UserRepository;
import ru.demo.domain.auth.AuthDataSource;
import ru.demo.domain.device.MakePhotoByCamera;
import ru.demo.domain.device.PhotoDataSource;
import ru.demo.domain.device.PickPhotosFromGallery;
import ru.demo.domain.feedback.FeedbackDataSource;
import ru.demo.domain.feedback.SendFeedbackUseCase;
import ru.demo.domain.files.UploadFiles;
import ru.demo.domain.message.MessageDataSource;
import ru.demo.domain.notifications.NotificationsDataSource;
import ru.demo.domain.notifications.SubscribeToPush;
import ru.demo.domain.user.InviteUserViaChat;
import ru.demo.domain.user.Logout;
import ru.demo.domain.user.UserDataSource;
import ru.demo.messenger.chats.single.ChatUpdateManager;
import ru.demo.messenger.helpers.ColorGenerator;
import ru.demo.messenger.helpers.CompositeImageHelper;
import ru.demo.messenger.utils.rx.RxSchedulersProvider;
import ru.demo.messenger.utils.rx.UiThread;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;

public final class Injection {

    private static WeakReference<ColorGenerator> colorGeneratorRef;
    private static WeakReference<ChatUpdateManager> chatUpdateManagerRef;
    private static WeakReference<FrescoSmallCacheFetcher> frescoSmallCacheFetcherRef;
    private static RxSchedulers rxSchedulers = new RxSchedulersProvider();
    @UiThread
    public static Scheduler uiScheduler = AndroidSchedulers.mainThread();

    @NonNull
    public synchronized static ColorGenerator provideColorGenerator(@NonNull Context context) {
        if (colorGeneratorRef == null || colorGeneratorRef.get() == null) {
            final ColorGenerator colorGenerator = new ColorGenerator(context);
            colorGeneratorRef = new WeakReference<>(colorGenerator);
            return colorGenerator;
        }
        return colorGeneratorRef.get();
    }

    @NonNull
    public static CompositeImageHelper provideCompositeImageHelper(@NonNull Context context) {
        return new CompositeImageHelper(provideColorGenerator(context));
    }

    @NonNull
    public synchronized static ChatUpdateManager provideChatUpdateManager() {
        if (chatUpdateManagerRef == null || chatUpdateManagerRef.get() == null) {
            final ChatUpdateManager colorGenerator = new ChatUpdateManager();
            chatUpdateManagerRef = new WeakReference<>(colorGenerator);
            return colorGenerator;
        }
        return chatUpdateManagerRef.get();
    }

    @NonNull
    public static FeedbackDataSource provideFeedbackDataSource(@NonNull Context context) {
        return new FeedbackRepository(context, rxSchedulers);
    }

    @NonNull
    public static SendFeedbackUseCase provideFeedback(@NonNull Context context) {
        return new SendFeedbackUseCase(provideFeedbackDataSource(context));
    }

    @NonNull
    public static MessageDataSource provideMessageDataSource(@NonNull Context context) {
        final FrescoSmallCacheFetcher frescoSmallCacheFetcher = provideFrescoSmallCacheFetcher();
        return new MessageRepository(context, frescoSmallCacheFetcher);
    }

    @NonNull
    public static FrescoSmallCacheFetcher provideFrescoSmallCacheFetcher() {
        if (frescoSmallCacheFetcherRef == null || frescoSmallCacheFetcherRef.get() == null) {
            final FrescoSmallCacheFetcher frescoSmallCacheFetcher = new FrescoSmallCacheFetcher();
            frescoSmallCacheFetcherRef = new WeakReference<>(frescoSmallCacheFetcher);
            return frescoSmallCacheFetcher;
        }
        return frescoSmallCacheFetcherRef.get();
    }

    @NonNull
    public static UploadFiles provideUploadFiles() {
        final FilesNetworkApi filesNetworkGateway = new FilesNetworkApi(rxSchedulers);
        return new UploadFiles(filesNetworkGateway);
    }

    @NonNull
    public static UserDataSource provideUserDataSource(Context context) {
        return new UserRepository(context);
    }

    @NonNull
    public static NotificationsDataSource provideNotificationsDataSource(Context context) {
        return new NotificationsRepository(context);
    }

    @NonNull
    public static Logout provideLogout(Context context) {
        return new Logout(provideNotificationsDataSource(context.getApplicationContext()));
    }

    @NonNull
    public static SubscribeToPush provideSubscribeToPush(Context context) {
        return new SubscribeToPush(provideNotificationsDataSource(context.getApplicationContext()));
    }

    @NonNull
    public static InviteUserViaChat provideInviteUserViaChat(Context context) {
        final UserDataSource userDataSource = provideUserDataSource(context);
        final MessageDataSource messageDataSource = provideMessageDataSource(context);
        return new InviteUserViaChat(userDataSource, messageDataSource);
    }

    @NonNull
    public static PhotoDataSource providePhotoDataSource(Context context) {
        return new PhotoRepository(context);
    }

    @NonNull
    public static MakePhotoByCamera provideMakePhotoByCamera(Context context) {
        return new MakePhotoByCamera(providePhotoDataSource(context));
    }

    @NonNull
    public static PickPhotosFromGallery providePickPhotosFromGallery(Context context) {
        return new PickPhotosFromGallery(providePhotoDataSource(context));
    }

    @NonNull
    public static AuthDataSource provideAuthDataSource(Context context)  {
        return new AuthRepository(context, new Gson(), rxSchedulers);
    }

}
