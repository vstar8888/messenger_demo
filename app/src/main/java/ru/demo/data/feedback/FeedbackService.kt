package ru.demo.data.feedback

import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query
import ru.demo.data.network.BaseResponse
import ru.demo.messenger.network.NetworkConst
import rx.Single

interface FeedbackService {

    @Headers(NetworkConst.Headers.Token.FLAG)
    @POST("net_home/api/v2/tools/send_to_support")
    fun sendFeedback(@Query("application_name") appName: String,
                     @Query("company_title") companyTitle: String?,
                     @Query("email") email: String?,
                     @Query("phone_number") phoneNumber: String?,
                     @Query("text") text: String): Single<BaseResponse>
}