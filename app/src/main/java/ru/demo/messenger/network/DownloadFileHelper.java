package ru.demo.messenger.network;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

import ru.demo.messenger.Consts;
import ru.demo.messenger.R;
import ru.demo.messenger.utils.Prefs;
import rx.Completable;

public class DownloadFileHelper {

    private final DownloadManager downloadManager;
    private DownloadOnCompleted downloadOnComplete;//vs

    public DownloadFileHelper(Context context) {
        downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
    }

    public DownloadFileHelper(Context context, DownloadOnCompleted downloadOnComplete) {
        this(context);
        this.downloadOnComplete = downloadOnComplete;
    }

    public Completable downloadFile(Context context, String sourceUrl, String filename) {
        return Completable.fromAction(() -> {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(sourceUrl));

            final String limitedToken = Prefs.get().getString(Consts.Prefs.LIMITED_TOKEN, null);
            final String accessToken = Prefs.get().getString(Consts.Prefs.AUTH_TOKEN, null);
            final String token = Prefs.get().getBoolean(Consts.Prefs.IS_USER_LOGGED, false)
                    ? accessToken
                    : limitedToken;
            request.addRequestHeader(NetworkConst.Headers.Auth.KEY, NetworkConst.Headers.Auth.VALUE_PREFIX + token);
            request.setTitle(context.getString(R.string.file_download_download, filename));
            request.allowScanningByMediaScanner();
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, filename);

            if (downloadOnComplete != null) {
                downloadOnComplete.file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), filename);
            }

            downloadManager.enqueue(request);
        });
    }

}
