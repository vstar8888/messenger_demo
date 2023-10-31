package ru.demo.messenger.data.message;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AttachedFiles implements Parcelable {
    private long count;
    private List<Attachment> data;
    private transient List<Attachment> images;
    private transient List<Attachment> files;

    public AttachedFiles(@NonNull List<Attachment> attachments) {
        this.data = attachments;
    }

    protected AttachedFiles(Parcel in) {
        count = in.readLong();
        data = in.createTypedArrayList(Attachment.CREATOR);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(count);
        dest.writeTypedList(data);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AttachedFiles> CREATOR = new Creator<AttachedFiles>() {
        @Override
        public AttachedFiles createFromParcel(Parcel in) {
            return new AttachedFiles(in);
        }

        @Override
        public AttachedFiles[] newArray(int size) {
            return new AttachedFiles[size];
        }
    };

    public boolean isEmpty() {
        return count == 0;
    }

    public long getCount() {
        return count;
    }

    public List<Attachment> getAttachments() {
        return data;
    }

    public List<Attachment> getImages() {
        if (images == null) {
            sortAttachments();
        }
        return images;
    }

    public List<Attachment> getFiles() {
        if (files == null) {
            sortAttachments();
        }
        return files;
    }

    private synchronized void sortAttachments() {
        if (files != null) {
            return;
        }
        for (int i = 0; i < data.size(); i++) {
            final Attachment attachment = data.get(i);
            if (attachment.isImage()) {
                addImage(attachment);
                continue;
            }
            addFile(attachment);
        }
        files = initEmptyIfNull(files);
        images = initEmptyIfNull(images);
    }

    private void addImage(Attachment attachment) {
        if (images == null) {
            images = new ArrayList<>();
        }
        images.add(attachment);
    }

    private void addFile(Attachment attachment) {
        if (files == null) {
            files = new ArrayList<>();
        }
        files.add(attachment);
    }

    private <T> List<T> initEmptyIfNull(List<T> list) {
        return list == null ? Collections.<T>emptyList() : list;
    }
}
