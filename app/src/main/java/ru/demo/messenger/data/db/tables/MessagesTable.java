package ru.demo.messenger.data.db.tables;

public class MessagesTable {
    public static final String TABLE_NAME = "messagestable";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_AUTHOR = "author";
    public static final String COLUMN_MEDIA_PATHS = "mediapaths";
    public static final String COLUMN_CREATED_AT = "createdat";
    public static final String COLUMN_CHAT_ID = "chatid";
    public static final String COLUMN_TEXT = "text";
    public static final String COLUMN_MEDIA_URLS = "mediaurls";
    public static final String COLUMN_FILE_PATHS = "fileurls";

    public static final String CREATE_TABLE_QUERY = "create table "
            + TABLE_NAME + "("

            + COLUMN_ID + " integer primary key, "
            + COLUMN_AUTHOR + " integer, "
            + COLUMN_MEDIA_PATHS + " text, "
            + COLUMN_CREATED_AT + " integer, "
            + COLUMN_CHAT_ID + " integer, "
            + COLUMN_FILE_PATHS + " text, "
            + COLUMN_MEDIA_URLS + " text, "
            + COLUMN_TEXT + " text"
            + ");";
}
