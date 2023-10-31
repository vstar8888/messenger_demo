package ru.demo.data.message

import com.google.gson.annotations.SerializedName

data class StickerSet(
        @SerializedName("id")
        val id: Long,
        @SerializedName("title")
        val title: String?,
        @SerializedName("stickers")
        val stickers: List<Sticker>
)