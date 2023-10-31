package ru.demo.data.device

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import biz.growapp.base.helpers.MediaFilePicker
import permissions.dispatcher.NeedsPermission
import permissions.dispatcher.OnNeverAskAgain
import permissions.dispatcher.OnPermissionDenied
import permissions.dispatcher.RuntimePermissions
import ru.demo.domain.base.ApplicationException
import ru.demo.messenger.BuildConfig.APPLICATION_ID
import ru.demo.messenger.R
import java.io.File

@RuntimePermissions
class EmptyPhotoActivity : Activity(), MediaFilePicker.OnFilePickerListener {

    companion object {
        private const val ARG_PHOTO_PICKER = "photo_picker"

        private const val EXTRA_IMAGE_SOURCE =
                "$APPLICATION_ID.extra.IMAGE_SOURCE"

        fun makePhotoByCamera(context: Context): Intent =
                newIntent(context, ImageSource.CAMERA)

        fun makePhotoFromGallery(
                context: Context
        ): Intent = newIntent(context, ImageSource.GALLERY)

        private fun newIntent(
                context: Context,
                imageSource: ImageSource
        ): Intent = Intent(context, EmptyPhotoActivity::class.java).apply {
            putExtra(EXTRA_IMAGE_SOURCE, imageSource)
        }
    }

    private lateinit var mediaFilePicker: MediaFilePicker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mediaFilePicker = MediaFilePicker(
                this,
                this,
                savedInstanceState?.getBundle(ARG_PHOTO_PICKER)
        )
        mediaFilePicker.setPrivateDir(true)

        if (savedInstanceState == null) {
            val imageSource = intent.getSerializableExtra(EXTRA_IMAGE_SOURCE) as ImageSource
            when (imageSource) {
                ImageSource.CAMERA -> requestCameraIntent()
                ImageSource.GALLERY -> EmptyPhotoActivityPermissionsDispatcher.requestGalleryIntentWithCheck(this)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBundle(ARG_PHOTO_PICKER, mediaFilePicker.saveState())
        super.onSaveInstanceState(outState)
    }

    private fun requestCameraIntent() {
        mediaFilePicker.requestCameraIntent()
    }

    @NeedsPermission(READ_EXTERNAL_STORAGE)
    fun requestGalleryIntent() {
        mediaFilePicker.requestGalleryIntent()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mediaFilePicker.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_CANCELED) {
            PhotoPickerManager.cancel()
            finish()
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EmptyPhotoActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults)
    }

    // If need - refactor MediaFilePicker and pass multiple files
    override fun onFilePicked(file: File) {
        PhotoPickerManager.setImagePicked(listOf(file))
        finish()
    }

    @OnPermissionDenied(READ_EXTERNAL_STORAGE)
    fun onPermissionDeniedForPickPhotoFromGallery() {
        onError(PermissionDeniedException(getString(R.string.attach_photo_alert_permission_explanation_storage)))
    }


    @OnNeverAskAgain(READ_EXTERNAL_STORAGE)
    fun onNeverForPickPhotoFromGallery() {
        onError(NeverAskedAgainPermissionException(getString(R.string.attach_photo_alert_permission_explanation_storage)))
    }

    private fun onError(applicationException: ApplicationException) {
        PhotoPickerManager.setError(applicationException)
        finish()
    }

    private enum class ImageSource {
        CAMERA,
        GALLERY
    }

}