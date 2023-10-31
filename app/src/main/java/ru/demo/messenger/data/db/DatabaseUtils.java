package ru.demo.messenger.data.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import ru.demo.messenger.Consts;
import ru.demo.messenger.data.chat.ChatModel;
import ru.demo.messenger.data.db.tables.ChatsTable;
import ru.demo.messenger.data.db.tables.MessagesTable;
import ru.demo.messenger.data.db.tables.UsersTable;
import ru.demo.messenger.data.message.MessageModel;
import ru.demo.messenger.data.user.UserModel;
import ru.demo.messenger.utils.Prefs;

/**
 * Created by igor on 21.10.15.
 */
public class DatabaseUtils {

    public static ArrayList<UserModel> getAllUsersFromDb (Context context) {
        ArrayList<UserModel> templist = new ArrayList<UserModel>();
        long userId = 0;
        userId = Prefs.get().getLong(Consts.Prefs.USER_ID, 0);

        Cursor data = DBOpenHelper.getInstance(context).getWritableDatabase().query(UsersTable.TABLE_NAME, null, null, null, null, null, null);
        if (data == null) {
            return null;
        }

        if (data.moveToFirst()) {
            do {
                UserModel model = new UserModel(data);
                if (model.getId()!=userId) {
                    templist.add(model);
                }
            } while (data.moveToNext());
        }

        data.close();
        return templist;
    }

    public static ArrayList<MessageModel> getAllMessagesForChat (Context context, long chatId) {
        ArrayList<MessageModel> templist = new ArrayList<MessageModel>();

        Cursor data = DBOpenHelper.getInstance(context).getWritableDatabase().query(MessagesTable.TABLE_NAME, null, MessagesTable.COLUMN_CHAT_ID + "=?", new String[]{""+chatId}, null, null, null);
        if (data == null) {
            return null;
        }

        if (data.moveToFirst()) {
            do {
                MessageModel model = new MessageModel(data);
                templist.add(model);
            } while (data.moveToNext());
        }

        data.close();
        return templist;
    }


    public static UserModel getUserById (Context context, long id) {
        Cursor data = DBOpenHelper.getInstance(context).getWritableDatabase().query(UsersTable.TABLE_NAME, null, UsersTable.COLUMN_ID + "=?", new String[]{""+id}, null, null, null);
        if (data == null) {
            return null;
        }

        if (data.moveToFirst()) {
            UserModel model = new UserModel(data);
            data.close();
            return model;
        } else {
            data.close();
            return null;
        }
    }

    public static ChatModel getChatById (Context context, long id) {
        Cursor data = DBOpenHelper.getInstance(context).getWritableDatabase().query(ChatsTable.TABLE_NAME, null, ChatsTable.COLUMN_ID + "=?", new String[]{""+id}, null, null, null);
        if (data == null) {
            return null;
        }

        if (data.moveToFirst()) {
            ChatModel model = new ChatModel(context, data);
            data.close();
            return model;
        } else {
            data.close();
            return null;
        }
    }


    public static MessageModel getMessageById (Context context, long id) {
        Cursor data = DBOpenHelper.getInstance(context).getWritableDatabase().query(MessagesTable.TABLE_NAME, null, MessagesTable.COLUMN_ID + "=?", new String[]{""+id}, null, null, null);
        if (data == null) {
            return null;
        }

        if (data.moveToFirst()) {
            MessageModel model = new MessageModel(data);
            data.close();
            return model;
        } else {
            data.close();
            return null;
        }
    }

    public static ArrayList<ChatModel> getAllChatsFromDb (Context context) {
        ArrayList<ChatModel> models = new ArrayList<ChatModel>();
        Cursor data = DBOpenHelper.getInstance(context).getWritableDatabase().query(ChatsTable.TABLE_NAME, null, null, null, null, null, null);
        if (data == null) {
            return null;
        }

        if (data.moveToFirst()) {
            do {
                ChatModel model = new ChatModel(context, data);
                models.add(model);
            } while (data.moveToNext());
        }

        data.close();
        return models;
    }

    public static boolean clearDb (Context context) {
        SQLiteDatabase db = DBOpenHelper.getInstance(context).getWritableDatabase();
        db.execSQL("delete from " + ChatsTable.TABLE_NAME);
        db.execSQL("delete from " + MessagesTable.TABLE_NAME);
        db.execSQL("delete from " + UsersTable.TABLE_NAME);
        return true;
    }


}
