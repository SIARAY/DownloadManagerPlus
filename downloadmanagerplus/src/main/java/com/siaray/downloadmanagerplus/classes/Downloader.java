package com.siaray.downloadmanagerplus.classes;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;
import android.text.TextUtils;

import com.siaray.downloadmanagerplus.R;
import com.siaray.downloadmanagerplus.enums.DownloadStatus;
import com.siaray.downloadmanagerplus.enums.Result;
import com.siaray.downloadmanagerplus.interfaces.ActionListener;
import com.siaray.downloadmanagerplus.interfaces.DownloadListener;
import com.siaray.downloadmanagerplus.utils.Constants;
import com.siaray.downloadmanagerplus.utils.Strings;
import com.siaray.downloadmanagerplus.utils.Utils;

import java.io.File;


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
    private String mReason;
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
    private boolean mScanningByMediaAllowed=false;

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

    private boolean createDownloadDir() {
        return mDestinationDir != null && Environment.getExternalStoragePublicDirectory(mDestinationDir).mkdirs();
    }

    public void start() {
        if (checkErrors()) {
            return;
        }
        forcedDownload();
    }

    private boolean checkErrors() {
        if (TextUtils.isEmpty(mUrl)) {
            mListener.onMessage(Result.ERROR, mContext.getString(R.string.url_not_found));
            return true;
        }
        if (TextUtils.isEmpty(mId)) {
            mListener.onMessage(Result.ERROR, mContext.getString(R.string.id_not_found));
            return true;
        }
        if (isDownloadRunning()) {
            if (mListener != null) {
                if (mDownloadStatus == DownloadStatus.SUCCESSFUL)
                    mListener.onComplete(null);
                else
                    mListener.onMessage(Result.MESSAGE, mContext.getString(R.string.download_in_progress));
            }
            return true;
        }
        return false;
    }

    public void forcedDownload() {
        createDownloadDir();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mUrl));

        request.setAllowedNetworkTypes(mNetworkTypes)
                .setTitle(mTitle)
                .setAllowedOverRoaming(false)
                .setVisibleInDownloadsUi(true)
                .setNotificationVisibility(mNotificationVisibility);
        if (mScanningByMediaAllowed)
            request.allowScanningByMediaScanner();
        if (mDestinationDir != null && mFileName != null)
            request.setDestinationInExternalPublicDir(mDestinationDir, mFileName);

        if (mDescription != null && !mDescription.isEmpty())
            request.setDescription(mDescription);

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
        String filePath = getDownloadedFilePath(id);
        if (filePath != null) {
            File downloadedFile = new File(filePath);
            if (downloadedFile.exists()) {
                boolean deleted = downloadedFile.delete();
                if (deleted) {
                    cancel(id);
                    listener.onSuccess(mContext.getString(R.string.file_deleted));
                    return true;
                } else {
                    listener.onFailure(mContext.getString(R.string.cannot_delete));
                    return false;
                }
            } else {
                listener.onFailure(mContext.getString(R.string.file_not_found));
                return false;
            }
        }
        listener.onFailure(mContext.getString(R.string.path_not_found));
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

    private boolean isFileExist() {
        return (new File(mLocalUri).exists());
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
                                    String message = statusMessage();
                                    switch (mDownloadStatus) {
                                        case SUCCESSFUL:
                                            mListener.onComplete(message);
                                            break;
                                        case PAUSED:
                                            mListener.onPause(message, mReason);
                                            break;
                                        case PENDING:
                                            mListener.onPending(message);
                                            break;
                                        case FAILED:
                                            mListener.onFail(message, mReason);
                                            break;
                                        case CANCELED:
                                            continuous[0] = false;
                                            mListener.onCancel(message);
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

    private String statusMessage() {
        String message;

        switch (mDownloadStatus) {
            case FAILED:
                message = mContext.getString(R.string.download_failed);
                break;

            case PAUSED:
                message = mContext.getString(R.string.download_paused);
                break;

            case PENDING:
                message = mContext.getString(R.string.download_pending);
                break;

            case CANCELED:
                message = mContext.getString(R.string.download_canceled);
                break;

            case SUCCESSFUL:
                message = mContext.getString(R.string.download_complete);
                break;

            default:
                message = mContext.getString(R.string.download_in_progress);
                break;
        }

        return (message);
    }

    private void getDownloadStatusWithReason() {

        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(mDownloadId);

        final Cursor cursor = mDownloadManager.query(q);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            mDownloadedBytes = cursor.getInt(cursor
                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            mTotalBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
            final int currentDownloadStatus = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
            int reason = cursor.getInt(columnReason);
            int filenameIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME);
            mLocalUri = cursor.getString(filenameIndex);

            switch (currentDownloadStatus) {
                case DownloadManager.STATUS_FAILED:
                    //statusText = "STATUS_FAILED";
                    mDownloadStatus = DownloadStatus.FAILED;
                    switch (reason) {
                        case DownloadManager.ERROR_CANNOT_RESUME:
                            mReason = "ERROR_CANNOT_RESUME";
                            break;
                        case DownloadManager.ERROR_DEVICE_NOT_FOUND:
                            mReason = "ERROR_DEVICE_NOT_FOUND";
                            break;
                        case DownloadManager.ERROR_FILE_ALREADY_EXISTS:
                            mReason = "ERROR_FILE_ALREADY_EXISTS";
                            break;
                        case DownloadManager.ERROR_FILE_ERROR:
                            mReason = "ERROR_FILE_ERROR";
                            break;
                        case DownloadManager.ERROR_HTTP_DATA_ERROR:
                            mReason = "ERROR_HTTP_DATA_ERROR";
                            break;
                        case DownloadManager.ERROR_INSUFFICIENT_SPACE:
                            mReason = "ERROR_INSUFFICIENT_SPACE";
                            break;
                        case DownloadManager.ERROR_TOO_MANY_REDIRECTS:
                            mReason = "ERROR_TOO_MANY_REDIRECTS";
                            break;
                        case DownloadManager.ERROR_UNHANDLED_HTTP_CODE:
                            mReason = "ERROR_UNHANDLED_HTTP_CODE";
                            break;
                        case DownloadManager.ERROR_UNKNOWN:
                            mReason = "ERROR_UNKNOWN";
                            break;
                    }
                    break;
                case DownloadManager.STATUS_PAUSED:
                    //statusText = "STATUS_PAUSED";
                    mDownloadStatus = DownloadStatus.PAUSED;
                    switch (reason) {
                        case DownloadManager.PAUSED_QUEUED_FOR_WIFI:
                            mReason = "PAUSED_QUEUED_FOR_WIFI";
                            break;
                        case DownloadManager.PAUSED_UNKNOWN:
                            mReason = "PAUSED_UNKNOWN";
                            break;
                        case DownloadManager.PAUSED_WAITING_FOR_NETWORK:
                            mReason = "PAUSED_WAITING_FOR_NETWORK";
                            break;
                        case DownloadManager.PAUSED_WAITING_TO_RETRY:
                            mReason = "PAUSED_WAITING_TO_RETRY";
                            break;
                    }
                    break;
                case DownloadManager.STATUS_PENDING:
                    //statusText = "STATUS_PENDING";
                    mDownloadStatus = DownloadStatus.PENDING;
                    mReason = null;
                    break;
                case DownloadManager.STATUS_RUNNING:
                    //statusText = "STATUS_RUNNING";
                    mDownloadStatus = DownloadStatus.RUNNING;
                    if (mTotalBytes > 0) {
                        mPercent = (int) ((mDownloadedBytes * 100L) / mTotalBytes);
                    }
                    mReason = null;
                    break;

                case DownloadManager.STATUS_SUCCESSFUL:
                    //statusText = "STATUS_SUCCESSFUL";
                    mDownloadStatus = DownloadStatus.SUCCESSFUL;
                    mReason = null;//"Filename:\n" + filename;
                    if (!isFileExist()) {
                        mDownloadStatus = DownloadStatus.CANCELED;
                        cancel(mId);
                    }
                    break;
                default:
                    mDownloadStatus = DownloadStatus.NONE;
                    mReason = null;
            }
        } else {
            mDownloadStatus = DownloadStatus.CANCELED;
        }
        if (cursor != null) {
            cursor.close();
        }

    }

    private boolean findDownloadHistory() {
        boolean isExist = false;
        if (mId == null)
            return isExist;

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
                isExist = true;
                mId = cur.getString(cur.getColumnIndex(Strings.DOWNLOAD_PLUS_ID));
                mUrl = cur.getString(cur.getColumnIndex(Strings.LINK));
                mDownloadId = cur.getLong(cur.getColumnIndex(Strings.DOWNLOAD_ID));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cur != null)
                cur.close();
            if (db != null)
                db.close();
        }
        return isExist;
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
                downloadPlusId = cur.getString(cur.getColumnIndex(Strings.DOWNLOAD_PLUS_ID));
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
                downloadId = cur.getLong(cur.getColumnIndex(Strings.DOWNLOAD_ID));
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

}
