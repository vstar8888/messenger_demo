package ru.demo.domain.message

import ru.demo.data.message.StickerSet
import ru.demo.messenger.data.chat.ChatModel
import ru.demo.messenger.data.message.MessageModel
import rx.Completable
import rx.Single

interface MessageDataSource {

    fun sendMessage(
            chatId: Long,
            message: String,
            fileIds: List<String>,
            payload: String
    ): Single<MessageModel>

    fun forwardMessage(
            messageId: Long,
            chainId: Long,
            payload: String
    ): Single<MessageModel>

    fun replyMessage(
            quoteMessageId: Long,
            chainId: Long,
            message: String,
            fileIds: List<String>,
            payload: String
    ): Single<MessageModel>

    fun sendSticker(stickerId: Long, chatId: Long, payload: String): Single<MessageModel>

    fun updateStickerSets(): Completable

    fun getStickerSets(): Single<List<StickerSet>>

    fun createChat(userIds: List<Long>, message: String, fileIds: List<String>): Single<ChatModel>

    fun createChatWithStickerSend(
            userIds: List<Long>,
            stickerId: Long
    ): Single<ChatModel>

}