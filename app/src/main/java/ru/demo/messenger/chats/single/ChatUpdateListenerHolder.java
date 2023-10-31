package ru.demo.messenger.chats.single;

class ChatUpdateListenerHolder {

    public long chatId;
    ChatUpdateManager.Listener listener;

    ChatUpdateListenerHolder(long chatId, ChatUpdateManager.Listener listener) {
        this.chatId = chatId;
        this.listener = listener;
    }

}