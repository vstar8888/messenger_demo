package ru.demo.messenger.chats.single.settings;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
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
import ru.demo.messenger.data.user.UserModel;
import ru.demo.messenger.helpers.ColorGenerator;
import ru.demo.messenger.helpers.UserNameHelper;
import ru.demo.messenger.people.OnlineUsersHolder;
import ru.demo.messenger.people.TextDrawable;
import ru.demo.messenger.utils.DimensionUtils;

class ChatParticipantDelegate extends AbstractAdapterDelegate<UserModel, Object, ChatParticipantDelegate.Holder> {

    public interface Callback {
        void onUserClick(int position);

        List<Long> getAdminsList();
    }

    private final LayoutInflater inflater;
    private final Callback callback;
    @NonNull private final ColorGenerator colorGenerator;
    private final int photoSize;
    private final String onlineText;

    ChatParticipantDelegate(@NonNull Context context, @NonNull Callback callback,
                            @NonNull ColorGenerator colorGenerator) {
        this.inflater = LayoutInflater.from(context);
        this.callback = callback;
        this.colorGenerator = colorGenerator;
        photoSize = DimensionUtils.dp(54);
        onlineText = context.getString(R.string.online);
    }

    @Override
    protected boolean isForViewType(@NonNull Object item, @NonNull List<Object> items, int position) {
        return item instanceof UserModel;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent) {
        final Holder holder = new Holder(inflater.inflate(R.layout.item_chat_participant, parent, false));
        holder.itemView.setOnClickListener(view -> callback.onUserClick(holder.getAdapterPosition()));
        return holder;
    }

    @Override
    protected void onBindViewHolder(@NonNull Holder holder, @NonNull UserModel item,
                                    @NonNull List<Object> payloads) {
        holder.name.setText(item.getFullName());
        if (OnlineUsersHolder.isOnline(item.getId())) {
            holder.status.setText(onlineText);
        } else {
            holder.status.setText(null);
        }
        holder.bindAvatar(item, colorGenerator.from(item), photoSize);

        if (callback.getAdminsList().contains(item.getId())) {
            holder.isAdmin.setVisibility(View.VISIBLE);
        } else {
            holder.isAdmin.setVisibility(View.GONE);
        }
    }

    static class Holder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvName) TextView name;
        @BindView(R.id.tvStatus) TextView status;
        @BindView(R.id.tvIsAdmin) TextView isAdmin;
        @BindView(R.id.sdvAvatar) SimpleDraweeView avatar;

        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        private void bindAvatar(@NonNull UserModel item, int color, int size) {
            avatar.getHierarchy().setPlaceholderImage(new TextDrawable(size, size, color, UserNameHelper.getInitials(item.getFullName()), true));
            avatar.setImageURI(item.getThumbnailX2());
        }
    }
}
