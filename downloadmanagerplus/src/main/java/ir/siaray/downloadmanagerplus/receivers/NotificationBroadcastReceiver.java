package ir.siaray.downloadmanagerplus.receivers;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import ir.siaray.downloadmanagerplus.classes.Downloader;
import ir.siaray.downloadmanagerplus.interfaces.DownloadNotificationListener;
import ir.siaray.downloadmanagerplus.model.DownloadItem;

/**
 * Created by SIARAY on 12/05/2017.
 */

public class NotificationBroadcastReceiver extends BroadcastReceiver implements DownloadNotificationListener {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, 0);
            String id = Downloader.getToken(context, downloadId);
            DownloadItem downloadItem = Downloader.getDownloadItem(context, id);
            if (downloadItem != null && downloadItem.getPercent() == 100) {
                this.onCompleted(context, intent, downloadId);
            } else {
                this.onFailed(context, intent, downloadId);
            }
        } else if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
            long[] downloadIdList = intent
                    .getLongArrayExtra(DownloadManager
                            .EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
            this.onClicked(context, intent, downloadIdList);
        }
    }

    @Override
    public void onCompleted(Context context, Intent intent, long downloadId) {
    }

    @Override
    public void onFailed(Context context, Intent intent, long downloadId) {
    }

    @Override
    public void onClicked(Context context, Intent intent, long[] downloadIdList) {
    }
}