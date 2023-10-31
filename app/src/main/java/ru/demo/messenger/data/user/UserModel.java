package ru.demo.messenger.data.user;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import ru.demo.messenger.data.db.DBOpenHelper;
import ru.demo.messenger.data.db.tables.UsersTable;

public class UserModel implements Parcelable {

    private long id;
    private String login;
    private String external_id;
    private String created_at;
    private boolean is_active;
    private String web_url;
    private String url;
    private String thumbnail_url;
    private String thumbnail_x2_url;
    private String photo_large_url;
    private String full_name;
    private String full_name_short;
    private String birth_date;

    private String main_alias;
    private String about_me;
    private String education;
    private String hobby;
    private String work_experience;
    private String subdivision_title;
    private String room_location;
    private String position;
    private long exp_points;
    private long exp_level;
    private Contacts contacts;
    private String region;

    private boolean is_followed_by_current_user;

    @SerializedName("alow_to_edit_fields")
    private String[] allowedToEditFields;

    @SerializedName("installed_chat_app")
    private boolean isInstalledChatApp;

    public UserModel(Cursor cursor) {
        id = cursor.getLong(cursor.getColumnIndexOrThrow(UsersTable.COLUMN_ID));
    }

    protected UserModel(Parcel in) {
        id = in.readLong();
        login = in.readString();
        external_id = in.readString();
        created_at = in.readString();
        is_active = in.readByte() != 0;
        web_url = in.readString();
        url = in.readString();
        thumbnail_url = in.readString();
        thumbnail_x2_url = in.readString();
        photo_large_url = in.readString();
        full_name = in.readString();
        full_name_short = in.readString();
        birth_date = in.readString();
        main_alias = in.readString();
        about_me = in.readString();
        education = in.readString();
        hobby = in.readString();
        work_experience = in.readString();
        subdivision_title = in.readString();
        room_location = in.readString();
        position = in.readString();
        exp_points = in.readLong();
        exp_level = in.readLong();
        contacts = in.readParcelable(Contacts.class.getClassLoader());
        is_followed_by_current_user = in.readByte() != 0;
        region = in.readString();
        allowedToEditFields = in.createStringArray();
        isInstalledChatApp = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(login);
        dest.writeString(external_id);
        dest.writeString(created_at);
        dest.writeByte((byte) (is_active ? 1 : 0));
        dest.writeString(web_url);
        dest.writeString(url);
        dest.writeString(thumbnail_url);
        dest.writeString(thumbnail_x2_url);
        dest.writeString(photo_large_url);
        dest.writeString(full_name);
        dest.writeString(full_name_short);
        dest.writeString(birth_date);
        dest.writeString(main_alias);
        dest.writeString(about_me);
        dest.writeString(education);
        dest.writeString(hobby);
        dest.writeString(work_experience);
        dest.writeString(subdivision_title);
        dest.writeString(room_location);
        dest.writeString(position);
        dest.writeLong(exp_points);
        dest.writeLong(exp_level);
        dest.writeParcelable(contacts, flags);
        dest.writeByte((byte) (is_followed_by_current_user ? 1 : 0));
        dest.writeString(region);
        dest.writeStringArray(allowedToEditFields);
        dest.writeByte((byte) (isInstalledChatApp ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserModel> CREATOR = new Creator<UserModel>() {
        @Override
        public UserModel createFromParcel(Parcel in) {
            return new UserModel(in);
        }

        @Override
        public UserModel[] newArray(int size) {
            return new UserModel[size];
        }
    };

    public long getId() {
        return id;
    }

    public String getLogin() {
        return login;
    }

    public String getExternalId() {
        return external_id;
    }

    public String getCreatedAt() {
        return created_at;
    }

    public boolean isActive() {
        return is_active;
    }

    public String getWebUrl() {
        return web_url;
    }

    public String getUrl() {
        return url;
    }

    public String getThumbnail() {
        return thumbnail_url;
    }

    void setThumbnail(String url) {
        thumbnail_url = url;
    }

    public String getThumbnailX2() {
        return thumbnail_x2_url;
    }

    void setThumbnailX2(String url) {
        thumbnail_x2_url = url;
    }

    public String getPhotoLarge() {
        return photo_large_url;
    }

    public String getFullName() {
        return full_name;
    }

    @NonNull
    public String getFirstName() {
        final String[] strings = full_name.split("\\s+");
        return strings[0];
    }

    @NonNull
    public String getLastName() {
        final String[] strings = full_name.split("\\s+");
        if (strings.length == 2) {
            return strings[1];
        } else {
            return "";
        }
    }

    public String getShortFullName() {
        return full_name_short;
    }

    public String getBirthDate() {
        return birth_date;
    }

    public String getMainAlias() {
        return main_alias;
    }

    public String getAbout() {
        return about_me;
    }

    public String getEducation() {
        return education;
    }

    public String getHobby() {
        return hobby;
    }

    public String getWorkExperience() {
        return work_experience;
    }

    public String getSubdivisionTitle() {
        return subdivision_title;
    }

    public String getRoomLocation() {
        return room_location;
    }

    public String getPosition() {
        return position;
    }

    public long getExpPoints() {
        return exp_points;
    }

    public long getExpLevel() {
        return exp_level;
    }

    public Contacts getContacts() {
        return contacts;
    }

    public boolean isFollowing() {
        return is_followed_by_current_user;
    }

    public String getRegion() {
        return region;
    }

    @Nullable
    public String[] getAllowedToEditFields() {
        return allowedToEditFields;
    }

    public boolean isAllowToEdit(Fields field) {
        if (login.equals(contacts.getEmail())) {
            return findString(field);
        } else {
            return !field.equals(Fields.MOBILE_PHONE) && findString(field);
        }
    }

    private boolean findString(Fields field) {
        if (allowedToEditFields == null || allowedToEditFields.length == 0)
            return true;
        else {
            for (String s : allowedToEditFields) {
                if (s.equals(field.key))
                    return true;
            }
        }
        return false;
    }

    @Deprecated
    public ContentValues getContentValues() {
        ContentValues vals = new ContentValues();
        return vals;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof UserModel) {
            if (((UserModel) o).id == id) {
                return true;
            }
        }
        return false;
    }

    public void setFullName(String fullName) {
        this.full_name = fullName;
    }

    public void setSubdivisionTitle(String subdivisionTitle) {
        this.subdivision_title = subdivisionTitle;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setRoomLocation(String roomLocation) {
        this.room_location = roomLocation;
    }

    public void setAboutMe(String aboutMe) {
        this.about_me = aboutMe;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }

    public void setEducation(String education) {
        this.education = education;
    }

    public void setWorkExperience(String workExperience) {
        this.work_experience = workExperience;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setBirthDate(String date) {
        this.birth_date = date;
    }

    public boolean isInstalledChatApp() {
        return isInstalledChatApp;
    }

    public void setInstalledChatApp(boolean installedChatApp) {
        isInstalledChatApp = installedChatApp;
    }

    @Deprecated
    public void saveToDB(Context context) {
        DBOpenHelper.getInstance(context).getWritableDatabase().insertWithOnConflict(UsersTable.TABLE_NAME, null, getContentValues(), SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Deprecated
    public void update(Context context) {
        DBOpenHelper.getInstance(context).getWritableDatabase().update(UsersTable.TABLE_NAME, getContentValues(), UsersTable.COLUMN_ID + "=?", new String[]{"" + id});
    }


}
