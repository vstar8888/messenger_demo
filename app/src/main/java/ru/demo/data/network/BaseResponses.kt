package ru.demo.data.network

import android.text.TextUtils
import com.google.gson.annotations.SerializedName

open class BaseResponse {
    @SerializedName("status")
    lateinit var status: String
        internal set
    @SerializedName("error")
    lateinit var error: String
        internal set

    @SerializedName("error_subtype")
    var errorSubtype: String? = null
        internal set

    @SerializedName("error_messages")
    lateinit var messages: HashMap<String, String>
        internal set

    val isSuccessful
        get() = "ok".equals(status, true)
}

open class PaginatedResponse : BaseResponse() {
    var load_more_url: String? = null
        internal set

    val hasMore
        get() = TextUtils.isEmpty(load_more_url).not()
}