package ru.demo.messenger.data.db.tables;

public class ChatsTable {
    public static final String TABLE_NAME = "chatstable";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_USERS = "users";
    public static final String COLUMN_LAST_MESSAGE = "lastmessage";

    public static final String CREATE_TABLE_QUERY = "create table "
            + TABLE_NAME + "("
            + COLUMN_ID + " integer primary key, "
            + COLUMN_LAST_MESSAGE + " integer, "
            + COLUMN_USERS + " text "
            + ");";
}
