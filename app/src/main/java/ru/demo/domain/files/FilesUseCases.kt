package ru.demo.domain.files

import ru.demo.domain.base.SingleUseCase
import rx.Single

class UploadFiles(private val filesNetworkGateway: FilesNetworkGateway
) : SingleUseCase<List<FileInfo>, UploadFiles.Params>() {

    override fun execute(params: Params): Single<List<FileInfo>> {
        return filesNetworkGateway.uploadFiles(params.filePaths)
    }

    class Params(val filePaths: List<String>)

}