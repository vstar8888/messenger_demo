package ru.demo.messenger.chats.list;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.collection.LongSparseArray;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import biz.growapp.base.adapter.AbstractAdapterDelegate;
import butterknife.BindView;
import butterknife.ButterKnife;
import ru.demo.messenger.Consts;
import ru.demo.messenger.R;
import ru.demo.messenger.data.chat.ChatModel;
import ru.demo.messenger.data.chat.LastChatMessage;
import ru.demo.messenger.data.message.MessageModel;
import ru.demo.messenger.data.user.UserModel;
import ru.demo.messenger.helpers.CompositeImageHelper;
import ru.demo.messenger.helpers.CompositeImageView;
import ru.demo.messenger.people.OnlineUsersHolder;
import ru.demo.messenger.utils.DateUtil;
import ru.demo.messenger.utils.Prefs;
import ru.demo.messenger.utils.VectorUtils;

class ChatsDelegate extends AbstractAdapterDelegate<ChatModel, Object, ChatsDelegate.Holder> {

    private static final int NO_INDEX = -1;

    private final long selfUserId;

    public interface Callback {
        void onSelectedChatsUpdated(int position);
    }

    private final Drawable statusReadAllIcon;
    private final Drawable statusReadSomeoneIcon;
    private final Drawable statusDeliveredIcon;
    private final Callback callback;
    private final CompositeImageHelper compositeImageHelper;
    private final Object receiveMessageMutex = this;
    private final LayoutInflater inflater;
    private final Context context;
    private final ChatsAdapter adapter;

    @ColorInt
    private final int lightBlueColor;

    private final Typeface robotoRegular;
    private final Typeface robotoMedium;

    private final LongSparseArray<ChatModel> selectedChats = new LongSparseArray<>();

    private ChatModel firstSelectedChat;
    private final boolean isForSelection;

    ChatsDelegate(ChatsAdapter adapter, Context context,
                  CompositeImageHelper compositeImageHelper, boolean isForSelection,
                  Callback callback) {
        this.adapter = adapter;
        this.inflater = LayoutInflater.from(context);
        this.context = context;
        this.selfUserId = Prefs.get().getLong(Consts.Prefs.USER_ID, 0);
        this.isForSelection = isForSelection;
        this.callback = callback;
        this.compositeImageHelper = compositeImageHelper;
        this.statusReadAllIcon = VectorUtils.getVectorDrawable(context, R.drawable.ic_chat_read_all);
        this.statusReadSomeoneIcon = VectorUtils.getVectorDrawable(context, R.drawable.ic_chat_read_someone);
        this.statusDeliveredIcon = VectorUtils.getVectorDrawable(context, R.drawable.ic_chat_delivered);
        this.lightBlueColor = ContextCompat.getColor(context, R.color.light_blue);
        this.robotoRegular = Typeface.create("sans-serif", Typeface.NORMAL);
        this.robotoMedium = Typeface.create("sans-serif-medium", Typeface.NORMAL);
    }

    @Override
    protected boolean isForViewType(@NonNull Object item, @NonNull List items, int position) {
        return item instanceof ChatModel;
    }

    ArrayList<ChatModel> getSelectedChats() {
        ArrayList<ChatModel> selected = new ArrayList<>();
        for (int i = 0; i < selectedChats.size(); i++) {
            long key = selectedChats.keyAt(i);
            selected.add(selectedChats.get(key));
        }
        return selected;
    }

    int getSelectedChatsSize() {
        return selectedChats.size();
    }

    ChatModel getFirstSelectedChat() {
        return firstSelectedChat;
    }

    private boolean isSelected(ChatModel chat) {
        return selectedChats.get(chat.getId()) != null;
    }

    void changeChatSelectState(int position) {
        final ChatModel chat = (ChatModel) adapter.getItems().get(position);
        if (isSelected(chat)) {
            selectedChats.remove(chat.getId());
        } else {
            selectedChats.put(chat.getId(), chat);
        }

        calculateFirstSelectedChat();

        adapter.notifyItemChanged(position);
    }

    private void calculateFirstSelectedChat() {
        final List<Object> items = adapter.getItems();
        int firstSelectedPosition = -1;
        for (int i = 0; i < items.size(); i++) {
            Object item = items.get(i);
            if (item instanceof ChatModel) {
                final ChatModel selectedChat = selectedChats.get(((ChatModel) item).getId());
                if (selectedChat != null) {
                    if (firstSelectedPosition == -1 || firstSelectedPosition > i) {
                        firstSelectedPosition = i;
                        firstSelectedChat = selectedChat;
                    }
                }
            }
        }
    }

    void userOnlineStatusChanged(long userId) {
        for (int pos = 0; pos < adapter.getItems().size(); pos++) {
            final ChatModel chat = (ChatModel) adapter.getItems().get(pos);
            if (chat.isOneToOne()) {
                final List<UserModel> chatParticipants = chat.getUsers();
                if (chatParticipants.isEmpty()) {
                    continue;
                }
                if (chatParticipants.get(0).getId() == userId) {
                    // TODO: DO 14.09.2016 pass data to payload
                    adapter.notifyItemChanged(pos);
                    return;
                }
            }
        }
    }

    void changeOutgoingMessageStatuses(long chatId, @MessageModel.ReadStatus String status) {
        for (int i = 0; i < adapter.getItems().size(); i++) {
            final ChatModel chat = (ChatModel) adapter.getItems().get(i);
            if (chatId == chat.getId()) {
                final LastChatMessage lastMessage = chat.getLastMessage();
                if (lastMessage == null) {
                    continue;
                }
                final boolean isOutgoingMessage = selfUserId == lastMessage.getId();
                if (isOutgoingMessage) {
                    final boolean isStatusChanged = !lastMessage.getReadStatus().equals(status);
                    if (isStatusChanged) {
                        lastMessage.setReadStatus(status);
                        adapter.notifyItemChanged(i);
                    }
                }
            }
        }
    }

    /**
     * Return true if added or false if replaced
     */
    boolean addOrUpdate(ChatModel chat) {
        synchronized (receiveMessageMutex) {
            final int chatIndex = getChatIndexById(chat.getId());
            if (chatIndex > NO_INDEX) {
                adapter.replace(chat, chatIndex);
                return false;
            }

            adapter.add(chat, 0);
            return true;
        }
    }

    void updateChatPhoto(long chatId, String photo) {
        synchronized (receiveMessageMutex) {
            final int chatPosition = getChatIndexById(chatId);
            if (chatPosition > NO_INDEX) {
                ((ChatModel) adapter.getItem(chatPosition)).setPhoto(photo);
                adapter.notifyItemChanged(chatPosition);
            }
        }
    }

    void updateLastMessageInChat(MessageModel message) {
        synchronized (receiveMessageMutex) {
            final int chatIndex = getChatIndexById(message.getChatId());
            if (chatIndex > NO_INDEX) {
                final ChatModel chat = (ChatModel) adapter.getItems().get(chatIndex);
                if (chat.getLastMessage().getId() == message.getId()) {
                    return;
                }
                final MessageModel originalMessage = message.getOriginalMessage();
                if (MessageModel.MessageType.FORWARD.equalsIgnoreCase(message.getMessageType())) {
                    chat.setLastMessage(new LastChatMessage(originalMessage));
                } else {
                    chat.setLastMessage(new LastChatMessage(message));
                }
                if (MessageModel.Direction.OUTGOING.equals(message.getDirection())) {
                    chat.setUnreadMessagesCount(0);
                } else {
                    chat.setUnreadMessagesCount(chat.getUnreadMessagesCount() + 1);
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
                adapter.notifyItemChanged(chatIndex);

                final ChatModel movedMessage = (ChatModel) adapter.getItems().remove(chatIndex);
                adapter.getItems().add(0, movedMessage);
                adapter.notifyItemMoved(chatIndex, 0);
            }
        }
    }

    boolean isChatExist(long chatId) {
        return getChatIndexById(chatId) > NO_INDEX;
    }

    private int getChatIndexById(long chatId) {
        for (int i = 0; i < adapter.getItems().size(); i++) {
            if (((ChatModel) adapter.getItems().get(i)).getId() == chatId) {
                return i;
            }
        }
        return NO_INDEX;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent) {
        final Holder holder = new Holder(inflater.inflate(R.layout.chat_item, parent, false));
        holder.itemView.setOnClickListener(v -> {
            final int adapterPosition = holder.getAdapterPosition();
            if (adapter.isActionMode()) {
                adapter.proceedActionMode(adapterPosition);
            } else {
                if (isForSelection) {
                    callback.onSelectedChatsUpdated(holder.getAdapterPosition());
                } else {
                    adapter.proceedClick(adapterPosition);
                }
            }
        });
        if (!isForSelection) {
            holder.itemView.setOnLongClickListener(v -> {
                adapter.proceedLongClick(holder.getAdapterPosition());
                return true;
            });
        }
        return holder;
    }

    @ColorInt
    private int getColor(@ColorRes int id) {
        return ContextCompat.getColor(context, id);
    }

    private void bindSelection(Holder holder, ChatModel chat) {
        if (isForSelection) {
            if (isSelected(chat)) {
                holder.vgChat.setBackgroundColor(getColor(R.color.blue_accent_200_alpha_130));
                holder.ivIsSelectedCircle.setVisibility(View.VISIBLE);
                holder.ivIsSelected.setVisibility(View.VISIBLE);
            } else {
                holder.itemView.setBackground(null);
                holder.ivIsSelectedCircle.setVisibility(View.GONE);
                holder.ivIsSelected.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onBindViewHolder(@NonNull Holder holder, @NonNull ChatModel item,
                                    @NonNull List<Object> payloads) {
        compositeImageHelper.showChatImage(holder.civAvatar, item, selfUserId);

        final LastChatMessage lastMessage = item.getLastMessage();
        holder.usersInChat.setText(item.getTitle());
        holder.time.setText(
                DateUtil.getDateStringFromTimestamp(context, lastMessage.getCreatedAt()));


        final List<UserModel> chatParticipants = item.getUsers();
        if (item.isOneToOne() && !chatParticipants.isEmpty()) {
            final long participantId = chatParticipants.get(0).getId();
            if (OnlineUsersHolder.isOnline(participantId)) {
                holder.ivOnlineStatus.setVisibility(View.VISIBLE);
            } else {
                holder.ivOnlineStatus.setVisibility(View.GONE);
            }
        } else {
            holder.ivOnlineStatus.setVisibility(View.GONE);
        }

        bindDocsLastMessage(holder, lastMessage);
        if (MessageModel.MessageType.STICKER.equalsIgnoreCase(lastMessage.getMessageType())) {
            lastMessage.setText(context.getString(R.string.chat_sticker));
        }

        final boolean outgoingMessage = selfUserId == lastMessage.getId();
        if (outgoingMessage) {
            holder.itemView.setBackground(null);
            holder.lastMessageText.setText(lastMessage.getText());
            holder.lastMessageText.setTypeface(robotoRegular);
            holder.usersInChat.setTypeface(robotoRegular);
            holder.tvUnreadCount.setVisibility(View.GONE);
            if (MessageModel.MessageType.CREATE_GROUP.equals(lastMessage.getMessageType())) {
                holder.tvLastMessageYou.setVisibility(View.GONE);
            } else {
                holder.tvLastMessageYou.setVisibility(View.VISIBLE);
            }
            holder.ivReadStatus.setVisibility(View.VISIBLE);
            if (MessageModel.ReadStatus.DELIVERED.equals(lastMessage.getReadStatus())) {
                holder.ivReadStatus.setImageDrawable(statusDeliveredIcon);
            } else if (MessageModel.ReadStatus.READ.equals(lastMessage.getReadStatus())) {
                holder.ivReadStatus.setImageDrawable(statusReadSomeoneIcon);
            } else if (MessageModel.ReadStatus.READ_ALL.equals(lastMessage.getReadStatus())) {
                holder.ivReadStatus.setImageDrawable(statusReadAllIcon);
            }
        } else {
            holder.ivReadStatus.setVisibility(View.GONE);
            holder.tvLastMessageYou.setVisibility(View.GONE);
            holder.lastMessageText.setText(lastMessage.getText());
            if (item.getUnreadMessagesCount() == 0) {
                holder.lastMessageText.setTypeface(robotoRegular);
                holder.usersInChat.setTypeface(robotoRegular);
                holder.itemView.setBackground(null);

                holder.tvUnreadCount.setVisibility(View.GONE);
            } else {
                holder.lastMessageText.setTypeface(robotoMedium);
                holder.usersInChat.setTypeface(robotoMedium);
                holder.itemView.setBackgroundColor(lightBlueColor);

                holder.tvUnreadCount.setVisibility(View.VISIBLE);
                holder.tvUnreadCount.setText(String.valueOf(item.getUnreadMessagesCount()));
            }
        }
        bindSelection(holder, item);
    }

    private void bindDocsLastMessage(Holder holder, LastChatMessage lastMessage) {
        boolean isOnlyFile = updateMessageIfOnlyFile(lastMessage, lastMessage.getMessageType());
        if (isOnlyFile) {
            holder.ivIsAttach.setVisibility(View.VISIBLE);
            holder.lastMessageText.setTextColor(ContextCompat.getColor(context, R.color.dark_blue));
        } else {
            holder.ivIsAttach.setVisibility(View.GONE);
            holder.lastMessageText.setTextColor(ContextCompat.getColor(context, R.color.dark_text_secondary));
        }
    }

    private boolean updateMessageIfOnlyFile(LastChatMessage lastChatMessage,
                                            @MessageModel.MessageType String messageType) {
        if (MessageModel.MessageType.FILE_IMAGE.equals(messageType)) {
            lastChatMessage.setText(context.getString(R.string.chat_image));
            return true;
        } else if (MessageModel.MessageType.FILE_VIDEO.equals(messageType)) {
            lastChatMessage.setText(context.getString(R.string.chat_video));
            return true;
        } else if (MessageModel.MessageType.FILE_DOCUMENT.equals(messageType)) {
            lastChatMessage.setText(context.getString(R.string.chat_doc));
            return true;
        }
        return false;
    }

    static class Holder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvName) TextView usersInChat;
        @BindView(R.id.tvLastMessage) TextView lastMessageText;
        @BindView(R.id.civAvatar) CompositeImageView civAvatar;
        @BindView(R.id.ivOnlineStatus) ImageView ivOnlineStatus;
        @BindView(R.id.tvDate) TextView time;
        @BindView(R.id.tvUnreadCount) TextView tvUnreadCount;
        @BindView(R.id.ivReadStatus) ImageView ivReadStatus;
        @BindView(R.id.tvLastMessageYou) TextView tvLastMessageYou;
        @BindView(R.id.ivIsAttach) ImageView ivIsAttach;
        @BindView(R.id.vgChat) ViewGroup vgChat;
        @BindView(R.id.ivIsSelectedCircle) ImageView ivIsSelectedCircle;
        @BindView(R.id.ivIsSelected) ImageView ivIsSelected;

        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}