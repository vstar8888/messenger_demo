package ru.demo.messenger.data.chat;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.IntDef;
import android.text.TextUtils;

import com.google.gson.annotations.SerializedName;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import ru.demo.messenger.data.db.tables.ChatsTable;
import ru.demo.messenger.data.user.UserModel;

public class ChatModel implements Parcelable {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({Type.ACTIVE, Type.ARCHIVE})
    public @interface Type {
        int ACTIVE = 1;
        int ARCHIVE = 2;
    }

    @SerializedName("chain_id")
    private long id;
    private String title;
    @SerializedName("chain_avatar_large")
    private String photo;
    @SerializedName("unread_messages_count")
    private int unreadMessagesCount;
    //    @SerializedName("chain_type")
    @Type
    private int type;
    @SerializedName("admins_list")
    private List<Long> adminsList;
    @SerializedName("is_in_chain")
    private boolean isInChain;
    @SerializedName("is_group_chain")
    private boolean isGroupChain;
    private List<UserModel> users;
    @SerializedName("last_message")
    private LastChatMessage lastMessage;

//    public ChatModel(long id, int type, List<UserModel> users, MessageModel lastMessage) {
//        this.id = id;
//        this.type = type;
//        this.users = users;
//        this.lastMessage = lastMessage;
//    }

    protected ChatModel(Parcel in) {
        id = in.readLong();
        title = in.readString();
        photo = in.readString();
        unreadMessagesCount = in.readInt();
        type = in.readInt();
        adminsList = new ArrayList<>();
        in.readList(adminsList, null);
        isInChain = in.readByte() != 0;
        isGroupChain = in.readByte() != 0;
        users = in.createTypedArrayList(UserModel.CREATOR);
        lastMessage = in.readParcelable(LastChatMessage.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(title);
        dest.writeString(photo);
        dest.writeInt(unreadMessagesCount);
        dest.writeInt(type);
        dest.writeList(adminsList);
        dest.writeByte((byte) (isInChain ? 1 : 0));
        dest.writeByte((byte) (isGroupChain ? 1 : 0));
        dest.writeTypedList(users);
        dest.writeParcelable(lastMessage, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ChatModel> CREATOR = new Creator<ChatModel>() {
        @Override
        public ChatModel createFromParcel(Parcel in) {
            return new ChatModel(in);
        }

        @Override
        public ChatModel[] newArray(int size) {
            return new ChatModel[size];
        }
    };

    public long getId() {
        return id;
    }

    @Type
    public int getType() {
        return type;
    }

    public List<UserModel> getUsers() {
        return users;
    }

    public List<Long> getAdminsList() {
        return adminsList;
    }

    public LastChatMessage getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(LastChatMessage lastMessage) {
        this.lastMessage = lastMessage;
    }

    public boolean isOneToOne() {
        return !isGroupChain;
    }

    public boolean isInChain() {
        return isInChain;
    }

    public String getName(long excludedUserId) {
        final StringBuilder chatName = new StringBuilder(15 * users.size());
        for (int i = 0; i < users.size(); i++) {
            final UserModel user = users.get(i);
            if (user.getId() == excludedUserId) {
                continue;
            }
            if (chatName.length() > 0) {
                chatName.append(", ");
            }
            chatName.append(user.getFullName());
        }
        return chatName.toString();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public int getUnreadMessagesCount() {
        return unreadMessagesCount;
    }

    public void setUnreadMessagesCount(int unreadMessagesCount) {
        this.unreadMessagesCount = unreadMessagesCount;
    }

    // TODO: DI 16.09.16 move out there
    public ChatModel(Context context, Cursor cursor) {
        id = cursor.getLong(cursor.getColumnIndexOrThrow(ChatsTable.COLUMN_ID));
        String users = cursor.getString(cursor.getColumnIndexOrThrow(ChatsTable.COLUMN_USERS));
        translateToArray(users);
        long lastMessageId = cursor.getLong(cursor.getColumnIndexOrThrow(ChatsTable.COLUMN_LAST_MESSAGE));
//        if (lastMessageId != 0) {
//            lastMessage = DatabaseUtils.getMessageById(context, lastMessageId);
//        }
    }

//    public ContentValues getContentValues() {
//        ContentValues vals = new ContentValues();
//        vals.put(ChatsTable.COLUMN_ID, id);
//        vals.put(ChatsTable.COLUMN_USERS, translateToString());
//        long lastMessageId = lastMessage == null ? 0 : lastMessage.getId();
//        vals.put(ChatsTable.COLUMN_LAST_MESSAGE, lastMessageId);
//
//        return vals;
//    }
//
//    public void saveToDB(Context context) {
//        DBOpenHelper.getInstance(context).getWritableDatabase().insertWithOnConflict(ChatsTable.TABLE_NAME, null, getContentValues(), SQLiteDatabase.CONFLICT_REPLACE);
//    }
//
//
//    public void update(Context context) {
//        DBOpenHelper.getInstance(context).getWritableDatabase().update(ChatsTable.TABLE_NAME, getContentValues(), ChatsTable.COLUMN_ID + "=?", new String[]{"" + id});
//    }

    private void translateToArray(String usersString) {
        if (!TextUtils.isEmpty(usersString)) {
            String[] splitted = usersString.split(",");
//            usersInChat = new ArrayList<Long>();
            for (int y = 0; y < splitted.length; y++) {
//                usersInChat.add(Long.parseLong(splitted[y]));
            }
        } else {
//            usersInChat = new ArrayList<Long>();
        }
    }


    private String translateToString() {
//        if (usersInChat != null) {
        String answer = "";
//            if (usersInChat.size() > 0) {
//                answer = "" + usersInChat.get(0);
//                for (int y = 1; y < usersInChat.size(); y++) {
        answer = answer + ",";
//                    answer = answer + usersInChat.get(y);
//                }
//            }
//            return answer;
//        } else {
        return "";
//        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ChatModel) {
            if (((ChatModel) o).id == id) {
                return true;
            }
        }
        return false;
    }

    public static class DateComparator implements Comparator<ChatModel> {

        @Override
        public int compare(ChatModel p1, ChatModel p2) {
            if (p1.lastMessage.getCreatedAt() == p2.lastMessage.getCreatedAt()) {
                if (p1.id == p2.id) {
                    return 0;
                } else if (p1.id > p2.id) {
                    return -1;
                } else {
                    return 1;
                }
            } else if (p1.lastMessage.getCreatedAt() > p2.lastMessage.getCreatedAt()) {
                return -1;
            } else {
                return 1;
            }
        }
    }

    public void setUsers(List<UserModel> users) {
        this.users = users;
    }

    public void setIsRead() {
        unreadMessagesCount = 0;
    }

    public void setInChain(boolean inChain) {
        isInChain = inChain;
    }

}
