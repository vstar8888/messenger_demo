package ru.demo.messenger.data.chat;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class MessageReadEvent {

    private int id;
    @SerializedName("userIds")
    private List<String> userIds = Collections.emptyList();
    @SerializedName("chainId")
    private int chatId;
    private String status;
    private int authorId;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

}
