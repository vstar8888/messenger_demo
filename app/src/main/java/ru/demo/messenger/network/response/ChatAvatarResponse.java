package ru.demo.messenger.network.response;

import com.google.gson.annotations.SerializedName;

import ru.demo.messenger.network.response.base.BaseResponse;

public class ChatAvatarResponse extends BaseResponse {

    @SerializedName("chain_avatar_url")
    public String chatAvatar;

}
