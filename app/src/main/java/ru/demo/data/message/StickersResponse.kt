package ru.demo.data.message

import com.google.gson.annotations.SerializedName
import ru.demo.data.network.BaseResponse

class StickersResponse(
        @SerializedName("stickers")
        val stickers: List<Sticker>
) : BaseResponse()