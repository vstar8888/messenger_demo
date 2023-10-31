package ru.demo.messenger.data.user;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;

public class Contacts implements Parcelable {
    private String email;
    private String google_talk;
    private String icq;
    private String phone_mobile;
    private String phone_work;
    private String skype;
    private String twitter;

    protected Contacts(Parcel in) {
        email = in.readString();
        google_talk = in.readString();
        icq = in.readString();
        phone_mobile = in.readString();
        phone_work = in.readString();
        skype = in.readString();
        twitter = in.readString();
    }

    @Nullable
    public String getEmail() {
        return email;
    }

    @Nullable
    public String getGoogleTalk() {
        return google_talk;
    }

    @Nullable
    public String getIcq() {
        return icq;
    }

    @Nullable
    public String getPhoneMobile() {
        return phone_mobile;
    }

    @Nullable
    public String getPhoneWork() {
        return phone_work;
    }

    @Nullable
    public String getSkype() {
        return skype;
    }

    @Nullable
    public String getTwitter() {
        return twitter;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(email);
        dest.writeString(google_talk);
        dest.writeString(icq);
        dest.writeString(phone_mobile);
        dest.writeString(phone_work);
        dest.writeString(skype);
        dest.writeString(twitter);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Contacts> CREATOR = new Creator<Contacts>() {
        @Override
        public Contacts createFromParcel(Parcel in) {
            return new Contacts(in);
        }

        @Override
        public Contacts[] newArray(int size) {
            return new Contacts[size];
        }
    };

    public void setPhoneMobile(String phoneMobile) {
        this.phone_mobile = phoneMobile;
    }

    public void setPhoneWork(String phoneWork) {
        this.phone_work = phoneWork;
    }

    public void setSkype(String skype) {
        this.skype = skype;
    }

    public void setGoogleTalk(String googleTalk) {
        this.google_talk = googleTalk;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public void setIcq(String icq) {
        this.icq = icq;
    }
}
