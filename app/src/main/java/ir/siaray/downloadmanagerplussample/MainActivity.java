package ir.siaray.downloadmanagerplussample;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.List;

import ir.siaray.downloadmanagerplus.classes.Downloader;
import ir.siaray.downloadmanagerplus.model.DownloadItem;
import ir.siaray.downloadmanagerplus.utils.Log;
import ir.siaray.downloadmanagerplus.utils.Utils;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private Downloader testDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUi();
    }

    private void initUi() {
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
                List<DownloadItem> list = Downloader.getDownloadsList(getApplicationContext());
                if(list.size()>0) {
                    Utils.openFile(MainActivity.this, list.get(0).getLocalUri());
                    Toast.makeText(this, Log.printItems(list.get(0)), Toast.LENGTH_LONG).show();
                }else{
                    Toast.makeText(this, "Download list is empty.", Toast.LENGTH_LONG).show();
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
    private void showStatus(){
        Log.i("statusssss: "+testDownload.getStatus("https://wallpaperbrowse.com/media/images/best-hd-wallpaper-download-12.jpg"));
    }

}
