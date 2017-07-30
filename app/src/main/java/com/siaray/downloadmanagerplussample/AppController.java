package com.siaray.downloadmanagerplussample;

import android.app.Application;
import android.app.DownloadManager;
import android.content.Context;

/**
 * Created by Siamak on 17/07/2017.
 */

public class AppController extends Application {

    public static DownloadManager downloadManager;

    @Override
    public void onCreate() {
        super.onCreate();
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

    }
}
