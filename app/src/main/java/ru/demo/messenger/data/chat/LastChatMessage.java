package ru.demo.messenger.data.chat;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import org.threeten.bp.Instant;
import org.threeten.bp.ZoneId;
import org.threeten.bp.ZonedDateTime;

import ru.demo.messenger.data.message.MessageModel;

public class LastChatMessage implements Parcelable {

    private long id;
    private String text;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("read_status")
    private String readStatus;
    @SerializedName("message_id")
    private long messageId;
    @SerializedName("message_type")
    private String messageType;

    public LastChatMessage(MessageModel message) {
        this.id = message.getAuthorId();
        this.messageId = message.getId();
        this.text = message.getMessage().toString();
        this.createdAt = Instant.ofEpochSecond(message.getCreatedAt())
                .toString();
        this.readStatus = message.getStatus();
        this.messageType = message.getMessageType();
    }

    protected LastChatMessage(Parcel in) {
        id = in.readLong();
        text = in.readString();
        createdAt = in.readString();
        readStatus = in.readString();
        messageId = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(text);
        dest.writeString(createdAt);
        dest.writeString(readStatus);
        dest.writeLong(messageId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<LastChatMessage> CREATOR = new Creator<LastChatMessage>() {
        @Override
        public LastChatMessage createFromParcel(Parcel in) {
            return new LastChatMessage(in);
        }

        @Override
        public LastChatMessage[] newArray(int size) {
            return new LastChatMessage[size];
        }
    };

    public long getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getCreatedAt() {
        return ZonedDateTime.parse(createdAt).withZoneSameInstant(ZoneId.systemDefault()).toEpochSecond();
    }

    public String getReadStatus() {
        return readStatus;
    }

    public void setReadStatus(String readStatus) {
        this.readStatus = readStatus;
    }

    public long getMessageId() {
        return messageId;
    }

    @MessageModel.MessageType
    public String getMessageType() {
        return messageType;
    }

}