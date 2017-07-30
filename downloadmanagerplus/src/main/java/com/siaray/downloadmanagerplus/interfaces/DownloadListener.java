package com.siaray.downloadmanagerplus.interfaces;

import com.siaray.downloadmanagerplus.enums.DownloadReason;

/**
 * Created by Siamak on 16/01/2017.
 */

public interface DownloadListener {

    void onComplete(int mTotalBytes);

    void onPause(int percent, DownloadReason reason, int mTotalBytes, int mDownloadedBytes);

    void onPending(int percent, int mTotalBytes, int mDownloadedBytes);

    void onFail(int percent, DownloadReason reason, int mTotalBytes, int mDownloadedBytes);

    void onCancel(int mTotalBytes, int mDownloadedBytes);

    void onRunning(int percent, int mTotalBytes, int mDownloadedBytes);

}
