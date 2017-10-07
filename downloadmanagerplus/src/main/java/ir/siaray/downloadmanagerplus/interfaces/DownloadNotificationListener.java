package ir.siaray.downloadmanagerplus.interfaces;

import android.content.Context;
import android.content.Intent;

/**
 * Created by SIARAY on 10/1/2017.
 */

public interface DownloadNotificationListener {
    void onCompleted(Context context, Intent intent, long downloadId);

    void onFailed(Context context, Intent intent, long downloadId);

    void onClicked(Context context, Intent intent, long[] downloadIdList);
}
