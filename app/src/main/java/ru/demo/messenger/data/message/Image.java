package ru.demo.messenger.data.message;

import android.os.Parcel;
import android.os.Parcelable;

public class Image implements Parcelable {
    private String url_small;
    private String url_large;


    public Image(String urlSmall, String urlLarge) {
        this.url_small = urlSmall;
        this.url_large = urlLarge;
    }

    protected Image(Parcel in) {
        url_small = in.readString();
        url_large = in.readString();
    }

    public String getSmallImage() {
        return url_small;
    }

    public String getLargeImage() {
        return url_large;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(url_small);
        dest.writeString(url_large);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Image> CREATOR = new Creator<Image>() {
        @Override
        public Image createFromParcel(Parcel in) {
            return new Image(in);
        }

        @Override
        public Image[] newArray(int size) {
            return new Image[size];
        }
    };
}
