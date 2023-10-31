package ru.demo.messenger.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class CompanyModel implements Parcelable{
    @SerializedName("Title")
    private String title;
    @SerializedName("HostWithSubHost")
    private String hostWithSubHost;

    protected CompanyModel(Parcel in) {
        title = in.readString();
        hostWithSubHost = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeString(hostWithSubHost);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<CompanyModel> CREATOR = new Creator<CompanyModel>() {
        @Override
        public CompanyModel createFromParcel(Parcel in) {
            return new CompanyModel(in);
        }

        @Override
        public CompanyModel[] newArray(int size) {
            return new CompanyModel[size];
        }
    };

    public String getTitle() {
        return title;
    }

    public String getHostWithSubHost() {
        return hostWithSubHost;
    }
}
