package ru.demo.data.network

import android.content.Context
import android.net.ConnectivityManager
import ru.demo.messenger.R
import rx.Completable
import rx.Single

object RestApi {

    private lateinit var connectivityManager: ConnectivityManager

    private lateinit var noConnectionMessage: String

    fun init(context: Context) {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        noConnectionMessage = context.getString(R.string.data_request_error_message)
    }

    fun <T> prepareRequest(request: Single<T>, validateResponse: Boolean = true): Single<T> {
        val checkedRequest = Completable.fromAction {
            val networkInfo = connectivityManager.activeNetworkInfo
                    ?: throw RuntimeException(noConnectionMessage)
            if (!networkInfo.isConnectedOrConnecting) {
                throw RuntimeException(noConnectionMessage)
            }
        }//-----|
                .andThen(request)
        return if (validateResponse) {
            checkedRequest.compose(validateResponse())
        } else {
            checkedRequest
        }
    }

    private val ERROR_VALIDATION: Single.Transformer<BaseResponse, Any> =
            Single.Transformer { responseSingle ->
                responseSingle.flatMap({ response ->
                    if (response.isSuccessful) {
                        Single.just(response)
                    } else {
                        Single.error(ServerError(response.error, response.errorSubtype, response.messages))
                    }
                })
            }

    @Suppress("UNCHECKED_CAST")
    private fun <T> validateResponse(): Single.Transformer<T, T> =
            ERROR_VALIDATION as Single.Transformer<T, T>

}