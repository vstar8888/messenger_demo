package ru.demo.data.message

import android.content.Context
import com.facebook.drawee.backends.pipeline.Fresco
import com.google.gson.reflect.TypeToken
import ru.demo.data.network.RestApi
import ru.demo.domain.message.MessageDataSource
import ru.demo.messenger.data.chat.ChatModel
import ru.demo.messenger.data.message.MessageModel
import ru.demo.messenger.network.RequestManager
import ru.demo.messenger.utils.Prefs
import rx.Completable
import rx.Single
import java.util.*

class MessageRepository(
        private val context: Context,
        private val frescoSmallCacheFetcher: FrescoSmallCacheFetcher
) : MessageDataSource {

    companion object {
        private const val STICKER_SETS = "STICKER_SETS"
    }

    private val messageService = RequestManager.createService(MessageService::class.java)

    override fun sendMessage(
            chatId: Long,
            message: String,
            fileIds: List<String>,
            payload: String
    ): Single<MessageModel> {
        return RestApi.prepareRequest(messageService.sendMessage(chatId, message, fileIds, payload))
                .map { it.message }
    }

    override fun forwardMessage(
            messageId: Long,
            chainId: Long,
            payload: String
    ): Single<MessageModel> {
        return RestApi.prepareRequest(messageService.forwardMessage(messageId, chainId, payload))
                .map { it.message }
    }

    override fun replyMessage(
            quoteMessageId: Long,
            chainId: Long,
            message: String,
            fileIds: List<String>,
            payload: String
    ): Single<MessageModel> {
        val request = messageService.quoteMessage(quoteMessageId, chainId, message, fileIds, payload)
        return RestApi.prepareRequest(request)
                .map { it.message }
    }

    override fun sendSticker(stickerId: Long, chatId: Long, payload: String): Single<MessageModel> {
        return RestApi.prepareRequest(messageService.sendSticker(stickerId, chatId, payload))
                .map { it.message }
    }

    override fun updateStickerSets(): Completable {
        return RestApi.prepareRequest(messageService.getStickerSets())
                .map { it.stickerSets }
                .flatMapCompletable { saveStickerSets(it).andThen(fetchStickersImage(it)) }
    }

    private fun saveStickerSets(stickerSets: List<StickerSet>) = Completable.fromAction {
        Prefs.save(STICKER_SETS, stickerSets)
    }

    private fun fetchStickersImage(stickerSets: List<StickerSet>) = Completable.fromAction {
        stickerSets.forEach {
            it.stickers.forEach { sticker: Sticker ->
                val request = frescoSmallCacheFetcher.requestFromUrl(sticker.imageUrl)
                Fresco.getImagePipeline().prefetchToDiskCache(request, context)
            }
        }
    }

    override fun getStickerSets(): Single<List<StickerSet>> {
        return Single.fromCallable {
            val type = object : TypeToken<List<StickerSet>>() {}.type
            Prefs.load(STICKER_SETS, type) as? List<StickerSet>
                    ?: Collections.emptyList<StickerSet>()
        }
    }

    override fun createChat(
            userIds: List<Long>,
            message: String,
            fileIds: List<String>
    ): Single<ChatModel> {
        val payload = UUID.randomUUID().toString()
        return RestApi.prepareRequest(messageService.createChat(userIds, message, fileIds, payload))
                .map { it.chat }
    }

    override fun createChatWithStickerSend(
            userIds: List<Long>,
            stickerId: Long
    ): Single<ChatModel> {
        val payload = UUID.randomUUID().toString()
        return RestApi.prepareRequest(messageService.createChatWithStickerSend(userIds, stickerId, payload))
                .map { it.chat }
    }

}