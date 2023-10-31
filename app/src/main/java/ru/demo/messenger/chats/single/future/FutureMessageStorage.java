package ru.demo.messenger.chats.single.future;

import android.util.LongSparseArray;

import java.util.ArrayList;
import java.util.List;

import ru.demo.messenger.chats.single.future.message.FutureMessage;

// TODO: ET 12.12.16 add saving in disk memory (shared prefs as json string or db)
public class FutureMessageStorage {

    public interface ChangeDataListener {
        void onMessageAdded(FutureMessage message);

        void onMessageRemoved(long chatId);

        void onDroppedAll();
    }

    private static FutureMessageStorage instance = new FutureMessageStorage();

    private LongSparseArray<List<FutureMessage>> chatMessageQueue;

    private List<ChangeDataListener> changeDataListeners;


    private FutureMessageStorage() {
        this.chatMessageQueue = new LongSparseArray<>();
        this.changeDataListeners = new ArrayList<>(10);
    }

    public static FutureMessageStorage getInstance() {
        return instance;
    }

    public LongSparseArray<List<FutureMessage>> getAllChats() {
        return chatMessageQueue;
    }

    public void add(FutureMessage message) {
        final long chatId = message.getChatId();
        List<FutureMessage> messagesQueue = chatMessageQueue.get(chatId);
        if (messagesQueue == null) {
            messagesQueue = new ArrayList<>();
            chatMessageQueue.put(chatId, messagesQueue);
        }
        messagesQueue.add(message);

        for (int i = 0; i < changeDataListeners.size(); i++) {
            changeDataListeners.get(i).onMessageAdded(message);
        }
    }

    public void remove(long chatId) {
        List<FutureMessage> messagesQueue = chatMessageQueue.get(chatId);
        if (messagesQueue == null) {
            return;
        }
        messagesQueue.remove(0);

        for (int i = 0; i < changeDataListeners.size(); i++) {
            changeDataListeners.get(i).onMessageRemoved(chatId);
        }
    }

    public void dropAllData() {
        for (int i = 0; i < changeDataListeners.size(); i++) {
            changeDataListeners.get(i).onDroppedAll();
        }
        chatMessageQueue.clear();
    }

    public void addChangeDataListener(ChangeDataListener listener) {
        if (listener == null) {
            return;
        }
        changeDataListeners.add(listener);
    }

    public void removeChangeDataListener(ChangeDataListener listener) {
        changeDataListeners.remove(listener);
    }

    public List<FutureMessage> getChatMessagesById(long chatId) {
        return chatMessageQueue.get(chatId);
    }

}
