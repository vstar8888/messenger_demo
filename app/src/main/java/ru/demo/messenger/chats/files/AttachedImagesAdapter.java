package ru.demo.messenger.chats.files;

import android.content.Context;
import android.net.Uri;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.lang.ref.WeakReference;

import ru.demo.messenger.R;
import biz.growapp.base.adapter.BaseAdapter;
import ru.demo.messenger.utils.BitmapUtils;
import ru.demo.messenger.utils.DimensionUtils;
import ru.demo.messenger.utils.UriUtils;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import static ru.demo.messenger.R.id.sdvPhoto;

public class AttachedImagesAdapter extends BaseAdapter<String, AttachedImagesAdapter.ImageItemHolder> {

    /**
     * Created by Igor Gusakov (auron057@gmail.com) on 07.04.2016.
     */
    public interface Callback {
        void onImageRemoved(String path);
    }

    private final Callback callback;
    private final int width;
    private final int height;

    protected final LayoutInflater inflater;
    protected final WeakReference<Context> context;

    public AttachedImagesAdapter(Context context, Callback callback) {
        this.inflater = LayoutInflater.from(context);
        this.context = new WeakReference<>(context);
        this.callback = callback;
        width = DimensionUtils.dp(48);
        height = DimensionUtils.dp(68);
    }

    @Override
    public ImageItemHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final ImageItemHolder holder = new ImageItemHolder(inflater.inflate(R.layout.item_attach_photo, viewGroup, false));
        holder.removePhoto.setOnClickListener(v -> {
            final int position = holder.getAdapterPosition();
            if (RecyclerView.NO_POSITION == position) {
                return;
            }
            callback.onImageRemoved(items.remove(position));
            notifyItemRemoved(position);
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(ImageItemHolder holder, int position) {
        final String path = getItem(position);

        if (holder.subscription != null) {
            holder.subscription.unsubscribe();
        }
        holder.subscription = Observable
                .fromCallable(() -> getCompressedPhotoFile(path, width, height))
                .subscribeOn(Schedulers.io())
                .map(Uri::fromFile)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(holder.photo::setImageURI);
    }

    private File getCompressedPhotoFile(String photoPath, int width, int height) {
        final String path = UriUtils.getPath(context.get(), Uri.fromFile(new File(photoPath)));
        final File compressedFile = BitmapUtils.getCompressFileFromGallery(context.get(), path, width, height);
        return BitmapUtils.rotateToCorrectOrientation(context.get(), compressedFile, path);
    }

    static class ImageItemHolder extends RecyclerView.ViewHolder {
        SimpleDraweeView photo;
        ImageView removePhoto;
        Subscription subscription;

        ImageItemHolder(View itemView) {
            super(itemView);
            photo = (SimpleDraweeView) itemView.findViewById(sdvPhoto);
            removePhoto = (ImageView) itemView.findViewById(R.id.remove);
        }
    }
}


