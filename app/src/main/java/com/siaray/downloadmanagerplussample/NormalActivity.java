package com.siaray.downloadmanagerplussample;

import android.app.DownloadManager;
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
import com.siaray.downloadmanagerplus.enums.DownloadReason;
import com.siaray.downloadmanagerplus.enums.DownloadStatus;
import com.siaray.downloadmanagerplus.enums.Errors;
import com.siaray.downloadmanagerplus.interfaces.ActionListener;
import com.siaray.downloadmanagerplus.interfaces.DownloadListener;
import com.siaray.downloadmanagerplus.utils.Log;
import com.siaray.downloadmanagerplus.utils.Utils;

public class NormalActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);
        inflateUi();
    }

    private Downloader getDownloader(FileItem item, DownloadListener listener) {
        return new Downloader(NormalActivity.this, MainActivity.downloadManager)
                .setListener(listener)
                .setUrl(item.getUri())
                .setId(item.getId())
                .setDescription("description")
                .setScanningByMediaScanner(true)
                .setNotificationVisibility(DownloadManager
                        .Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setDestinationDir(Environment.DIRECTORY_DOWNLOADS
                        , Utils.getFileName(item.getUri()))
                .setNotificationTitle(getFileShortName(Utils.getFileName(item.getUri())));
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

        tvName.setText(Utils.getFileName(item.getUri()));

        final ActionListener deleteListener = getDeleteListener(ivAction, btnAction, numberProgressBar);
        //final DownloadListener listener = getDownloadListener(ivAction, numberProgressBar);
        item.setListener(getDownloadListener(ivAction, numberProgressBar));
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Downloader downloader = getDownloader(item, item.getListener()/*listener*/);
                if (downloader.getStatus(item.getId()) == DownloadStatus.RUNNING
                        || downloader.getStatus(item.getId()) == DownloadStatus.PAUSED
                        || downloader.getStatus(item.getId()) == DownloadStatus.PENDING)
                    downloader.cancel(item.getId());
                else if (downloader.getStatus(item.getId()) == DownloadStatus.SUCCESSFUL) {
                    Utils.openFile(NormalActivity.this, downloader.getDownloadedFilePath(item.getId()));
                } else
                    downloader.start();
            }
        });
        showPercent(item, item.getListener());

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Downloader downloader = new Downloader(NormalActivity.this, MainActivity.downloadManager, item.getUri())
                        .setListener(item.getListener());

                downloader.deleteFile(item.getId(), deleteListener);
            }
        });


    }

    private ActionListener getDeleteListener(final ImageView ivAction, final ViewGroup btnDelete, final NumberProgressBar numberProgressBar) {
        return new ActionListener() {
            @Override
            public void onSuccess() {
                ivAction.setImageResource(R.mipmap.ic_start);
                numberProgressBar.setProgress(0);
                Toast.makeText(NormalActivity.this, "Deleted" , Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Errors error) {
                Toast.makeText(NormalActivity.this, "" + error, Toast.LENGTH_SHORT).show();

            }
        };
    }

    private DownloadListener getDownloadListener(final ImageView ivAction
            , final NumberProgressBar numberProgressBar) {
        return new DownloadListener() {
            DownloadStatus lastStatus = DownloadStatus.NONE;

            @Override
            public void onComplete() {
                Log.i("onComplete");
                ivAction.setImageResource(R.mipmap.ic_complete);
                numberProgressBar.setProgress(100);
                lastStatus = DownloadStatus.SUCCESSFUL;
            }

            @Override
            public void onPause(int percent, DownloadReason reason) {
                if (lastStatus != DownloadStatus.PAUSED) {
                    Log.i("onPause - percent: " + percent
                            + " lastStatus:" + lastStatus
                            + " reason:" + reason);
                    ivAction.setImageResource(R.mipmap.ic_cancel);
                    numberProgressBar.setProgress(percent);
                }
                lastStatus = DownloadStatus.PAUSED;
            }

            @Override
            public void onPending(int percent) {
                if (lastStatus != DownloadStatus.PENDING) {
                    Log.i("onPending - lastStatus:" + lastStatus);
                    ivAction.setImageResource(R.mipmap.ic_cancel);
                    numberProgressBar.setProgress(percent);
                }
                lastStatus = DownloadStatus.PENDING;
            }

            @Override
            public void onFail(int percent, DownloadReason reason) {
                Log.i("onFail - percent: " + percent
                        + " lastStatus:" + lastStatus
                        + " reason:" + reason);
                ivAction.setImageResource(R.mipmap.ic_start);
                numberProgressBar.setProgress(0);
                lastStatus = DownloadStatus.FAILED;

            }

            @Override
            public void onCancel() {
                Log.i("onCancel");
                ivAction.setImageResource(R.mipmap.ic_start);
                numberProgressBar.setProgress(0);
                lastStatus = DownloadStatus.CANCELED;
            }

            @Override
            public void onRunning(int percent, int mTotalBytes, int mDownloadedBytes) {
                ivAction.setImageResource(R.mipmap.ic_cancel);
                numberProgressBar.setProgress(percent);
                lastStatus = DownloadStatus.RUNNING;
            }

            /*@Override
            public void onMessage(Result result, String msg) {
                if (result == Result.ERROR) {
                    ivAction.setImageResource(R.mipmap.ic_start);
                    numberProgressBar.setProgress(0);
                }
                Toast.makeText(NormalActivity.this, "" + msg, Toast.LENGTH_SHORT).show();
            }*/
        };
    }

    private void showPercent(FileItem item, DownloadListener listener) {
        getDownloader(item, listener).showProgress();
    }

    private String getFileShortName(String name) {
        if (name.length() > 10) {
            name = name.substring(0, 5) + ".." + name.substring(name.length() - 4, name.length());
        }
        return name;
    }

}
