package ru.demo.messenger.chats.single.delegates;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.vectordrawable.graphics.drawable.ArgbEvaluator;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.TypefaceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalTime;
import org.threeten.bp.ZoneId;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.List;

import ru.demo.messenger.R;
import ru.demo.messenger.data.message.AttachedFiles;
import ru.demo.messenger.data.message.Attachment;
import ru.demo.messenger.data.message.MessageModel;
import ru.demo.messenger.utils.DimensionUtils;
import ru.demo.messenger.utils.VectorUtils;

public class MessageBindHelper {

    public interface Callback extends MessageImageAdapter.Callback {
        void onProfileClick(int viewId, long userId);

        void onAttachedFileClick(Attachment attachment);
    }

    // TODO: ET 09.02.2018 refator?
    private static final ZoneId systemZone = ZoneId.systemDefault();
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

    static String getFormattedTime(long time) {
        return timeFormatter.format(
                LocalTime.from(Instant.ofEpochSecond(time).atZone(systemZone))
        );
    }

    static void bindOutgoingMessage(OutgoingMessageDelegate.Holder holder,
                                    MessageModel message,
                                    MessagesAdapter adapter,
                                    Callback callback,
                                    LayoutInflater inflater
    ) {
        bindForward(holder, message, callback);
        bindReply(holder, message);

        if (holder.highlightAnimator != null && holder.getAdapterPosition() != adapter.posForHighlight) {
            // because of reusing items
            holder.highlightAnimator.cancel();
            holder.highlightAnimator = null;
        }

        if (MessageModel.MessageType.FORWARD.equalsIgnoreCase(message.getMessageType())) {
            holder.tvMessage.setMovementMethod(LinkMovementMethod.getInstance());
            holder.tvMessage.setText(message.getOriginalMessage().getMessage());

            final AttachedFiles attachedFiles = message.getOriginalMessage().getFiles();
            bindImages(callback, holder, attachedFiles.getImages());
            bindFiles(inflater, callback, holder, attachedFiles.getFiles());

            holder.ivForwardFileArrow.setVisibility(View.INVISIBLE);
        } else {
            holder.tvMessage.setMovementMethod(LinkMovementMethod.getInstance());
            holder.tvMessage.setText(message.getMessage());

            final AttachedFiles attachedFiles = message.getFiles();
            bindImages(callback, holder, attachedFiles.getImages());
            bindFiles(inflater, callback, holder, attachedFiles.getFiles());

            if (attachedFiles.isEmpty()) {
                holder.ivForwardFileArrow.setVisibility(View.INVISIBLE);
            } else {
                holder.ivForwardFileArrow.setVisibility(View.VISIBLE);
            }
        }

        holder.tvSendTime.setText(adapter.timeFormatter.format(
                LocalTime.from(Instant.ofEpochSecond(message.getCreatedAt()).atZone(adapter.systemZone)))
        );

        if (adapter.isActionMode) {
            setSelectedBackground(holder.vgMessage, adapter, message);
        } else {
            holder.vgMessage.setBackground(null);
            // подсветить reply
            if (adapter.needHighlight && holder.getAdapterPosition() == adapter.posForHighlight) {
                adapter.needHighlight = false;
                highlightRepliedMessage(holder);
            }
        }
    }

    public static void setSelectedBackground(View itemView,
                                             MessagesAdapter adapter,
                                             MessageModel message) {
        if (adapter.isSelected(message)) {
            itemView.setBackgroundColor(
                    ContextCompat.getColor(itemView.getContext(), R.color.blue_accent_200_alpha_130)
            );
        } else {
            itemView.setBackground(null);
        }
    }

    private static void highlightRepliedMessage(OutgoingMessageDelegate.Holder holder) {
        Context context = holder.vgBubble.getContext();
        holder.vgMessage.setBackgroundColor(getColor(context, R.color.blue_accent_200_alpha_130));
        holder.highlightAnimator = ValueAnimator.ofObject(new ArgbEvaluator(),
                getColor(context, R.color.blue_accent_200_alpha_130),
                getColor(context, android.R.color.transparent)
        );
        holder.highlightAnimator.setDuration(2000);
        holder.highlightAnimator.addUpdateListener(animator ->
                holder.vgMessage.setBackgroundColor((int) animator.getAnimatedValue())
        );
        holder.highlightAnimator.start();
    }

    @ColorInt
    private static int getColor(Context context, @ColorRes int id) {
        return ContextCompat.getColor(context, id);
    }

    public static void bindReadStatus(OutgoingMessageDelegate.Holder holder, MessageModel message) {
        bindReadStatus(holder.tvSendTime, message);
    }

    public static void bindReadStatus(TextView tvSendTime, MessageModel message) {
        Context context = tvSendTime.getContext();
        if (MessageModel.ReadStatus.WAIT.equals(message.getStatus())) {
            final Drawable icon = VectorUtils.getVectorDrawable(context, R.drawable.ic_sending);
            tvSendTime.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
        } else if (MessageModel.ReadStatus.DELIVERED.equals(message.getStatus())) {
            final Drawable icon = VectorUtils.getVectorDrawable(context, R.drawable.ic_chat_delivered);
            tvSendTime.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
        } else if (MessageModel.ReadStatus.READ.equals(message.getStatus())) {
            final Drawable icon = VectorUtils.getVectorDrawable(context, R.drawable.ic_chat_read_someone);
            tvSendTime.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
        } else if (MessageModel.ReadStatus.READ_ALL.equals(message.getStatus())) {
            final Drawable icon = VectorUtils.getVectorDrawable(context, R.drawable.ic_chat_read_all);
            tvSendTime.setCompoundDrawablesWithIntrinsicBounds(null, null, icon, null);
        }
    }

    public static void bindImages(Callback callback,
                                  OutgoingMessageDelegate.Holder holder,
                                  List<Attachment> images
    ) {
        Context context = holder.rvAttachments.getContext();
        if (images.isEmpty()) {
            holder.rvAttachments.setVisibility(View.GONE);
        } else {
            if (holder.adapter == null) {
                holder.adapter = new MessageImageAdapter(context, callback);
                holder.rvAttachments.setAdapter(holder.adapter);
                holder.rvAttachments.setLayoutManager(
                        new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
            }
            holder.adapter.replaceAll(images);
            holder.rvAttachments.setVisibility(View.VISIBLE);
        }
    }

    public static void bindFiles(LayoutInflater inflater,
                                 Callback callback,
                                 OutgoingMessageDelegate.Holder holder,
                                 List<Attachment> files
    ) {
        if (files.isEmpty()) {
            holder.vgFilesContainer.setVisibility(View.GONE);
        } else {
            holder.vgFilesContainer.removeAllViews();
            for (int i = 0; i < files.size(); i++) {
                final Attachment attachment = files.get(i);
                final View view = inflater.inflate(R.layout.item_message_attach_file,
                        holder.vgFilesContainer,
                        false);
                final TextView tvFilename = (TextView) view.findViewById(R.id.filename);
                tvFilename.setText(attachment.getName());

                view.setOnClickListener(v -> {
                    if (callback != null) {
                        callback.onAttachedFileClick(attachment);
                    }
                });
                holder.vgFilesContainer.addView(view);
            }
            holder.vgFilesContainer.setVisibility(View.VISIBLE);
        }
    }


    public static void bindForward(OutgoingMessageDelegate.Holder holder,
                                   MessageModel message,
                                   Callback callback
    ) {
        final Context context = holder.itemView.getContext();
        if (MessageModel.MessageType.FORWARD.equalsIgnoreCase(message.getMessageType())) {
            holder.tvForwardedMessageTitle.setVisibility(View.VISIBLE);
            holder.tvForwardedMessageFrom.setVisibility(View.VISIBLE);
            final MessageModel forwardedMessage = message.getOriginalMessage();
            final String authorFullName = forwardedMessage.getAuthorFullName();
            String title = context.getString(R.string.chat_item_forward_from, authorFullName);
            int indexOfAuthor = title.indexOf(authorFullName);
            SpannableStringBuilder authorBuilder = new SpannableStringBuilder(title);
            authorBuilder.setSpan(
                    new TypefaceSpan("sans-serif-medium"),
                    indexOfAuthor,
                    title.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            authorBuilder.setSpan(
                    new ClickableSpan() {
                        @Override
                        public void onClick(View view) {
                            callback.onProfileClick(view.getId(), forwardedMessage.getAuthorId());
                        }

                        @Override
                        public void updateDrawState(TextPaint ds) {
                        }
                    },
                    indexOfAuthor,
                    title.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            holder.tvForwardedMessageFrom.setText(authorBuilder);
            holder.tvForwardedMessageFrom.setMovementMethod(new LinkMovementMethod());
            holder.tvMessage.setText(forwardedMessage.getMessage());
            holder.tvMessage.setVisibility(View.VISIBLE);
        } else {
            holder.tvForwardedMessageTitle.setVisibility(View.GONE);
            holder.tvForwardedMessageFrom.setVisibility(View.GONE);
        }
    }

    private static void changeReplySize(OutgoingMessageDelegate.Holder holder) {
        if (holder.bubbleWidth <= 0 || holder.replyWidth <= 0) {
            return;
        }
        holder.vgReply.setLayoutParams(
                new LinearLayout.LayoutParams(
                        holder.bubbleWidth - DimensionUtils.dp(36), // bubbleWidth - (padding left+right)
                        holder.vgReply.getLayoutParams().height
                )
        );
        holder.vgReply.postDelayed(() ->
                holder.vgReply.requestLayout(), 10
        );
    }

    public static void bindReply(OutgoingMessageDelegate.Holder holder,
                                 MessageModel message
    ) {
        if (MessageModel.MessageType.QUOTE.equals(message.getMessageType())) {
            holder.vgReply.setVisibility(View.VISIBLE);
        } else {
            holder.vgReply.setVisibility(View.GONE);
            return;
        }
        holder.vgBubble.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                holder.vgBubble.removeOnLayoutChangeListener(this);
                holder.bubbleWidth = right - left;
                changeReplySize(holder);
            }
        });
        holder.vgBubble.requestLayout();
        holder.vgReply.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                holder.vgReply.removeOnLayoutChangeListener(this);
                holder.replyWidth = right - left;
                changeReplySize(holder);
            }
        });
        holder.vgReply.setLayoutParams(
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        );

        final MessageModel originalMessage;
        if (MessageModel.MessageType.FORWARD.equalsIgnoreCase(message.getOriginalMessage().getMessageType())) {
            originalMessage = message.getOriginalMessage().getOriginalMessage();
        } else {
            originalMessage = message.getOriginalMessage();
        }
        holder.vgReply.setVisibility(View.VISIBLE);
        holder.tvReplyAuthorName.setText(originalMessage.getAuthorFullName());

        List<Attachment> images = originalMessage.getFiles().getImages();

        if (originalMessage.isStickerMessage()) {
            final Context context = holder.tvReplyContent.getContext();
            holder.tvReplyContent.setText(context.getString(R.string.chat_reply_sticker));
        } else if (TextUtils.isEmpty(originalMessage.getMessage())) {
            Context context = holder.tvReplyContent.getContext();
            if (images.isEmpty()) {
                holder.tvReplyContent.setText(context.getString(R.string.chat_reply_document));
            } else {
                holder.tvReplyContent.setText(context.getString(R.string.chat_reply_image));
            }
        } else {
            holder.tvReplyContent.setText(originalMessage.getMessage());
        }

        if (originalMessage.isStickerMessage() && originalMessage.getSticker() != null) {
            holder.sdvReplyImagePreview.setVisibility(View.VISIBLE);
            holder.sdvReplyImagePreview.setImageURI(originalMessage.getSticker().getImageUrl());
        } else if (images.isEmpty()) {
            holder.sdvReplyImagePreview.setVisibility(View.GONE);
        } else {
            holder.sdvReplyImagePreview.setVisibility(View.VISIBLE);
            holder.sdvReplyImagePreview.setImageURI(images.get(0).getUrl());
        }
        holder.ivReplyImageCloseCircle.setVisibility(View.GONE);
        holder.vCloseReply.setVisibility(View.GONE);
        holder.ivReplyClose.setVisibility(View.GONE);
    }

}
