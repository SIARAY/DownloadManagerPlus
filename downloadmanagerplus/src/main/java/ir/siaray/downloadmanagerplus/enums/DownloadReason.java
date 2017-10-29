package ir.siaray.downloadmanagerplus.enums;

import android.app.DownloadManager;

/**
 * Created by SIARAY on 09/06/2017.
 */

public enum DownloadReason {

    //DownloadManager.STATUS_FAILED
    ERROR_UNKNOWN(DownloadManager.ERROR_UNKNOWN),
    ERROR_FILE_ERROR(DownloadManager.ERROR_FILE_ERROR),
    ERROR_UNHANDLED_HTTP_CODE(DownloadManager.ERROR_UNHANDLED_HTTP_CODE),
    ERROR_HTTP_DATA_ERROR(DownloadManager.ERROR_HTTP_DATA_ERROR),
    ERROR_TOO_MANY_REDIRECTS(DownloadManager.ERROR_TOO_MANY_REDIRECTS),
    ERROR_INSUFFICIENT_SPACE(DownloadManager.ERROR_INSUFFICIENT_SPACE),
    ERROR_DEVICE_NOT_FOUND(DownloadManager.ERROR_DEVICE_NOT_FOUND),
    ERROR_CANNOT_RESUME(DownloadManager.ERROR_CANNOT_RESUME),
    ERROR_FILE_ALREADY_EXISTS(DownloadManager.ERROR_FILE_ALREADY_EXISTS),
    //DownloadManager.STATUS_PAUSED
    PAUSED_WAITING_TO_RETRY(DownloadManager.PAUSED_WAITING_TO_RETRY),
    PAUSED_WAITING_FOR_NETWORK(DownloadManager.PAUSED_WAITING_FOR_NETWORK),
    PAUSED_QUEUED_FOR_WIFI(DownloadManager.PAUSED_QUEUED_FOR_WIFI),
    PAUSED_UNKNOWN(DownloadManager.PAUSED_UNKNOWN),
    //DownloadManagerPlus Error
    UNKNOWN(-1),
    URL_NOT_VALID(-2),
    ID_NOT_FOUND(-3),
    DOWNLOAD_IN_PROGRESS(-4),
    WRITE_EXTERNAL_STORAGE_PERMISSION_REQUIRED(-5),
    INTERNET_PERMISSION_REQUIRED(-6),
    DESTINATION_DIRECTORY_NOT_FOUND(-7);


    int value;

    DownloadReason(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
