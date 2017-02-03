package com.siaray.downloadmanagerplussample;


import com.siaray.downloadmanagerplus.enums.DownloadStatus;
import com.siaray.downloadmanagerplus.interfaces.DownloadListener;

/**
 * Created by Siamak on 07/01/2017.
 */
public class FileItem {

    private String id;
    private String link;
    private long downloadId;
    private DownloadStatus downloadStatus=DownloadStatus.NONE;
    private String reasonMessage;
    private int percent;
    private Thread thread;
private DownloadListener listener;

    public FileItem() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public long getDownloadId() {
        return downloadId;
    }

    public void setDownloadId(long downloadId) {
        this.downloadId = downloadId;
    }

    public DownloadStatus getDownloadStatus() {
        return downloadStatus;
    }

    public void setDownloadStatus(DownloadStatus downloadStatus) {
        this.downloadStatus = downloadStatus;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public String getReasonMessage() {
        return reasonMessage;
    }

    public void setReasonMessage(String reasonMessage) {
        this.reasonMessage = reasonMessage;
    }

    public void startDownload(Runnable runnable) {
        if (thread == null || !thread.isAlive()) {
            thread = new Thread(runnable);
            thread.start();
        }
    }

    public DownloadListener getListener() {
        return listener;
    }

    public void setListener(DownloadListener listener) {
        this.listener = listener;
    }
}
