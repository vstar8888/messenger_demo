package ru.demo.data.message

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Sticker(
        @SerializedName("id")
        val id: Long,
        @SerializedName("set_id")
        val setId: Long,
        @SerializedName("image_url")
        val imageUrl: String?,
        @SerializedName("image_url_large")
        val largeImageUrl: String?
) : Parcelable {

    constructor(parcel: Parcel) : this(
            parcel.readLong(),
            parcel.readLong(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(id)
        parcel.writeLong(setId)
        parcel.writeString(imageUrl)
        parcel.writeString(largeImageUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Sticker> {
        override fun createFromParcel(parcel: Parcel): Sticker {
            return Sticker(parcel)
        }

        override fun newArray(size: Int): Array<Sticker?> {
            return arrayOfNulls(size)
        }
    }

}
