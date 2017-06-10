package com.siaray.downloadmanagerplus.interfaces;

import com.siaray.downloadmanagerplus.enums.DownloadReason;

/**
 * Created by Siamak on 16/01/2017.
 */

public interface DownloadListener {

    void onComplete();

    void onPause(int percent, DownloadReason reason);

    void onPending(int percent);

    void onFail(int percent, DownloadReason reason);

    void onCancel();

    void onRunning(int percent, int mTotalBytes, int mDownloadedBytes);

    //void onMessage(Result results, String msg);

}
