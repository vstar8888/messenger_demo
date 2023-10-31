package ru.demo.data.message

import com.google.gson.annotations.SerializedName
import ru.demo.data.network.BaseResponse

import ru.demo.messenger.data.message.MessageModel

class MessageResponse(
        @SerializedName("private_message")
        val message: MessageModel
) : BaseResponse()
