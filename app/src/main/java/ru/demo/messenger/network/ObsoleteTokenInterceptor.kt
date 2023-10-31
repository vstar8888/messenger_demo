package ru.demo.messenger.network

import okhttp3.Interceptor
import okhttp3.Response

class ObsoleteTokenInterceptor(private val openLogin: () -> Unit) : Interceptor {
    companion object {
        const val OBSOLETE_TOKEN_HEADER = "Wrong-token"
        const val VALUE_OBSOLETE = "obsolete"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (response.header(OBSOLETE_TOKEN_HEADER) == VALUE_OBSOLETE) {
            openLogin.invoke()
        }
        return response
    }
}