package ru.demo.messenger.chats.single.delegates;

import android.animation.ValueAnimator;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import biz.growapp.base.adapter.AbstractAdapterDelegate;
import butterknife.BindView;
import butterknife.ButterKnife;
import ru.demo.messenger.R;
import ru.demo.messenger.data.message.MessageModel;

public class OutgoingMessageDelegate extends AbstractAdapterDelegate<MessageModel, Object, OutgoingMessageDelegate.Holder> {

    private final MessagesAdapter.Callback callback;
    private final LayoutInflater inflater;
    private final MessagesAdapter adapter;

    public OutgoingMessageDelegate(MessagesAdapter.Callback callback, Context context, MessagesAdapter adapter) {
        this.callback = callback;
        this.inflater = LayoutInflater.from(context);
        this.adapter = adapter;
    }

    @Override
    protected boolean isForViewType(@NonNull Object item, @NonNull List items, int position) {
        return item instanceof MessageModel
                && ((MessageModel) item).isBubbleMessage()
                && ((MessageModel) item).getAuthorId() == adapter.myUserId;
    }

    private void setClickListeners(Holder holder) {
        holder.itemView.setOnClickListener(v -> {
            if (adapter.isActionMode) {
                adapter.proceedActionMode(holder);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            adapter.proceedLongClick(holder);
            return true;
        });
        holder.tvMessage.setOnLongClickListener(v -> {
            adapter.proceedLongClick(holder);
            return true;
        });
        holder.tvMessage.setOnClickListener(v -> {
            if (adapter.isActionMode) {
                adapter.proceedActionMode(holder);
            }
        });
        holder.ivForwardFileArrow.setOnClickListener(v -> {
            callback.onFileForwardClick(v.getId(), holder.getAdapterPosition());
        });
        holder.vgReply.setOnClickListener(v -> {
            if (adapter.isActionMode) {
                adapter.proceedActionMode(holder);
            } else {
                callback.onReplyMessageClick(v.getId(), holder.getAdapterPosition());
            }
        });
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent) {
        Holder outgoingHolder = new Holder(inflater.inflate(R.layout.item_message_outgoing, parent, false));
        setClickListeners(outgoingHolder);
        return outgoingHolder;
    }

    @Override
    protected void onBindViewHolder(@NonNull Holder holder, @NonNull MessageModel item,
                                    @NonNull List<Object> payloads) {
        MessageBindHelper.bindReadStatus(holder, item);
        MessageBindHelper.bindOutgoingMessage(holder, item, adapter, callback, inflater);
    }

    public static class Holder extends RecyclerView.ViewHolder {
        public @BindView(R.id.tvMessage) TextView tvMessage;
        public @BindView(R.id.tvSendTime) TextView tvSendTime;
        @BindView(R.id.rvAttachments) RecyclerView rvAttachments;
        @BindView(R.id.vgFilesContainer) LinearLayout vgFilesContainer;
        @BindView(R.id.vgMessage) ViewGroup vgMessage;
        @BindView(R.id.vgBubble) ViewGroup vgBubble;

        @BindView(R.id.vgReply) ViewGroup vgReply;
        @BindView(R.id.tvReplyAuthorName) TextView tvReplyAuthorName;
        @BindView(R.id.tvReplyContent) TextView tvReplyContent;
        @BindView(R.id.sdvReplyImagePreview) SimpleDraweeView sdvReplyImagePreview;
        @BindView(R.id.ivReplyImageCloseCircle) ImageView ivReplyImageCloseCircle;
        @BindView(R.id.vCloseReply) View vCloseReply;
        @BindView(R.id.ivReplyClose) ImageView ivReplyClose;

        @BindView(R.id.tvForwardedMessageTitle) TextView tvForwardedMessageTitle;
        @BindView(R.id.tvForwardedMessageFrom) TextView tvForwardedMessageFrom;
        public @BindView(R.id.ivForwardFileArrow) ImageView ivForwardFileArrow;

        MessageImageAdapter adapter;

        int replyWidth = 0;
        int bubbleWidth = 0;
        ValueAnimator highlightAnimator;

        public Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}