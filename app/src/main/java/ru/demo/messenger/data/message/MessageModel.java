package ru.demo.messenger.data.message;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;
import android.text.Spanned;
import android.text.TextUtils;

import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ru.demo.data.message.Sticker;
import ru.demo.messenger.data.db.DBOpenHelper;
import ru.demo.messenger.data.db.tables.MessagesTable;
import ru.demo.messenger.utils.StringUtils;

public class MessageModel implements Parcelable {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({ReadStatus.DELIVERED, ReadStatus.READ, ReadStatus.READ_ALL})
    public @interface ReadStatus {
        // simulated status for sent messages
        String WAIT = "Wait";
        String DELIVERED = "Delivered";
        String READ = "Read";
        String READ_ALL = "ReadAll";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({Direction.INCOMING, Direction.OUTGOING})
    public @interface Direction {
        // simulated status for sent messages
        String INCOMING = "Incoming";
        String OUTGOING = "Outgoing";
    }

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({MessageType.MESSAGE, MessageType.FORWARD, MessageType.STICKER,
            MessageType.FILE_VIDEO, MessageType.FILE_IMAGE, MessageType.FILE_DOCUMENT,
            MessageType.ADD_PARTICIPANT, MessageType.REMOVE_PARTICIPANT,
            MessageType.REMOVE_YOU, MessageType.YOU_NEW_ADMIN, MessageType.SELF_REMOVE,
            MessageType.CREATE_GROUP, MessageType.QUOTE
    })
    public @interface MessageType {
        // simulated status for sent messages
        String MESSAGE = "message";
        String FORWARD = "forward";
        String STICKER = "sticker";
        String FILE_VIDEO = "file_video";
        String FILE_IMAGE = "file_image";
        String FILE_DOCUMENT = "file_document";
        String ADD_PARTICIPANT = "add_participient";
        String REMOVE_PARTICIPANT = "remove_participient";
        String REMOVE_YOU = "remove_you";
        String YOU_NEW_ADMIN = "you_new_admin";
        String SELF_REMOVE = "self_remove";
        String CREATE_GROUP = "create_group";
        String QUOTE = "quote";
    }

    private long id;
    private long chatId;
    private long createdAt;
    private String direction;
    private String status;
    private long authorId;
    private String message;
    private AttachedFiles files;
    private String authorFullName;
    private String authorAvatarUrl;
    private String authorAlias;
    private String payload;
    private String messageType;

    private transient String text;

    @Nullable
    private MessageModel originalMessage;
    @Nullable
    private Sticker sticker;

    public MessageModel(long id, long chatId, String createdDate, String direction, String status,
                        long authorId, String message, AttachedFiles files,
                        String authorFullName, String authorAvatarUrl, String authorAlias,
                        String payload, String messageType, @Nullable MessageModel originalMessage,
                        @Nullable Sticker sticker) {
        this.id = id;
        this.chatId = chatId;
        this.createdAt = ZonedDateTime.parse(createdDate)
                .withZoneSameInstant(ZoneId.systemDefault())
                .toEpochSecond();
        this.direction = direction;
        this.status = status;
        this.authorId = authorId;
        this.message = message;
        this.files = files;
        this.authorFullName = authorFullName;
        this.authorAvatarUrl = authorAvatarUrl;
        this.authorAlias = authorAlias;
        this.payload = payload;
        this.messageType = messageType;
        this.originalMessage = originalMessage;
        this.sticker = sticker;
    }

    protected MessageModel(Parcel in) {
        id = in.readLong();
        chatId = in.readLong();
        createdAt = in.readLong();
        direction = in.readString();
        status = in.readString();
        authorId = in.readLong();
        message = in.readString();
        files = in.readParcelable(AttachedFiles.class.getClassLoader());
        authorFullName = in.readString();
        authorAvatarUrl = in.readString();
        authorAlias = in.readString();
        payload = in.readString();
        messageType = in.readString();
        originalMessage = in.readParcelable(MessageModel.class.getClassLoader());
        sticker = in.readParcelable(Sticker.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeLong(chatId);
        dest.writeLong(createdAt);
        dest.writeString(direction);
        dest.writeString(status);
        dest.writeLong(authorId);
        dest.writeString(message);
        dest.writeParcelable(files, flags);
        dest.writeString(authorFullName);
        dest.writeString(authorAvatarUrl);
        dest.writeString(authorAlias);
        dest.writeString(payload);
        dest.writeString(messageType);
        dest.writeParcelable(originalMessage, flags);
        dest.writeParcelable(sticker, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MessageModel> CREATOR = new Creator<MessageModel>() {
        @Override
        public MessageModel createFromParcel(Parcel in) {
            return new MessageModel(in);
        }

        @Override
        public MessageModel[] newArray(int size) {
            return new MessageModel[size];
        }
    };

    public boolean isMeRemovedMessageType() {
        return MessageType.SELF_REMOVE.equals(messageType) ||
                MessageType.REMOVE_YOU.equals(messageType);
    }

    public boolean isBubbleMessage() {
        return MessageType.MESSAGE.equals(messageType) ||
                MessageType.FORWARD.equals(messageType) ||
                MessageType.QUOTE.equals(messageType) ||
                MessageType.FILE_VIDEO.equals(messageType) ||
                MessageType.FILE_IMAGE.equals(messageType) ||
                MessageType.FILE_DOCUMENT.equals(messageType);
    }

    public boolean isStickerMessage() {
        return MessageType.STICKER.equalsIgnoreCase(messageType);
    }

    public long getId() {
        return id;
    }

    public long getChatId() {
        return chatId;
    }

    /**
     * @return second from Epoch time
     */
    public long getCreatedAt() {
        return createdAt;
    }

    public String getDirection() {
        return direction;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(@ReadStatus String status) {
        this.status = status;
    }

    public long getAuthorId() {
        return authorId;
    }

    public Spanned getMessage() {
        return StringUtils.fromHtmlCompat(message);
    }

    public String getText() {
        if (text == null) {
            text = StringUtils.fromHtmlCompat(message).toString();
        }
        return text;
    }

    public AttachedFiles getFiles() {
        return files;
    }


    public String getAuthorFullName() {
        return authorFullName;
    }

    @Nullable
    public String getAuthorAvatarUrl() {
        return authorAvatarUrl;
    }

    public String getAuthorAlias() {
        return authorAlias;
    }

    @Nullable
    public String getPayload() {
        return payload;
    }

    @MessageModel.MessageType
    public String getMessageType() {
        return messageType;
    }

    @Nullable
    public MessageModel getOriginalMessage() {
        return originalMessage;
    }

    @Nullable
    public Sticker getSticker() {
        return sticker;
    }

    // TODO: DI 16.09.16 move out
    public MessageModel(Cursor cursor) {
        id = cursor.getLong(cursor.getColumnIndexOrThrow(MessagesTable.COLUMN_ID));
        text = cursor.getString(cursor.getColumnIndexOrThrow(MessagesTable.COLUMN_TEXT));
        String paths = cursor.getString(cursor.getColumnIndexOrThrow(MessagesTable.COLUMN_MEDIA_PATHS));
        if (!TextUtils.isEmpty(paths)) {
            translatePathsToMap(paths);
        } else {
//            serverAndLocalImagePaths = new HashMap<String, String>();
        }
        String filePaths = cursor.getString(cursor.getColumnIndexOrThrow(MessagesTable.COLUMN_FILE_PATHS));
        if (!TextUtils.isEmpty(filePaths)) {
            translateFilePathsToMap(filePaths);
        } else {
//            serverAndLocalFilePaths = new HashMap<String, String[]>();
        }

        authorId = cursor.getLong(cursor.getColumnIndexOrThrow(MessagesTable.COLUMN_AUTHOR));
        chatId = cursor.getLong(cursor.getColumnIndexOrThrow(MessagesTable.COLUMN_CHAT_ID));
        createdAt = cursor.getLong(cursor.getColumnIndexOrThrow(MessagesTable.COLUMN_CREATED_AT));
    }

    private ContentValues getContentValues() {
        ContentValues vals = new ContentValues();
        vals.put(MessagesTable.COLUMN_ID, id);
        vals.put(MessagesTable.COLUMN_AUTHOR, authorId);
        vals.put(MessagesTable.COLUMN_CHAT_ID, chatId);
        vals.put(MessagesTable.COLUMN_CREATED_AT, createdAt);
        vals.put(MessagesTable.COLUMN_TEXT, text);
        vals.put(MessagesTable.COLUMN_MEDIA_PATHS, translateMapToString());
        vals.put(MessagesTable.COLUMN_FILE_PATHS, translateFilesMapToString());

        return vals;
    }

    public void saveToDb(Context context) {
        long savedid = DBOpenHelper.getInstance(context).getWritableDatabase().insertWithOnConflict(MessagesTable.TABLE_NAME, null, getContentValues(), SQLiteDatabase.CONFLICT_REPLACE);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof MessageModel) {
            if (((MessageModel) o).id == id) {
                return true;
            }
        }
        return false;
    }

    private void translatePathsToMap(String input) {
        String[] splitted = input.split(",");
//        serverAndLocalImagePaths = new HashMap<String, String>();
        for (int y = 0; y < splitted.length; y++) {
            String keyvalue[] = splitted[y].split("<::>");
            if (keyvalue.length > 1) {
//                serverAndLocalImagePaths.put(keyvalue[0], keyvalue[1]);
            } else {
//                serverAndLocalImagePaths.put(keyvalue[0], "");
            }
        }
    }

    private void translateFilePathsToMap(String input) {
        //
        String[] splitted = input.split(",,,");
//        serverAndLocalFilePaths = new HashMap<String, String[]>();
        for (int y = 0; y < splitted.length; y++) {
            String keyvalue[] = splitted[y].split("<::>");
            if (keyvalue.length > 2) {
//                serverAndLocalFilePaths.put(keyvalue[0], new String[]{keyvalue[1], keyvalue[2]});
            } else if (keyvalue.length > 1) {
//                serverAndLocalFilePaths.put(keyvalue[0], new String[]{keyvalue[1], ""});
            } else if (keyvalue.length == 1) {
//                serverAndLocalFilePaths.put(keyvalue[0], new String[2]);
            }
        }
    }

    private String translateMapToString() {
//        if (serverAndLocalImagePaths != null) {
//            String answer = "";
//            if (serverAndLocalImagePaths.size() > 0) {
//                String[] keySet = serverAndLocalImagePaths.keySet().toArray(new String[serverAndLocalImagePaths.size()]);
//                answer = "" + keySet[0] + "<::>" + serverAndLocalImagePaths.get(keySet[0]);
//                for (int y = 1; y < keySet.length; y++) {
//                    answer = answer + ",";
//                    answer = answer + keySet[y] + "<::>" + serverAndLocalImagePaths.get(keySet[y]);
//                }
//            }
//            return answer;
//        } else {
        return "";
//        }
    }

    private String translateFilesMapToString() {
//        if (serverAndLocalFilePaths != null) {
//            String answer = "";
//            if (serverAndLocalFilePaths.size() > 0) {
//                //creating array of keys
//                String[] keySet = serverAndLocalFilePaths.keySet().toArray(new String[serverAndLocalFilePaths.size()]);
//
//                String[] fileCreds = serverAndLocalFilePaths.get(keySet[0]);
//
//                answer = "" + keySet[0] + "<::>" + fileCreds[0] + "<::>" + (fileCreds.length == 1 ? "" : fileCreds[1]);
//                for (int y = 1; y < keySet.length; y++) {
//                    answer = answer + ",,,";
//                    fileCreds = serverAndLocalFilePaths.get(keySet[y]);
//                    answer = answer + keySet[y] + "<::>" + fileCreds[0] + "<::>" + (fileCreds.length == 1 ? "" : fileCreds[1]);
//                }
//            }
//            return answer;
//        } else {
        return "";
//        }
    }

/*
    private void translateLocalPathsToArray (String input) {
        if (!TextUtils.isEmpty(input)) {
            String[] splitted = input.split(",");
            localPathsToImages = new ArrayList<String>();
            for (int y = 0; y < splitted.length; y++) {
                localPathsToImages.add(splitted[y]);
            }
        } else {
            localPathsToImages = new ArrayList<String>();
        }
    }

    private void translateServerPathsToArray (String input) {
        if (!TextUtils.isEmpty(input)) {
            String[] splitted = input.split(",");
            serverPathsToImages = new ArrayList<String>();
            for (int y = 0; y < splitted.length; y++) {
                serverPathsToImages.add(splitted[y]);
            }
        } else {
            serverPathsToImages = new ArrayList<String>();
        }
    }

    private String translateLocalPathsToString () {
        if (localPathsToImages!=null) {
            String answer = "";
            if (localPathsToImages.size() > 0) {
                answer = "" + localPathsToImages.get(0);
                for (int y = 1; y < localPathsToImages.size(); y++) {
                    answer = answer + ",";
                    answer = answer + localPathsToImages.get(y);
                }
            }
            return answer;
        } else {
            return "";
        }
    }

    private String translateServerUrlsToString () {
        if (serverPathsToImages!=null) {
            String answer = "";
            if (serverPathsToImages.size() > 0) {
                answer = "" + serverPathsToImages.get(0);
                for (int y = 1; y < serverPathsToImages.size(); y++) {
                    answer = answer + ",";
                    answer = answer + serverPathsToImages.get(y);
                }
            }
            return answer;
        } else {
            return "";
        }
    }*/

}
