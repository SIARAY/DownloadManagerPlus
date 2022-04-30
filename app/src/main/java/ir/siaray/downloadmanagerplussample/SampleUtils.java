package ir.siaray.downloadmanagerplussample;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import ir.siaray.downloadmanagerplus.classes.Downloader;
import ir.siaray.downloadmanagerplus.enums.DownloadStatus;
import ir.siaray.downloadmanagerplus.enums.Storage;
import ir.siaray.downloadmanagerplus.interfaces.ActionListener;
import ir.siaray.downloadmanagerplus.model.DownloadItem;
import ir.siaray.downloadmanagerplus.utils.Log;
import ir.siaray.downloadmanagerplus.utils.Utils;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static ir.siaray.downloadmanagerplus.enums.Storage.DIRECTORY_ALARMS;
import static ir.siaray.downloadmanagerplus.enums.Storage.DIRECTORY_AUDIOBOOKS;
import static ir.siaray.downloadmanagerplus.enums.Storage.DIRECTORY_DCIM;
import static ir.siaray.downloadmanagerplus.enums.Storage.DIRECTORY_DOCUMENTS;
import static ir.siaray.downloadmanagerplus.enums.Storage.DIRECTORY_DOWNLOADS;
import static ir.siaray.downloadmanagerplus.enums.Storage.DIRECTORY_MOVIES;
import static ir.siaray.downloadmanagerplus.enums.Storage.DIRECTORY_MUSIC;
import static ir.siaray.downloadmanagerplus.enums.Storage.DIRECTORY_NOTIFICATIONS;
import static ir.siaray.downloadmanagerplus.enums.Storage.DIRECTORY_PICTURES;
import static ir.siaray.downloadmanagerplus.enums.Storage.DIRECTORY_PODCASTS;
import static ir.siaray.downloadmanagerplus.enums.Storage.DIRECTORY_RINGTONES;
import static ir.siaray.downloadmanagerplus.enums.Storage.DIRECTORY_SCREENSHOTS;

/**
 * Created by Siamak on 28/01/2017.
 */

public class SampleUtils {

    public static final String STORAGE_DIRECTORY = Environment.getExternalStorageDirectory().getPath();
    public static String DOWNLOAD_DIRECTORY = Storage.DIRECTORY_DOWNLOADS;//STORAGE_DIRECTORY + "/dmp";
    public static int NOTIFICATION_VISIBILITY = DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
    static String[] downloadDirectoryArray = new String[]{
            DIRECTORY_ALARMS
            , DIRECTORY_AUDIOBOOKS
            , DIRECTORY_DCIM
            , DIRECTORY_DOCUMENTS
            , DIRECTORY_DOWNLOADS
            , DIRECTORY_MOVIES
            , DIRECTORY_MUSIC
            , DIRECTORY_NOTIFICATIONS
            , DIRECTORY_PICTURES
            , DIRECTORY_PODCASTS
            , DIRECTORY_RINGTONES
            , DIRECTORY_SCREENSHOTS
    };

    public static FileItem getDownloadItem(int number) {
        FileItem item = new FileItem();

        if (number == 1) {
            String link = "http://techslides.com/demos/samples/sample.jpg";
            item.setToken("id1252");
            item.setUri(link);
        } else if (number == 2) {
            String link = "http://techslides.com/demos/samples/sample.pdf";
            item.setToken("id1259");
            item.setUri(link);
        } else {
            String link = "http://techslides.com/demos/samples/sample.mp4";
            item.setToken("id1282");
            item.setUri(link);
        }

        return item;
    }

    public static void getFileList(List<DownloadItem> list, int number) {
        for (int i = 0; i < number; i++) {
            DownloadItem item = new DownloadItem();
            item.setToken("" + i);
            String link = "";
            switch (i % 12) {
                case 0:
                    link = "https://s3-us-west-1.amazonaws.com/powr/defaults/image-slider2.jpg";
                    break;
                case 1:
                    link = "http://dolly.roslin.ed.ac.uk/wp-content/uploads/2016/01/DollySideView.jpg";
                    break;
                case 2:
                    link = "https://i.pinimg.com/originals/ce/69/4f/ce694f560636dffcf42ecf40d4f2f962.gif";
                    break;
                case 3:
                    link = "https://dl2.soft98.ir/soft/m/Mozilla.Firefox.76.0.1.EN.x64.zip";
                    break;
                case 4:
                    link = "https://wallpapercave.com/wp/wp5211914.jpg";
                    break;
                case 5:
                    link = "http://wallpaperpulse.com/img/2242184.jpg";
                    break;
                case 6:
                    link = "https://file-examples-com.github.io/wp-content/uploads/2017/04/file_example_MP4_1280_10MG.mp4";
                    break;
                case 7:
                    link = "https://file-examples-com.github.io/wp-content/uploads/2017/11/file_example_MP3_2MG.mp3";
                    break;
                case 8:
                    link = "http://yesofcorsa.com/wp-content/uploads/2016/12/4k-Love-Wallpaper-HQ-1024x576.jpg";
                    break;
                case 9:
                    link = "http://yesofcorsa.com/wp-content/uploads/2017/01/4K-Rain-Wallpaper-Download-1024x640.jpeg";
                    break;
                case 10:
                    link = "https://file-examples.com/wp-content/uploads/2017/10/file-example_PDF_500_kB.pdf";
                    break;
                case 11:
                    link = "https://file-examples.com/wp-content/uploads/2017/10/file_example_JPG_100kB.jpg";
                    break;

                default:
                    link = "https://file-examples.com/wp-content/uploads/2017/02/zip_2MB.zip";
            }
            item.setUri(link);
            list.add(item);
        }
    }

    public static String getFileShortName(String name) {
        if (name.length() > 10) {
            name = name.substring(0, 5) + ".." + name.substring(name.length() - 4, name.length());
        }
        return name;
    }

    /*public static String getFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1, url.length());
    }*/

    public static void setFileSize(final Context context, final FileItem item) {
        new AsyncTask<String, Integer, Integer>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Integer doInBackground(String... params) {
                HttpURLConnection connection = null;
                try {
                    URL url = new URL(item.getUri());
                    connection = (HttpURLConnection) url.openConnection();
                    connection.setRequestMethod("HEAD");
                    connection.setConnectTimeout(3000);
                    connection.getInputStream();
                    return connection.getContentLength();
                } catch (IOException e) {
                    return -1;
                } finally {
                    if (connection != null)
                        connection.disconnect();
                }
            }

            @Override
            protected void onPostExecute(Integer i) {
                item.setFileSize(i);
                super.onPostExecute(i);
            }
        }.execute();
    }

    public static String getFileType(String url) {
        if (url == null || !url.contains("."))
            return null;
        return url.substring(url.lastIndexOf('.'), url.length());
    }


    public static boolean isStoragePermissionGranted(Activity activity) {
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R&&!Environment.isExternalStorageManager()) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.addCategory("android.intent.category.DEFAULT");
                intent.setData(Uri.parse(String.format("package:%s",activity.getPackageName())));
                activity.startActivityForResult(intent, 1012);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                activity.startActivityForResult(intent, 1012);
            }
            return false;
        }*/
        if (ActivityCompat.checkSelfPermission(activity
                , WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }
        return true;
    }


    public static void showInfoDialog(final Activity activity, final DownloadItem downloadItem) {
        if (downloadItem == null) {
            Toast.makeText(activity, "Download details not available", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog dialog;
        AlertDialog.Builder builder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(activity);
        }
        dialog = builder.setTitle("Details")
                .setMessage(Log.printItems(downloadItem))
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
                Utils.openFile(activity, downloadItem.getLocalUri());

                //else dialog stays open. Make sure you have an obvious way to close the dialog especially if you set cancellable to false.
            }
        });
    }

    public static void showPopUpMenu(final Activity activity, View view, final FileItem item, final ActionListener deleteListener) {
        PopupMenu overflowPopupMenu = new PopupMenu(activity, view);
        overflowPopupMenu.getMenuInflater().inflate(R.menu.popup_overflow_options, overflowPopupMenu.getMenu());

        overflowPopupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(android.view.MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.popUpCancel:
                        cancelDownload(activity, item);
                        break;
                    case R.id.popUpDelete:
                        deleteFile(activity, item, deleteListener);
                        break;
                    case R.id.popUpDetails:
                        showInfoDialog(activity, Downloader.getDownloadItem(activity, item.getToken()));
                        break;
                }
                return true;
            }
        });
        ir.siaray.downloadmanagerplussample.Log.print("status: " + item.getToken() + " : " + item.getDownloadStatus());
        MenuItem cancelButton = overflowPopupMenu.getMenu().getItem(0);
        if (item.getDownloadStatus() == DownloadStatus.SUCCESSFUL
                || item.getDownloadStatus() == DownloadStatus.FAILED
                || item.getDownloadStatus() == DownloadStatus.CANCELED
                || item.getDownloadStatus() == DownloadStatus.NONE)
            cancelButton.setVisible(false);
        overflowPopupMenu.show();
    }

    public static void deleteFile(Context context, FileItem item, ActionListener deleteListener) {
        Downloader downloader = Downloader.getInstance(context)
                .setUrl(item.getUri())
                .setListener(item.getListener());

        boolean deleted = downloader.deleteFile(item.getToken(), deleteListener);
        ir.siaray.downloadmanagerplussample.Log.print("File deleted: " + deleted);
    }

    public static void cancelDownload(Context context, FileItem item) {
        Downloader downloader = Downloader.getInstance(context)
                .setUrl(item.getUri())
                .setListener(item.getListener());

        downloader.cancel(item.getToken());
    }

    public static boolean isSamsung() {
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer != null) return manufacturer.contains("samsung");
        return false;
    }

    static void setDownloadBackgroundColor(View view, DownloadStatus status) {
        if (status == DownloadStatus.SUCCESSFUL) {
            view.setBackgroundResource(R.drawable.download_button_background_shape_green);
        } else if (status == DownloadStatus.FAILED) {
            view.setBackgroundResource(R.drawable.download_button_background_shape_red);
        } else if (status == DownloadStatus.PENDING || status == DownloadStatus.PAUSED) {
            view.setBackgroundResource(R.drawable.download_button_background_shape_yellow);
        } else if (status == DownloadStatus.RUNNING) {
            view.setBackgroundResource(R.drawable.download_button_background_shape_blue);
        } else {
            view.setBackgroundResource(R.drawable.download_button_background_shape_gray);
        }
    }

    static AdapterView.OnItemSelectedListener getNotificationOnItemSelectListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        NOTIFICATION_VISIBILITY = DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;

                        break;
                    case 1:
                        NOTIFICATION_VISIBILITY = DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION;
                        break;

                    case 2:
                        NOTIFICATION_VISIBILITY = DownloadManager.Request.VISIBILITY_VISIBLE;
                        break;

                    case 3:
                        NOTIFICATION_VISIBILITY = DownloadManager.Request.VISIBILITY_HIDDEN;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                NOTIFICATION_VISIBILITY = DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
            }
        };
    }

    static AdapterView.OnItemSelectedListener getDownloadOnItemSelectListener() {
        return new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                DOWNLOAD_DIRECTORY = downloadDirectoryArray[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                DOWNLOAD_DIRECTORY = DIRECTORY_DOWNLOADS;
            }
        };
    }
}
