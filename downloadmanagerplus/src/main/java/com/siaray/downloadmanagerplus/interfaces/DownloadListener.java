package com.siaray.downloadmanagerplus.interfaces;

import com.siaray.downloadmanagerplus.enums.Result;

/**
 * Created by Siamak on 16/01/2017.
 */

public interface DownloadListener {

    void onComplete(String msg);

    void onPause(String msg, String reason);

    void onPending(String msg);

    void onFail(String msg, String reason);

    void onCancel(String msg);

    void onRunning(int percent, int mTotalBytes, int mDownloadedBytes);

    void onMessage(Result results, String msg);

}
