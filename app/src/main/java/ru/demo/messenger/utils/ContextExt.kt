@file:JvmName("ActionUtils")
package ru.demo.messenger.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog

import ru.demo.messenger.R

fun Context.showBrowserLink(url: String) {
    val intent = Intent(Intent.ACTION_VIEW)
    intent.data = Uri.parse(url)
    if (intent.resolveActivity(packageManager) == null) {
        AlertDialog.Builder(this)
                .setMessage(R.string.select_address_browser_not_exist)
                .setPositiveButton(R.string.dialog_ok, null)
                .create()
                .show()
    } else {
        startActivity(intent)
    }
}

fun Context.makeEmail(email: String, subject: String? = null, body: String? = null): Boolean {
    try {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        startActivity(intent)
        return true
    } catch (e: Exception) {
        return false
    }
}

fun Context.openAppSettings(): Boolean {
    val intent = Intent().apply {
        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        data = Uri.parse("package:$packageName")
    }
    return launchIfAvailable(this, packageManager, intent)
}

fun launchIfAvailable(context: Context, packageManager: PackageManager, intent: Intent): Boolean {
    if (intent.resolveActivity(packageManager) != null) {
        context.startActivity(intent)
        return true
    }
    return false
}
