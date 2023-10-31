package ru.demo.messenger.chats.single;

public class MessageReadStatus {

    private long chatId;
    private long messageId;


    public MessageReadStatus(long chatId, long messageId) {
        this.chatId = chatId;
        this.messageId = messageId;
    }

    public long getChatId() {
        return chatId;
    }

    public long getMessageId() {
        return messageId;
    }

}
