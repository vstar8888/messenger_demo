package ru.demo.data.files

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import ru.demo.data.RxSchedulers
import ru.demo.data.network.RestApi
import ru.demo.domain.files.FileInfo
import ru.demo.domain.files.FilesNetworkGateway
import ru.demo.messenger.network.RequestManager
import rx.Observable
import rx.Single
import java.io.File

class FilesNetworkApi(
        private val rxSchedulers: RxSchedulers
) : FilesNetworkGateway {

    companion object {
        private val MEDIA_TYPE_FILE = "multipart/form-data".toMediaType()
        private const val MULTIPART_BODY_NAME = "attach"
    }

    private val filesService = RequestManager.createService(FilesService::class.java)

    override fun uploadFiles(filePaths: List<String>): Single<List<FileInfo>> {
        return Single.fromCallable {
            filePaths.map { File(it) }
                    .filter { it.exists() || it.length() > 0L }
        }//-----|
                .flatMapObservable { Observable.from(it) }
                .flatMapSingle {
                    val multipartBody = MultipartBody.Part.createFormData(
                            MULTIPART_BODY_NAME,
                            it.name,
                            RequestBody.create(MEDIA_TYPE_FILE, it))
                    val uploadFileRequest = filesService.uploadFile(multipartBody)
                    RestApi.prepareRequest(uploadFileRequest, false)
                }
                .map { it.fileInfo.toModel() }
                .toList()
                .toSingle()
                .subscribeOn(rxSchedulers.computation)
    }

}


fun FileResponse.FileInfoResponse.toModel(): FileInfo {
    return FileInfo(
            id = id,
            title = title,
            extension = extension,
            size = size
    )
}