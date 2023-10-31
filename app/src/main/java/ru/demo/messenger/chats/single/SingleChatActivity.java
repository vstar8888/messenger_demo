package ru.demo.messenger.chats.single;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ActionMode;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.growapp.base.adapter.DelegationAdapter;
import biz.growapp.base.dialogs.BaseAlertDialog;
import biz.growapp.base.loading.BaseAppLoadingActivity;
import biz.growapp.base.pagination.PaginationAdapter;
import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import kotlin.Unit;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.RuntimePermissions;
import ru.demo.data.message.Sticker;
import ru.demo.data.message.StickerSet;
import ru.demo.messenger.R;
import ru.demo.messenger.chats.files.AttachedFilesAdapter;
import ru.demo.messenger.chats.files.AttachedImagesAdapter;
import ru.demo.messenger.chats.fullscreen.PictureViewActivity;
import ru.demo.messenger.chats.single.attach.AttachPhotoActivity;
import ru.demo.messenger.chats.single.delegates.AddStickerDelegate;
import ru.demo.messenger.chats.single.delegates.IncomingMessageDelegate;
import ru.demo.messenger.chats.single.delegates.MessageDateDelegate;
import ru.demo.messenger.chats.single.delegates.MessagesAdapter;
import ru.demo.messenger.chats.single.delegates.OutgoingMessageDelegate;
import ru.demo.messenger.chats.single.delegates.SelectItemManager;
import ru.demo.messenger.chats.single.delegates.StickerDelegate;
import ru.demo.messenger.chats.single.delegates.StickerMessageDelegate;
import ru.demo.messenger.chats.single.delegates.StickerSetDelegate;
import ru.demo.messenger.chats.single.delegates.SystemMessageDelegate;
import ru.demo.messenger.chats.single.future.FutureMessagesService;
import ru.demo.messenger.chats.single.future.ReceivedMessageForBroadcast;
import ru.demo.messenger.chats.single.info.MessageInfoActivity;
import ru.demo.messenger.chats.single.selection.ChatSelectionActivity;
import ru.demo.messenger.chats.single.settings.ChatSettingsActivity;
import ru.demo.messenger.data.chat.ChatModel;
import ru.demo.messenger.data.message.AttachedFiles;
import ru.demo.messenger.data.message.Attachment;
import ru.demo.messenger.data.message.MessageModel;
import ru.demo.messenger.data.user.UserModel;
import ru.demo.messenger.helpers.CompositeImageHelper;
import ru.demo.messenger.helpers.CompositeImageView;
import ru.demo.messenger.internal.di.Injection;
import ru.demo.messenger.network.ConnectionService;
import ru.demo.messenger.network.DownloadFileHelper;
import ru.demo.messenger.network.DownloadOnCompleted;
import ru.demo.messenger.people.profile.ProfileActivity;
import ru.demo.messenger.utils.ActionUtils;
import ru.demo.messenger.utils.DisplayUtils;
import ru.demo.messenger.utils.StringUtils;
import ru.demo.messenger.utils.UriUtils;
import ru.demo.messenger.utils.VectorUtils;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

@RuntimePermissions
public class SingleChatActivity extends BaseAppLoadingActivity
        implements SingleChatPresenter.View, PaginationAdapter.Loader,
        MessagesAdapter.Callback, AttachedImagesAdapter.Callback, AttachedFilesAdapter.Callback {

    private static final String EXTRA_CHAT_USER = "extra.CHAT_USERS";
    private static final String EXTRA_CHAT = "extra.CHAT";
    private static final String EXTRA_CHAT_ID = "extra.CHAT_ID";
    private static final String EXTRA_NEED_OPEN_GROUP_CHAT = "extra.NEED_OPEN_GROUP_CHAT";
    private static final String EXTRA_NEED_CREATE_SINGLE_CHAT = "extra.NEED_CREATE_SINGLE_CHAT";
    private static final String EXTRA_FORWARD_CHATS = "extra.FORWARD_CHATS";
    private static final String EXTRA_FORWARD_CHAT_FOR_OPEN = "extra.FORWARD_CHAT_FOR_OPEN";
    private static final String EXTRA_FORWARD_MESSAGES = "extra.FORWARD_MESSAGES";
    private static final String EXTRA_OUT_UPDATED_CHAT = "extra.OUT_UPDATED_CHAT";
    private static final String EXTRA_IS_CHAT_DELETED = "extra.IS_CHAT_DELETED";

    private static final String ARG_ATTACHED_FILES = "attached_files";
    private static final String ARG_ATTACHED_PHOTOS = "attached_photos";

    private static final int REQUEST_ATTACH_FILE = 100;
    private static final int REQUEST_IMAGES = 111;
    private static final int REQUEST_CHAT_SETTINGS = 222;
    private static final int REQUEST_SELECT_CHATS = 333;

    private static final int SCROLL_DOWN_THRESHOLD = 3;
    private static final int SHOW_FAB_MESSAGES_THRESHOLD = 3;

    public static Intent openSingleChat(Context context, UserModel userModel) {
        return new Intent(context, SingleChatActivity.class)
                .putExtra(EXTRA_CHAT_USER, userModel)
                .putExtra(EXTRA_NEED_CREATE_SINGLE_CHAT, true);
    }

    public static Intent openNewGroupChat(Context context, ChatModel chat) {
        return new Intent(context, SingleChatActivity.class)
                .putExtra(EXTRA_CHAT, chat)
                .putExtra(EXTRA_NEED_OPEN_GROUP_CHAT, true);
    }

    public static Intent openChat(Context context, @NonNull ChatModel chat) {
        return new Intent(context, SingleChatActivity.class)
                .putExtra(EXTRA_CHAT, chat);
    }

    public static Intent openChatWithId(Context context, long chatId) {
        return new Intent(context, SingleChatActivity.class)
                .putExtra(EXTRA_CHAT_ID, chatId);
    }

    @Nullable
    public static ChatModel unpackUpdatedChat(@NonNull Intent data) {
        return data.getParcelableExtra(EXTRA_OUT_UPDATED_CHAT);
    }

    public static boolean unpackIsChatDeleted(@NonNull Intent data) {
        return data.getBooleanExtra(EXTRA_IS_CHAT_DELETED, false);
    }

    public static Intent openChatForForward(Context context,
                                            ArrayList<ChatModel> selectedChats,
                                            ChatModel chatForOpen,
                                            ArrayList<MessageModel> messages) {
        return new Intent(context, SingleChatActivity.class)
                .putExtra(EXTRA_FORWARD_MESSAGES, messages)
                .putExtra(EXTRA_FORWARD_CHAT_FOR_OPEN, chatForOpen)
                .putExtra(EXTRA_FORWARD_CHATS, selectedChats);
    }

    @BindView(R.id.rvMessages) RecyclerView rvMessages;
    @BindView(R.id.etMessage) EditText etMessage;

    @BindView(R.id.ivAttachFiles) ImageView ivAttachFiles;
    @BindView(R.id.ivAttachImages) ImageView ivAttachImages;

    @BindView(R.id.ivAttachment) ImageView ivAttachment;
    @BindView(R.id.ivSend) ImageView ivSend;
    @BindView(R.id.vgMessage) ViewGroup vgMessage;
    @BindView(R.id.tvIsNoMoreParticipant) TextView tvIsNoMoreParticipant;
    @BindView(R.id.tvSecondMemberIsBlocked) TextView tvSecondMemberIsBlocked;

    @BindView(R.id.vgAttachments) ViewGroup vgAttachments;

    @BindView(R.id.vgForward) ViewGroup vgForward;
    @BindView(R.id.tvForwardNames) TextView tvForwardNames;
    @BindView(R.id.tvForwardContent) TextView tvForwardContent;
    @BindView(R.id.ivCloseForward) ImageView ivCloseForward;

    @BindView(R.id.vgReply) ViewGroup vgReply;
    @BindView(R.id.tvReplyAuthorName) TextView tvReplyAuthorName;
    @BindView(R.id.tvReplyContent) TextView tvReplyContent;
    @BindView(R.id.sdvReplyImagePreview) SimpleDraweeView sdvReplyImagePreview;
    @BindView(R.id.ivReplyImageCloseCircle) ImageView ivReplyImageCloseCircle;
    @BindView(R.id.vCloseReply) View vCloseReply;

    @BindView(R.id.fabToBottom) FloatingActionButton fabToBottom;
    @BindView(R.id.ivNewMessagesFabCount) ImageView ivNewMessagesFabCount;
    @BindView(R.id.tvNewMessagesFabCount) TextView tvNewMessagesFabCount;

    @BindView(R.id.rvPhotos) RecyclerView rvPhotos;
    @BindView(R.id.rvFiles) RecyclerView rvFiles;

    @BindView(R.id.vgToolbarContent) ViewGroup vgToolbarContent;
    @BindView(R.id.civToolbarIcon) CompositeImageView civToolbarIcon;
    @BindView(R.id.tvToolbarTitle) TextView tvToolbarTitle;
    @BindView(R.id.tvToolbarSubtitle) TextView tvToolbarSubtitle;

    @BindView(R.id.vgStickersContainer) ViewGroup vgStickersContainer;
    @BindView(R.id.vgStickersContent) ViewGroup vgStickersContent;
    @BindView(R.id.ivStickers) ImageView ivStickers;
    @BindView(R.id.rvStickerSets) RecyclerView rvStickerSets;
    @BindView(R.id.rvStickers) RecyclerView rvStickers;
    @BindView(R.id.pbLoading) ProgressBar pbLoading;

    private final DelegationAdapter<Object> stickerSetsAdapter = new DelegationAdapter<>();
    private final DelegationAdapter<Object> stickersAdapter = new DelegationAdapter<>();

    private SingleChatPresenter presenter;

    private MessagesAdapter adapter;
    private LinearLayoutManager messagesLayoutManager;

    private AttachedFilesAdapter attachedFilesAdapter;
    private AttachedImagesAdapter attachedPhotosAdapter;

    private CompositeImageHelper compositeImageHelper;

    private Drawable stickerIcon;
    private Drawable keyboardIcon;

    private boolean needHighlightReply = false;
    @Nullable private MessageModel replyMessage;

    private BroadcastReceiver sentMessageReceiver;
    private ActionMode actionMode;
    private boolean isForward = false;
    private MessageModel attachmentMessageForForward;

    private int newMessagesFabCount = 0;

    private boolean isActivityResumed = false;
    private boolean needProceedMarkAsRead = false;

    private SelectItemManager selectStickerManager;

    private DownloadOnCompleted downloadOnComplete;//vs

    @Override
    public int getMainContainerId() {
        return R.id.vgContainer;
    }

    private List<ChatModel> getForwardChats() {
        return getIntent().getParcelableArrayListExtra(EXTRA_FORWARD_CHATS);
    }

    private List<MessageModel> getForwardMessages() {
        return getIntent().getParcelableArrayListExtra(EXTRA_FORWARD_MESSAGES);
    }

    private ChatModel getFirstForwardChat() {
        return getIntent().getParcelableExtra(EXTRA_FORWARD_CHAT_FOR_OPEN);
    }

    @Override
    public UserModel getUserForSingleChat() {
        return getIntent().getParcelableExtra(EXTRA_CHAT_USER);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_chat);
        setupRecyclerView();

        compositeImageHelper = Injection.provideCompositeImageHelper(this);

        downloadOnComplete = new DownloadOnCompleted(this);//vs
        downloadOnComplete.onCreate();

        presenter = new SingleChatPresenter(this,
                new DownloadFileHelper(this, downloadOnComplete),
                Injection.provideChatUpdateManager(),
                Injection.provideMessageDataSource(this)
        );

        if (savedInstanceState != null) {
            final ArrayList<String> filePaths =
                    savedInstanceState.getStringArrayList(ARG_ATTACHED_FILES);
            final ArrayList<String> imagePaths =
                    savedInstanceState.getStringArrayList(ARG_ATTACHED_PHOTOS);

            final boolean imagesExist = imagePaths != null && imagePaths.size() > 0;
            if (imagesExist) {
                presenter.addToAttachmentPaths(imagePaths);
                attachedPhotosAdapter.addAll(imagePaths);
                showPhotoAttachments(true);
            }

            final boolean filesExist = filePaths != null && filePaths.size() > 0;
            if (filesExist) {
                presenter.addToAttachmentPaths(filePaths);
                for (int i = 0; i < filePaths.size(); i++) {
                    attachedFilesAdapter.add(Uri.parse(filePaths.get(i)));
                }
                showFileAttachments(true);
            }

            if (imagesExist || filesExist) {
                showAttachmentContainer(true);
                showActiveAttachmentMethods();
                showSendButtonIfFileAttached();
            }
        }

        if (getIntent().hasExtra(EXTRA_FORWARD_CHATS)) {
            prepareForForward();
        } else if (getIntent().hasExtra(EXTRA_NEED_CREATE_SINGLE_CHAT)) {
            checkSingleChatAvailable();

        } else if (getIntent().hasExtra(EXTRA_NEED_OPEN_GROUP_CHAT)) {
            final ChatModel chat = getIntent().getParcelableExtra(EXTRA_CHAT);
            presenter.getChatById(chat.getId(), false);

        } else {
            final ChatModel chat = getIntent().getParcelableExtra(EXTRA_CHAT);
            if (chat != null) {
                presenter.onChatLoaded(chat);
            } else {
                final long chatId = getIntent().getLongExtra(EXTRA_CHAT_ID, -1);
                if (chatId == -1) {
                    finish();
                }
                // need for push notification
                startFutureMessageService();
                presenter.getChatById(chatId, false);
            }
        }

        setupSentMessageReceiver();

        setupStickers();
    }

    private void setupStickers() {
        stickerIcon = VectorUtils.getVectorDrawable(SingleChatActivity.this, R.drawable.ic_sticker);
        keyboardIcon = VectorUtils.getVectorDrawable(SingleChatActivity.this, R.drawable.ic_keyboard);

        rvStickerSets.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );
        selectStickerManager = new SelectItemManager(stickerSetsAdapter,
                StickerSetDelegate.SELECT_PAYLOAD,
                StickerSetDelegate.UNSELECT_PAYLOAD
        );
        stickerSetsAdapter.getManager()
                .addDelegate(
                        new StickerSetDelegate(this, (viewId, position) -> {
                            rvStickerSets.post(() -> selectStickerManager.onItemClick(position));

                            final StickerSet item = (StickerSet) stickerSetsAdapter.getItem(position);
                            stickersAdapter.replaceAll(item.getStickers());
                            return Unit.INSTANCE;
                        })
                )
                .addDelegate(
                        new AddStickerDelegate(this, (viewId, position) -> {
                            if (clickHelper.isDoubleClicked(viewId)) {
                                return Unit.INSTANCE;
                            }
                            showAddStickersDialog();
                            return Unit.INSTANCE;
                        })
                )
        ;
        rvStickerSets.setAdapter(stickerSetsAdapter);

        rvStickers.setLayoutManager(new GridLayoutManager(this, 4));
        stickersAdapter.getManager()
                .addDelegate(
                        new StickerDelegate(this,
                                Injection.provideFrescoSmallCacheFetcher(),
                                position -> {
                                    final Sticker item = (Sticker) stickersAdapter.getItem(position);
                                    presenter.sendSticker(item);
                                    changeStickersVisibility(false, false);
                                    return Unit.INSTANCE;
                                }
                        )
                );
        rvStickers.setAdapter(stickersAdapter);

        ivStickers.setOnClickListener(v -> changeStickersVisibility(!isStickersVisible(), true));

        etMessage.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                changeStickersVisibility(false, false);
            }
        });
    }

    @Override
    public void addStickerMessage(MessageModel item) {
        adapter.addWithDateAndHeader(item);
        rvMessages.smoothScrollToPosition(adapter.getItemCount());
    }

    private boolean isStickersVisible() {
        return vgStickersContainer.getVisibility() == View.VISIBLE;
    }

    private void showAddStickersDialog() {
        final SpannableString messageText = new SpannableString(
                getString(R.string.chat_add_stickers)
        );
        Linkify.addLinks(messageText, Linkify.ALL);

        final AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setMessage(messageText)
                .setPositiveButton(R.string.dialog_ok, null)
                .setCancelable(false)
                .show();

        final TextView tvMessage = (TextView) alertDialog.findViewById(android.R.id.message);
        if (tvMessage != null) {
            tvMessage.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    public void changeStickersVisibility(boolean visible, boolean withKeyboard) {
        if (visible) {
            showAttachmentContainer(false);
            ivStickers.setImageDrawable(keyboardIcon);
            vgStickersContainer.setVisibility(View.VISIBLE);
            vgStickersContent.setVisibility(View.GONE);
            pbLoading.setVisibility(View.VISIBLE);
            if (withKeyboard) {
                DisplayUtils.hideSoftKeyboard(SingleChatActivity.this);
            }

            presenter.getStickers();
        } else {
            ivStickers.setImageDrawable(stickerIcon);
            vgStickersContainer.setVisibility(View.GONE);
            if (withKeyboard) {
                showKeyboard(etMessage);
            }
        }
    }

    @Override
    public void showStickerSets(List<StickerSet> stickerSets) {
        pbLoading.setVisibility(View.GONE);
        vgStickersContent.setVisibility(View.VISIBLE);

        final List<Object> stickerSetItems = stickerSetsAdapter.getItems();
        final List<Object> stickersWithoutAddButton = stickerSetItems.isEmpty()
                ? Collections.emptyList()
                : stickerSetItems.subList(0, stickerSetItems.size() - 1);
        if (stickersWithoutAddButton.equals(stickerSets)) {
            return;
        }

        stickerSetsAdapter.replaceAll(stickerSets);
        /**
         * Removed stickers add. Maybe need uncomment in the future.
         */
//        stickerSetsAdapter.add(new AddStickerItem());
        if (!stickerSets.isEmpty()) {
            stickersAdapter.replaceAll(stickerSets.get(0).getStickers());
            selectStickerManager.reset();
            rvStickerSets.post(() -> selectStickerManager.onItemClick(0));
        }
    }

    private void prepareForForward() {
        isForward = true;
        showSendButton(true);
        vgForward.setVisibility(View.VISIBLE);
        ivCloseForward.setOnClickListener(v -> {
            vgForward.setVisibility(View.GONE);
            isForward = false;
            if (TextUtils.isEmpty(etMessage.getText().toString())) {
                showSendButton(false);
            } else {
                showSendButton(true);
            }
            if (actionMode != null) {
                // Show reply action
                actionMode.invalidate();
            }
        });

        final List<MessageModel> messages = getForwardMessages();
        final ChatModel chatForOpen = getFirstForwardChat();

        final MessageModel forwardedMessage = messages.get(0);
        final MessageModel dataMessage = forwardedMessage.getOriginalMessage() == null ?
                forwardedMessage : forwardedMessage.getOriginalMessage();
        tvForwardNames.setText(dataMessage.getAuthorFullName());

        final String forwardText;
        if (messages.size() > 1) {
            forwardText = getResources().getQuantityString(R.plurals.chat_forward_content, messages.size(), messages.size());
        } else {
            if (forwardedMessage.isStickerMessage()) {
                forwardText = getString(R.string.chat_forward_content_sticker);
            } else if (TextUtils.isEmpty(forwardedMessage.getMessage())) {
                AttachedFiles files = forwardedMessage.getFiles();
                if (!files.getImages().isEmpty()) {
                    forwardText = getString(R.string.chat_forward_content_image);
                } else {
                    forwardText = getString(R.string.chat_forward_content_file);
                }
            } else {
                forwardText = forwardedMessage.getMessage().toString();
            }
        }
        tvForwardContent.setText(forwardText);

        presenter.onChatLoaded(chatForOpen);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        final List<Uri> adapterItems = attachedFilesAdapter.getItems();
        final ArrayList<String> filePaths = new ArrayList<>(adapterItems.size());
        for (int i = 0; i < adapterItems.size(); i++) {
            filePaths.add(adapterItems.get(i).toString());
        }
        outState.putStringArrayList(ARG_ATTACHED_FILES, filePaths);
        outState.putStringArrayList(ARG_ATTACHED_PHOTOS, new ArrayList<>(attachedPhotosAdapter.getItems()));
        super.onSaveInstanceState(outState);
    }

    @Override
    public void checkSingleChatAvailable() {
        final UserModel user = getUserForSingleChat();
        updateToolbarForOneToOneChat(user);
        presenter.checkChatAvailable(user.getId());
    }

    private void startFutureMessageService() {
        final Intent serviceIntent = FutureMessagesService.getIntent(this);
        startService(serviceIntent);
    }

    private void setupRecyclerView() {
        messagesLayoutManager = new LinearLayoutManager(this);
        messagesLayoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(messagesLayoutManager);

        rvPhotos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        attachedPhotosAdapter = new AttachedImagesAdapter(this, this);
        rvPhotos.setAdapter(attachedPhotosAdapter);

        rvFiles.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        attachedFilesAdapter = new AttachedFilesAdapter(this, this);
        rvFiles.setAdapter(attachedFilesAdapter);
        setupFabVisibilityListener();
    }

    private void setupFabVisibilityListener() {
        rvMessages.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                int position = messagesLayoutManager.findLastVisibleItemPosition();
                if ((position < adapter.getItemCount() - SHOW_FAB_MESSAGES_THRESHOLD
                        && position != adapter.getItemCount() - 1)) {
                    fabToBottom.show();
                } else {
                    hideFab();
                }
            }
        });
    }

    private void hideFab() {
        proceedNewMessagesCount(false);
        newMessagesFabCount = 0;
        fabToBottom.hide();
    }

    private void setNewMessagesCount() {
        String newMessagesCount;
        ++newMessagesFabCount;
        if (newMessagesFabCount > 99) {
            newMessagesCount = "99+";
        } else {
            newMessagesCount = String.valueOf(newMessagesFabCount);
        }
        tvNewMessagesFabCount.setText(newMessagesCount);
    }

    private void proceedNewMessagesCount(boolean needShow) {
        if (needShow && fabToBottom.getVisibility() == View.VISIBLE) {
            setNewMessagesCount();
            ivNewMessagesFabCount.setVisibility(View.VISIBLE);
            tvNewMessagesFabCount.setVisibility(View.VISIBLE);
        } else {
            ivNewMessagesFabCount.setVisibility(View.GONE);
            tvNewMessagesFabCount.setVisibility(View.GONE);
        }
    }

    private void setupAdapter() {
        adapter = new MessagesAdapter(this, presenter.getChat().isOneToOne(), this, PaginationAdapter.Direction.TO_START);
        adapter.getManager()
                .addDelegate(new IncomingMessageDelegate(this, this, adapter))
                .addDelegate(new OutgoingMessageDelegate(this, this, adapter))
                .addDelegate(new MessageDateDelegate(this))
                .addDelegate(new StickerMessageDelegate(this, presenter.getSelfUserId(), adapter))
                .addDelegate(new SystemMessageDelegate(this));
        rvMessages.setAdapter(adapter);
    }

    private void updateTitleWithChat(ChatModel chat) {
        final String formattedChatTitle = StringUtils.fromHtmlCompat(chat.getTitle()).toString().trim();
        setToolbarTitle(formattedChatTitle);
    }

    private void updateGroupCountSubtitle(int usersCount) {
        final String quantityUsers =
                getResources().getQuantityString(R.plurals.group_chat_count, usersCount);
        final String usersCountString = String.format(quantityUsers, usersCount);
        setToolbarSubTitle(usersCountString);
    }

    private void setupSentMessageReceiver() {
        sentMessageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final ReceivedMessageForBroadcast sendResult =
                        intent.getParcelableExtra(FutureMessagesService.EXTRA_CHAT_MESSAGE);

                if (sendResult.getChatId() != presenter.getChatId()) {
                    return;
                }
                final int position = adapter.findFirstWaitItem();
                if (sendResult.isSuccessfulSend()) {
                    if (position != -1) {
                        adapter.replace(sendResult.getMessage(), position);
                    } else {
                        adapter.addWithDateAndHeader(sendResult.getMessage());
                    }
                    rvMessages.smoothScrollToPosition(adapter.getItemCount());
                } else {
                    if (position != -1) {
                        adapter.remove(position);
                    }
                }
            }
        };

        final IntentFilter filter = new IntentFilter();
        filter.addAction(FutureMessagesService.ACTION_SEND_MESSAGE);

        registerReceiver(sentMessageReceiver, filter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        isActivityResumed = true;
        if (needProceedMarkAsRead) {
            needProceedMarkAsRead = false;
            presenter.proceedMarkAsRead();
        }
        startFutureMessageService();//vs
        ConnectionService.tryToConnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        isActivityResumed = false;
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(sentMessageReceiver);
        downloadOnComplete.onDestroy();//vs
        presenter.onDestroyView();
        super.onDestroy();
    }

    @OnTextChanged(R.id.etMessage)
    protected void onMessageChanged(Editable s) {
        if (isNotBlank(s.toString()) || presenter.isAttachmentsExist()) {
            showSendButton(true);
        } else {
            showSendButton(false);
        }
    }

    @Override
    public void onMessagesLoaded(List<Object> messages) {
        adapter.addAll(messages, PaginationAdapter.Direction.TO_START);
        if (needHighlightReply) {
            needHighlightReply = false;
            findMessageAndHighlight(replyMessage.getId());
        }
    }

    @Override
    public void updateIsParticipant(ChatModel chat) {
        if (chat.isInChain() || chat.isOneToOne()) {
            vgMessage.setVisibility(View.VISIBLE);
            tvIsNoMoreParticipant.setVisibility(View.GONE);
        } else {
            vgMessage.setVisibility(View.GONE);
            tvIsNoMoreParticipant.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void updateScreenForGroupChat(ChatModel chat) {
        setupAdapter();
        toolbar.setOnClickListener(v -> {
            startActivityForResult(
                    ChatSettingsActivity.getIntent(SingleChatActivity.this, presenter.getChat()),
                    REQUEST_CHAT_SETTINGS
            );
        });
        updateTitleWithChat(chat);
        updateToolbarParticipants(chat);
    }

    private void updateIsSecondMemberActive(boolean isMemberActive) {
        if (isMemberActive) {
            vgMessage.setVisibility(View.VISIBLE);
            tvSecondMemberIsBlocked.setVisibility(View.GONE);
        } else {
            vgMessage.setVisibility(View.GONE);
            tvSecondMemberIsBlocked.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void updateScreenForOneToOneChat(ChatModel chat, UserModel member, boolean isMemberActive) {
        setupAdapter();
        updateIsSecondMemberActive(isMemberActive);
        updateToolbarForOneToOneChat(member);
    }

    @Override
    public void updateToolbarParticipants(ChatModel chat) {
        compositeImageHelper.showChatImage(civToolbarIcon, chat, presenter.getSelfUserId());
        updateGroupCountSubtitle(chat.getUsers().size());
    }

    private void updateToolbarForOneToOneChat(UserModel member) {
        final long id = member.getId();
        toolbar.setOnClickListener(v -> {
            startActivity(ProfileActivity.ofUser(SingleChatActivity.this, id));
        });
        compositeImageHelper.showUserImage(civToolbarIcon, member);
        setToolbarTitle(member.getFullName());
    }

    private void setToolbarTitle(String title) {
        tvToolbarTitle.setText(title);
    }

    private void setToolbarSubTitle(String subTitle) {
        if (TextUtils.isEmpty(subTitle)) {
            tvToolbarSubtitle.setVisibility(View.GONE);
        } else {
            tvToolbarSubtitle.setVisibility(View.VISIBLE);
            tvToolbarSubtitle.setText(subTitle);
        }
    }

    @Override
    public void onLoadMore(int offset) {
        presenter.getMessages();
    }

    @Override
    public void onReceiveMessage(MessageModel message) {
        proceedNewMessagesCount(true);
        adapter.addWithDateAndHeader(message);

        if (isActivityResumed) {
            presenter.addAndProceedMarkAsRead(message.getId());
        } else {
            needProceedMarkAsRead = true;
            presenter.addMarkAsRead(message.getId());
        }
        final int lastVisibleItemPosition = messagesLayoutManager.findLastVisibleItemPosition();
        final int lastItemPosition = adapter.getItemCount() - 1;
        if (lastVisibleItemPosition >= lastItemPosition - SCROLL_DOWN_THRESHOLD) {
            rvMessages.smoothScrollToPosition(adapter.getItemCount());
        }
    }

    @Override
    public void onMessageDelivered(@MessageModel.ReadStatus String status) {
        adapter.changeDeliveredMessagesStatus(status);
    }

    @Override
    public void onRetryButtonClick(View v) {
        switchToLoading(true);
        presenter.reloadChat();
    }

    @Override
    public void onAttachedFileClick(Attachment attachment) {
        SingleChatActivityPermissionsDispatcher.downloadFileWithCheck(this, attachment);
    }

    @NeedsPermission(WRITE_EXTERNAL_STORAGE)
    protected void downloadFile(Attachment attachment) {
        String toastMessage = getString(R.string.file_download_added_queue, attachment.getName());
        Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show();
        presenter.downloadFile(this, attachment.getUrl(), attachment.getName());
    }

    @Override
    public void onImageClick(Attachment attachment, int position) {
        startActivity(PictureViewActivity.navigateToFullscreenImage(this, attachment.getUrl()));
    }

    @Override
    public void onProfileClick(int viewId, long userId) {
        if (clickHelper.isDoubleClicked(viewId)) {
            return;
        }
        startActivity(ProfileActivity.ofUser(this, userId));
    }

    @Override
    public void invalidateActionMode() {
        actionMode.invalidate();
    }

    private void proceedCopyOfSelectedMessages() {
        String message = presenter.generateCopyMessage(
                adapter.getSingleSelectedMessage(),
                adapter.getSelectedMessages()
        );
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(
                ClipData.newPlainText(message, message)
        );
        disableActionMode();
        Toast.makeText(this, getString(R.string.chat_toast_copied), Toast.LENGTH_SHORT).show();
    }

    @Override
    public String getFormattedStringForCopy(String messageDate, String authorFullName, Spanned message) {
        return getString(R.string.chat_copy_format, messageDate, authorFullName, message);
    }

    private ActionMode.Callback actionCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.getMenuInflater().inflate(R.menu.messages_select_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            MessageModel message = adapter.getSingleSelectedMessage();
            if (message != null) {
                menu.findItem(R.id.action_info).setVisible(
                        message.getAuthorId() == presenter.getSelfUserId()
                );
                menu.findItem(R.id.action_reply).setVisible(!isForward);
            } else {
                menu.findItem(R.id.action_info).setVisible(false);
                menu.findItem(R.id.action_reply).setVisible(false);
            }

            MenuItem copy = menu.findItem(R.id.action_copy);
            copy.setVisible(true);
            ArrayList<MessageModel> messages = adapter.getSelectedMessages();
            for (int i = 0; i < messages.size(); i++) {
                if (TextUtils.isEmpty(messages.get(i).getMessage())) {
                    copy.setVisible(false);
                    break;
                }
            }

            MenuItem forward = menu.findItem(R.id.action_forward);
            forward.setVisible(messages.size() == 1);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case (R.id.action_reply): {
                    showReplyContent();
                    disableActionMode();
                    return true;
                }
                case (R.id.action_info): {
                    DisplayUtils.hideSoftKeyboard(SingleChatActivity.this);
                    startActivity(MessageInfoActivity.getIntent(SingleChatActivity.this, adapter.getSingleSelectedMessage()));
                    return true;
                }
                case (R.id.action_copy): {
                    proceedCopyOfSelectedMessages();
                    return true;
                }
                case (R.id.action_forward): {
                    DisplayUtils.hideSoftKeyboard(SingleChatActivity.this);
                    startChatSelectionActivity(true);
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            adapter.disableActionMode();
            actionMode = null;
        }
    };

    private void showReplyContent() {
        showKeyboard(etMessage);
        final MessageModel message = adapter.getSingleSelectedMessage();
        if (message == null) {
            return;
        }
        replyMessage = message;

        final MessageModel dataMessage;
        if (MessageModel.MessageType.FORWARD.equalsIgnoreCase(message.getMessageType())) {
            dataMessage = message.getOriginalMessage() == null ?
                    message : message.getOriginalMessage();
        } else {
            dataMessage = message;
        }
        vgReply.setVisibility(View.VISIBLE);
        tvReplyContent.setMaxLines(1);
        tvReplyAuthorName.setText(dataMessage.getAuthorFullName());

        List<Attachment> images = dataMessage.getFiles().getImages();

        if (dataMessage.isStickerMessage()) {
            tvReplyContent.setText(getString(R.string.chat_reply_sticker));
        } else if (TextUtils.isEmpty(dataMessage.getMessage())) {
            if (images.isEmpty()) {
                tvReplyContent.setText(getString(R.string.chat_reply_document));
            } else {
                tvReplyContent.setText(getString(R.string.chat_reply_image));
            }
        } else {
            tvReplyContent.setText(dataMessage.getMessage());
        }

        if (dataMessage.isStickerMessage() && dataMessage.getSticker() != null) {
            sdvReplyImagePreview.setVisibility(View.VISIBLE);
            ivReplyImageCloseCircle.setVisibility(View.VISIBLE);
            sdvReplyImagePreview.setImageURI(dataMessage.getSticker().getImageUrl());
        } else if (images.isEmpty()) {
            sdvReplyImagePreview.setVisibility(View.GONE);
            ivReplyImageCloseCircle.setVisibility(View.GONE);
        } else {
            sdvReplyImagePreview.setVisibility(View.VISIBLE);
            ivReplyImageCloseCircle.setVisibility(View.VISIBLE);
            sdvReplyImagePreview.setImageURI(images.get(0).getUrl());
        }
        vCloseReply.setOnClickListener(v -> hideReplyContent());
    }

    private void hideReplyContent() {
        replyMessage = null;
        ivAttachment.setVisibility(View.VISIBLE);
        vgReply.setVisibility(View.GONE);
    }

    private void showKeyboard(View view) {
        if (!DisplayUtils.isKeyboardVisible(this)) {
            DisplayUtils.showSoftKeyboard(this, etMessage);
            view.requestFocus();
        }
    }

    private void startChatSelectionActivity(boolean isActionModeForward) {
        startActivityForResult(
                ChatSelectionActivity.getIntent(SingleChatActivity.this, isActionModeForward),
                REQUEST_SELECT_CHATS
        );
    }

    @Override
    public void onMessageLongClick(int adapterPosition) {
        actionMode = startSupportActionMode(actionCallback);
        adapter.enableActionMode();
    }

    @Override
    public void disableActionMode() {
        actionMode.finish();
    }

    @Override
    public void onReplyMessageClick(int viewId, int adapterPosition) {
        if (clickHelper.isDoubleClicked(viewId)) {
            return;
        }
        MessageModel clickedMessage = (MessageModel) adapter.getItem(adapterPosition);
        replyMessage = clickedMessage.getOriginalMessage();
        findMessageAndHighlight(replyMessage.getId());
    }

    private int findPositionByMessageId(long messageId) {
        for (int i = 0; i < adapter.getItemCount(); i++) {
            Object item = adapter.getItem(i);
            if (item instanceof MessageModel &&
                    ((MessageModel) item).getId() == messageId) {
                return i;
            }
        }
        return -1;
    }

    // TODO: 31.01.2018  The highlight of replied message is temporary switched off
    private void findMessageAndHighlight(long replyMessageId) {
//        int pos = findPositionByMessageId(replyMessageId);
//        if (pos >= 0) {
//            rvMessages.scrollToPosition(pos);
//            adapter.highlightRepliedMessage(pos);
//        }
    }

    @Override
    public void onFileForwardClick(int viewId, int position) {
        if (clickHelper.isDoubleClicked(viewId)) {
            return;
        }
        attachmentMessageForForward = (MessageModel) adapter.getItems().get(position);
        startChatSelectionActivity(false);
    }

    @OnClick(R.id.ivSend)
    public void onSendMessageClick(View view) {
        if (clickHelper.isDoubleClicked(view.getId())) {
            return;
        }

        final String message = etMessage.getText().toString().trim();

        if (presenter.isChatExist()) {
            final List<MessageModel> waitMessages;
            if (isForward) {
                waitMessages = presenter.sendMessage(message,
                        getForwardMessages().get(0),
                        getForwardChats()
                );
            } else if (replyMessage != null) {
                waitMessages = presenter.sendReplyMessage(message, replyMessage);
                hideReplyContent();
            } else {
                waitMessages = presenter.sendMessage(message);
            }
            for (MessageModel waitMessage : waitMessages) {
                adapter.addWithDateAndHeader(waitMessage);
            }
            rvMessages.smoothScrollToPosition(adapter.getItemCount());
            resetMessagePanel();
        } else {
            DisplayUtils.hideSoftKeyboard(this);
            presenter.createNewSingleChat(message);
        }
    }

    @Override
    public void onSingleUserChatCreated(ChatModel chat) {
        resetMessagePanel();
        chat.setUsers(Collections.singletonList(getUserForSingleChat()));
        setupAdapter();
        presenter.getMessages();
    }

    private void clearAttachments() {
        presenter.clearAttachmentPaths();
        attachedPhotosAdapter.clear();
        attachedFilesAdapter.clear();
        showPhotoAttachments(false);
        showFileAttachments(false);
        resetMessageContainer();
    }

    private void resetMessagePanel() {
        etMessage.setText(null);
        clearAttachments();
        vgForward.setVisibility(View.GONE);
        isForward = false;
    }

    @OnClick(R.id.ivAttachment)
    protected void onAttachmentClick(View view) {
        if (View.VISIBLE == vgAttachments.getVisibility()) {
            showAttachmentContainer(false);
        } else {
            showAttachmentContainer(true);
            showActiveAttachmentMethods();

            changeStickersVisibility(false, false);
        }
    }

    @OnClick(R.id.ivAttachFiles)
    protected void onAttachFileClick(View view) {
        if (clickHelper.isDoubleClicked(view.getId())) {
            return;
        }
        SingleChatActivityPermissionsDispatcher.openFileChooserWithCheck(this);
    }

    @NeedsPermission(WRITE_EXTERNAL_STORAGE)
    protected void openFileChooser() {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        final Intent chooser = Intent.createChooser(intent, getString(R.string.chat_select_file));
        if (chooser.resolveActivity(getPackageManager()) == null) {
            new BaseAlertDialog.AlertDialogBuilder(this)
                    .setMessage(R.string.chat_file_manager_not_exist)
                    .setPositiveButtonText(R.string.chat_dialog_ok)
                    .build()
                    .show(getSupportFragmentManager(), null);
        } else {
            startActivityForResult(chooser, REQUEST_ATTACH_FILE);
        }
    }

    @Override
    public void onFileRemoved(String path) {
        presenter.removeFromAttachmentPaths(path);
        if (!presenter.isAttachmentsExist()) {
            rvFiles.setVisibility(View.GONE);

            resetMessageContainer();
        }
    }

    @OnClick(R.id.ivAttachImages)
    protected void onAttachImageClick(View view) {
        if (clickHelper.isDoubleClicked(view.getId())) {
            return;
        }
        SingleChatActivityPermissionsDispatcher.openAttachPhotoWithCheck(this);
    }

    @NeedsPermission(WRITE_EXTERNAL_STORAGE)
    protected void openAttachPhoto() {
        startActivityForResult(AttachPhotoActivity.getIntent(this), REQUEST_IMAGES);
    }

    @OnPermissionDenied(WRITE_EXTERNAL_STORAGE)
    protected void onDeniedForFileRead() {
        Toast.makeText(this, R.string.attach_photo_alert_permission_explanation_storage, Toast.LENGTH_SHORT).show();
    }

    @OnNeverAskAgain(WRITE_EXTERNAL_STORAGE)
    protected void onNeverForFileRead() {
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.attach_photo_alert_permission_explanation_storage))
                .setPositiveButton(getString(R.string.permissions_alert_open_settings), (dialog, which) -> {
                    ActionUtils.openAppSettings(this);
                })
                .setCancelable(true)
                .show();
    }

    @Override
    public void onImageRemoved(String path) {
        presenter.removeFromAttachmentPaths(path);
        if (!presenter.isAttachmentsExist()) {
            rvPhotos.setVisibility(View.GONE);

            resetMessageContainer();
        }
    }

    private boolean isNotBlank(String text) {
        return text != null && text.trim().length() > 0;
    }

    private void resetMessageContainer() {
        if (isNotBlank(etMessage.getText().toString())) {
            showSendButton(true);
        } else {
            showSendButton(false);
        }
        showAttachmentContainer(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        SingleChatActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (RESULT_OK != resultCode) {
            return;
        }
        switch (requestCode) {
            case REQUEST_ATTACH_FILE:
                if (data == null) {
                    return;
                }
                final Uri dataUri = data.getData();
                if (isFileTypeVideo(dataUri)) {
                    Toast.makeText(this, getString(R.string.chat_unsupported_file_type), Toast.LENGTH_SHORT).show();
                } else {
                    final String filePath = UriUtils.getPath(this, dataUri);
                    presenter.onFileSelected(filePath);
                }
                break;
            case REQUEST_IMAGES:
                proceedSelectedImages(data);
                break;
            case REQUEST_CHAT_SETTINGS:
                final ChatModel updatedChat = ChatSettingsActivity.unpackUpdatedChat(data);
                if (updatedChat == null) {
                    setResultAndFinish(true);
                } else {
                    presenter.setChat(updatedChat);
                    updateToolbarParticipants(updatedChat);
                    updateTitleWithChat(updatedChat);
                    updateGroupCountSubtitle(updatedChat.getUsers().size());
                    updateIsParticipant(updatedChat);
                }
                break;
            case REQUEST_SELECT_CHATS:
                ArrayList<ChatModel> selectedChats = ChatSelectionActivity.unpackSelectedChats(data);
                ChatModel chatForOpen = ChatSelectionActivity.unpackChatForOpen(data);
                boolean isActionModeForward = ChatSelectionActivity.unpackIsActionModeForward(data);

                if (selectedChats != null && chatForOpen != null) {
                    final ArrayList<MessageModel> forwardMessages;
                    if (isActionModeForward) {
                        forwardMessages = adapter.getSelectedMessages();
                    } else {
                        forwardMessages = new ArrayList<>();
                        forwardMessages.add(attachmentMessageForForward);
                    }
                    startActivity(SingleChatActivity.openChatForForward(this, selectedChats, chatForOpen, forwardMessages));
                    finish();
                }
                break;
        }
    }

    private boolean isFileTypeVideo(Uri uri) {
        String mimeType;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            mimeType = getContentResolver().getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
        }
        return mimeType != null && mimeType.startsWith("video/");
    }

    @Override
    public void attachFile(String filePath) {
        attachedFilesAdapter.add(Uri.parse(filePath), 0);
        rvFiles.smoothScrollToPosition(0);

        showFileAttachments(true);
        showOnlyFileAttachMethod();
        showSendButtonIfFileAttached();
    }

    private void proceedSelectedImages(Intent data) {
        final List<String> imagePaths = AttachPhotoActivity.unpackImagesPath(data);
        if (imagePaths == null || imagePaths.isEmpty()) {
            return;
        }
        presenter.onImagesSelected(imagePaths);
    }

    @Override
    public void attachImages(List<String> filteredImagePaths) {
        attachedPhotosAdapter.addAll(filteredImagePaths, 0);
        rvPhotos.smoothScrollToPosition(0);

        showPhotoAttachments(true);
        showOnlyPhotoAttachMethod();
        showSendButtonIfFileAttached();
    }

    private void showSendButtonIfFileAttached() {
        if (presenter.isAttachmentsExist()) {
            showSendButton(true);
        }
    }

    @Override
    public void showMaxAttachmentsToast() {
        Toast.makeText(this,
                getString(R.string.single_chat_max_attachments, SingleChatPresenter.MAX_ATTACHMENTS_COUNT),
                Toast.LENGTH_SHORT)
                .show();
    }

    private void showSendButton(boolean show) {
        if (show || isForward) {
            ivSend.setVisibility(View.VISIBLE);
        } else {
            ivSend.setVisibility(View.GONE);
        }
    }

    private void showAttachmentContainer(boolean show) {
        if (show) {
            vgAttachments.setVisibility(View.VISIBLE);
        } else {
            vgAttachments.setVisibility(View.GONE);
        }
    }

    private void showActiveAttachmentMethods() {
        final int fileAttachmentsCount = attachedFilesAdapter.getItemCount();
        final int photoAttachmentsCount = attachedPhotosAdapter.getItemCount();

        if (fileAttachmentsCount == 0 && photoAttachmentsCount == 0) {
            ivAttachFiles.setVisibility(View.VISIBLE);
            ivAttachImages.setVisibility(View.VISIBLE);
            return;
        }

        if (fileAttachmentsCount == 0) {
            ivAttachFiles.setVisibility(View.GONE);
        } else {
            ivAttachFiles.setVisibility(View.VISIBLE);
        }
        if (photoAttachmentsCount == 0) {
            ivAttachImages.setVisibility(View.GONE);
        } else {
            ivAttachImages.setVisibility(View.VISIBLE);
        }
    }

    private void showOnlyPhotoAttachMethod() {
        ivAttachFiles.setVisibility(View.GONE);
    }

    private void showOnlyFileAttachMethod() {
        ivAttachImages.setVisibility(View.GONE);
    }

    public void showPhotoAttachments(boolean show) {
        if (show) {
            rvPhotos.setVisibility(View.VISIBLE);
        } else {
            rvPhotos.setVisibility(View.GONE);
        }
    }

    public void showFileAttachments(boolean show) {
        if (show) {
            rvFiles.setVisibility(View.VISIBLE);
        } else {
            rvFiles.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (android.R.id.home == item.getItemId()) {
            setResultAndFinish(false);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isStickersVisible()) {
            changeStickersVisibility(false, false);
        } else {
            setResultAndFinish(false);
        }
    }

    private void setResultAndFinish(boolean isChatDeleted) {
        final Intent data = new Intent()
                .putExtra(EXTRA_OUT_UPDATED_CHAT, presenter.getChat())
                .putExtra(EXTRA_IS_CHAT_DELETED, isChatDeleted);
        setResult(RESULT_OK, data);
        finish();
    }

    @OnClick(R.id.fabToBottom)
    public void goToLastMessage(View view) {
        rvMessages.scrollToPosition(adapter.getItemCount() - 1);
    }

}
