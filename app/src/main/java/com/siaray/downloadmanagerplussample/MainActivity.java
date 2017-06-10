package com.siaray.downloadmanagerplussample;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.siaray.downloadmanagerplus.classes.Downloader;
import com.siaray.downloadmanagerplus.model.DownloadItem;
import com.siaray.downloadmanagerplus.utils.Utils;

import java.util.List;

import static com.siaray.downloadmanagerplus.utils.Log.printItems;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static DownloadManager downloadManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUi();
    }

    private void initUi() {
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Button btnDownload = (Button) findViewById(R.id.button1);
        Button btnList = (Button) findViewById(R.id.button2);
        Button btnShowDownloads = (Button) findViewById(R.id.button3);
        btnDownload.setOnClickListener(this);
        btnList.setOnClickListener(this);
        btnShowDownloads.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {

            case R.id.button1:
                intent = new Intent(MainActivity.this, NormalActivity.class);
                break;

            case R.id.button2:
                List<DownloadItem> list = Downloader.getDownloadsList(getApplicationContext()
                        ,downloadManager);
                if(list.size()>0) {
                    Utils.openFile(MainActivity.this, list.get(0).getLocalUri());
                    Toast.makeText(this, printItems(list.get(0)), Toast.LENGTH_LONG).show();
                }
                //notifyThis("Notification Title", "Notification Message");
                //intent = new Intent(MainActivity.this, ListActivity.class);
                break;

            case R.id.button3:
                intent = new Intent();
                intent.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    /*public void notifyThis(String title, String message) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(this);
        b.setAutoCancel(true)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("{your tiny message}")
                .setContentTitle(title)
                .setContentText(message)
                .setContentInfo("INFO");

        NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(1, b.build());
    }*/

}
