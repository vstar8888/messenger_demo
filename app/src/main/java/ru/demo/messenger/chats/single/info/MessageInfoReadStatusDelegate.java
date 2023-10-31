package ru.demo.messenger.chats.single.info;

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import biz.growapp.base.adapter.AbstractAdapterDelegate;
import butterknife.BindView;
import butterknife.ButterKnife;
import ru.demo.messenger.R;
import ru.demo.messenger.utils.VectorUtils;

class MessageInfoReadStatusDelegate extends
        AbstractAdapterDelegate<MessageInfoReadStatusDelegate.Item, Object, MessageInfoReadStatusDelegate.Holder> {

    static class Item {
        private String readStatus;

        public Item(String readStatus) {
            this.readStatus = readStatus;
        }
    }

    private final LayoutInflater inflater;
    private final Drawable iconReadAll;
    private final Drawable iconReadSomeone;

    MessageInfoReadStatusDelegate(@NonNull Context context) {
        this.inflater = LayoutInflater.from(context);
        iconReadAll = VectorUtils.getVectorDrawable(context, R.drawable.ic_chat_read_all);
        iconReadSomeone = VectorUtils.getVectorDrawable(context, R.drawable.ic_chat_read_someone);
    }

    @Override
    protected boolean isForViewType(@NonNull Object item, @NonNull List<Object> items, int position) {
        return item instanceof MessageInfoReadStatusDelegate.Item;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new Holder(inflater.inflate(R.layout.item_message_info_title, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull Holder holder, @NonNull Item item,
                                    @NonNull List<Object> payloads) {
        final Context context = holder.tvStatus.getContext();
        if (InfoReadStatus.ISREAD.equals(item.readStatus)) {
            holder.ivStatus.setImageDrawable(iconReadAll);
            holder.tvStatus.setText(context.getString(R.string.message_info_is_read));
            holder.vDivider.setVisibility(View.GONE);
        } else {
            holder.ivStatus.setImageDrawable(iconReadSomeone);
            holder.tvStatus.setText(context.getString(R.string.message_info_unread));
            holder.vDivider.setVisibility(View.VISIBLE);
        }
    }

    static class Holder extends RecyclerView.ViewHolder {
        @BindView(R.id.ivStatus) ImageView ivStatus;
        @BindView(R.id.tvStatus) TextView tvStatus;
        @BindView(R.id.vDivider) View vDivider;

        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}