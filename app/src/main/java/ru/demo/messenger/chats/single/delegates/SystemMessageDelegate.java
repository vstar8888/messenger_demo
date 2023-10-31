package ru.demo.messenger.chats.single.delegates;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import biz.growapp.base.adapter.AbstractAdapterDelegate;
import ru.demo.messenger.R;
import ru.demo.messenger.data.message.MessageModel;

public class SystemMessageDelegate extends AbstractAdapterDelegate<MessageModel, Object, SystemMessageDelegate.Holder> {

    private final LayoutInflater inflater;

    public SystemMessageDelegate(Context context) {
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    protected boolean isForViewType(@NonNull Object item, @NonNull List items, int position) {
        return item instanceof MessageModel
                && !((MessageModel) item).isBubbleMessage() && !((MessageModel) item).isStickerMessage();
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent) {
        return new Holder(inflater.inflate(R.layout.item_message_header, parent, false));
    }

    @Override
    protected void onBindViewHolder(@NonNull Holder holder, @NonNull MessageModel item,
                                    @NonNull List<Object> payloads) {
        holder.text.setText(item.getText());
    }

    static class Holder extends RecyclerView.ViewHolder {
        TextView text;

        Holder(View itemView) {
            super(itemView);
            text = (TextView) itemView;
        }
    }

}