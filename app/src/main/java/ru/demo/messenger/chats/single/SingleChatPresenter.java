package ru.demo.messenger.chats.single;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArraySet;
import android.text.Spanned;
import android.text.TextUtils;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import biz.growapp.base.loading.BaseLoadingView;
import biz.growapp.base.loading.BasePresenter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;
import ru.demo.data.message.Sticker;
import ru.demo.data.message.StickerSet;
import ru.demo.domain.message.MessageDataSource;
import ru.demo.messenger.Consts;
import ru.demo.messenger.MainApp;
import ru.demo.messenger.chats.single.future.FutureMessageMapper;
import ru.demo.messenger.chats.single.future.FutureMessageStorage;
import ru.demo.messenger.chats.single.future.message.ForwardFutureMessage;
import ru.demo.messenger.chats.single.future.message.FutureMessage;
import ru.demo.messenger.chats.single.future.message.ReplyFutureMessage;
import ru.demo.messenger.chats.single.future.message.StickerFutureMessage;
import ru.demo.messenger.chats.single.future.message.TextFutureMessage;
import ru.demo.messenger.data.chat.ChatModel;
import ru.demo.messenger.data.chat.MessageReadEvent;
import ru.demo.messenger.data.message.MessageModel;
import ru.demo.messenger.data.user.UserModel;
import ru.demo.messenger.network.BaseSubscriber;
import ru.demo.messenger.network.ConnectionService;
import ru.demo.messenger.network.DownloadFileHelper;
import ru.demo.messenger.network.RequestManager;
import ru.demo.messenger.network.response.ChatList;
import ru.demo.messenger.network.response.ChatResponse;
import ru.demo.messenger.network.response.FileResponse;
import ru.demo.messenger.network.response.MessageList;
import ru.demo.messenger.network.response.base.BaseResponse;
import ru.demo.messenger.network.response.base.ServerError;
import ru.demo.messenger.network.services.FileService;
import ru.demo.messenger.network.services.MessagesService;
import ru.demo.messenger.utils.DateUtil;
import ru.demo.messenger.utils.Prefs;
import rx.Observable;
import rx.Single;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

class SingleChatPresenter extends BasePresenter<SingleChatPresenter.View> {

    private static final int MARK_AS_READ_DELAY = 1;
    private static final int TEMP_MESSAGE_ID = -1;
    private static final int EMPTY_CHAT_ID = -1;
    static final int MAX_ATTACHMENTS_COUNT = 10;

    private static MediaType MEDIA_TYPE = MediaType.parse("multipart/form-data");

    interface View extends BaseLoadingView {
        void onMessagesLoaded(List<Object> messages);

        void updateIsParticipant(ChatModel chat);

        void onSingleUserChatCreated(ChatModel chat);

        void onReceiveMessage(MessageModel message);

        void onMessageDelivered(@MessageModel.ReadStatus String status);

        void updateToolbarParticipants(ChatModel chat);

        void attachFile(String filePath);

        void attachImages(List<String> filteredImagePaths);

        void showMaxAttachmentsToast();

        void checkSingleChatAvailable();

        String getFormattedStringForCopy(String messageDate, String authorFullName, Spanned message);

        void updateScreenForOneToOneChat(ChatModel chat, UserModel member, boolean isMemberActive);

        void updateScreenForGroupChat(ChatModel chat);

        void showStickerSets(List<StickerSet> stickerSets);

        void addStickerMessage(MessageModel item);

        UserModel getUserForSingleChat();
    }

    private final MessagesService messagesService;
    private final FileService fileService;
    private final DownloadFileHelper downloadFileHelper;
    private final MessageDataSource messageDataSource;

    private FutureMessageStorage futureMessageStorage;

    private final ZoneId systemZone = ZoneId.systemDefault();
    private final DateTimeFormatter thisYearFormatter = DateTimeFormatter.ofPattern("dd.MM, HH:mm",
            DateUtil.getDefaultLocale()
    );
    private final DateTimeFormatter pastYearsFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy, HH:mm",
            DateUtil.getDefaultLocale()
    );

    private boolean firstLoad = true;
    private boolean isNoMoreData = false;
    private String loadMoreUrl;

    private boolean firstLoaded = true;

    private Subscription markAsReadSubscription;
    private Set<Long> messageIds;

    private HashSet<String> mobilePayloads = new HashSet<>();
    private final long selfUserId;
    private ChatModel chat;

    private ChatUpdateManager chatUpdateManager;
    private boolean isUpdateListenerAdded = false;

    private final LinkedList<String> attachmentPaths;

    private final FutureMessageMapper futureMessageMapper;

    private boolean stickersLoaded;
    private Subscription getStickersSubscription;

    SingleChatPresenter(@NonNull View view,
                        DownloadFileHelper downloadFileHelper,
                        ChatUpdateManager chatUpdateManager,
                        MessageDataSource messageDataSource) {
        super(view);
        this.chatUpdateManager = chatUpdateManager;
        this.messageDataSource = messageDataSource;
        this.downloadFileHelper = downloadFileHelper;
        selfUserId = Prefs.get().getLong(Consts.Prefs.USER_ID, 0L);
        messagesService = RequestManager.createService(MessagesService.class);
        fileService = RequestManager.createService(FileService.class);
        messageIds = new ArraySet<>(50);
        attachmentPaths = new LinkedList<>();
        futureMessageStorage = FutureMessageStorage.getInstance();
        futureMessageMapper = new FutureMessageMapper(selfUserId);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        chatUpdateManager.removeListener(chatUpdateListener);
    }

    public ChatModel getChat() {
        return chat;
    }

    public void setChat(ChatModel chat) {
        this.chat = chat;
    }

    long getSelfUserId() {
        return selfUserId;
    }

    long getChatId() {
        return chat != null ? chat.getId() : EMPTY_CHAT_ID;
    }

    boolean isChatExist() {
        return EMPTY_CHAT_ID != getChatId();
    }

    void reloadChat() {
        firstLoad = true;
        if (isChatExist()) {
            refresh();
        } else {
            getView().checkSingleChatAvailable();
        }
        ConnectionService.tryToConnect();
    }

    private void refresh() {
        unsubscribeAll();
        isNoMoreData = false;
        loadMoreUrl = null;
        firstLoaded = true;
        getMessages();
    }

    void getChatById(long chatId, boolean needOnlyParticipantsUpdate) {
        if (!isUpdateListenerAdded) {
            isUpdateListenerAdded = true;
            chatUpdateManager.addListener(chatId, chatUpdateListener);
        }
        Subscription getChatById = chatUpdateManager.getChatById(chatId, needOnlyParticipantsUpdate);
        if (getChatById != null) {
            subscriptions.add(getChatById);
        }
    }

    private ChatUpdateManager.Listener chatUpdateListener = new ChatUpdateManager.Listener() {
        @Override
        public void onNext(ChatList result, boolean needOnlyParticipantsUpdate) {
            if (result.chats == null || result.chats.isEmpty()) {
                getView().switchToMain(true);
            } else {
                final ChatModel chat = result.chats.get(0);
                if (needOnlyParticipantsUpdate) {
                    SingleChatPresenter.this.chat = chat;
                    getView().updateToolbarParticipants(chat);
                } else {
                    onChatLoaded(chat);
                }
            }
        }

        @Override
        public void onError(ServerError error) {
            getView().switchToError(true, error.getMessage());
        }
    };

    void checkChatAvailable(long userId) {
        subscriptions.add(
                messagesService.getChainByUserIds(Collections.singletonList(userId))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BaseSubscriber<ChatList>() {
                            @Override
                            public void onNext(ChatList result) {
                                if (result.chats == null || result.chats.isEmpty()) {
                                    getView().switchToMain(true);
                                } else {
                                    final ChatModel chat = result.chats.get(0);
                                    onChatLoaded(chat);
                                }
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().switchToError(true, error.getMessage());
                            }
                        })
        );
    }

    void getMessages() {
        if (isNoMoreData) {
            proceedLoadedMessages(Collections.emptyList());
            return;
        }
        if (loadMoreUrl == null) {
            subscribe(messagesService.getMessagesForChat(getChatId()));
        } else {
            subscribe(messagesService.getMessages(loadMoreUrl));
        }
    }

    private void subscribeToReceiveMessage(long chatId) {
        subscriptions.add(
                MainApp.globalBus.observeEvents(MessageModel.class)
                        .observeOn(AndroidSchedulers.mainThread())
                        .filter(newMessage -> newMessage.getChatId() == chatId)
                        .subscribe(message -> {
                            if (selfUserId == message.getAuthorId() && mobilePayloads.remove(message.getPayload())) {
                                return;
                            }
                            if (message.isMeRemovedMessageType()) {
                                chat.setInChain(false);
                            } else {
                                chat.setInChain(true);
                            }
                            if (MessageModel.MessageType.YOU_NEW_ADMIN.equals(message.getMessageType())) {
                                if (!chat.getAdminsList().contains(selfUserId)) {
                                    chat.getAdminsList().add(selfUserId);
                                }
                            }
                            if (MessageModel.MessageType.REMOVE_PARTICIPANT.equals(message.getMessageType()) ||
                                    MessageModel.MessageType.ADD_PARTICIPANT.equals(message.getMessageType())) {
                                getChatById(chatId, true);
                            }
                            getView().updateIsParticipant(chat);
                            getView().onReceiveMessage(message);
                        })
        );
    }

    private void subscribeToDeliveringMessage(long chatId) {
        subscriptions.add(
                MainApp.globalBus.observeEvents(MessageReadEvent.class)
                        .observeOn(AndroidSchedulers.mainThread())
                        .filter(event -> event.getChatId() == chatId)
                        .filter(event ->
                                MessageModel.ReadStatus.READ.equals(event.getStatus()) ||
                                        MessageModel.ReadStatus.READ_ALL.equals(event.getStatus()))
                        .subscribe(event -> getView().onMessageDelivered(event.getStatus()))
        );
    }

    private void subscribe(Observable<Response<MessageList>> chats) {
        subscriptions.add(
                chats.subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BaseSubscriber<MessageList>() {
                            @Override
                            public void onNext(MessageList result) {
                                getView().switchToMain(true);
                                loadMoreUrl = result.load_more_url;
                                if (loadMoreUrl == null) {
                                    isNoMoreData = true;
                                }
                                proceedLoadedMessages(result.private_messages);

                                if (firstLoaded) {
                                    firstLoaded = false;
                                    subscribeToReceiveMessage(getChatId());
                                    subscribeToDeliveringMessage(getChatId());
                                }
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().switchToError(true, error.getMessage());
                            }
                        })
        );
    }

    private void createSingleUserChat(String message, List<String> uploadedFileIds) {
        getView().switchToLoading(true);
        subscriptions.add(
                messagesService.createChat(
                        message,
                        Collections.singletonList(getView().getUserForSingleChat().getId()),
                        uploadedFileIds,
                        generateMobilePayload()
                )//-----|
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BaseSubscriber<ChatResponse>() {
                            @Override
                            public void onNext(ChatResponse result) {
                                chat = result.chat;
                                getView().onSingleUserChatCreated(result.chat);
                            }

                            @Override
                            public void onError(ServerError error) {
                                getView().switchToMain(true);
                                getView().showErrorDialog(error.getMessage());
                            }
                        })
        );
    }

    private void uploadCreateChatFiles(String message, List<String> uploadFileIds) {
        getView().switchToLoading(true);
        subscriptions.add(
                Observable.from(uploadFileIds)
                        .map(File::new)
                        .map(file -> MultipartBody.Part.createFormData("attach",
                                file.getName(),
                                RequestBody.create(MEDIA_TYPE, file)))
                        .concatMap(fileService::uploadFile)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new BaseSubscriber<FileResponse>() {
                            private final List<String> files = new ArrayList<>(uploadFileIds.size());

                            @Override
                            public void onCompleted() {
                                createSingleUserChat(message, files);
                            }

                            @Override
                            public void onNext(FileResponse result) {
                                files.add(result.file_info.id);
                            }

                            @Override
                            public void onError(ServerError error) {
                                unsubscribe();
                                getView().switchToMain(true);
                                getView().showErrorDialog(error.getMessage());
                            }
                        })
        );
    }

    void downloadFile(Context context, String sourceUrl, String filename) {
        subscriptions.add(downloadFileHelper.downloadFile(context, sourceUrl, filename)
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                }, throwable -> {
                })
        );
    }

    void addAndProceedMarkAsRead(long messageId) {
        if (checkIsTempMessageId(messageId)) {
            return;
        }
        messageIds.add(messageId);
        proceedMarkAsRead();
    }

    void addMarkAsRead(long messageId) {
        if (checkIsTempMessageId(messageId)) {
            return;
        }
        messageIds.add(messageId);
    }

    private boolean checkIsTempMessageId(long messageId) {
        if (messageId == TEMP_MESSAGE_ID) {
            markAsReadMessage(getChatId(), messageId);
            return true;
        } else {
            return false;
        }
    }

    void proceedMarkAsRead() {
        if (messageIds.isEmpty()) {
            return;
        }
        final List<Long> nowMessageIds = new ArrayList<>(messageIds.size());
        nowMessageIds.addAll(messageIds);

        if (markAsReadSubscription != null && !markAsReadSubscription.isUnsubscribed()) {
            markAsReadSubscription.unsubscribe();
        }
        markAsReadSubscription = Observable.timer(MARK_AS_READ_DELAY, TimeUnit.SECONDS)
                .concatMap(o -> messagesService.markMessageAsRead(nowMessageIds))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseSubscriber<BaseResponse>() {
                    @Override
                    public void onNext(BaseResponse result) {
                        markAsReadMessage(getChatId(), nowMessageIds.get(nowMessageIds.size() - 1));
                        messageIds.removeAll(nowMessageIds);
                    }

                    @Override
                    public void onError(ServerError error) {

                    }
                });
    }

    private void markAsReadMessage(long chatId, long messageId) {
        final MessageReadStatus messageReadStatus = new MessageReadStatus(chatId, messageId);
        MainApp.globalBus.send(messageReadStatus);
    }

    void onChatLoaded(ChatModel chat) {
        this.chat = chat;
        getView().updateIsParticipant(chat);

        if (chat.isOneToOne()) {
            UserModel member = null;
            final List<UserModel> users = chat.getUsers();
            for (int index = 0; index < users.size(); index++) {
                final UserModel user = users.get(index);
                if (user.getId() != selfUserId) {
                    member = user;
                    break;
                }
            }
            if (member != null) {
                getView().updateScreenForOneToOneChat(chat, member, member.isActive());
            }
        } else {
            getView().updateScreenForGroupChat(chat);
        }
        getMessages();
    }

    private void proceedLoadedMessages(List<MessageModel> messages) {
        List<Object> resultList = new ArrayList<>();
        if (firstLoad) {
            firstLoad = false;
            final List<FutureMessage> futureMessages =
                    futureMessageStorage.getChatMessagesById(getChatId());
            if (futureMessages != null) {
                final List<Object> messagesFromFuture =
                        new ArrayList<>(futureMessageMapper.messagesOf(futureMessages));
                resultList.addAll(messagesFromFuture);
            }
            if (!messages.isEmpty()) {
                final MessageModel firstMessage = messages.get(messages.size() - 1);
                markAsReadMessage(firstMessage.getChatId(), firstMessage.getId());
            }
        }
        resultList.addAll(messages);
        getView().onMessagesLoaded(resultList);
    }

    String generateCopyMessage(@Nullable MessageModel singleSelectedMessage, ArrayList<MessageModel> selectedMessages) {
        String message;
        if (singleSelectedMessage != null) {
            message = singleSelectedMessage.getMessage().toString();
        } else {
            Collections.sort(selectedMessages, (m1, m2) -> {
                if (m1.getCreatedAt() > m2.getCreatedAt())
                    return -1;
                else if (m1.getCreatedAt() < m2.getCreatedAt())
                    return 1;
                return 0;
            });
            StringBuilder messagesBuilder = new StringBuilder();
            for (int i = selectedMessages.size() - 1; i >= 0; i--) {
                MessageModel selectedMessage = selectedMessages.get(i);
                LocalDateTime date = LocalDateTime.from(Instant.ofEpochSecond(selectedMessage.getCreatedAt())
                        .atZone(systemZone));
                String messageDate = date.getYear() == LocalDate.now().getYear()
                        ? thisYearFormatter.format(date)
                        : pastYearsFormatter.format(date);
                messagesBuilder.append(
                        getView().getFormattedStringForCopy(
                                messageDate,
                                selectedMessage.getAuthorFullName(),
                                selectedMessage.getMessage()
                        )
                );
                if (i != 0) {
                    messagesBuilder.append("\n");
                }
            }
            message = messagesBuilder.toString();
        }
        return message;
    }

    void createNewSingleChat(String message) {
        if (isAttachmentsExist()) {
            uploadCreateChatFiles(message, attachmentPaths);
        } else {
            createSingleUserChat(message, null);
        }
    }

    void addToAttachmentPaths(List<String> imagePaths) {
        attachmentPaths.addAll(imagePaths);
    }

    void removeFromAttachmentPaths(String path) {
        attachmentPaths.remove(path);
    }

    void clearAttachmentPaths() {
        attachmentPaths.clear();
    }

    boolean isAttachmentsExist() {
        return !attachmentPaths.isEmpty();
    }

    void onFileSelected(String filePath) {
        if (attachmentPaths.contains(filePath)) {
            return;
        }
        if (MAX_ATTACHMENTS_COUNT == attachmentPaths.size()) {
            getView().showMaxAttachmentsToast();
            return;
        }
        attachmentPaths.add(0, filePath);
        getView().attachFile(filePath);
    }

    void onImagesSelected(List<String> imagePaths) {
        List<String> filteredImagePaths = new ArrayList<>(imagePaths.size());
        for (int i = 0; i < imagePaths.size(); i++) {
            final String imagePath = imagePaths.get(i);
            if (!attachmentPaths.contains(imagePath)) {
                filteredImagePaths.add(imagePath);
            }
        }
        if (filteredImagePaths.isEmpty()) {
            return;
        }
        if (MAX_ATTACHMENTS_COUNT == attachmentPaths.size()) {
            getView().showMaxAttachmentsToast();
            return;
        }
        final int totalPathsCount = filteredImagePaths.size() + attachmentPaths.size();
        if (MAX_ATTACHMENTS_COUNT < totalPathsCount) {
            getView().showMaxAttachmentsToast();
            final int overheadCount = totalPathsCount - MAX_ATTACHMENTS_COUNT;
            final int addPathsCount = filteredImagePaths.size() - overheadCount;
            filteredImagePaths = imagePaths.subList(0, addPathsCount);
        }
        Collections.reverse(filteredImagePaths);

        attachmentPaths.addAll(0, filteredImagePaths);
        getView().attachImages(filteredImagePaths);
    }

    List<MessageModel> sendMessage(String message) {
        return sendMessage(message, null, Collections.emptyList());
    }

    List<MessageModel> sendReplyMessage(String message,
                                        @NonNull MessageModel replyMessage) {
        final List<String> attachmentPaths;
        if (isAttachmentsExist()) {
            attachmentPaths = new ArrayList<>(this.attachmentPaths);
        } else {
            attachmentPaths = Collections.emptyList();
        }

        final ReplyFutureMessage replyFutureMessage = new ReplyFutureMessage(
                getChatId(),
                generateMobilePayload(),
                message,
                attachmentPaths,
                replyMessage
        );
        futureMessageStorage.add(replyFutureMessage);
        final MessageModel waitMessage = futureMessageMapper.messageOf(replyFutureMessage);
        mobilePayloads.add(waitMessage.getPayload());
        return Collections.singletonList(waitMessage);
    }

    List<MessageModel> sendMessage(String message,
                                   @Nullable MessageModel forwardMessage,
                                   List<ChatModel> targetChats) {
        final boolean sendToTargetChats = !targetChats.isEmpty();
        final List<Long> targetChatIds = new ArrayList<>();
        if (sendToTargetChats) {
            for (ChatModel targetChat : targetChats) {
                targetChatIds.add(targetChat.getId());
            }
        } else {
            targetChatIds.add(getChatId());
        }

        final String payload = generateMobilePayload();

        if (forwardMessage != null && forwardMessage.isStickerMessage()) {
            MessageModel messageModel = null;
            for (Long chatId : targetChatIds) {
                final StickerFutureMessage futureMessage =
                        getFutureMessageFormSticker(chatId, forwardMessage.getSticker());
                futureMessageStorage.add(futureMessage);

                if (chatId == getChatId()) {
                    messageModel = futureMessageMapper.messageOf(futureMessage);
                }
            }

            if (messageModel == null) {
                return Collections.emptyList();
            } else {
                return Collections.singletonList(messageModel);
            }
        }

        final List<String> attachmentPaths;
        if (isAttachmentsExist()) {
            attachmentPaths = new ArrayList<>(this.attachmentPaths);
        } else {
            attachmentPaths = Collections.emptyList();
        }

        final List<MessageModel> waitMessages = new ArrayList<>();

        for (Long chatId : targetChatIds) {
            final TextFutureMessage textFutureMessage;
            if (TextUtils.isEmpty(message) && attachmentPaths.isEmpty()) {
                textFutureMessage = null;
            } else {
                textFutureMessage = new TextFutureMessage(
                        chatId,
                        payload,
                        message,
                        attachmentPaths
                );
                futureMessageStorage.add(textFutureMessage);
            }

            final ForwardFutureMessage forwardFutureMessage;
            if (forwardMessage == null) {
                forwardFutureMessage = null;
            } else {
                final MessageModel dataMessage;
                if (MessageModel.MessageType.FORWARD.equalsIgnoreCase(forwardMessage.getMessageType())) {
                    dataMessage = forwardMessage.getOriginalMessage() == null ?
                            forwardMessage : forwardMessage.getOriginalMessage();
                } else {
                    dataMessage = forwardMessage;
                }
                final String forwardPayload = "forward:" + payload;
                forwardFutureMessage = new ForwardFutureMessage(
                        chatId,
                        forwardPayload,
                        dataMessage.getId(),
                        dataMessage.getAuthorId(),
                        dataMessage.getAuthorFullName(),
                        dataMessage.getMessage().toString(),
                        dataMessage.getFiles()
                );
                futureMessageStorage.add(forwardFutureMessage);
            }
            if (chatId == getChatId()) {
                if (textFutureMessage != null) {
                    waitMessages.add(futureMessageMapper.messageOf(textFutureMessage));
                }
                if (forwardFutureMessage != null) {
                    waitMessages.add(futureMessageMapper.messageOf(forwardFutureMessage));
                }
            }
        }


        for (int i = 0; i < waitMessages.size(); i++) {
            mobilePayloads.add(waitMessages.get(i).getPayload());
        }

        return waitMessages;
    }

    private String generateMobilePayload() {
        return UUID.randomUUID().toString();
    }

    void getStickers() {
        if (getStickersSubscription != null && !getStickersSubscription.isUnsubscribed()) {
            return;
        }
        final Single<List<StickerSet>> getStickers;
        if (stickersLoaded) {
            getStickers = messageDataSource.getStickerSets();
        } else {
            getStickers = messageDataSource.getStickerSets()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSuccess(stickerSets -> {
                        if (!stickerSets.isEmpty()) {
                            getView().showStickerSets(stickerSets);
                        }
                    })
                    .observeOn(Schedulers.io())
                    .toCompletable()
                    .andThen(messageDataSource.updateStickerSets())
                    .andThen(messageDataSource.getStickerSets())
                    .doOnSuccess(stickerSets -> stickersLoaded = true);
        }

        getStickersSubscription = getStickers
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(stickerSets -> getView().showStickerSets(stickerSets),
                        throwable -> getView().showStickerSets(Collections.emptyList())
                );
        subscriptions.add(getStickersSubscription);
    }

    private void createChatWithStickerSend(Sticker sticker) {
        getView().switchToLoading(true);
        messageDataSource.createChatWithStickerSend(
                Collections.singletonList(getView().getUserForSingleChat().getId()),
                sticker.getId()
        )//-----|
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(chat -> {
                    this.chat = chat;
                    getView().onSingleUserChatCreated(chat);
                }, throwable -> {
                    getView().switchToMain(true);
                    getView().showErrorDialog(throwable.getMessage());
                });
    }

    void sendSticker(Sticker sticker) {
        if (isChatExist()) {
            final StickerFutureMessage futureMessage = getFutureMessageFormSticker(getChatId(), sticker);
            mobilePayloads.add(futureMessage.getPayload());
            getView().addStickerMessage(futureMessageMapper.messageOf(futureMessage));
            futureMessageStorage.add(futureMessage);
        } else {
            createChatWithStickerSend(sticker);
        }
    }

    private StickerFutureMessage getFutureMessageFormSticker(long chatId, Sticker sticker) {
        return new StickerFutureMessage(chatId, generateMobilePayload(), sticker);
    }

}
