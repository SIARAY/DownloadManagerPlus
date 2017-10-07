package ir.siaray.downloadmanagerplussample;


import ir.siaray.downloadmanagerplus.interfaces.DownloadListener;
import ir.siaray.downloadmanagerplus.model.DownloadItem;

/**
 * Created by Siamak on 07/01/2017.
 */
public class FileItem extends DownloadItem {

    private String reasonMessage;
    private DownloadListener listener;
    private int fileSize = -1;

    public FileItem() {
        super();
    }

    public FileItem(DownloadItem downloadItem) {
        super(downloadItem);
    }

    public String getReasonMessage() {
        return reasonMessage;
    }

    public void setReasonMessage(String reasonMessage) {
        this.reasonMessage = reasonMessage;
    }

    public DownloadListener getListener() {
        return listener;
    }

    public void setListener(DownloadListener listener) {
        this.listener = listener;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }
}
