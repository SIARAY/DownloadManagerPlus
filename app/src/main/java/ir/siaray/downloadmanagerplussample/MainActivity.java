package ir.siaray.downloadmanagerplussample;

import android.app.DownloadManager;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import ir.siaray.downloadmanagerplus.BuildConfig;
import ir.siaray.downloadmanagerplus.classes.Downloader;
import ir.siaray.downloadmanagerplus.enums.DownloadReason;
import ir.siaray.downloadmanagerplus.enums.DownloadStatus;
import ir.siaray.downloadmanagerplus.interfaces.DownloadListener;
import ir.siaray.downloadmanagerplus.model.DownloadItem;
import ir.siaray.downloadmanagerplus.utils.Log;
import ir.siaray.downloadmanagerplus.utils.Utils;

import static ir.siaray.downloadmanagerplussample.SampleUtils.getFileType;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    public static final String STORAGE_DIRECTORY = Environment.getExternalStorageDirectory().getPath();
    public static final String DOWNLOAD_DIRECTORY = STORAGE_DIRECTORY + "/dmp";
    private AlertDialog dialog;
    private AlertDialog.Builder builder;
    private ClipboardManager.OnPrimaryClipChangedListener listener;
    private ClipboardManager clipboardManager;
    private int notificationVisibility = DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
    private DownloadListener downloadListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUi();
        initializeClipboardManager();
    }

    private void initializeClipboardManager() {
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
            public void onRunning(int percent, int totalBytes, int downloadedBytes) {
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
    }

    private void initUi() {
        TextView tvVersion = findViewById(R.id.tv_version);
        Button btnDownload = (Button) findViewById(R.id.button1);
        Button btnList = (Button) findViewById(R.id.button2);
        Button btnShowDownloads = (Button) findViewById(R.id.button3);
        btnDownload.setOnClickListener(this);
        btnList.setOnClickListener(this);
        btnShowDownloads.setOnClickListener(this);
        tvVersion.setText("v" + BuildConfig.VERSION_NAME);
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
                if (list.size() > 0) {
                    showInfoDialog(list);
                    //Toast.makeText(this, Log.printItems(list.get(0)), Toast.LENGTH_LONG).show();
                } else {
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

    private void showInfoDialog(final List<DownloadItem> list) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        dialog = builder.setTitle("Details")
                .setMessage(Log.printItems(list.get(0)))
                .setPositiveButton("Open", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.openFile(MainActivity.this, list.get(0).getLocalUri());

                //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
            }
        });
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
