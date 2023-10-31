package ru.demo.messenger.network

import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import ru.demo.messenger.Consts
import ru.demo.messenger.utils.Prefs

class RequestTokenInterceptor : Interceptor {

    private var previouslyUsedAuthToken: String? = null

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        //val checkAddressResult = checkNetAddress(request.url().toString())
        val checkAddressResult = checkNetAddress(request.url.toString())


        if (request.header(NetworkConst.Headers.Token.KEY) == null && !checkAddressResult) {
            return chain.proceed(request)
        }

        val limitedToken = Prefs.get().getString(Consts.Prefs.LIMITED_TOKEN, null)
        val accessToken = Prefs.get().getString(Consts.Prefs.AUTH_TOKEN, null)

        previouslyUsedAuthToken = initPreviouslyUsedAuthToken(accessToken)

        val token = checkWhichToUse(accessToken, limitedToken)
        val newRequest = request.newBuilder()

        if (checkAddressResult) {
            newRequest
                    .removeHeader(NetworkConst.Headers.Token.KEY)
                    .addHeader(NetworkConst.Headers.Auth.KEY, "OAuth $token")
        } else {
            newRequest
                    .removeHeader(NetworkConst.Headers.Token.KEY)
                    .removeHeader(NetworkConst.Headers.Auth.KEY)
        }

        return chain.proceed(newRequest.build())
    }

    private fun checkNetAddress(url: String): Boolean {
        val baseUrl = NetworkConst.getBaseUrl()
        baseUrl?.let {
            return it in url
        }
        return true
    }

    private fun checkWhichToUse(authToken: String?, limitedToken: String?): String? {
        return if (limitedToken != null) {
            if (authToken == previouslyUsedAuthToken || authToken == null) {
                limitedToken
            } else {
                Prefs.get().edit().putString(Consts.Prefs.LIMITED_TOKEN, null)
                authToken
            }
        } else {
            authToken
        }
    }

    private fun initPreviouslyUsedAuthToken(authToken: String?): String? {
        return if (previouslyUsedAuthToken == null && authToken != null) {
            authToken
        } else {
            " "
        }
    }

}