package ru.demo.domain.notifications

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import ru.demo.data.network.BaseResponse
import ru.demo.messenger.network.NetworkConst
import ru.demo.messenger.network.request.NotificationToken
import rx.Single

interface NotificationService {

    @Headers(NetworkConst.Headers.Token.FLAG)
    @POST("api/v2/notifications/Register_Android_Device")
    fun registerDevice(
            @Body request: NotificationToken
    ): Single<BaseResponse>

    @Headers(NetworkConst.Headers.Token.FLAG)
    @POST("api/v2/notifications/unregister_android_device")
    fun unregisterDevice(
            @Body request: NotificationToken
    ): Single<BaseResponse>

}
