package ru.demo.messenger.chats.single.delegates;

import android.content.Context;
import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;

import java.io.File;
import java.lang.ref.WeakReference;

import biz.growapp.base.adapter.BaseAdapter;
import ru.demo.messenger.R;
import ru.demo.messenger.data.message.Attachment;
import ru.demo.messenger.data.message.Image;

class MessageImageAdapter extends BaseAdapter<Attachment, MessageImageAdapter.FileHolder> {

    public interface Callback {
        void onImageClick(Attachment attachment, int position);
    }

    private final Callback callback;
    protected final LayoutInflater inflater;
    protected final WeakReference<Context> context;

    private final int imageSize;


    MessageImageAdapter(Context context, Callback callback) {
        this.inflater = LayoutInflater.from(context);
        this.context = new WeakReference<>(context);
        this.callback = callback;
        this.imageSize = (int) context.getResources().getDimension(R.dimen.attachment_image_size);
    }

    @Override
    public MessageImageAdapter.FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final FileHolder holder =
                new FileHolder(inflater.inflate(R.layout.item_message_attach_image, parent, false));
        holder.itemView.setOnClickListener(v -> {
            if (callback != null) {
                final int position = holder.getAdapterPosition();
                callback.onImageClick(items.get(position), position);
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(FileHolder holder, int position) {
        final Attachment attachment = items.get(position);

        final Image image = attachment.getImage();
        if (image == null) {
            holder.sdvPhoto.setImageURI((String) null);
        } else {
            final String imageUri = image.getLargeImage();
            final boolean isFile = imageUri.startsWith("/");
            if (isFile) {
                final ImageRequest request =
                        ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(new File(imageUri)))
                                .setResizeOptions(new ResizeOptions(imageSize, imageSize))
                                .build();
                final DraweeController controller = Fresco.newDraweeControllerBuilder()
                        .setOldController(holder.sdvPhoto.getController())
                        .setImageRequest(request)
                        .build();
                holder.sdvPhoto.setController(controller);
            } else {
                holder.sdvPhoto.setImageURI(imageUri);
            }
        }
    }

    static class FileHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView sdvPhoto;

        FileHolder(View itemView) {
            super(itemView);
            sdvPhoto = (SimpleDraweeView) itemView;
        }
    }
}