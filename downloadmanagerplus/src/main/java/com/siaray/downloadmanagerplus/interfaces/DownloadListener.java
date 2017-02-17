package com.siaray.downloadmanagerplus.interfaces;

import com.siaray.downloadmanagerplus.enums.Result;

/**
 * Created by Siamak on 16/01/2017.
 */

public interface DownloadListener {

    void OnComplete(String msg);

    void OnPause(String msg, String reason);

    void OnPending(String msg);

    void OnFail(String msg, String reason);

    void OnCancel(String msg);

    void OnRunning(int percent, int mTotalBytes, int mDownloadedBytes);

    void OnMessage(Result results, String msg);

}
