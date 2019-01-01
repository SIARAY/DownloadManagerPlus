package ir.siaray.downloadmanagerplus.interfaces;

import ir.siaray.downloadmanagerplus.enums.DownloadReason;

/**
 * Created by Siamak on 16/01/2017.
 */

public interface DownloadListener {

    void onComplete(int totalBytes);

    void onPause(int percent, DownloadReason reason, int totalBytes, int downloadedBytes);

    void onPending(int percent, int totalBytes, int downloadedBytes);

    void onFail(int percent, DownloadReason reason, int totalBytes, int downloadedBytes);

    void onCancel(int totalBytes, int downloadedBytes);

    void onRunning(int percent, int totalBytes, int downloadedBytes, float downloadSpeed);

}
