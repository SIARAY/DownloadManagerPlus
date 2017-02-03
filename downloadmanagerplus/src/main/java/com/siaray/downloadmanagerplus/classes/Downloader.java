package com.siaray.downloadmanagerplus.classes;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;

import com.siaray.downloadmanagerplus.R;
import com.siaray.downloadmanagerplus.enums.DownloadStatus;
import com.siaray.downloadmanagerplus.interfaces.DownloadListener;
import com.siaray.downloadmanagerplus.utils.Log;

import java.io.File;


/**
 * Created by Siamak on 19/01/2017.
 */

public class Downloader {
    private Context mContext;
    private String mUrl;
    private DownloadListener mListener;
    private String mTitle;
    private String mFileName;
    private String mDestinationDir;
    private long mDownloadId;
    private DownloadStatus mDownloadStatus = DownloadStatus.CANSELED;
    private int mPercent = 0;
    private String mReason;
    private String mFieldId;
    private final String DOWNLOAD_DB_NAME = "Downloads.db";
    private final String DOWNLOAD_DB_TABLE = "tbl_downloads";
    private DownloadManager mDownloadManager;
    private int mDownloadedBytes;
    private int mTotalBytes;
    private String mLocalUri;

    public Downloader(Context mContext, DownloadManager downloadManager, String url) {
        this.mContext = mContext;
        mDownloadManager = downloadManager;
        mUrl = url;
    }

    public Downloader(Context mContext, DownloadManager downloadManager) {
        this.mContext = mContext;
        mDownloadManager = downloadManager;
    }

    public Downloader saveDownloadHistory(String fieldId) {
        mFieldId = fieldId;
        createDBTables();
        return this;
    }


    public Downloader setListener(DownloadListener listener) {
        mListener = listener;
        return this;
    }

    public Downloader setNotificationTitle(String title) {
        mTitle = title;
        return this;
    }


    public Downloader setDestinationDir(String destinationDir, String fileName) {
        mDestinationDir = destinationDir;
        mFileName = fileName;
        return this;
    }

    private boolean createDownloadDir() {
        if (mDestinationDir != null)
            return Environment.getExternalStoragePublicDirectory(mDestinationDir).mkdirs();
        return false;
    }

    //////////////////////////////////////////////////////////////////////////////
    public void start() {

        if (isDownloadRunning()) {
            if (mListener != null) {
                if (mDownloadStatus == DownloadStatus.SUCCESSFUL)
                    mListener.OnMessage("Download Completed");
                else
                    mListener.OnMessage("Download is Running");
            }
            return;
        }
        createDownloadDir();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mUrl));

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                | DownloadManager.Request.NETWORK_MOBILE)
                .setTitle(mTitle)
                .setAllowedOverRoaming(false)
                .setVisibleInDownloadsUi(true)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //.setDescription("Something useful. No, really.")
        if (mDestinationDir != null && mFileName != null)
            request.setDestinationInExternalPublicDir(mDestinationDir, mFileName);


        mDownloadId = mDownloadManager.enqueue(request);

        updateDB();
        showProgress();
    }

    //////////////////////////////////////////////////////////////////////////////
    public void forcedDownload() {

        createDownloadDir();
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mUrl));

        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI
                | DownloadManager.Request.NETWORK_MOBILE)
                .setTitle(mTitle)
                .setAllowedOverRoaming(false)
                .setVisibleInDownloadsUi(true)
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        //.setDescription("Something useful. No, really.")
        if (mDestinationDir != null && mFileName != null)
            request.setDestinationInExternalPublicDir(mDestinationDir, mFileName);


        mDownloadId = mDownloadManager.enqueue(request);

        updateDB();
        showProgress();
    }

    private boolean isDownloadRunning() {
        mDownloadStatus = getStatus(mFieldId);
        if (mDownloadStatus == DownloadStatus.RUNNING
                || mDownloadStatus == DownloadStatus.PENDING
                || mDownloadStatus == DownloadStatus.PAUSED
                || mDownloadStatus == DownloadStatus.SUCCESSFUL) {
            return true;
        }
        return false;
    }

    //////////////////////////////////////////////////////////////////////////////
    public void cancel(String fieldId) {
        mFieldId = fieldId;
        findDownloadHistory();
        if (mDownloadId > 0) {
            mDownloadManager.remove(mDownloadId);
            deleteDownload(fieldId);
        }
    }

    //////////////////////////////////////////////////////////////////////////////
    public void deleteFile(String fieldId) {
        cancel(fieldId);
        String filePath = getDownloadedFilePath(fieldId);
        if (filePath != null) {
            File downloadedFile = new File(filePath);
            if (downloadedFile.exists()) {
                if (downloadedFile.delete()) {
                    mListener.OnMessage("Deleted");
                    return;
                }
            }
        }
        mListener.OnMessage("Error: " + filePath);

    }

    //////////////////////////////////////////////////////////////////////////////
    public DownloadStatus getStatus(String fieldId) {
        if (fieldId == null) {
            return DownloadStatus.NONE;
        }
        mFieldId = fieldId;
        findDownloadHistory();
        getDownloadStatusWithReason();
        return mDownloadStatus;
    }

    //////////////////////////////////////////////////////////////////////////////
    public String getDownloadedFilePath(String fieldId) {
        mFieldId = fieldId;
        findDownloadHistory();
        getDownloadStatusWithReason();
        String path = mLocalUri;
        if (mDestinationDir != null && mFileName != null)
            path = mDestinationDir + File.separator + mFileName;
        return path;
    }

    //////////////////////////////////////////////////////////////////////////////
    public void showProgress() {
        if (findDownloadHistory()) {
            new Thread(new Runnable() {

                @Override
                public void run() {
                    int counter = 0;

                    do {

                        if (mContext != null) {
                            setDownloadStatusWithReason();


                            if (counter > 99999)
                                counter = 0;
                            if (counter++ % 500 == 0) {
                                long threadId = Thread.currentThread().getId();
                                Log.i("Thread: " + threadId + " status:" + statusMessage());
                            }


                            //Log.i("percent: " + item.getPercent());
                            ((Activity) mContext).runOnUiThread(new Runnable() {

                                @Override
                                public void run() {
                                    String message = statusMessage();
                                    switch (mDownloadStatus) {
                                        case SUCCESSFUL:
                                            mListener.OnComplete(message);
                                            break;
                                        case PAUSED:
                                            mListener.OnPause(message, mReason);
                                            break;
                                        case PENDING:
                                            mListener.OnPending(message);
                                            break;
                                        case FAILED:
                                            mListener.OnFail(message, mReason);
                                            break;
                                        case CANSELED:
                                            mListener.OnCancel(message);
                                            break;
                                        default://Running
                                            mListener.OnRunning(mPercent, mTotalBytes, mDownloadedBytes);
                                    }

                                }
                            });
                        } else {
                            break;
                        }

                    } while ((mDownloadStatus == DownloadStatus.RUNNING)
                            || (mDownloadStatus == DownloadStatus.PAUSED)
                            || (mDownloadStatus == DownloadStatus.PENDING));
                }
            }).start();
        }
    }

    //////////////////////////////////////////////////////////////////////////
    private String statusMessage() {
        String msg = "Unknown";

        switch (mDownloadStatus) {
            case FAILED:
                msg = mContext.getString(R.string.download_failed);
                break;

            case PAUSED:
                msg = mContext.getString(R.string.download_paused);
                break;

            case PENDING:
                msg = mContext.getString(R.string.download_pending);
                break;

            case CANSELED:
                msg = mContext.getString(R.string.download_canseled);
                break;

            case SUCCESSFUL:
                msg = mContext.getString(R.string.download_complete);
                break;

            default:
                msg = mContext.getString(R.string.download_in_progress);
                break;
        }

        return (msg);
    }

    ///////////////////////////////////////////////////////////////////////////////////
    private void setDownloadStatusWithReason() {

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
            String filename = cursor.getString(filenameIndex);
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
                    int percent = (int) ((mDownloadedBytes * 100l) / mTotalBytes);
                    mPercent = percent;
                    mReason = null;
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    //statusText = "STATUS_SUCCESSFUL";
                    mDownloadStatus = DownloadStatus.SUCCESSFUL;
                    mReason = null;//"Filename:\n" + filename;
                    break;
            }
        } else {
            mDownloadStatus = DownloadStatus.CANSELED;//Download Canseled
        }
        cursor.close();

    }

    ///////////////////////////////////////////////////////////////////////////////////
    private void getDownloadStatusWithReason() {

        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(mDownloadId);

        final Cursor cursor = mDownloadManager.query(q);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            int downloadedBytes = cursor.getInt(cursor
                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            int totalBytes = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
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
                    int percent = (int) ((downloadedBytes * 100l) / totalBytes);
                    mPercent = percent;
                    mReason = null;
                    break;
                case DownloadManager.STATUS_SUCCESSFUL:
                    //statusText = "STATUS_SUCCESSFUL";
                    mDownloadStatus = DownloadStatus.SUCCESSFUL;
                    mReason = null;//"Filename:\n" + filename;
                    break;
                default:
                    mDownloadStatus = DownloadStatus.NONE;
                    mReason = null;
            }
        } else {
            mDownloadStatus = DownloadStatus.CANSELED;//Download Canseled
        }
        cursor.close();

    }

    ///////////////////////////////////////////////////////////////////////////////////
    private void createDBTables() {
        SQLiteDatabase db = null;
        try {
            db = mContext.openOrCreateDatabase(DOWNLOAD_DB_NAME, Context.MODE_PRIVATE, null);

            db.execSQL("CREATE TABLE IF NOT EXISTS ["
                    + DOWNLOAD_DB_TABLE
                    + "] ("
                    + "[id] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + "[field_id] NVARCHAR NOT NULL, "
                    + "[link] NVARCHAR, "
                    + "[download_id] LONG, "
                    + "UNIQUE(field_id) "
                    + ");");

        } catch (Exception e) {
        } finally {
            if (db != null) {
                db.close();
            }
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////
    private void updateDB() {
        if (mFieldId != null) {
            SQLiteDatabase db = mContext.openOrCreateDatabase(DOWNLOAD_DB_NAME, Context.MODE_PRIVATE, null);
            try {
                db.execSQL("INSERT OR REPLACE INTO "
                        + DOWNLOAD_DB_TABLE
                        + " ( id, field_id, link, download_id ) "
                        + "VALUES "
                        + "((SELECT id FROM "
                        + DOWNLOAD_DB_TABLE
                        + " WHERE field_id = '"
                        + mFieldId
                        + "'),'"
                        + mFieldId
                        + "', '"
                        + mUrl
                        + "',"
                        + mDownloadId
                        + ");");
            } catch (Exception e) {
            } finally {
                db.close();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    private boolean findDownloadHistory() {
        boolean isExist = false;
        String query;
        SQLiteDatabase db = mContext.openOrCreateDatabase(DOWNLOAD_DB_NAME, Context.MODE_PRIVATE, null);
        query = "SELECT * FROM "
                + DOWNLOAD_DB_TABLE
                + " WHERE field_id = "
                + mFieldId + ";";

        Cursor cur = db.rawQuery(query, null);
        cur.moveToFirst();
        try {
            if (cur != null && cur.getCount() > 0) {
                isExist = true;
                mFieldId = cur.getString(cur.getColumnIndex("field_id"));
                mUrl = cur.getString(cur.getColumnIndex("link"));
                mDownloadId = cur.getLong(cur.getColumnIndex("download_id"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            cur.close();
            db.close();
            return isExist;
        }
    }


    ////////////////////////////////////////////////////////////////////////////////////////////

    private boolean deleteDownload(String fieldId) {

        SQLiteDatabase db = mContext.openOrCreateDatabase(DOWNLOAD_DB_NAME, Context.MODE_PRIVATE, null);
        try {

            db.execSQL("delete from " + DOWNLOAD_DB_TABLE + " WHERE field_id=" + fieldId);
        } catch (Exception e) {
            return false;
        } finally {
            db.close();
        }
        return true;
    }
}
