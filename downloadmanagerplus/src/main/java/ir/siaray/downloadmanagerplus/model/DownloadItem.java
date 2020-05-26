package ir.siaray.downloadmanagerplus.model;

import android.app.DownloadManager;
import android.net.Uri;

import ir.siaray.downloadmanagerplus.enums.DownloadStatus;
import ir.siaray.downloadmanagerplus.utils.Log;
import ir.siaray.downloadmanagerplus.utils.Utils;

/**
 * Created by Siamak on 01/06/2017.
 */

public class DownloadItem {

    private String token;
    private int percent;
    private long downloadId;
    private int downloadedBytes;
    private int totalBytes;
    private DownloadStatus downloadStatus = DownloadStatus.NONE;
    private int reason;
    private String localUri;
    private String localFilePath;
    private String description;
    private long lastTimeModified;
    private String mediaType;
    private String title;
    private String uri;

    public DownloadItem(DownloadItem downloadItem) {
        setToken(downloadItem.getToken());
        setTitle(downloadItem.getTitle());
        setMediaType(downloadItem.getMediaType());
        setLastTimeModified(downloadItem.getLastTimeModified());
        setDescription(downloadItem.getDescription());
        setDownloadedBytes(downloadItem.getDownloadedBytes());
        setDownloadId(downloadItem.getDownloadId());
        setDownloadStatus(downloadItem.getDownloadStatus());
        setLocalFilePath(downloadItem.getFilePath());
        setLocalUri(downloadItem.getLocalUri());
        setReason(downloadItem.getReason());
        setTotalBytes(downloadItem.getTotalBytes());
        setUri(downloadItem.getUri());
        setPercent(downloadItem.getPercent());
    }

    public DownloadItem() {

    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }

    public int getDownloadedBytes() {
        return downloadedBytes;
    }

    public void setDownloadedBytes(int downloadedBytes) {
        this.downloadedBytes = downloadedBytes;
    }

    public int getTotalBytes() {
        return totalBytes;
    }

    public void setTotalBytes(int totalBytes) {
        this.totalBytes = totalBytes;
    }

    public DownloadStatus getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public void setDownloadStatus(int downloadStatus) {

        switch (downloadStatus) {
            case DownloadManager.STATUS_FAILED:
                this.downloadStatus = DownloadStatus.FAILED;
                break;
            case DownloadManager.STATUS_PAUSED:
                this.downloadStatus = DownloadStatus.PAUSED;
                break;
            case DownloadManager.STATUS_PENDING:
                this.downloadStatus = DownloadStatus.PENDING;
                break;
            case DownloadManager.STATUS_RUNNING:
                this.downloadStatus = DownloadStatus.RUNNING;
                break;
            case DownloadManager.STATUS_SUCCESSFUL:
                this.downloadStatus = DownloadStatus.SUCCESSFUL;
                if (localUri != null && !Utils.isFileExist(localUri)) {
                    this.downloadStatus = DownloadStatus.CANCELED;
                }
                break;
            default:
                this.downloadStatus = DownloadStatus.NONE;
        }
    }

    public int getReason() {
        return reason;
    }

    public void setReason(int reason) {
        this.reason = reason;
    }

    public String getLocalUri() {
        return localUri;
    }

    public void setLocalUri(String localUri) {
        localFilePath = getFilePathFromUri(localUri);
        this.localUri = localUri;
        Log.i("### localFilePath: " + localFilePath);
        Log.i("### localUri: " + localUri);
    }

    private String getFilePathFromUri(String localUri) {
        if (localUri != null) {
            Uri fileUri = Uri.parse(localUri);
            return fileUri.getPath();
        }
        return null;
    }


    public String getFilePath() {
        return localFilePath;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getLastTimeModified() {
        return lastTimeModified;
    }

    public void setLastTimeModified(long lastTimeModified) {
        this.lastTimeModified = lastTimeModified;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }


}
