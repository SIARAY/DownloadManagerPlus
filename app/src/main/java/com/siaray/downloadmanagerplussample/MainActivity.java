package com.siaray.downloadmanagerplussample;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;


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
}
