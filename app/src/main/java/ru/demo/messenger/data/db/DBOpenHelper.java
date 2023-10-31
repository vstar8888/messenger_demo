package ru.demo.messenger.data.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;

import ru.demo.messenger.data.db.tables.ChatsTable;
import ru.demo.messenger.data.db.tables.MessagesTable;
import ru.demo.messenger.data.db.tables.UsersTable;

public class DBOpenHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "demo.db";

    private static DBOpenHelper instance;

    private DBOpenHelper(Context context) {
        super(context.getApplicationContext(), DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static DBOpenHelper getInstance(@NonNull Context context) {
        DBOpenHelper localInstance = instance;
        if (localInstance == null) {
            synchronized (DBOpenHelper.class) {
                localInstance = instance;
                if (localInstance == null) {
                    instance = localInstance = new DBOpenHelper(context);
                }
            }
        }
        return localInstance;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(UsersTable.CREATE_TABLE_QUERY);
        db.execSQL(MessagesTable.CREATE_TABLE_QUERY);
        db.execSQL(ChatsTable.CREATE_TABLE_QUERY);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTables(db, UsersTable.TABLE_NAME, MessagesTable.TABLE_NAME, ChatsTable.TABLE_NAME);
        onCreate(db);
    }

    private void dropTables(SQLiteDatabase db, String... tableNames) {
        for (String tableName : tableNames) {
            db.execSQL(getDropTableQuery(tableName));
        }
    }

    private static String getDropTableQuery(@NonNull String tableName) {
        return "DROP TABLE IF EXISTS " + tableName;
    }
}
