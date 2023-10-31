package ru.demo.data.user

import android.content.ContentResolver
import android.content.Context
import android.database.ContentObserver
import android.database.Cursor
import android.net.Uri
import android.os.Handler

class SentSmsObserver(
        val context: Context,
        val handler: Handler,
        val smsListener: (sms: Sms) -> Unit
) {

    companion object {
        val SMS_URI = Uri.parse("content://sms")!!
    }

    private val contentResolver = context.contentResolver!!
    private val sentSmsExctractor = SentSmsExtractor(contentResolver, SMS_URI)

    private val observer: ContentObserver = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            sentSmsExctractor.getFromUri(uri)?.let { smsListener(it) }
        }
    }

    fun start() {
        contentResolver.registerContentObserver(SMS_URI, true, observer)
    }

    fun stop() {
        contentResolver.unregisterContentObserver(observer)
    }

}

class Sms(val phone: String)

class SentSmsExtractor(
        private val contentResolver: ContentResolver,
        private val defaultContainerUri: Uri
) {

    fun getFromUri(uri: Uri?): Sms? {
        val cursor = getCursor(uri) ?: return null
        cursor.use {
            it.moveToNext()
            val addressIndex = it.getColumnIndexOrThrow("address")
            val addressValue = it.getString(addressIndex)
            return Sms(addressValue)
        }
    }

    private fun getCursor(uri: Uri?): Cursor? {
        val correctUri = uri ?: defaultContainerUri
        return contentResolver.query(
                correctUri,
                null,
                null,
                null,
                null
        )
    }

}