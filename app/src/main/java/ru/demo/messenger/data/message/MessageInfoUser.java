package ru.demo.messenger.data.message;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class MessageInfoUser implements Parcelable {

    @SerializedName("id")
    private long id;
    @SerializedName("status")
    private String status;
    @SerializedName("user_id")
    private long userId;
    @SerializedName("first_name")
    private String firstName;
    @SerializedName("last_name")
    private String lastName;
    @SerializedName("avatar_url")
    private String avatarUrl;

    public String getStatus() {
        return status;
    }

    public long getUserId() {
        return userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    protected MessageInfoUser(Parcel in) {
        id = in.readLong();
        status = in.readString();
        userId = in.readLong();
        firstName = in.readString();
        lastName = in.readString();
        avatarUrl = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(status);
        dest.writeLong(userId);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(avatarUrl);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MessageInfoUser> CREATOR = new Creator<MessageInfoUser>() {
        @Override
        public MessageInfoUser createFromParcel(Parcel in) {
            return new MessageInfoUser(in);
        }

        @Override
        public MessageInfoUser[] newArray(int size) {
            return new MessageInfoUser[size];
        }
    };
}
