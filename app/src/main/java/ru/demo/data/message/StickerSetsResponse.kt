package ru.demo.data.message

import com.google.gson.annotations.SerializedName
import ru.demo.data.network.BaseResponse

class StickerSetsResponse(
        @SerializedName("sticker_sets")
        val stickerSets: List<StickerSet>
) : BaseResponse()