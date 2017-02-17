package com.siaray.downloadmanagerplus.enums;

/**
 * Created by Siamak on 15/01/2017.
 */

public enum DownloadStatus {
    SUCCESSFUL(-1),
    PAUSED(-2),
    PENDING(-3),
    FAILED(-4),
    CANCELED(-5),
    RUNNING(-6),
    NONE(-7);

    int value;
    DownloadStatus(int value) {
        this.value=value;
    }

    public int getValue() {
        return value;
    }
}
