package ir.siaray.downloadmanagerplus.enums;


import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.annotation.StringDef;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public abstract class Storage {
    @Retention(SOURCE)
    @StringDef(value = {DIRECTORY_ALARMS
            , DIRECTORY_AUDIOBOOKS
            , DIRECTORY_DCIM
            , DIRECTORY_DOCUMENTS
            , DIRECTORY_DOWNLOADS
            , DIRECTORY_MOVIES
            , DIRECTORY_MUSIC
            , DIRECTORY_NOTIFICATIONS
            , DIRECTORY_PICTURES
            , DIRECTORY_PODCASTS
            , DIRECTORY_RINGTONES
            , DIRECTORY_SCREENSHOTS
    })
    public @interface DownloadDirectory {
    }

    public static final String DIRECTORY_ALARMS = "Alarms";
    @RequiresApi(Build.VERSION_CODES.Q)
    public static final String DIRECTORY_AUDIOBOOKS = "Audiobooks";
    public static final String DIRECTORY_DCIM = "DCIM";
    @RequiresApi(Build.VERSION_CODES.Q)
    public static final String DIRECTORY_DOCUMENTS = "Documents";
    public static final String DIRECTORY_DOWNLOADS = "Download";
    public static final String DIRECTORY_MOVIES = "Movies";
    public static final String DIRECTORY_MUSIC = "Music";
    public static final String DIRECTORY_NOTIFICATIONS = "Notifications";
    public static final String DIRECTORY_PICTURES = "Pictures";
    public static final String DIRECTORY_PODCASTS = "Podcasts";
    public static final String DIRECTORY_RINGTONES = "Ringtones";
    @RequiresApi(Build.VERSION_CODES.Q)
    public static final String DIRECTORY_SCREENSHOTS = "Screenshots";

    public abstract void setDownloadDirectory(@DownloadDirectory String directory);

    @DownloadDirectory
    public abstract String getDownloadDirectory();

    public static boolean isValidDownloadDirectory(String dir) {
        switch (dir) {
            case DIRECTORY_ALARMS:
            case DIRECTORY_AUDIOBOOKS:
            case DIRECTORY_DCIM:
            case DIRECTORY_DOCUMENTS:
            case DIRECTORY_DOWNLOADS:
            case DIRECTORY_MOVIES:
            case DIRECTORY_MUSIC:
            case DIRECTORY_NOTIFICATIONS:
            case DIRECTORY_PICTURES:
            case DIRECTORY_PODCASTS:
            case DIRECTORY_RINGTONES:
            case DIRECTORY_SCREENSHOTS:
                return true;
            default:
                return false;
        }
    }
}
