package com.siaray.downloadmanagerplussample;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.siaray.downloadmanagerplus.classes.Downloader;
import com.siaray.downloadmanagerplus.enums.DownloadStatus;
import com.siaray.downloadmanagerplus.interfaces.DownloadListener;
import com.siaray.downloadmanagerplus.utils.Log;

import java.io.File;

public class NormalActivity extends AppCompatActivity {

    private FileItem item;
    private ImageView ivAction;
    private NumberProgressBar numberProgressBar;
    private ViewGroup btnDelete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);
        initUi();
    }

    private void initUi() {
        ivAction = (ImageView) findViewById(R.id.iv_image);
        ViewGroup btnAction = (ViewGroup) findViewById(R.id.btn_action);
        btnDelete = (ViewGroup) findViewById(R.id.btn_delete);
        TextView tvName = (TextView) findViewById(R.id.tv_name);
        numberProgressBar = (NumberProgressBar) findViewById(R.id.progressbar);
        item = Utils.getDownloadItem();
        tvName.setText(Utils.getFileName(item.getLink()));

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("start");

                Downloader downloader = new Downloader(NormalActivity.this, MainActivity.downloadManager, item.getLink())
                        .setListener(listener)
                        .saveDownloadHistory(item.getId())
                        .setDestinationDir(Environment.DIRECTORY_DOWNLOADS
                                , Utils.getFileName(item.getLink()))
                        .setNotificationTitle(Utils.getFileName(item.getLink()));
                if (downloader.getStatus(item.getId()) == DownloadStatus.RUNNING)
                    downloader.cancel(item.getId());
                else if (downloader.getStatus(item.getId()) == DownloadStatus.SUCCESSFUL) {
                    com.siaray.downloadmanagerplus.utils.Utils
                            .openFile(NormalActivity.this, Environment.getExternalStorageDirectory()
                                    + File.separator + downloader.getDownloadedFilePath(item.getId()));
                } else
                    downloader.start();
            }
        });
        showPercent();

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Downloader downloader = new Downloader(NormalActivity.this, MainActivity.downloadManager, item.getLink())
                        .setListener(listener)
                        .saveDownloadHistory(item.getId())
                        .setDestinationDir(Environment.DIRECTORY_DOWNLOADS
                                , Utils.getFileName(item.getLink()))
                        .setNotificationTitle(Utils.getFileName(item.getLink()));
                //if (downloader.getStatus(item.getId()) == DownloadStatus.RUNNING)
                downloader.deleteFile(item.getId());
            }
        });
    }

    private void showPercent() {
        new Downloader(NormalActivity.this, MainActivity.downloadManager, item.getLink())
                .setListener(listener)
                .saveDownloadHistory(item.getId())
                .setDestinationDir(Environment.DIRECTORY_DOWNLOADS
                        , Utils.getFileName(item.getLink()))
                .setNotificationTitle(Utils.getFileName(item.getLink()))
                .showProgress();
    }

    DownloadListener listener = new DownloadListener() {
        @Override
        public void OnComplete(String msg) {
            ivAction.setImageResource(R.mipmap.ic_complete);
            numberProgressBar.setProgress(100);
        }

        @Override
        public void OnPause(String msg, String reason) {

        }

        @Override
        public void OnPending(String msg) {

        }

        @Override
        public void OnFail(String msg, String reason) {
            ivAction.setImageResource(R.mipmap.ic_start);
            numberProgressBar.setProgress(0);

        }

        @Override
        public void OnCancel(String msg) {
            ivAction.setImageResource(R.mipmap.ic_start);
            numberProgressBar.setProgress(0);

        }

        @Override
        public void OnRunning(int percent, int mTotalBytes, int mDownloadedBytes) {
            ivAction.setImageResource(R.mipmap.ic_cancel);
            numberProgressBar.setProgress(percent);
        }

        @Override
        public void OnMessage(String msg) {
            ivAction.setImageResource(R.mipmap.ic_start);
            Toast.makeText(NormalActivity.this, "" + msg, Toast.LENGTH_SHORT).show();
            numberProgressBar.setProgress(0);
        }
    };
}
