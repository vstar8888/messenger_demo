package ru.demo.data.device

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.provider.MediaStore
import ru.demo.domain.device.PhotoDataSource
import ru.demo.messenger.R
import rx.Single
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class PhotoRepository(private val context: Context) : PhotoDataSource {

    override fun makePhotoByCamera(): Single<File> {
        return getPhotos(EmptyPhotoActivity.makePhotoByCamera(context))
                .map { it[0] }
    }

    override fun pickPhotosFromGallery(isMultiplePhotoSelection: Boolean): Single<List<File>> {
        return getPhotos(EmptyPhotoActivity.makePhotoFromGallery(context))
    }

    private fun getPhotos(intent: Intent): Single<List<File>> {
        PhotoPickerManager.subscribe()
        context.startActivity(intent)
        return Single.fromEmitter {
            val callback: PhotoPickerManager.Callback = object : PhotoPickerManager.Callback {
                override fun onImagesPicked(files: List<File>) {
                    it?.onSuccess(files)
                }

                override fun onError(t: Throwable?) {
                    it?.onError(t)
                }

                override fun onCancel() = Unit
            }
            it?.setCancellation(callback::onCancel)
            PhotoPickerManager.callback = callback
        }
    }

    override fun loadPhotoFromGallery(): Single<List<String>> {
        return Single.fromCallable {
            try {
                val images = getImagesPath(context)
                removeBrokenImages(images)
                return@fromCallable images
            } catch (e: SecurityException) {
                throw NeverAskedAgainPermissionException(context.getString(R.string.attach_photo_alert_permission_explanation_storage))
            }

        }
    }

    private fun getImagesPath(context: Context): MutableList<String> {
        val uri = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val listOfAllImages = ArrayList<String>()
        val cursor: Cursor?
        val column_index_data: Int
        var pathOfImage: String

        val projection = arrayOf(MediaStore.MediaColumns.DATA)

        cursor = context.contentResolver.query(uri, projection, null, null, null)

        if (cursor != null) {
            try {
                column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                while (cursor.moveToNext()) {
                    pathOfImage = cursor.getString(column_index_data)

                    if (!listOfAllImages.contains(pathOfImage)) {
                        listOfAllImages.add(0, pathOfImage)
                    }
                }
                return listOfAllImages
            } finally {
                cursor.close()
            }
        }
        return Collections.emptyList()
    }

    private fun removeBrokenImages(listImagesPath: MutableList<String>) {
        for (i in listImagesPath.size - 1 downTo 0) {
            val file = File(listImagesPath[i])
            if (file.length() == 0L) {
                listImagesPath.removeAt(i)
            }
        }
    }
}
