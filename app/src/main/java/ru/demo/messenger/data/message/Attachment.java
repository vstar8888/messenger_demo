package ru.demo.messenger.data.message;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Attachment implements Parcelable {
    private String id;
    private String name;
    private String type;
    private String url;
    private long size;
    private @Nullable Image image;
    private @Nullable Image video_preview;


    public Attachment(@NonNull String filePath) {
        this.type = getExtension(filePath);
        this.url = filePath;
        this.name = getFullFileName(filePath);
        if (isImageExtension(type)) {
            this.image = new Image(url, url);
        }
    }

    private String getExtension(@NonNull String filePath) {
        String extension = "";

        final int i = filePath.lastIndexOf('.');
        if (i > 0) {
            extension = filePath.substring(i + 1);
        }
        return extension;
    }

    private String getFullFileName(@NonNull String filePath) {
        String fullFileName = "";

        final int i = filePath.lastIndexOf('/');
        if (i > 0) {
            fullFileName = filePath.substring(i + 1);
        }
        return fullFileName;
    }

    private boolean isImageExtension(String type) {
        return "jpg".equalsIgnoreCase(type) ||
                "png".equalsIgnoreCase(type) ||
                "jpeg".equalsIgnoreCase(type) ||
                "bmp".equalsIgnoreCase(type) ||
                "gif".equalsIgnoreCase(type);
    }

    protected Attachment(Parcel in) {
        id = in.readString();
        name = in.readString();
        type = in.readString();
        url = in.readString();
        size = in.readLong();
        image = in.readParcelable(Image.class.getClassLoader());
        video_preview = in.readParcelable(Image.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(type);
        dest.writeString(url);
        dest.writeLong(size);
        dest.writeParcelable(image, flags);
        dest.writeParcelable(video_preview, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Attachment> CREATOR = new Creator<Attachment>() {
        @Override
        public Attachment createFromParcel(Parcel in) {
            return new Attachment(in);
        }

        @Override
        public Attachment[] newArray(int size) {
            return new Attachment[size];
        }
    };

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public long getSize() {
        return size;
    }

    public boolean isImage() {
        return image != null;
    }

    public boolean isVideo() {
        return video_preview != null;
    }

    @Nullable
    public Image getImage() {
        return image;
    }

    @Nullable
    public Image getVideoPreview() {
        return video_preview;
    }
}
