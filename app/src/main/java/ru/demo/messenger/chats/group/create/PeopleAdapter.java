package ru.demo.messenger.chats.group.create;


import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.lang.ref.WeakReference;

import biz.growapp.base.adapter.BaseAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import ru.demo.messenger.R;
import ru.demo.messenger.data.user.UserModel;
import ru.demo.messenger.helpers.UserNameHelper;
import ru.demo.messenger.helpers.ColorGenerator;
import ru.demo.messenger.people.TextDrawable;

class PeopleAdapter extends BaseAdapter<UserModel, PeopleAdapter.UserViewHolder> {

    interface ItemClickListener {
        void onRemoveClick(int viewId, int position);
    }

    private final ItemClickListener itemClickListener;

    protected final LayoutInflater inflater;
    protected final WeakReference<Context> context;

    private final int photoSize;
    private final ColorGenerator colorGenerator;

    PeopleAdapter(Context context,
                  ItemClickListener itemClickListener,
                  ColorGenerator colorGenerator) {
        this.inflater = LayoutInflater.from(context);
        this.context = new WeakReference<>(context);
        this.photoSize = context.getResources()
                .getDimensionPixelSize(R.dimen.selected_people_avatar_size);
        this.itemClickListener = itemClickListener;
        this.colorGenerator = colorGenerator;
    }

    @Override
    public PeopleAdapter.UserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new UserViewHolder(inflater.inflate(R.layout.item_group_user, parent, false));
    }

    @Override
    public void onBindViewHolder(PeopleAdapter.UserViewHolder holder, int position) {
        final UserModel item = getItem(position);

        holder.sdvAvatar.getHierarchy().setPlaceholderImage(
                new TextDrawable(photoSize, photoSize,
                        colorGenerator.from(item),
                        UserNameHelper.getInitials(item.getFullName()),
                        true
                )
        );
        holder.sdvAvatar.setImageURI(item.getThumbnailX2());
        holder.ivRemoveUser.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onRemoveClick(v.getId(), holder.getAdapterPosition());
            }
        });
        holder.tvUserName.setText(item.getFirstName());
    }

    public boolean isExist(UserModel userModel) {
        for (int i = 0; i < items.size(); i++) {
            if (userModel.getId() == items.get(i).getId()) {
                return true;
            }
        }
        return false;
    }

    public void addFirstWithAnim(UserModel user) {
        if (isEmpty()) {
            add(user, 0);
        } else {
            items.add(0, user);
            notifyItemInserted(0);
        }
    }

    boolean removeWithAnim(int position) {
        if (RecyclerView.NO_ID == position) {
            return false;
        }
        if (isEmpty()) {
            remove(position);
        } else {
            items.remove(position);
            notifyItemRemoved(position);
        }
        return true;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvUserName) TextView tvUserName;
        @BindView(R.id.sdvAvatar) SimpleDraweeView sdvAvatar;
        @BindView(R.id.ivRemoveUser) ImageView ivRemoveUser;

        UserViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

}
