package ru.demo.messenger.network.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import ru.demo.messenger.data.chat.ChatModel;
import ru.demo.messenger.network.response.base.PagedResponse;

public class ChatList extends PagedResponse {
    @SerializedName("chains")
    public List<ChatModel> chats;
}
