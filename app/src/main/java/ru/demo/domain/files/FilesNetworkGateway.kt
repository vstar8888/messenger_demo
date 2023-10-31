package ru.demo.domain.files

import rx.Single

interface FilesNetworkGateway {

    fun uploadFiles(filePaths: List<String>): Single<List<FileInfo>>

}