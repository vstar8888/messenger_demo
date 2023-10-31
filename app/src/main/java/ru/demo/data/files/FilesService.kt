package ru.demo.data.files

import okhttp3.MultipartBody
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import ru.demo.messenger.network.NetworkConst
import rx.Single

interface FilesService {

    @Headers(NetworkConst.Headers.Token.FLAG)
    @Multipart
    @POST("api/v2/files/upload_file")
    fun uploadFile(@Part file: MultipartBody.Part): Single<FileResponse>

}