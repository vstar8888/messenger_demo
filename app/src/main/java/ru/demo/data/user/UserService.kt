package ru.demo.data.user

import okhttp3.RequestBody
import retrofit2.http.*
import ru.demo.data.network.BaseResponse
import ru.demo.messenger.network.NetworkConst
import rx.Single

interface UserService {

    @POST("api/v3/privatemessages/invite_user")
    @Headers(NetworkConst.Headers.Token.FLAG)
    fun inviteUser(
            @Query("user_id") userId: Long
    ): Single<BaseResponse>


    @Headers(NetworkConst.Headers.Token.FLAG)
    @GET("api/v2/users/by_id")
    fun getUser(@Query("id") userId: Long): Single<UserResponse>

    @Headers(NetworkConst.Headers.Token.FLAG)
    @FormUrlEncoded
    @POST("api/v3/users/update_user")
    fun updateMyUser(
            @Field("first_name") fullName: String,
            @Field("last_name") lastName: String?,
            @Field("subdivision") subdivision: String,
            @Field("position") position: String,
            @Field("date_Of_Birth") birthday: String?,
            @Field("phone_mobile") phoneMobile: String,
            @Field("phone_work") phoneWork: String,
            @Field("skype") skype: String,
            @Field("google_Talk") googleTalk: String,
            @Field("twitter") twitter: String,
            @Field("VKUrl") vkUrl: String?,
            @Field("facebook_Url") facebookUrl: String?,
            @Field("ICQ") icq: String,
            @Field("room_Location") roomLocation: String,
            @Field("about_Me") about: String,
            @Field("hobby") hobby: String,
            @Field("education") education: String,
            @Field("work_experience") workExperience: String,
            @Field("region") region: String
    ): Single<UserResponse>

    @Headers(NetworkConst.Headers.Token.FLAG)
    @POST("api/v2/users/upload_photo")
    fun updateMyUserAvatar(@Body body: RequestBody): Single<BaseResponse>

}