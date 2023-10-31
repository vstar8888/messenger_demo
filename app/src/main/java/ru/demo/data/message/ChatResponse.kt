package ru.demo.data.message

import com.google.gson.annotations.SerializedName
import ru.demo.data.network.BaseResponse

import ru.demo.messenger.data.chat.ChatModel

class ChatResponse(
        @SerializedName("private_message")
        var chat: ChatModel
) : BaseResponse()
