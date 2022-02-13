package ir.siaray.downloadmanagerplussample;

import android.app.DownloadManager.Request;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.akexorcist.roundcornerprogressbar.indeterminate.IndeterminateRoundCornerProgressBar;
import com.pnikosis.materialishprogress.ProgressWheel;

import ir.siaray.downloadmanagerplus.classes.Downloader;
import ir.siaray.downloadmanagerplus.enums.DownloadReason;
import ir.siaray.downloadmanagerplus.enums.DownloadStatus;
import ir.siaray.downloadmanagerplus.enums.Errors;
import ir.siaray.downloadmanagerplus.interfaces.ActionListener;
import ir.siaray.downloadmanagerplus.interfaces.DownloadListener;
import ir.siaray.downloadmanagerplus.model.DownloadItem;
import ir.siaray.downloadmanagerplus.utils.Log;
import ir.siaray.downloadmanagerplus.utils.Utils;

import static ir.siaray.downloadmanagerplussample.SampleUtils.DOWNLOAD_DIRECTORY;
import static ir.siaray.downloadmanagerplussample.SampleUtils.NOTIFICATION_VISIBILITY;
import static ir.siaray.downloadmanagerplussample.SampleUtils.setDownloadBackgroundColor;
import static ir.siaray.downloadmanagerplussample.SampleUtils.showInfoDialog;
import static ir.siaray.downloadmanagerplussample.SampleUtils.showPopUpMenu;

public class NormalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal);
        inflateUi();
    }


    private void inflateUi() {
        ViewGroup itemContainer = findViewById(R.id.item_container);
        View fView = getLayoutInflater().inflate(R.layout.download_list_item, null);
        itemContainer.addView(fView);
        FileItem fItem = SampleUtils.getDownloadItem(1);
        SampleUtils.setFileSize(getApplicationContext(), fItem);
        initUi(fView, fItem);

        View sView = getLayoutInflater().inflate(R.layout.download_list_item, null);
        itemContainer.addView(sView);
        FileItem sItem = SampleUtils.getDownloadItem(2);
        SampleUtils.setFileSize(getApplicationContext(), sItem);
        initUi(sView, sItem);

        View tView = getLayoutInflater().inflate(R.layout.download_list_item, null);
        itemContainer.addView(tView);
        FileItem tItem = SampleUtils.getDownloadItem(3);
        SampleUtils.setFileSize(getApplicationContext(), tItem);
        initUi(tView, tItem);
    }

    private void initUi(View view, final FileItem item) {
        final ImageView ivAction = view.findViewById(R.id.iv_image);
        final ViewGroup btnAction = view.findViewById(R.id.btn_action);
        final ViewGroup btnDelete = view.findViewById(R.id.btn_delete);
        TextView tvName = view.findViewById(R.id.tv_name);
        TextView tvSize = view.findViewById(R.id.tv_size);
        TextView tvSpeed = view.findViewById(R.id.tv_speed);
        TextView tvPercent = view.findViewById(R.id.tv_percent);
        IndeterminateRoundCornerProgressBar loading = view.findViewById(R.id.loading);
        final RoundCornerProgressBar downloadProgressBar = view.findViewById(R.id.progressbar);

        tvName.setText(Utils.getFileName(item.getUri()));

        final ActionListener deleteListener = getDeleteListener(item
                , ivAction
                , btnAction
                , downloadProgressBar
                , loading
                , tvSize
                , tvSpeed
                , tvPercent);
        item.setListener(getDownloadListener(
                item
                , btnAction
                , ivAction
                , downloadProgressBar
                , loading
                , tvSize
                , tvSpeed
                , tvPercent));

        //Download Button
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickOnActionButton(item);
            }
        });

        //Showing progress for running downloads.
        showProgress(item, item.getListener());

        //Delete Button
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopUpMenu(NormalActivity.this, view, item, deleteListener);
            }
        });
    }

    private void clickOnActionButton(FileItem item) {
        if (!SampleUtils.isStoragePermissionGranted(this))
            return;
        final Downloader downloader = getDownloader(item, item.getListener());
        if (downloader.getStatus(item.getToken()) == DownloadStatus.RUNNING
                || downloader.getStatus(item.getToken()) == DownloadStatus.PAUSED
                || downloader.getStatus(item.getToken()) == DownloadStatus.PENDING) {
            if (downloader.getStatus(item.getToken()) == DownloadStatus.PENDING
                    || downloader.getDownloadedBytes() <= 0) {
                downloader.cancel(item.getToken());
            } else if (downloader.getStatus(item.getToken()) == DownloadStatus.PAUSED) {
                //int status = Downloader.resume(this, item.getToken());
                downloader.resume();
                Toast.makeText(this, "Resume clicked", Toast.LENGTH_SHORT).show();
            } else {
                //int status = Downloader.pause(this, item.getToken());
                downloader.pause();
                Toast.makeText(this, "Pause clicked", Toast.LENGTH_SHORT).show();
            }

        } else if (downloader.getStatus(item.getToken()) == DownloadStatus.SUCCESSFUL) {
            Utils.openFile(NormalActivity.this, downloader.getDownloadedFilePath(item.getToken()));
        } else {
            downloader.start();
        }
    }

    private Downloader getDownloader(FileItem item, DownloadListener listener) {
        Downloader request = Downloader.getInstance(this)
                .setListener(listener)
                .setUrl(item.getUri())
                .setToken(item.getToken())
                .setKeptAllDownload(false)//if true: canceled download token keep in database
                .setAllowedOverRoaming(true)
                .setVisibleInDownloadsUi(true)
                .setDescription(Utils.readableFileSize(item.getFileSize()))
                .setScanningByMediaScanner(true)
                .setNotificationVisibility(NOTIFICATION_VISIBILITY)
                .setAllowedNetworkTypes(Request.NETWORK_WIFI | Request.NETWORK_MOBILE)
                .addRequestHeader("YourHeader","YourValue")
                //.setCustomDestinationDir(DOWNLOAD_DIRECTORY, Utils.getFileName(item.getUri()))//TargetApi 28 and lower
                .setDestinationDir(DOWNLOAD_DIRECTORY, Utils.getFileName(item.getUri()))
                .setNotificationTitle(SampleUtils.getFileShortName(Utils.getFileName(item.getUri())));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            request.setAllowedOverMetered(true); //Api 16 and higher
        }
        return request;
    }

    private ActionListener getDeleteListener(
            final FileItem item
            , final ImageView ivAction
            , final ViewGroup btnAction
            , final RoundCornerProgressBar downloadProgressBar
            , IndeterminateRoundCornerProgressBar progressWheel
            , final TextView tvSize
            , final TextView tvSpeed
            , final TextView tvPercent) {
        return new ActionListener() {
            @Override
            public void onSuccess() {
                item.setDownloadStatus(DownloadStatus.NONE);
                ivAction.setImageResource(R.mipmap.ic_start);
                downloadProgressBar.setProgress(0);
                tvSize.setText(" Deleted");
                tvPercent.setText("0%");
                Toast.makeText(NormalActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                setDownloadBackgroundColor(btnAction, DownloadStatus.CANCELED);
            }

            @Override
            public void onFailure(Errors error) {
                Toast.makeText(NormalActivity.this, "" + error, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private DownloadListener getDownloadListener(
            final FileItem item
            , final View btnAction
            , final ImageView ivAction
            , final RoundCornerProgressBar downloadProgressBar
            , final IndeterminateRoundCornerProgressBar loading
            , final TextView tvSize
            , final TextView tvSpeed
            , final TextView tvPercent) {
        return new DownloadListener() {
            DownloadStatus lastStatus = DownloadStatus.NONE;
            long startTime = 0;
            int lastDownloadedBytes = 0;
            int lastPercent = 0;

            @Override
            public void onComplete(int totalBytes, DownloadItem mDownloadItem) {
                item.setDownloadStatus(DownloadStatus.SUCCESSFUL);
                Log.i("onComplete");
                ivAction.setImageResource(R.mipmap.ic_complete);
                setDownloadBackgroundColor(btnAction, DownloadStatus.SUCCESSFUL);
                downloadProgressBar.setProgress(100);
                lastStatus = DownloadStatus.SUCCESSFUL;
                loading.setVisibility(View.GONE);
                tvPercent.setText("100%");
                tvSize.setText(Utils.readableFileSize(totalBytes)
                        + "/" + Utils.readableFileSize(totalBytes) + " - Completed");
            }

            @Override
            public void onPause(int percent, DownloadReason reason, int totalBytes, int downloadedBytes, DownloadItem mDownloadItem) {
                if (lastStatus != DownloadStatus.PAUSED) {
                    item.setDownloadStatus(DownloadStatus.PAUSED);
                    Log.i("onPause - percent: " + percent
                            + " lastStatus:" + lastStatus
                            + " reason:" + reason);
                    ivAction.setImageResource(R.mipmap.ic_play);
                    downloadProgressBar.setProgress(percent);
                    loading.setVisibility(View.VISIBLE);
                    tvPercent.setText(percent + "%");

                    setDownloadBackgroundColor(btnAction, DownloadStatus.PAUSED);
                }
                tvSize.setText(Utils.readableFileSize(downloadedBytes)
                        + "/" + Utils.readableFileSize(totalBytes) + " - Paused - "+reason);
                lastStatus = DownloadStatus.PAUSED;
            }

            @Override
            public void onPending(int percent, int totalBytes, int downloadedBytes, DownloadItem mDownloadItem) {
                if (lastStatus != DownloadStatus.PENDING) {
                    item.setDownloadStatus(DownloadStatus.PENDING);
                    Log.i("onPending - lastStatus:" + lastStatus);
                    ivAction.setImageResource(R.mipmap.ic_cancel);
                    downloadProgressBar.setProgress(percent);
                    loading.setVisibility(View.VISIBLE);
                    tvPercent.setText(percent + "%");
                    tvSize.setText(Utils.readableFileSize(downloadedBytes)
                            + "/" + Utils.readableFileSize(totalBytes) + " - Pending");
                    setDownloadBackgroundColor(btnAction, DownloadStatus.PENDING);
                }
                lastStatus = DownloadStatus.PENDING;
            }

            @Override
            public void onFail(int percent, DownloadReason reason, int totalBytes, int downloadedBytes, DownloadItem mDownloadItem) {
                //Toast.makeText(NormalActivity.this, "Failed: " + reason, Toast.LENGTH_SHORT).show();
                Log.i("onFail - percent: " + percent
                        + " lastStatus:" + lastStatus
                        + " reason:" + reason);
                item.setDownloadStatus(DownloadStatus.FAILED);
                ivAction.setImageResource(R.mipmap.ic_start);
                downloadProgressBar.setProgress(percent);
                lastStatus = DownloadStatus.FAILED;
                loading.setVisibility(View.GONE);
                tvPercent.setText(percent + "%");
                tvSize.setText(Utils.readableFileSize(downloadedBytes)
                        + "/" + Utils.readableFileSize(totalBytes) + " - Failed");
                setDownloadBackgroundColor(btnAction, DownloadStatus.FAILED);

            }

            @Override
            public void onCancel(int totalBytes, int downloadedBytes, DownloadItem mDownloadItem) {
                Log.i("onCancel");
                item.setDownloadStatus(DownloadStatus.CANCELED);
                ivAction.setImageResource(R.mipmap.ic_start);
                downloadProgressBar.setProgress(0);
                lastStatus = DownloadStatus.CANCELED;
                loading.setVisibility(View.GONE);
                tvPercent.setText("0%");
                tvSize.setText(Utils.readableFileSize(downloadedBytes)
                        + "/" + Utils.readableFileSize(totalBytes) + " - Canceled");
                setDownloadBackgroundColor(btnAction, DownloadStatus.CANCELED);
            }

            @Override
            public void onRunning(int percent, int totalBytes, int downloadedBytes, float downloadSpeed, DownloadItem mDownloadItem) {
                if (percent > lastPercent) {
                    Log.i("onRunning percent: " + percent);
                    lastPercent = percent;
                }
                if (lastStatus != DownloadStatus.RUNNING) {
                    item.setDownloadStatus(DownloadStatus.RUNNING);
                    ivAction.setImageResource(R.mipmap.ic_pause);
                    setDownloadBackgroundColor(btnAction, DownloadStatus.RUNNING);
                    loading.setVisibility(View.GONE);
                }
                downloadProgressBar.setProgress(percent);
                lastStatus = DownloadStatus.RUNNING;
                tvPercent.setText(percent + "%");
                if (totalBytes < 0 || downloadedBytes < 0)
                    tvSize.setText("loading...");
                else
                    tvSize.setText(Utils.readableFileSize(downloadedBytes)
                            + "/" + Utils.readableFileSize(totalBytes));
                tvSpeed.setText(Math.round(downloadSpeed) + " KB/sec");
                Log.print("CHECK URL " + mDownloadItem.getUri());

            }

        };
    }

    private void showProgress(FileItem item, DownloadListener listener) {
        getDownloader(item, listener).showProgress();
    }

}
