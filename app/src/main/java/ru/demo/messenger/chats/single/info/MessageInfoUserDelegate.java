package ru.demo.messenger.chats.single.info;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.util.List;

import biz.growapp.base.adapter.AbstractAdapterDelegate;
import butterknife.BindView;
import butterknife.ButterKnife;
import ru.demo.messenger.R;
import ru.demo.messenger.data.message.MessageInfoUser;
import ru.demo.messenger.helpers.UserNameHelper;
import ru.demo.messenger.people.TextDrawable;

class MessageInfoUserDelegate extends AbstractAdapterDelegate<MessageInfoUser, Object, MessageInfoUserDelegate.Holder> {

    public interface Callback {
        void onProfileClick(int viewId, long userId);
    }

    private final LayoutInflater inflater;
    private final Callback callback;
    private final int userPhotoSize;
    private final int previewBackgroundColor;

    MessageInfoUserDelegate(@NonNull Context context, @NonNull Callback callback) {
        this.inflater = LayoutInflater.from(context);
        this.callback = callback;
        this.userPhotoSize = context.getResources().getDimensionPixelSize(R.dimen.user_photo_size_min);
        this.previewBackgroundColor = ContextCompat.getColor(context, R.color.dark_blue);
    }

    @Override
    protected boolean isForViewType(@NonNull Object item, @NonNull List<Object> items, int position) {
        return item instanceof MessageInfoUser;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new Holder(inflater.inflate(R.layout.item_message_info_user, parent, false), callback);
    }

    @Override
    protected void onBindViewHolder(@NonNull Holder holder, @NonNull MessageInfoUser item, @NonNull List<Object> payloads) {
        holder.message = item;
        bindPhoto(holder, item);
        holder.tvName.setText(String.format("%s %s", item.getFirstName(), item.getLastName()));
    }

    private void bindPhoto(@NonNull Holder holder, @NonNull MessageInfoUser message) {
        String firstName = TextUtils.isEmpty(message.getFirstName()) ? "" : message.getFirstName();
        String lastName = TextUtils.isEmpty(message.getLastName()) ? "" : message.getLastName();
        holder.sdvPhoto.getHierarchy().setPlaceholderImage(
                new TextDrawable(userPhotoSize, userPhotoSize, previewBackgroundColor,
                        UserNameHelper.getInitials(firstName + lastName), true)
        );
        holder.sdvPhoto.setImageURI(message.getAvatarUrl());
    }

    static class Holder extends RecyclerView.ViewHolder {
        @BindView(R.id.sdvPhoto) SimpleDraweeView sdvPhoto;
        @BindView(R.id.tvName) TextView tvName;
        MessageInfoUser message;

        Holder(View itemView, Callback callback) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onProfileClick(v.getId(), message.getUserId());
                }
            });
        }
    }
}