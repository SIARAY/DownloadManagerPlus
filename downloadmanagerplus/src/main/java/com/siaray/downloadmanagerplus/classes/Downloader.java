package com.siaray.downloadmanagerplus.classes;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.webkit.URLUtil;

import com.siaray.downloadmanagerplus.enums.DownloadReason;
import com.siaray.downloadmanagerplus.enums.DownloadStatus;
import com.siaray.downloadmanagerplus.enums.Errors;
import com.siaray.downloadmanagerplus.interfaces.ActionListener;
import com.siaray.downloadmanagerplus.interfaces.DownloadListener;
import com.siaray.downloadmanagerplus.model.DownloadItem;
import com.siaray.downloadmanagerplus.utils.Constants;
import com.siaray.downloadmanagerplus.utils.Log;
import com.siaray.downloadmanagerplus.utils.Strings;
import com.siaray.downloadmanagerplus.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Siamak on 19/01/2017.
 */

public class Downloader {
    private Context mContext;
    private String mUrl;
    private String mTitle;
    private String mFileName;
    private String mDestinationDir;
    private String mDescription;
    private DownloadReason mReason;
    private String mId = null;
    private String mLocalUri;
    private DownloadListener mListener;
    private DownloadManager mDownloadManager;
    private DownloadStatus mDownloadStatus = DownloadStatus.CANCELED;
    private long mDownloadId;
    private int mPercent;
    private int mDownloadedBytes;
    private int mTotalBytes;
    private int mNotificationVisibility = DownloadManager
            .Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
    private int mNetworkTypes = DownloadManager.Request.NETWORK_WIFI
            | DownloadManager.Request.NETWORK_MOBILE;
    private boolean mScanningByMediaAllowed = false;
    private boolean mRoamingAllowed = false;
    private boolean mVisibleInDownloadsUi = true;
    private boolean mMeteredAllowed = false;

    public Downloader(Context mContext, DownloadManager downloadManager, String url) {
        this.mContext = mContext;
        mDownloadManager = downloadManager;
        mUrl = url;
    }

    public Downloader(Context mContext, DownloadManager downloadManager) {
        this.mContext = mContext;
        mDownloadManager = downloadManager;
    }

    public Downloader setId(String id) {
        mId = id;
        Utils.createDBTables(mContext);
        return this;
    }

    public Downloader setListener(DownloadListener listener) {
        mListener = listener;
        return this;
    }

    public Downloader setUrl(String url) {
        mUrl = url;
        return this;
    }

    public Downloader setNotificationTitle(String title) {
        mTitle = title;
        return this;
    }


    public Downloader setDescription(String description) {
        mDescription = description;
        return this;
    }

    public Downloader setScanningByMediaScanner(Boolean scanningByMediaAllowed) {
        mScanningByMediaAllowed = scanningByMediaAllowed;
        return this;
    }

    public Downloader setDestinationDir(String destinationDir, String fileName) {
        mDestinationDir = destinationDir;
        mFileName = fileName;
        return this;
    }

    public Downloader setNotificationVisibility(int visibility) {
        mNotificationVisibility = visibility;
        return this;
    }

    public Downloader setAllowedNetworkTypes(int networkTypes) {
        mNetworkTypes = networkTypes;
        return this;
    }

    public Downloader setAllowedOverRoaming(boolean allowed) {
        mRoamingAllowed = allowed;
        return this;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public Downloader setAllowedOverMetered(boolean allowed) {
        mMeteredAllowed = allowed;
        return this;
    }

    public Downloader setVisibleInDownloadsUi(boolean isVisible) {
        mVisibleInDownloadsUi = isVisible;
        return this;
    }

    private boolean createDownloadDir() {
        return mDestinationDir != null && Environment.getExternalStoragePublicDirectory(mDestinationDir).mkdirs();
    }

    public void start() {
        if (isThereAnError()) {
            return;
        }
        startDownload();
    }

    private boolean isThereAnError() {
        if (TextUtils.isEmpty(mUrl) || !URLUtil.isValidUrl(mUrl)) {
            mListener.onFail(mPercent, DownloadReason.URL_NOT_VALID, mTotalBytes, mDownloadedBytes);
            return true;
        }
        if (TextUtils.isEmpty(mId)) {
            mListener.onFail(mPercent, DownloadReason.ID_NOT_FOUND, mTotalBytes, mDownloadedBytes);
            return true;
        }
        if (isDownloadRunning()) {
            if (mListener != null) {
                if (mDownloadStatus == DownloadStatus.SUCCESSFUL)
                    mListener.onComplete(mTotalBytes);
                else
                    mListener.onFail(mPercent, DownloadReason.DOWNLOAD_IN_PROGRESS, mTotalBytes, mDownloadedBytes);
            }
            return true;
        }
        return false;
    }

    private void startDownload() {
        createDownloadDir();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mUrl));

        request.setAllowedNetworkTypes(mNetworkTypes)
                .setTitle(mTitle)
                .setAllowedOverRoaming(mRoamingAllowed)
                .setVisibleInDownloadsUi(mVisibleInDownloadsUi)
                .setNotificationVisibility(mNotificationVisibility);

        if (mScanningByMediaAllowed)
            request.allowScanningByMediaScanner();
        if (mDestinationDir != null && mFileName != null)
            request.setDestinationInExternalPublicDir(mDestinationDir, mFileName);

        if (mDescription != null && !mDescription.isEmpty())
            request.setDescription(mDescription);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            request.setAllowedOverMetered(mMeteredAllowed);
        }

        cancel(mId);
        mDownloadId = mDownloadManager.enqueue(request);

        Utils.updateDB(mContext, mId, mUrl, mDownloadId);
        showProgress();
    }

    private boolean isDownloadRunning() {
        mDownloadStatus = getStatus(mId);
        return mDownloadStatus == DownloadStatus.RUNNING
                || mDownloadStatus == DownloadStatus.PENDING
                || mDownloadStatus == DownloadStatus.PAUSED
                || mDownloadStatus == DownloadStatus.SUCCESSFUL;
    }

    public void cancel(String id) {
        mId = id;
        findDownloadHistory();
        if (mDownloadId > 0) {
            mDownloadManager.remove(mDownloadId);
            Utils.deleteDownload(mContext, id);
        }
    }

    public boolean deleteFile(String id, ActionListener listener) {
        String fileUri = getDownloadedFilePath(id);
        if (fileUri != null) {
            String filePath = Uri.parse(fileUri).getPath();
            if (filePath != null) {
                File downloadedFile = new File(filePath);
                if (downloadedFile.exists()) {
                    boolean deleted = downloadedFile.delete();
                    if (deleted) {
                        cancel(id);
                        listener.onSuccess();
                        return true;
                    } else {
                        listener.onFailure(Errors.CAN_NOT_DELETE_FILE);
                        return false;
                    }
                } else {
                    listener.onFailure(Errors.FILE_NOT_FOUND);
                    return false;
                }
            }
        }
        listener.onFailure(Errors.FILE_PATH_NOT_FOUND);
        return false;
    }

    public DownloadStatus getStatus(String id) {
        if (id == null) {
            return DownloadStatus.NONE;
        }
        mId = id;
        findDownloadHistory();
        getDownloadStatusWithReason();
        return mDownloadStatus;
    }

    public String getDownloadedFilePath(String id) {
        mId = id;
        findDownloadHistory();
        getDownloadStatusWithReason();
        return mLocalUri;
    }

    public void showProgress() {
        if (findDownloadHistory()) {
            final Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    final boolean[] continuous = {true};
                    do {

                        if (mContext != null) {
                            getDownloadStatusWithReason();

                            ((Activity) mContext).runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    if (!continuous[0])
                                        return;
                                    //String message = statusMessage();
                                    switch (mDownloadStatus) {
                                        case SUCCESSFUL:
                                            mListener.onComplete(mTotalBytes);
                                            break;
                                        case PAUSED:
                                            mListener.onPause(mPercent, mReason, mTotalBytes, mDownloadedBytes);
                                            break;
                                        case PENDING:
                                            mListener.onPending(mPercent, mTotalBytes, mDownloadedBytes);
                                            break;
                                        case FAILED:
                                            mListener.onFail(mPercent, mReason, mTotalBytes, mDownloadedBytes);
                                            break;
                                        case CANCELED:
                                            continuous[0] = false;
                                            mListener.onCancel(mTotalBytes, mDownloadedBytes);
                                            break;
                                        default://Running
                                            mListener.onRunning(mPercent, mTotalBytes, mDownloadedBytes);
                                            break;
                                    }
                                }
                            });
                        } else {
                            break;
                        }

                    } while (((mDownloadStatus == DownloadStatus.RUNNING)
                            || (mDownloadStatus == DownloadStatus.PAUSED)
                            || (mDownloadStatus == DownloadStatus.PENDING))
                            && (mContext != null)
                            && !Thread.interrupted()
                            && continuous[0]);

                    int index = com.siaray.downloadmanagerplus.utils.Utils.getThreadListIndex(Thread.currentThread());
                    if (index >= 0) {
                        Utils.removeFromThreadList(mId);
                    }

                }
            });
            Utils.removeFromThreadList(mId);
            Utils.addToThreadList(mId, thread);

            thread.start();
        }
    }

    private void getDownloadStatusWithReason() {

        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(mDownloadId);

        final Cursor cursor = mDownloadManager.query(q);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            mDownloadedBytes = Utils.getColumnInt(cursor
                    , DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
            mTotalBytes = Utils.getColumnInt(cursor
                    , DownloadManager.COLUMN_TOTAL_SIZE_BYTES);
            final int currentDownloadStatus = Utils.getColumnInt(cursor
                    , DownloadManager.COLUMN_STATUS);
            int reason = Utils.getColumnInt(cursor
                    , DownloadManager.COLUMN_REASON);
            mReason = getDownloadReason(reason);
            mLocalUri = Utils.getColumnString(cursor
                    , DownloadManager.COLUMN_LOCAL_URI);
            //, DownloadManager.COLUMN_LOCAL_FILENAME);

            switch (currentDownloadStatus) {
                case DownloadManager.STATUS_FAILED:
                    mDownloadStatus = DownloadStatus.FAILED;
                    break;
                case DownloadManager.STATUS_PAUSED:
                    mDownloadStatus = DownloadStatus.PAUSED;
                    break;
                case DownloadManager.STATUS_PENDING:
                    mDownloadStatus = DownloadStatus.PENDING;
                    break;
                case DownloadManager.STATUS_RUNNING:
                    mDownloadStatus = DownloadStatus.RUNNING;
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    mDownloadStatus = DownloadStatus.SUCCESSFUL;
                    if (!Utils.isFileExist(mLocalUri)) {
                        mDownloadStatus = DownloadStatus.CANCELED;
                        cancel(mId);
                    }
                    break;
                default:
                    mDownloadStatus = DownloadStatus.NONE;
            }
            if (mTotalBytes > 0) {
                mPercent = (int) ((mDownloadedBytes * 100L) / mTotalBytes);
            }
        } else {
            mDownloadStatus = DownloadStatus.CANCELED;
        }
        if (cursor != null) {
            cursor.close();
        }

    }

    private boolean findDownloadHistory() {
        boolean existed = false;
        if (mId == null)
            return existed;

        String query;
        SQLiteDatabase db = Utils.openDatabase(mContext);
        query = "SELECT * FROM "
                + Constants.DOWNLOAD_DB_TABLE
                + " WHERE " + Strings.DOWNLOAD_PLUS_ID + " = '"
                + mId + "';";

        Cursor cur = null;
        try {
            cur = db.rawQuery(query, null);
            if (cur != null && cur.getCount() > 0) {
                cur.moveToFirst();
                existed = true;
                mId = Utils.getColumnString(cur, Strings.DOWNLOAD_PLUS_ID);
                mUrl = Utils.getColumnString(cur, Strings.URL);
                mDownloadId = Utils.getColumnLong(cur, Strings.DOWNLOAD_ID);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cur != null)
                cur.close();
            if (db != null)
                db.close();
        }
        return existed;
    }

    public static String getId(Context context, long downloadId) {
        String query;
        SQLiteDatabase db = Utils.openDatabase(context);
        query = "SELECT * FROM "
                + Constants.DOWNLOAD_DB_TABLE
                + " WHERE " + Strings.DOWNLOAD_ID + " = "
                + downloadId + ";";
        String downloadPlusId = null;
        Cursor cur = null;
        try {
            cur = db.rawQuery(query, null);
            if (cur != null && cur.getCount() > 0) {
                cur.moveToFirst();
                downloadPlusId = Utils.getColumnString(cur, Strings.DOWNLOAD_PLUS_ID);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cur != null)
                cur.close();
            if (db != null)
                db.close();
        }
        return downloadPlusId;
    }


    public static long getDownloadId(Context context, String id) {
        String query;
        SQLiteDatabase db = Utils.openDatabase(context);
        query = "SELECT * FROM "
                + Constants.DOWNLOAD_DB_TABLE
                + " WHERE " + Strings.DOWNLOAD_PLUS_ID + " = '"
                + id + "';";
        long downloadId = -1;
        Cursor cur = null;
        try {
            cur = db.rawQuery(query, null);
            if (cur != null && cur.getCount() > 0) {
                cur.moveToFirst();
                downloadId = Utils.getColumnLong(cur, Strings.DOWNLOAD_ID);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cur != null)
                cur.close();
            if (db != null)
                db.close();
        }
        return downloadId;
    }


    public static List<DownloadItem> getDownloadsList(Context context, DownloadManager downloadManager) {
        List<DownloadItem> downloadList = new ArrayList<>();
        DownloadManager.Query q = new DownloadManager.Query();
        //q.setFilterById(downloadId);

        final Cursor cursor = downloadManager.query(q);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                DownloadItem downloadItem = fetchDownloadItem(context, cursor);
                Log.printItems(downloadItem);
                if (downloadItem.getId() == null)
                    downloadManager.remove(downloadItem.getDownloadId());
                else
                    downloadList.add(downloadItem);
            } while (cursor.moveToNext());
        } else {
            //List is Empty
        }
        if (cursor != null) {
            cursor.close();
        }
        return downloadList;
    }

    public static DownloadItem getDownloadItem(Context context, DownloadManager downloadManager, String id) {
        if (context == null
                || downloadManager == null
                || id == null
                || id.isEmpty()) {
            return null;
        }
        DownloadItem downloadItem = null;
        long downloadId = Downloader.getDownloadId(context, id);
        if (downloadId < 0)
            return null;
        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(downloadId);

        final Cursor cursor = downloadManager.query(q);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            downloadItem = fetchDownloadItem(context, cursor);
        } else {
        }
        if (cursor != null) {
            cursor.close();
        }
        return downloadItem;
    }

    @NonNull
    private static DownloadItem fetchDownloadItem(Context context, Cursor cursor) {
        DownloadItem downloadItem;
        downloadItem = new DownloadItem();
        downloadItem.setDownloadId(Utils.getColumnLong(cursor
                , DownloadManager.COLUMN_ID));
        downloadItem.setDownloadedBytes(Utils.getColumnInt(cursor
                , DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
        downloadItem.setTotalBytes(Utils.getColumnInt(cursor
                , DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
        downloadItem.setLocalFileName(Utils.getColumnString(cursor
                , DownloadManager.COLUMN_LOCAL_FILENAME));
        downloadItem.setLocalUri(Utils.getColumnString(cursor
                , DownloadManager.COLUMN_LOCAL_URI));
        downloadItem.setUri(Utils.getColumnString(cursor
                , DownloadManager.COLUMN_URI));
        downloadItem.setDownloadStatus(Utils.getColumnInt(cursor
                , DownloadManager.COLUMN_STATUS));
        downloadItem.setReason(Utils.getColumnInt(cursor
                , DownloadManager.COLUMN_REASON));
        downloadItem.setDescription(Utils.getColumnString(cursor
                , DownloadManager.COLUMN_DESCRIPTION));
        downloadItem.setLastTimeModified(Utils.getColumnLong(cursor
                , DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP));
        downloadItem.setMediaType(Utils.getColumnString(cursor
                , DownloadManager.COLUMN_MEDIA_TYPE));
        downloadItem.setTitle(Utils.getColumnString(cursor
                , DownloadManager.COLUMN_TITLE));
        downloadItem.setId(Downloader.getId(context, downloadItem.getDownloadId()));
        int totalBytes = downloadItem.getTotalBytes();
        int downloadedBytes = downloadItem.getDownloadedBytes();
        int percent = 0;
        if (totalBytes > 0) {
            percent = (int) ((downloadedBytes * 100L) / totalBytes);
        }
        downloadItem.setPercent(percent);
        return downloadItem;
    }

    private DownloadReason getDownloadReason(int reason) {
        switch (reason) {
            //DownloadManager.STATUS_FAILED
            case DownloadManager.ERROR_CANNOT_RESUME:
                return DownloadReason.ERROR_CANNOT_RESUME;
            case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                return DownloadReason.ERROR_DEVICE_NOT_FOUND;
            case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                return DownloadReason.ERROR_FILE_ALREADY_EXISTS;
            case DownloadManager.ERROR_FILE_ERROR:
                return DownloadReason.ERROR_FILE_ERROR;
            case DownloadManager.ERROR_HTTP_DATA_ERROR:
                return DownloadReason.ERROR_HTTP_DATA_ERROR;
            case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                return DownloadReason.ERROR_INSUFFICIENT_SPACE;
            case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                return DownloadReason.ERROR_TOO_MANY_REDIRECTS;
            case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                return DownloadReason.ERROR_UNHANDLED_HTTP_CODE;
            case DownloadManager.ERROR_UNKNOWN:
                return DownloadReason.ERROR_UNKNOWN;
            //DownloadManager.STATUS_PAUSED
            case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                return DownloadReason.PAUSED_QUEUED_FOR_WIFI;
            case DownloadManager.PAUSED_UNKNOWN:
                return DownloadReason.PAUSED_UNKNOWN;
            case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                return DownloadReason.PAUSED_WAITING_FOR_NETWORK;
            case DownloadManager.PAUSED_WAITING_TO_RETRY:
                return DownloadReason.PAUSED_WAITING_TO_RETRY;
            default:
                return DownloadReason.UNKNOWN;
        }
    }

}
