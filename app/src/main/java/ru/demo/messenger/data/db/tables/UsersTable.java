package ru.demo.messenger.data.db.tables;

public class UsersTable {
    public static final String TABLE_NAME = "userstable";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_SMALL_SERVER = "smallserver";
    public static final String COLUMN_SMALL_LOCAL = "smalllocal";
    public static final String COLUMN_FULL_SERVER = "fullserver";
    public static final String COLUMN_FULL_LOCAL = "fulllocal";
    public static final String COLUMN_DATA = "data";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_IN_SUBS = "insubs";

    public static final String CREATE_TABLE_QUERY = "create table "
            + TABLE_NAME + "("
            + COLUMN_ID + " integer primary key, "
            + COLUMN_NAME + " text, "
            + COLUMN_SMALL_SERVER + " text, "
            + COLUMN_SMALL_LOCAL + " text, "
            + COLUMN_FULL_SERVER + " text, "
            + COLUMN_FULL_LOCAL + " text, "
            + COLUMN_IN_SUBS + " integer, "
            + COLUMN_DATA + " text"
            + ");";
}
