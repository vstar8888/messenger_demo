package ru.demo.domain.device

import ru.demo.domain.base.SingleUseCase
import rx.Single
import java.io.File

class MakePhotoByCamera(
        private val photoDataSource: PhotoDataSource
) : SingleUseCase<File, Void?>() {

    override fun execute(params: Void?): Single<File> {
        return photoDataSource.makePhotoByCamera()
    }

}

class PickPhotosFromGallery(
        private val photoDataSource: PhotoDataSource
) : SingleUseCase<List<File>, PickPhotosFromGallery.Params?>() {

    override fun execute(params: Params?): Single<List<File>> {
        return photoDataSource.pickPhotosFromGallery(params?.isMultiplePhotoSelection ?: false)
    }

    class Params(val isMultiplePhotoSelection: Boolean)
}

class LoadPhotosFromGallery(
        private val photoDataSource: PhotoDataSource
) : SingleUseCase<List<String>, Void?>() {

    override fun execute(params: Void?): Single<List<String>> {
        return photoDataSource.loadPhotoFromGallery()
    }
}