package ir.siaray.downloadmanagerplussample;

import android.app.DownloadManager;
import android.content.ClipboardManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import ir.siaray.downloadmanagerplus.BuildConfig;
import ir.siaray.downloadmanagerplus.classes.Downloader;
import ir.siaray.downloadmanagerplus.enums.Storage;
import ir.siaray.downloadmanagerplus.utils.Utils;

import static ir.siaray.downloadmanagerplus.enums.Storage.*;
import static ir.siaray.downloadmanagerplussample.BuildConfig.DEBUG;
import static ir.siaray.downloadmanagerplussample.SampleUtils.DOWNLOAD_DIRECTORY;
import static ir.siaray.downloadmanagerplussample.SampleUtils.STORAGE_DIRECTORY;
import static ir.siaray.downloadmanagerplussample.SampleUtils.downloadDirectoryArray;
import static ir.siaray.downloadmanagerplussample.SampleUtils.isSamsung;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //private ClipboardManager.OnPrimaryClipChangedListener listener;
    //private ClipboardManager clipboardManager;
    //private DownloadListener downloadListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUi();
        initDownloadDirectorySpinner();
        initNotificationTypeSpinner();
        //initializeClipboardManager();
    }

    private void initNotificationTypeSpinner() {
        Spinner dropdown = findViewById(R.id.sp_notification_type);
        String[] items = new String[]{
                "VISIBILITY_VISIBLE_NOTIFY_COMPLETED",
                "VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION",
                "VISIBILITY_VISIBLE",
                "VISIBILITY_HIDDEN"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner, items);
        dropdown.setAdapter(adapter);
        dropdown.setOnItemSelectedListener(SampleUtils.getNotificationOnItemSelectListener());
        dropdown.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
    }

    private void initDownloadDirectorySpinner() {
        Spinner dropdown = findViewById(R.id.sp_download_directory);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.spinner, downloadDirectoryArray);
        dropdown.setAdapter(adapter);
        dropdown.setSelection(4);

        dropdown.setOnItemSelectedListener(SampleUtils.getDownloadOnItemSelectListener());
        dropdown.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
    }

    /*private void initializeClipboardManager() {
        Log.i("initializeClipboardManager");
        initializeClipboardListener();
        clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.addPrimaryClipChangedListener(listener);
    }

    private void initializeClipboardListener() {
        Log.i("initializeClipboardListener");
        listener = new ClipboardManager.OnPrimaryClipChangedListener() {
            public void onPrimaryClipChanged() {
                String text = clipboardManager.getText().toString();
                if (text != null && URLUtil.isValidUrl(text)) {
                    String fileType = getFileType(text);
                    Log.i("text: " + text);
                    Toast.makeText(getBaseContext(), "Copy: text\n" + fileType, Toast.LENGTH_LONG).show();
                    if (fileType.equalsIgnoreCase(".jpg")
                            || fileType.equalsIgnoreCase(".mp4")
                            || fileType.equalsIgnoreCase(".apk")
                            || fileType.equalsIgnoreCase(".pdf")
                            || fileType.equalsIgnoreCase(".mp3")) {
                        Downloader downloader = getDownloader(text);
                        if (downloader.getStatus(text) != DownloadStatus.RUNNING
                                && downloader.getStatus(text) != DownloadStatus.PAUSED
                                && downloader.getStatus(text) != DownloadStatus.PENDING) {
                            downloader.start();
                        }
                    }
                }
            }
        };
    }

    private Downloader getDownloader(String url) {
        initClipboardDownloadListener();
        Downloader request = Downloader.getInstance(getApplicationContext())
                .setListener(downloadListener)
                .setUrl(url)
                .setToken(url)
                .setAllowedOverRoaming(true)
                .setVisibleInDownloadsUi(true)
                .setDescription(url)
                .setScanningByMediaScanner(true)
                .setNotificationVisibility(notificationVisibility)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setDestinationDir(DOWNLOAD_DIRECTORY
                        , Utils.getFileName(url))
                .setNotificationTitle(SampleUtils.getFileShortName(Utils.getFileName(url)));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            request.setAllowedOverMetered(true); //Api 16 and higher
        }
        return request;
    }

    private void initClipboardDownloadListener() {
        downloadListener = new DownloadListener() {
            private int lastPercent = 0;

            @Override
            public void onComplete(int totalBytes) {
                Log.i("Clipboard download onComplete");
            }

            @Override
            public void onPause(int percent, DownloadReason reason, int totalBytes, int downloadedBytes) {
                Log.i("Clipboard download onPause");

            }

            @Override
            public void onPending(int percent, int totalBytes, int downloadedBytes) {
                Log.i("Clipboard download onPending");

            }

            @Override
            public void onFail(int percent, DownloadReason reason, int totalBytes, int downloadedBytes) {
                Log.i("Clipboard download onFail: " + reason);

            }

            @Override
            public void onCancel(int totalBytes, int downloadedBytes) {
                Log.i("Clipboard download onCancel");

            }

            @Override
            public void onRunning(int percent, int totalBytes, int downloadedBytes, float downloadSpeed) {
                if (percent > lastPercent)
                    Log.i("Clipboard download onRunning : " + percent);
                lastPercent = percent;
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clipboardManager.removePrimaryClipChangedListener(listener);
    }*/

    private void initUi() {
        TextView tvVersion = findViewById(R.id.tv_version);
        Button btnDownload = (Button) findViewById(R.id.button1);
        Button btnList = (Button) findViewById(R.id.button2);
        Button btnShowDownloads = (Button) findViewById(R.id.button3);
        btnDownload.setOnClickListener(this);
        btnList.setOnClickListener(this);
        btnShowDownloads.setOnClickListener(this);
        //tvVersion.setText("v" + BuildConfig.VERSION_NAME);
        //tvVersion.setText("target: " + Utils.getAppTargetSdkVersion(this));
    }

    @Override
    public void onClick(View view) {
        Intent intent = null;
        switch (view.getId()) {

            case R.id.button1:
                intent = new Intent(MainActivity.this, NormalActivity.class);
                break;

            case R.id.button2:
                intent = new Intent(MainActivity.this, ListActivity.class);
                break;

            case R.id.button3:
                intent = new Intent();
                if (isSamsung()) {
                    intent = MainActivity.this.getPackageManager()
                            .getLaunchIntentForPackage("com.sec.android.app.myfiles");
                    intent.setAction("samsung.myfiles.intent.action.LAUNCH_MY_FILES");
                    if (Utils.isValidDefaultDirectory(DOWNLOAD_DIRECTORY))
                        intent.putExtra("samsung.myfiles.intent.extra.START_PATH",
                                STORAGE_DIRECTORY + "/" + DOWNLOAD_DIRECTORY);
                    else
                        intent.putExtra("samsung.myfiles.intent.extra.START_PATH",
                                DOWNLOAD_DIRECTORY);
                    //startActivity(intent);
                } else
                    intent.setAction(DownloadManager.ACTION_VIEW_DOWNLOADS);
                break;
        }
        if (intent != null) {
            startActivity(intent);
        }
    }
}
