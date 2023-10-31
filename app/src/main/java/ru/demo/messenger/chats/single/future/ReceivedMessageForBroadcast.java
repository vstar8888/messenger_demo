package ru.demo.messenger.chats.single.future;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;

import ru.demo.messenger.data.message.MessageModel;

public class ReceivedMessageForBroadcast implements Parcelable {
    private MessageModel message;
    private boolean isSuccessfulSend;
    private long chatId;

    @Nullable public MessageModel getMessage() {
        return isSuccessfulSend ? message : null;
    }

    public boolean isSuccessfulSend() {
        return isSuccessfulSend;
    }

    public long getChatId() {
        return chatId;
    }

    static ReceivedMessageForBroadcast ofSuccessfulSend(MessageModel message) {
        return new ReceivedMessageForBroadcast(message);
    }

    static ReceivedMessageForBroadcast ofError(long chatId) {
        return new ReceivedMessageForBroadcast(chatId);
    }

    private ReceivedMessageForBroadcast(MessageModel message) {
        this.message = message;
        this.chatId = message.getChatId();
        this.isSuccessfulSend = true;
    }

    private ReceivedMessageForBroadcast(long chatId) {
        this.chatId = chatId;
        this.isSuccessfulSend = false;
    }

    private ReceivedMessageForBroadcast(Parcel in) {
        message = in.readParcelable(MessageModel.class.getClassLoader());
        isSuccessfulSend = in.readByte() != 0;
        chatId = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(message, flags);
        dest.writeByte((byte) (isSuccessfulSend ? 1 : 0));
        dest.writeLong(chatId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ReceivedMessageForBroadcast> CREATOR = new Creator<ReceivedMessageForBroadcast>() {
        @Override
        public ReceivedMessageForBroadcast createFromParcel(Parcel in) {
            return new ReceivedMessageForBroadcast(in);
        }

        @Override
        public ReceivedMessageForBroadcast[] newArray(int size) {
            return new ReceivedMessageForBroadcast[size];
        }
    };
}