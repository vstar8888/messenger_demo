package ru.demo.messenger.chats.single.delegates;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import biz.growapp.base.adapter.AbstractAdapterDelegate;
import butterknife.BindView;
import ru.demo.messenger.R;
import ru.demo.messenger.data.message.MessageModel;
import ru.demo.messenger.helpers.UserNameHelper;
import ru.demo.messenger.people.TextDrawable;

public class IncomingMessageDelegate extends AbstractAdapterDelegate<MessageModel, Object, IncomingMessageDelegate.Holder> {

    private final MessagesAdapter.Callback callback;
    private final LayoutInflater inflater;
    private final MessagesAdapter adapter;
    private final int userPhotoSize;
    private final int previewBackgroundColor;

    public IncomingMessageDelegate(MessagesAdapter.Callback callback, Context context, MessagesAdapter adapter) {
        this.callback = callback;
        this.inflater = LayoutInflater.from(context);
        this.adapter = adapter;
        this.userPhotoSize = context.getResources().getDimensionPixelSize(R.dimen.message_user_photo_size);
        this.previewBackgroundColor = ContextCompat.getColor(context, R.color.dark_blue);
    }

    @Override
    protected boolean isForViewType(@NonNull Object item, @NonNull List items, int position) {
        return item instanceof MessageModel
                && ((MessageModel)item).isBubbleMessage()
                && ((MessageModel)item).getAuthorId() != adapter.myUserId;
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
        Holder incomingHolder = new Holder(inflater.inflate(R.layout.item_message_incoming, parent, false),
                adapter.isOneToOneChat, callback);
        setClickListeners(incomingHolder);
        return incomingHolder;
    }

    @Override
    protected void onBindViewHolder(@NonNull Holder holder, @NonNull MessageModel item,
                                    @NonNull List<Object> payloads) {
        holder.message = item;

        MessageBindHelper.bindOutgoingMessage(
                holder, item, adapter, callback, inflater
        );

        if (adapter.isOneToOneChat) {
            return;
        }
        if (adapter.isNeedShowAvatar(item)) {
            holder.sdvPhoto.getHierarchy().setPlaceholderImage(
                    new TextDrawable(userPhotoSize, userPhotoSize,
                            previewBackgroundColor,
                            UserNameHelper.getInitials(item.getAuthorFullName()), true)
            );
            holder.sdvPhoto.setImageURI(item.getAuthorAvatarUrl());
            holder.sdvPhoto.setVisibility(View.VISIBLE);
            holder.tvName.setVisibility(View.VISIBLE);
            holder.tvName.setText(item.getAuthorFullName());
        } else {
            holder.sdvPhoto.setVisibility(View.INVISIBLE);
            holder.tvName.setVisibility(View.GONE);
        }
    }

    static class Holder extends OutgoingMessageDelegate.Holder {
        @BindView(R.id.tvName) TextView tvName;
        @BindView(R.id.sdvPhoto) SimpleDraweeView sdvPhoto;
        MessageModel message;

        Holder(View itemView, boolean isOneToOneChat, MessagesAdapter.Callback callback) {
            super(itemView);
            if (isOneToOneChat) {
                sdvPhoto.setVisibility(View.GONE);
                tvName.setVisibility(View.GONE);
            } else {
                sdvPhoto.setOnClickListener(v -> {
                    if (callback != null) {
                        callback.onProfileClick(v.getId(), message.getAuthorId());
                    }
                });
            }
        }
    }

}