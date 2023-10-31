package ru.demo.messenger.network;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import androidx.core.content.FileProvider;
import androidx.appcompat.app.AlertDialog;

import java.io.File;

public class DownloadOnCompleted {
    public File file;
    private Activity a;
    public DownloadOnCompleted(Activity a) {
        this.a = a;
    }
    public void onCreate() {
        a.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
    public void onDestroy() {
        a.unregisterReceiver(onComplete);
    }
    private BroadcastReceiver onComplete = new BroadcastReceiver() {
        public void onReceive(Context c, Intent intent) {
        if (a == null) return;
        showDialog(a);
    }};
    private void showDialog(Context c) {
        new AlertDialog.Builder(c)
                .setMessage("Download completed! Open with?")
                .setPositiveButton(android.R.string.ok, this::openFileWith)
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
    private void openFileWith(@SuppressWarnings("unused") DialogInterface dialogInterface, @SuppressWarnings("unused") int i) {
        if (file == null)
            throw new IllegalArgumentException("file cant be null");
        Uri uri = FileProvider.getUriForFile(a, a.getApplicationContext().getPackageName() + ".provider", file);
        String mime = a.getContentResolver().getType(uri);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mime);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        a.startActivity(intent);
    }
}