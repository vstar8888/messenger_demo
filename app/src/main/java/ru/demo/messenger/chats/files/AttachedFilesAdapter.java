package ru.demo.messenger.chats.files;

import android.content.Context;
import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import biz.growapp.base.adapter.BaseAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import ru.demo.messenger.R;

public class AttachedFilesAdapter extends BaseAdapter<Uri, AttachedFilesAdapter.ImageItemHolder> {

    public interface Callback {
        void onFileRemoved(String path);
    }

    private final Callback callback;

    protected final LayoutInflater inflater;
    protected final WeakReference<Context> context;

    public AttachedFilesAdapter(Context context, Callback callback) {
        this.inflater = LayoutInflater.from(context);
        this.context = new WeakReference<>(context);
        this.callback = callback;
    }

    @Override
    public ImageItemHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final ImageItemHolder holder =
                new ImageItemHolder(inflater.inflate(R.layout.item_attach_file, viewGroup, false));
        holder.ivRemove.setOnClickListener(v -> {
            final int position = holder.getAdapterPosition();
            callback.onFileRemoved(items.remove(position).toString());
            notifyItemRemoved(position);
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ImageItemHolder holder, int position) {
        final Uri path = getItem(position);
        holder.tvFileName.setText(path.getLastPathSegment());
    }

    static class ImageItemHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tvFileName) TextView tvFileName;
        @BindView(R.id.ivRemove) ImageView ivRemove;

        ImageItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}


