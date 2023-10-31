package ru.demo.messenger.chats.single.settings;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import biz.growapp.base.adapter.AbstractAdapterDelegate;
import butterknife.ButterKnife;
import ru.demo.messenger.R;

class LeaveGroupDelegate extends AbstractAdapterDelegate<LeaveGroupDelegate.Item, Object, LeaveGroupDelegate.Holder> {

    static class Item {
    }

    public interface Callback {
        void onLeaveGroupClick();
    }

    private final LayoutInflater inflater;
    private final LeaveGroupDelegate.Callback callback;

    LeaveGroupDelegate(@NonNull Context context, @NonNull LeaveGroupDelegate.Callback callback) {
        this.inflater = LayoutInflater.from(context);
        this.callback = callback;
    }

    @Override
    protected boolean isForViewType(@NonNull Object item, @NonNull List<Object> items, int position) {
        return item instanceof LeaveGroupDelegate.Item;
    }

    @NonNull
    @Override
    public LeaveGroupDelegate.Holder onCreateViewHolder(@NonNull ViewGroup parent) {
        final LeaveGroupDelegate.Holder holder = new LeaveGroupDelegate.Holder(inflater.inflate(R.layout.item_leave_group, parent, false));
        holder.itemView.setOnClickListener(view -> callback.onLeaveGroupClick());
        return holder;
    }

    @Override
    protected void onBindViewHolder(@NonNull Holder holder, @NonNull Item item,
                                    @NonNull List<Object> payloads) {

    }

    static class Holder extends RecyclerView.ViewHolder {
        Holder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}