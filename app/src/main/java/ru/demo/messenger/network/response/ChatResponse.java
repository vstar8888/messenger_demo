package ru.demo.messenger.network.response;

import com.google.gson.annotations.SerializedName;

import ru.demo.messenger.data.chat.ChatModel;
import ru.demo.messenger.network.response.base.BaseResponse;

public class ChatResponse extends BaseResponse {
    @SerializedName("private_message")
    public ChatModel chat;
}
