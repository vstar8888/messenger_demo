package ru.demo.data.network

import android.text.TextUtils
import java.util.*

class ServerError(val errorType: String,
                  val errorSubType: String?,
                  private val messages: Map<String, String>
) : RuntimeException() {

    companion object {
        private const val DEFAULT_KEY = "default"
    }

    constructor(errorType: String, defaultMessage: String) : this(
            errorType,
            null,
            Collections.singletonMap(DEFAULT_KEY, defaultMessage)
    )

    override val message: String?
        get() {
            val defaultMessage = getMessage(DEFAULT_KEY)
            if (!TextUtils.isEmpty(defaultMessage)) {
                return defaultMessage
            } else if (messages.size == 1) {
                for ((_, value) in messages) {
                    return value
                }
            }
            return null
        }

    fun getMessage(key: String): String? {
        return messages[key]
    }

}