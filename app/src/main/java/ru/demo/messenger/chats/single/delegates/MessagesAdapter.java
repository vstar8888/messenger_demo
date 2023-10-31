package ru.demo.messenger.chats.single.delegates;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import android.util.LongSparseArray;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.growapp.base.pagination.PaginationAdapter;
import ru.demo.messenger.Consts;
import ru.demo.messenger.data.message.Attachment;
import ru.demo.messenger.data.message.MessageModel;
import ru.demo.messenger.data.message.MessageModel.ReadStatus;
import ru.demo.messenger.utils.Prefs;

public class MessagesAdapter extends PaginationAdapter<Object> {

    public interface Callback extends MessageBindHelper.Callback {
        void onAttachedFileClick(Attachment attachment);

        void onProfileClick(int viewId, long userId);

        void onMessageLongClick(int adapterPosition);

        void disableActionMode();

        void invalidateActionMode();

        void onFileForwardClick(int viewId, int adapterPosition);

        void onReplyMessageClick(int viewId, int adapterPosition);
    }

    final ZoneId systemZone = ZoneId.systemDefault();
    final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    final long myUserId;
    final boolean isOneToOneChat;
    private final Callback callback;
    private final LongSparseArray<Boolean> groupHeaderMessageIds;

    private final LongSparseArray<MessageModel> selectedMessages = new LongSparseArray<>();
    boolean isActionMode = false;
    int posForHighlight = -1;
    boolean needHighlight = false;

    public MessagesAdapter(Loader loader, boolean isOneToOne, Callback callback, @Direction String direction) {
        super(loader, direction);
        this.callback = callback;
        this.isOneToOneChat = isOneToOne;
        this.myUserId = Prefs.get().getLong(Consts.Prefs.USER_ID, 0);
        this.groupHeaderMessageIds = new LongSparseArray<>();
    }

    // TODO: 31.01.2018  The highlight of replied message is temporary switched off
    public void highlightRepliedMessage(int adapterPosition) {
        posForHighlight = adapterPosition;
        needHighlight = true;
        notifyItemChanged(adapterPosition);
    }

    public void enableActionMode() {
        isActionMode = true;
    }

    public void disableActionMode() {
        isActionMode = false;
        selectedMessages.clear();
        notifyDataSetChanged();
    }

    public void changeDeliveredMessagesStatus(@MessageModel.ReadStatus String status) {
        for (int i = items.size() - 1; i >= 0; i--) {
            final MessageModel message = items.get(i) instanceof MessageModel ?
                    ((MessageModel) items.get(i)) : null;
            if (message == null) {
                continue;
            }
            final boolean hasMutableStatus = ReadStatus.DELIVERED.equals(message.getStatus()) ||
                    ReadStatus.READ.equals(message.getStatus());
            final boolean isOutgoingMessage = MessageModel.Direction.OUTGOING.equals(message.getDirection());
            if (isOutgoingMessage && hasMutableStatus) {
                if (message.getStatus().equals(status)) {
                    continue;
                }
                message.setStatus(status);
                notifyItemChanged(i);
            }
        }
    }

    public int findFirstWaitItem() {
        for (int i = 0; i < items.size(); i++) {
            Object item = items.get(i);
            if (item instanceof MessageModel) {
                final String readStatus = ((MessageModel) item).getStatus();
                if (ReadStatus.WAIT.equals(readStatus)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void clear() {
        groupHeaderMessageIds.clear();
        super.clear();
    }

    private void addEdgeDateHeaderForEnd(final List<Object> tempList, List<Object> loadedItems) {
        final MessageModel loadedMessage = (MessageModel) loadedItems.get(0);
        LocalDate firstLoadedMessageDate = LocalDate.from(Instant.ofEpochSecond(loadedMessage.getCreatedAt())
                .atZone(systemZone));
        LocalDate lastAdapterMessageDate = null;
        for (int i = items.size() - 1; i >= 0; i--) {
            Object item = items.get(i);
            if (item instanceof LocalDate) {
                lastAdapterMessageDate = (LocalDate) item;
                break;
            }
        }
        if (lastAdapterMessageDate != null && firstLoadedMessageDate.isAfter(lastAdapterMessageDate)) {
            tempList.add(0, firstLoadedMessageDate);
        }
    }

    // WA: NR 11.01.2019  fixed duplication show of sent message with slow connection
    private List<Object> removeDuplicates(List<Object> loadedItems) {
        ArrayList<Object> newMessages = new ArrayList<>();
        for (int i = 0; i < loadedItems.size(); i++) {
            final MessageModel loadedMessage = (MessageModel) loadedItems.get(i);
            boolean hasMessage = false;
            for (int j = 0; j < items.size(); j++) {
                Object item = items.get(j);
                if (item.equals(loadedMessage)) {
                    hasMessage = true;
                    break;
                }
            }
            if (!hasMessage) {
                newMessages.add(loadedMessage);
            }
        }
        return newMessages;
    }

    public void addAll(List<Object> newItems, @PaginationAdapter.Direction String direction) {
        int posForAdd;
        if (PaginationAdapter.Direction.TO_END.equals(direction)) {
            posForAdd = getItemCount();
        } else {
            posForAdd = 0;
        }
        if (newItems.isEmpty()) {
            addAll(newItems, posForAdd);
            return;
        }

        List<Object> loadedItems = removeDuplicates(newItems);

        //add dates before messages
        final List<Object> tempList = new ArrayList<>();
        LocalDate firstDate = null;

        if (!PaginationAdapter.Direction.TO_END.equals(direction)) {
            for (int i = 0; i < items.size(); i++) {
                Object item = items.get(i);
                if (item instanceof LocalDate) {
                    items.remove(0);
                    notifyItemRemoved(0);
                    firstDate = (LocalDate) item;
                    break;
                }
            }
        }
        int endIndex = loadedItems.size();

        LocalDate messageDate = firstDate;
        for (int i = loadedItems.size() - 1; i >= 0; i--) {
            final MessageModel message = (MessageModel) loadedItems.get(i);
            messageDate = LocalDate.from(Instant.ofEpochSecond(message.getCreatedAt())
                    .atZone(systemZone));
            if (firstDate == null) {
                firstDate = messageDate;
            }
            if (messageDate.isBefore(firstDate)) {
                final int startIndex = i + 1;
                if (startIndex <= endIndex) {
                    tempList.addAll(0, (List) loadedItems.subList(startIndex, endIndex));
                }
                tempList.add(0, firstDate);
                firstDate = messageDate;
                endIndex = startIndex;
            }
        }
        tempList.addAll(0, (List) loadedItems.subList(0, endIndex));
        // add edge date header
        if (PaginationAdapter.Direction.TO_END.equals(direction)) {
            addEdgeDateHeaderForEnd(tempList, loadedItems);
        } else {
            tempList.add(0, messageDate);
        }

        addAll(tempList, posForAdd);

        // active group chat design - show photo and name of income message owner
        LongSparseArray<Boolean> prevGroupHeaderMessageIds = groupHeaderMessageIds.clone();
        groupHeaderMessageIds.clear();
        if (!isOneToOneChat) {
            int repeatCount = 0;
            long lastAuthorId = -1;

            for (int i = 0; i < items.size(); i++) {
                final Object item = items.get(i);
                MessageModel message = null;
                if (item instanceof MessageModel) {
                    message = (MessageModel) item;
                }
                if (message == null) {
                    lastAuthorId = -1;
                    repeatCount = 0;
                    continue;
                }
                if (MessageModel.Direction.INCOMING.equals(message.getDirection())
                        && (message.isBubbleMessage() || message.isStickerMessage())) {
                    if (lastAuthorId == message.getAuthorId()) {
                        repeatCount++;
                    } else {
                        repeatCount = 0;
                        lastAuthorId = message.getAuthorId();
                    }
                    if (repeatCount == 0) {
                        groupHeaderMessageIds.put(message.getId(), true);
                    }
                } else {
                    repeatCount = 0;
                    lastAuthorId = -1;
                }
            }
        }

        // remove avatars from previous positions
        for (int i = 0; i < prevGroupHeaderMessageIds.size(); i++) {
            long messageId = prevGroupHeaderMessageIds.keyAt(i);
            for (int j = getItemCount() - 1; j >= 0; j--) {
                Object item = getItem(j);
                if (item instanceof MessageModel &&
                        messageId == ((MessageModel) item).getId()) {
                    MessageModel message = (MessageModel) item;

                    if (j - 1 >= 0) {
                        Object prevItem = getItem(j - 1);
                        if (prevItem instanceof MessageModel
                                && (((MessageModel) prevItem).isBubbleMessage() || ((MessageModel) prevItem).isStickerMessage())
                                && ((MessageModel) prevItem).getAuthorId() == message.getAuthorId()) {
                            notifyItemChanged(j);
                            break;
                        }
                    }
                }
            }
        }
    }

    public synchronized void addWithDateAndHeader(MessageModel newMessage) {
        List<Object> messageWithoutDuplicates = removeDuplicates(Collections.singletonList(newMessage));
        if (messageWithoutDuplicates.isEmpty()) {
            return;
        }
        MessageModel message = (MessageModel) messageWithoutDuplicates.get(0);

        if (getItemCount() > 0) {
            // try add date
            final int lastItemIndex = getItems().size() - 1;
            for (int i = lastItemIndex; i >= 0; i--) {
                final Object item = getItems().get(i);
                if (item instanceof LocalDate) {
                    final LocalDate lastDate = (LocalDate) item;
                    final LocalDate newDate =
                            LocalDate.from(Instant.ofEpochSecond(message.getCreatedAt())
                                    .atZone(systemZone));
                    final boolean lastDateBeforeNewDate =
                            lastDate.getDayOfYear() < newDate.getDayOfYear();
                    final boolean lastDateNotEqualNewDate =
                            lastDate.getDayOfYear() != newDate.getDayOfYear();
                    if (lastDateBeforeNewDate && lastDateNotEqualNewDate) {
                        add(newDate);
                    }
                    break;
                }
            }

            // try add header
            if (!isOneToOneChat) {
                if (MessageModel.Direction.INCOMING.equals(message.getDirection())) {
                    final Object lastItem = getItems().get(lastItemIndex);

                    boolean isGroupHeaderPosition = false;
                    if (lastItem instanceof MessageModel) {
                        final MessageModel lastMessage = (MessageModel) lastItem;
                        if (lastMessage.getAuthorId() != message.getAuthorId()) {
                            isGroupHeaderPosition = true;
                        }
                    } else if (lastItem instanceof LocalDate) {
                        isGroupHeaderPosition = true;
                    } else {
                        isGroupHeaderPosition = true;
                    }
                    if (isGroupHeaderPosition) {
                        groupHeaderMessageIds.put(message.getId(), true);
                    }
                }
            }
        }
        add(message);
    }

    boolean isNeedShowAvatar(MessageModel message) {
        return groupHeaderMessageIds.get(message.getId()) != null;
    }

    void proceedLongClick(RecyclerView.ViewHolder holder) {
        if (!isActionMode) {
            final Object selectedItem = getItem(holder.getAdapterPosition());
            if (selectedItem instanceof MessageModel) {
                final String messageStatus = ((MessageModel) selectedItem).getStatus();
                if (ReadStatus.WAIT.equalsIgnoreCase(messageStatus)) {
                    return;
                }
            }
            callback.onMessageLongClick(holder.getAdapterPosition());
            proceedActionMode(holder);
        }
    }

    public ArrayList<MessageModel> getSelectedMessages() {
        ArrayList<MessageModel> selected = new ArrayList<>();
        for (int i = 0; i < selectedMessages.size(); i++) {
            long key = selectedMessages.keyAt(i);
            selected.add(selectedMessages.get(key));
        }
        return selected;
    }

    boolean isSelected(MessageModel message) {
        return selectedMessages.get(message.getId()) != null;
    }

    @Nullable
    public MessageModel getSingleSelectedMessage() {
        if (selectedMessages.size() == 1) {
            return selectedMessages.valueAt(0);
        } else {
            return null;
        }
    }

    void proceedActionMode(RecyclerView.ViewHolder holder) {
        MessageModel message = (MessageModel) getItems().get(holder.getAdapterPosition());
        if (ReadStatus.WAIT.equalsIgnoreCase(message.getStatus())) {
            return;
        }
        if (isSelected(message)) {
            selectedMessages.remove(message.getId());
        } else {
            selectedMessages.put(message.getId(), message);
        }
        if (selectedMessages.size() == 0) {
            callback.disableActionMode();
        } else {
            callback.invalidateActionMode();
            notifyItemChanged(holder.getAdapterPosition());
        }
    }

    @Override
    public long getItemId(int position) {
        final Object obj = items.get(position);
        if (obj instanceof MessageModel) {
            return ((MessageModel) obj).getId();
        }
        if (obj instanceof LocalDate) {
            return -((LocalDate) obj).toEpochDay();
        }
        return RecyclerView.NO_ID;
    }

}