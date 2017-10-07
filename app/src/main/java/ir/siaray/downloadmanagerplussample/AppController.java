package ir.siaray.downloadmanagerplussample;

import android.app.Application;

/**
 * Created by SIARAY on 17/07/2017.
 */

public class AppController extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        //Downloader.initialize(getApplicationContext(), new YourNotificationBroadcastReceiver());
    }

}
