package ru.demo.domain.device

import rx.Single
import java.io.File

interface PhotoDataSource {

    fun makePhotoByCamera(): Single<File>

    fun pickPhotosFromGallery(isMultiplePhotoSelection: Boolean): Single<List<File>>

    fun loadPhotoFromGallery(): Single<List<String>>
}