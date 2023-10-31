package ru.demo.messenger.chats.single.attach;

import android.content.Context;
import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.facebook.common.util.UriUtil;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import biz.growapp.base.adapter.BaseAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import ru.demo.messenger.R;
import ru.demo.messenger.utils.DisplayUtils;

class GalleryPhotoAdapter extends BaseAdapter<String, GalleryPhotoAdapter.ItemViewHolder> {
    public static final String TAG = GalleryPhotoAdapter.class.getSimpleName();

    interface OnImageClickListener {
        void onImageClick(String path, boolean isAdd, int count);
    }

    private final int size;
    private ArrayList<String> selectedPhotos;
    private final OnImageClickListener listener;

    protected final LayoutInflater inflater;
    protected final WeakReference<Context> context;

    GalleryPhotoAdapter(Context context, int countColumns, OnImageClickListener listener) {
        this.inflater = LayoutInflater.from(context);
        this.context = new WeakReference<>(context);
        size = DisplayUtils.getScreenWidth(context) / countColumns;
        selectedPhotos = new ArrayList<>();
        this.listener = listener;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final ItemViewHolder holder = new ItemViewHolder(inflater.inflate(R.layout.item_attach_gallery_photo, parent, false), size);
        holder.itemView.setOnClickListener(v -> {
            final boolean isChecked = holder.ivCheckIcon.isChecked();
            final String item = items.get(holder.getAdapterPosition());
            if(isChecked) {
                selectedPhotos.remove(item);
            } else {
                selectedPhotos.add(item);
            }
            holder.ivCheckIcon.setChecked(!isChecked);
            listener.onImageClick(item, !isChecked, selectedPhotos.size());
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        String item = items.get(position);

        Uri uri = new Uri.Builder()
                .scheme(UriUtil.LOCAL_FILE_SCHEME) // "res"
                .path(item)
                .build();

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(new ResizeOptions(size, size))
                .build();
        PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
                .setOldController(holder.sdvPhoto.getController())
                .setImageRequest(request)
                .build();

        holder.sdvPhoto.setController(controller);
        holder.ivCheckIcon.setChecked(selectedPhotos.contains(item));
    }

    ArrayList<String> getSelectedPhotos() {
        return selectedPhotos;
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.sdvImage) SimpleDraweeView sdvPhoto;
        @BindView(R.id.ivCheckIcon) CheckBox ivCheckIcon;

        ItemViewHolder(View itemView, int size) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            invalidateViewSize(itemView, size);
        }

        private void invalidateViewSize(View view, int size) {
            final ViewGroup.LayoutParams params = view.getLayoutParams();
            params.height = size;
            params.width = size;
            view.setLayoutParams(params);
            view.requestLayout();
        }
    }
}
