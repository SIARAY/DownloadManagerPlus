package com.siaray.downloadmanagerplussample;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.siaray.downloadmanagerplus.classes.Downloader;
import com.siaray.downloadmanagerplus.enums.DownloadStatus;
import com.siaray.downloadmanagerplus.enums.Result;
import com.siaray.downloadmanagerplus.interfaces.ActionListener;
import com.siaray.downloadmanagerplus.interfaces.DownloadListener;
import com.siaray.downloadmanagerplus.utils.Utils;

public class NormalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);
        //initUi();
        inflateUi();
    }

    private void inflateUi() {
        LinearLayout parent = (LinearLayout) findViewById(R.id.main_container);
        View fView = getLayoutInflater().inflate(R.layout.download_list_item, null);
        parent.addView(fView);
        FileItem fItem = SampleUtils.getDownloadItem(1);
        initUi(fView, fItem);

        View sView = getLayoutInflater().inflate(R.layout.download_list_item, null);
        parent.addView(sView);
        FileItem sItem = SampleUtils.getDownloadItem(2);
        initUi(sView, sItem);

        View tView = getLayoutInflater().inflate(R.layout.download_list_item, null);
        parent.addView(tView);
        FileItem tItem = SampleUtils.getDownloadItem(3);
        initUi(tView, tItem);
    }

    private void initUi(View view, final FileItem item) {
        final ImageView ivAction = (ImageView) view.findViewById(R.id.iv_image);
        final ViewGroup btnAction = (ViewGroup) view.findViewById(R.id.btn_action);
        final ViewGroup btnDelete = (ViewGroup) view.findViewById(R.id.btn_delete);
        TextView tvName = (TextView) view.findViewById(R.id.tv_name);
        final NumberProgressBar numberProgressBar = (NumberProgressBar) view.findViewById(R.id.progressbar);

        final DownloadListener listener = getDownloadListener(ivAction, numberProgressBar);
        final ActionListener deleteListener = getDeleteListener(ivAction, btnAction, numberProgressBar);

        tvName.setText(Utils.getFileName(item.getLink()));

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Downloader downloader = new Downloader(NormalActivity.this, MainActivity.downloadManager)
                        .setListener(listener)
                        .setUrl(item.getLink())
                        .setDownloadId(item.getId())
                        .setDestinationDir(Environment.DIRECTORY_DOWNLOADS
                                , Utils.getFileName(item.getLink()))
                        .setNotificationTitle(Utils.getFileName(item.getLink()));
                if (downloader.getStatus(item.getId()) == DownloadStatus.RUNNING
                        ||downloader.getStatus(item.getId()) == DownloadStatus.PAUSED
                        ||downloader.getStatus(item.getId()) == DownloadStatus.PENDING)
                    downloader.cancel(item.getId());
                else if (downloader.getStatus(item.getId()) == DownloadStatus.SUCCESSFUL) {
                    Utils.openFile(NormalActivity.this, downloader.getDownloadedFilePath(item.getId()));
                } else
                    downloader.start();
            }
        });
        showPercent(item, listener);

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Downloader downloader = new Downloader(NormalActivity.this, MainActivity.downloadManager, item.getLink())
                        .setListener(listener);

                downloader.deleteFile(item.getId(), deleteListener);
            }
        });


    }

    private ActionListener getDeleteListener(final ImageView ivAction, final ViewGroup btnDelete, final NumberProgressBar numberProgressBar) {
        return new ActionListener() {
            @Override
            public void OnSuccess(String message) {
                ivAction.setImageResource(R.mipmap.ic_start);
                numberProgressBar.setProgress(0);
                Toast.makeText(NormalActivity.this, "" + message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void OnFailure(String message) {
                Toast.makeText(NormalActivity.this, "" + message, Toast.LENGTH_SHORT).show();

            }
        };
    }

    private DownloadListener getDownloadListener(final ImageView ivAction
            , final NumberProgressBar numberProgressBar) {
        return new DownloadListener() {
            @Override
            public void OnComplete(String msg) {
                //Log.i("OnComplete called: " + msg);
                ivAction.setImageResource(R.mipmap.ic_complete);
                numberProgressBar.setProgress(100);
            }

            @Override
            public void OnPause(String msg, String reason) {
                //Log.i("OnPause called: " + msg);

            }

            @Override
            public void OnPending(String msg) {
                //Log.i("OnPending called: " + msg);

            }

            @Override
            public void OnFail(String msg, String reason) {
                //Log.i("OnFail called: " + msg);
                ivAction.setImageResource(R.mipmap.ic_start);
                numberProgressBar.setProgress(0);

            }

            @Override
            public void OnCancel(String msg) {
                //Log.i("onCancel called: " + msg);
                ivAction.setImageResource(R.mipmap.ic_start);
                numberProgressBar.setProgress(0);
            }

            @Override
            public void OnRunning(int percent, int mTotalBytes, int mDownloadedBytes) {
                ivAction.setImageResource(R.mipmap.ic_cancel);
                numberProgressBar.setProgress(percent);
            }

            @Override
            public void OnMessage(Result result, String msg) {
                if (result == Result.ERROR) {
                    ivAction.setImageResource(R.mipmap.ic_start);
                    numberProgressBar.setProgress(0);
                }
                Toast.makeText(NormalActivity.this, "" + msg, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void showPercent(FileItem item, DownloadListener listener) {
        new Downloader(NormalActivity.this, MainActivity.downloadManager, item.getLink())
                .setListener(listener)
                .setDownloadId(item.getId())
                .setDestinationDir(Environment.DIRECTORY_DOWNLOADS
                        , Utils.getFileName(item.getLink()))
                .setNotificationTitle(Utils.getFileName(item.getLink()))
                .showProgress();
    }


}
