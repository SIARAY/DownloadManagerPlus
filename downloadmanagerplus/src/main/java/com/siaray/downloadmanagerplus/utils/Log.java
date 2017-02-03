package com.siaray.downloadmanagerplus.utils;

/**
 * Created by Siamak on 15/12/2016.
 */

public class Log {

    public static <T> void i(T msg){
        android.util.Log.i("DownloadManagerPlus", "" +msg);
    }

}
