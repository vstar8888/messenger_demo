package ru.demo.messenger.network.services;

import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import ru.demo.messenger.network.NetworkConst;
import ru.demo.messenger.network.request.NotificationToken;
import ru.demo.messenger.network.response.base.BaseResponse;
import rx.Observable;

public interface NotificationService {

    @Headers(NetworkConst.Headers.Token.FLAG)
    @POST("api/v2/notifications/Register_Android_Device")
    Observable<Response<BaseResponse>> registerToken(@Body NotificationToken request);

    @Headers(NetworkConst.Headers.Token.FLAG)
    @POST("api/v2/notifications/unregister_android_device")
    Observable<Response<BaseResponse>> unregisterToken(@Body NotificationToken request);

}
