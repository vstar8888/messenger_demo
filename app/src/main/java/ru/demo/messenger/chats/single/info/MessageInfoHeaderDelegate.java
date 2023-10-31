package ru.demo.messenger.chats.single.info;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.List;

import biz.growapp.base.adapter.AbstractAdapterDelegate;
import ru.demo.messenger.R;
import ru.demo.messenger.chats.single.delegates.MessageBindHelper;
import ru.demo.messenger.chats.single.delegates.OutgoingMessageDelegate;
import ru.demo.messenger.data.message.AttachedFiles;
import ru.demo.messenger.data.message.MessageModel;

class MessageInfoHeaderDelegate extends
        AbstractAdapterDelegate<MessageModel, Object, OutgoingMessageDelegate.Holder> {

    public interface Callback extends MessageBindHelper.Callback {
        void onProfileClick(int viewId, long userId);

        void onFileForwardClick(int adapterPosition);
    }

    private final LayoutInflater inflater;
    private final Callback callback;
    private final ZoneId systemZone = ZoneId.systemDefault();
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    MessageInfoHeaderDelegate(@NonNull Context context, @NonNull Callback callback) {
        this.inflater = LayoutInflater.from(context);
        this.callback = callback;
    }

    @Override
    protected boolean isForViewType(@NonNull Object item, @NonNull List<Object> items, int position) {
        return item instanceof MessageModel;
    }

    @NonNull
    @Override
    public OutgoingMessageDelegate.Holder onCreateViewHolder(@NonNull ViewGroup parent) {
        final OutgoingMessageDelegate.Holder holder =
                new OutgoingMessageDelegate.Holder(inflater.inflate(R.layout.item_message_info_header, parent, false));
        holder.ivForwardFileArrow.setOnClickListener(v ->
                callback.onFileForwardClick(holder.getAdapterPosition())
        );
        return holder;
    }

    @Override
    protected void onBindViewHolder(@NonNull OutgoingMessageDelegate.Holder holder,
                                    @NonNull MessageModel item, @NonNull List<Object> payloads) {
        holder.tvMessage.setText(item.getMessage());
        holder.tvSendTime.setText(timeFormatter.format(
                LocalTime.from(Instant.ofEpochSecond(item.getCreatedAt()).atZone(systemZone)))
        );

        MessageBindHelper.bindReadStatus(holder, item);

        holder.ivForwardFileArrow.setVisibility(View.INVISIBLE);
        final AttachedFiles attachedFiles;
        if (MessageModel.MessageType.FORWARD.equalsIgnoreCase(item.getMessageType())) {
            attachedFiles = item.getOriginalMessage().getFiles();
        } else {
            attachedFiles = item.getFiles();

        }
        MessageBindHelper.bindImages(callback, holder, attachedFiles.getImages());
        MessageBindHelper.bindFiles(inflater, callback, holder, attachedFiles.getFiles());

        MessageBindHelper.bindForward(holder, item, callback);
        MessageBindHelper.bindReply(holder, item);
    }

}