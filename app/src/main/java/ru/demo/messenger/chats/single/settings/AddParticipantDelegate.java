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

class AddParticipantDelegate extends
        AbstractAdapterDelegate<AddParticipantDelegate.Item,
                Object,
                AddParticipantDelegate.Holder> {

    static class Item {
    }

    public interface Callback {
        void onAddUserClick(int position);
    }

    private final LayoutInflater inflater;
    private final Callback callback;

    AddParticipantDelegate(@NonNull Context context, @NonNull Callback callback) {
        this.inflater = LayoutInflater.from(context);
        this.callback = callback;
    }

    @Override
    protected boolean isForViewType(@NonNull Object item, @NonNull List<Object> items, int position) {
        return item instanceof Item;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent) {
        final Holder holder = new Holder(inflater.inflate(R.layout.item_add_participant, parent, false));
        holder.itemView.setOnClickListener(view -> callback.onAddUserClick(holder.getAdapterPosition()));
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