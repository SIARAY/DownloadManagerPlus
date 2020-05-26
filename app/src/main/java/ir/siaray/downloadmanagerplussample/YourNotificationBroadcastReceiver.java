package ir.siaray.downloadmanagerplussample;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import ir.siaray.downloadmanagerplus.classes.Downloader;
import ir.siaray.downloadmanagerplus.model.DownloadItem;
import ir.siaray.downloadmanagerplus.receivers.NotificationBroadcastReceiver;
import ir.siaray.downloadmanagerplus.utils.Log;

/**
 * Created by Siamak on 12/05/2017.
 */

public class YourNotificationBroadcastReceiver extends NotificationBroadcastReceiver {

    @Override
    public void onCompleted(Context context, Intent intent, long downloadId) {
        super.onCompleted(context, intent, downloadId);
       Log.i("Complete downloaddddd id: "+downloadId);
        printDownloadedFileId(context, intent);
    }

    @Override
    public void onClicked(Context context, Intent intent, long[] downloadIdList) {
        super.onClicked(context, intent, downloadIdList);
        Toast.makeText(context, "Download Notification Clicked.", Toast.LENGTH_SHORT).show();
        printDownloadingFilesId(context, intent);
        openCustomActivity(context);
    }

    @Override
    public void onFailed(Context context, Intent intent, long downloadId) {
        super.onFailed(context, intent, downloadId);
        //You cant get download item here because android download manager removed it data.
        Toast.makeText(context, "Download failed.", Toast.LENGTH_SHORT).show();
        Log.i("Download failed ,id: " + downloadId);
        if(downloadId>0){
            //for getting token by id here, you must setKeptAllDownload(true)
            String token=Downloader.getToken(context,downloadId);
            Log.i("Download failed ,token : " + token);
            //Do not use the following method because it returns the null value.
            //DownloadItem downloadItem=Downloader.getDownloadItem(context,token);
        }
    }

    private void printDownloadedFileId(Context context, Intent intent) {
        long downloadId = intent.getLongExtra(
                DownloadManager.EXTRA_DOWNLOAD_ID, 0);
        String id = Downloader.getToken(context, downloadId);
        DownloadItem downloadItem = Downloader.getDownloadItem(context, id);
        if (downloadItem != null && downloadItem.getPercent() == 100) {
            Log.i("Download Completed. Download id: " + downloadId);
            Log.i("Download plus id: " + id);
            Log.i("Download id: " + Downloader.getDownloadId(context, Downloader.getToken(context, downloadId)));
            Log.i("Download uri: " +downloadItem.getLocalUri());
            Log.i("Download path: " +downloadItem.getFilePath());
            Toast.makeText(context, "Download Completed.", Toast.LENGTH_SHORT).show();
        } else {
            Log.i("Download Failed. Download id: " + downloadId);
            Toast.makeText(context, "Download Failed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void openCustomActivity(Context context) {
        Intent dm = new Intent(context, NormalActivity.class);
        dm.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        dm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(dm);
    }

    private void openDownloadedFilesActivity(Context context) {
        Intent dm = new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS);
        dm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(dm);
    }

    private void printDownloadingFilesId(Context context, Intent intent) {
        long[] downloadId = intent.getLongArrayExtra(
                DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
        for (long id : downloadId) {
            Log.i("Download id: " + id);
            Log.i("Download plus id: " + Downloader.getToken(context, id));
            Log.i("Download id: " + Downloader.getDownloadId(context, Downloader.getToken(context, id)));
        }
    }

}