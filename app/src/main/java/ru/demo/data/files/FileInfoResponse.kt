package ru.demo.data.files

import com.google.gson.annotations.SerializedName
import ru.demo.data.network.BaseResponse

class FileResponse : BaseResponse() {

    @SerializedName("file_info")
    lateinit var fileInfo: FileInfoResponse

    class FileInfoResponse(
            @SerializedName("id")
            val id: String,
            @SerializedName("title")
            val title: String,
            @SerializedName("extension")
            val extension: String,
            @SerializedName("size")
            val size: Long
    )

}