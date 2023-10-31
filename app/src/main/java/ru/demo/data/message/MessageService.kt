package ru.demo.data.message

import retrofit2.http.*
import ru.demo.messenger.network.NetworkConst
import rx.Single

interface MessageService {

    @Headers(NetworkConst.Headers.Token.FLAG)
    @POST("api/v3/privatemessages/reply")
    fun sendMessage(
            @Query("chain_id") chatId: Long,
            @Query("text") message: String,
            @Query("file_ids") fileIds: List<String>,
            @Query("payload") payload: String
    ): Single<MessageResponse>

    @POST("api/v3/privatemessages/forward_to_chain")
    @Headers(NetworkConst.Headers.Token.FLAG)
    fun forwardMessage(
            @Query("id") messageId: Long,
            @Query("to_chain_id") chatId: Long,
            @Query("payload") payload: String
    ): Single<MessageResponse>


    @Headers(NetworkConst.Headers.Token.FLAG)
    @POST("api/v3/privatemessages/quote")
    @FormUrlEncoded
    fun quoteMessage(
            @Field("id") quoteMessageId: Long,
            @Field("chain_id") chatId: Long,
            @Field("text") message: String,
            @Query("file_ids") fileIds: List<String>,
            @Field("payload") payload: String
    ): Single<MessageResponse>

    @Headers(NetworkConst.Headers.Token.FLAG)
    @POST("api/v3/privatemessages/send_sticker_to_chain")
    fun sendSticker(
            @Query("sticker_id") stickerId: Long,
            @Query("to_chain_id") chatId: Long,
            @Query("payload") payload: String
    ): Single<MessageResponse>

    @Headers(NetworkConst.Headers.Token.FLAG)
    @POST("api/v3/privatemessages/send_sticker_to_contacts")
    fun createChatWithStickerSend(
            @Query("assign_to_user_ids") userIds: List<Long>,
            @Query("sticker_id") stickerId: Long,
            @Query("payload") payload: String
    ): Single<ChatResponse>

    @Headers(NetworkConst.Headers.Token.FLAG)
    @GET("api/v3/privatemessages/get_sticker_sets")
    fun getStickerSets(): Single<StickerSetsResponse>

    @Headers(NetworkConst.Headers.Token.FLAG)
    @GET("api/v3/privatemessages/get_sticker_set_by_id/{id}")
    fun getStickers(
            @Path("id") setId: String
    ): Single<StickersResponse>

    @Headers(NetworkConst.Headers.Token.FLAG)
    @POST("api/v3/privatemessages/create")
    fun createChat(
            @Query("assign_to_user_ids") userIds: List<Long>,
            @Query("text") message: String,
            @Query("file_ids") fileIds: List<String>,
            @Query("payload") payload: String
    ): Single<ChatResponse>

}
