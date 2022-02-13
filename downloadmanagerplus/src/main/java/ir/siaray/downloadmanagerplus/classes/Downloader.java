package ir.siaray.downloadmanagerplus.classes;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ir.siaray.downloadmanagerplus.enums.DownloadReason;
import ir.siaray.downloadmanagerplus.enums.DownloadStatus;
import ir.siaray.downloadmanagerplus.enums.Errors;
import ir.siaray.downloadmanagerplus.enums.Storage;
import ir.siaray.downloadmanagerplus.interfaces.ActionListener;
import ir.siaray.downloadmanagerplus.interfaces.DownloadListener;
import ir.siaray.downloadmanagerplus.model.DownloadItem;
import ir.siaray.downloadmanagerplus.model.DownloadManagerHeader;
import ir.siaray.downloadmanagerplus.utils.Constants;
import ir.siaray.downloadmanagerplus.utils.Log;
import ir.siaray.downloadmanagerplus.utils.Strings;
import ir.siaray.downloadmanagerplus.utils.Utils;

import static ir.siaray.downloadmanagerplus.utils.Strings.DOWNLOAD_DEFAULT_DIRECTORY;
import static ir.siaray.downloadmanagerplus.utils.Utils.createDirectory;
import static ir.siaray.downloadmanagerplus.utils.Utils.getAppTargetSdkVersion;
import static ir.siaray.downloadmanagerplus.utils.Utils.isIdEmpty;
import static ir.siaray.downloadmanagerplus.utils.Utils.isValidDefaultDirectory;
import static ir.siaray.downloadmanagerplus.utils.Utils.isValidDirectory;


/**
 * Created by SIARAY on 19/01/2017.
 * https://github.com/SIARAY/DownloadManagerPlus
 */

public class Downloader {
    private Context mContext;
    private String mUrl;
    private String mTitle;
    private String mFileName;
    private String mDestinationDir;
    private String mDescription;
    private DownloadReason mReason;
    private String mToken = null;
    private String mLocalUri;
    private DownloadListener mListener;
    private DownloadStatus mDownloadStatus = DownloadStatus.CANCELED;
    private long mDownloadId;
    private int mPercent;
    private int mDownloadedBytes = 0;
    private int mTotalBytes = -1;
    private int mNotificationVisibility = DownloadManager
            .Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;
    private int mNetworkTypes = ~0; //DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE;
    private boolean mScanningByMediaAllowed = false;
    private boolean mRoamingAllowed = false;
    private boolean mVisibleInDownloadsUi = true;
    private boolean mMeteredAllowed = true;
    private static DownloadManager downloadManager;
    private boolean mAllDownloadKept = false;
    private long mStartMeasureDownloadSpeedTime = 0;
    private int mLastDownloadedBytes = 0;
    private float mDownloadSpeed;
    //private String mHeader;
    //private String mHeaderValue;
    private ArrayList<DownloadManagerHeader> mHeader;
    private DownloadItem mDownloadInfo = null;

    public static Downloader getInstance(Context mContext) {
        return (new Downloader(mContext));
    }

    public static DownloadManager getDownloadManager(Context context) {
        if (downloadManager == null)
            setDownloadManager(context);
        return downloadManager;
    }

    private Downloader(Context context) {
        this.mContext = context;
        setDownloadManager(mContext);
        Utils.createDBTables(mContext);
    }

    private static void setDownloadManager(Context context) {
        if (downloadManager == null) {
            downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        }
    }

    public Downloader setToken(String token) {
        mToken = token;
        return this;
    }

    public Downloader setListener(DownloadListener listener) {
        mListener = listener;
        return this;
    }

    public Downloader setUrl(String url) {
        mUrl = url;
        if (isIdEmpty(mToken)) {
            mToken = mUrl;
        }
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

    public Downloader addRequestHeader(String header, String value) {
        if (mHeader == null)
            mHeader = new ArrayList<DownloadManagerHeader>();
        DownloadManagerHeader downloadManagerHeader = new DownloadManagerHeader();
        downloadManagerHeader.setHeader(header);
        downloadManagerHeader.setValue(value);
        mHeader.add(downloadManagerHeader);
        return this;
    }

    public Downloader setScanningByMediaScanner(Boolean scanningByMediaAllowed) {
        mScanningByMediaAllowed = scanningByMediaAllowed;
        return this;
    }

    /**
     * @param destinationDir
     * @param fileName
     * @deprecated Custom directory only work on targetSdkVersion 28 and lower<br/>
     * If your app targetSdkVersion is 29 and higher, downloads will be saved
     * in root/Download directory.
     */
    @Deprecated()
    public Downloader setCustomDestinationDir(String destinationDir, String fileName) {
        if (getAppTargetSdkVersion(mContext) < Build.VERSION_CODES.Q) {
            mDestinationDir = destinationDir;
            mFileName = fileName;
        } else {
            setDestinationDir(Storage.DIRECTORY_DOWNLOADS, fileName);
        }
        return this;
    }

    public Downloader setDestinationDir(@Storage.DownloadDirectory String destinationDir, String fileName) {
        mDestinationDir = destinationDir;
        mFileName = fileName;
        return this;
    }

    private String getCorrectionDownloadDir() {
        if (!TextUtils.isEmpty(mDestinationDir)) {
            if (isValidDefaultDirectory(mDestinationDir)) {
                return mDestinationDir;
            }
            if (getAppTargetSdkVersion(mContext) < Build.VERSION_CODES.Q) {
                String storageDir = Environment.getExternalStorageDirectory().getPath();
                return mDestinationDir.replaceFirst(storageDir, "");
            }
        }
        mDestinationDir = DOWNLOAD_DEFAULT_DIRECTORY;
        return DOWNLOAD_DEFAULT_DIRECTORY;
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
        if (isValidDefaultDirectory(mDestinationDir)) {
            Log.i("Default directory no need to create");
            return true;
        }
        return createDirectory(mDestinationDir);
    }

    public void start() {
        createDownloadDir();
        if (isThereAnError()) {
            return;
        }
        startDownload();
    }

    private boolean isThereAnError() {
        int permissionError = Utils.getPermissionsError(mContext);
        if (permissionError != 0) {
            if (mListener != null) {
                if (permissionError == DownloadReason.INTERNET_PERMISSION_REQUIRED.getValue()) {
                    mListener.onFail(mPercent, DownloadReason.INTERNET_PERMISSION_REQUIRED, mTotalBytes, mDownloadedBytes, mDownloadInfo);
                } else if (permissionError == DownloadReason.WRITE_EXTERNAL_STORAGE_PERMISSION_REQUIRED.getValue()) {
                    mListener.onFail(mPercent, DownloadReason.WRITE_EXTERNAL_STORAGE_PERMISSION_REQUIRED, mTotalBytes, mDownloadedBytes, mDownloadInfo);
                } else {
                    mListener.onFail(mPercent, DownloadReason.UNKNOWN, mTotalBytes, mDownloadedBytes, mDownloadInfo);
                }
            }
            return true;
        }

        if (TextUtils.isEmpty(mUrl) || !URLUtil.isValidUrl(mUrl)) {
            Log.print("url can not be empty");
            if (mListener != null)
                mListener.onFail(mPercent, DownloadReason.URL_NOT_VALID, mTotalBytes, mDownloadedBytes, mDownloadInfo);
            return true;
        }

        /*if (isIdEmpty(mToken)) {
            Log.print("Token can not be null");
            mToken = mUrl;
        }*/

        if (!isValidDirectory(mContext, mDestinationDir)) {
            Log.print("DownloadDirectory is not valid, downloaded file will be save in default directory.");
            mDestinationDir = Storage.DIRECTORY_DOWNLOADS;
            Log.i("$$$ path: " + mDestinationDir);
            //createDownloadDir();
            if (!isValidDirectory(mContext, mDestinationDir)) {
                mListener.onFail(mPercent, DownloadReason.DESTINATION_DIRECTORY_NOT_FOUND, mTotalBytes, mDownloadedBytes, mDownloadInfo);
                return true;
            }
        }

        if (TextUtils.isEmpty(mFileName)) {
            mFileName = Utils.getFileName(mUrl);
        }

        if (isDownloadRunning()) {
            if (mListener != null) {
                if (mDownloadStatus == DownloadStatus.SUCCESSFUL)
                    mListener.onComplete(mTotalBytes, mDownloadInfo);
                else
                    mListener.onFail(mPercent, DownloadReason.DOWNLOAD_IN_PROGRESS, mTotalBytes, mDownloadedBytes, mDownloadInfo);
            }
            return true;
        }
        return false;
    }

    private void startDownload() {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mUrl));

        request.setAllowedNetworkTypes(mNetworkTypes)
                .setTitle(mTitle)
                .setAllowedOverRoaming(mRoamingAllowed)
                .setVisibleInDownloadsUi(mVisibleInDownloadsUi)
                .setNotificationVisibility(mNotificationVisibility);

        if (mHeader != null) {
            for (DownloadManagerHeader header:mHeader
                 ) {
                request.addRequestHeader(header.getHeader(), header.getValue());
            }
        }

        if (mScanningByMediaAllowed)
            request.allowScanningByMediaScanner();

        if (!TextUtils.isEmpty(mDestinationDir) && !TextUtils.isEmpty(mFileName)) {
            request.setDestinationInExternalPublicDir(getCorrectionDownloadDir(), mFileName);
        }

        if (!TextUtils.isEmpty(mDescription)) {
            request.setDescription(mDescription);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            request.setAllowedOverMetered(mMeteredAllowed);
        }

        cancel(mToken);

        mDownloadId = downloadManager.enqueue(request);
        DownloadItem downloadInfo = new DownloadItem();
        downloadInfo.setTotalBytes(mTotalBytes);
        downloadInfo.setTitle(mTitle);
        downloadInfo.setDescription(mDescription);
        downloadInfo.setDownloadedBytes(mDownloadedBytes);
        downloadInfo.setLocalUri(mLocalUri);
        downloadInfo.setDownloadId(mDownloadId);
        downloadInfo.setToken(mToken);
        downloadInfo.setDownloadId(mDownloadId);
        downloadInfo.setUri(mUrl);
        mDownloadInfo = downloadInfo;

        Utils.updateDB(mContext, mToken, mUrl, mDownloadId);
        showProgress();
    }

    private boolean isDownloadRunning() {
        mDownloadStatus = getStatus(mToken);
        return mDownloadStatus == DownloadStatus.RUNNING
                || mDownloadStatus == DownloadStatus.PENDING
                || mDownloadStatus == DownloadStatus.PAUSED
                || mDownloadStatus == DownloadStatus.SUCCESSFUL;
    }

    public void cancel(String token) {
        if (isIdEmpty(token)) {
            Log.print("token can not be null");
            return;
        }
        mToken = token;
        findDownloadHistory();
        if (mDownloadId > 0) {
            resetDownloadValues();
            downloadManager.remove(mDownloadId);
            if (!mAllDownloadKept) {
                Utils.deleteDownload(mContext, token);
            }
        }
    }

    /**
     * pause download
     *
     * @param context
     * @param token   the IDs of the downloads to be resumed
     * @return the number of downloads actually paused
     */
    public static int pause(Context context, String token) {
        if (!Utils.isIdEmpty(token)) {
            DownloadItem item = getDownloadItem(context, token);
            if (item != null) {
                long downloadId = item.getDownloadId();
                return pauseDownload(context, downloadId);
            } else {
                Log.print("Download item not found");
            }
        } else {
            Log.print("Download token cannot be null");
        }
        //return mResolver.update(mBaseUri, values, getWhereClauseForIds(ids),getWhereArgsForIds(ids));//pause multiple download
        return -1;
    }

    public int pause() {
        int status = pauseDownload(mContext, mDownloadId);
        /*if(status>0){
            Log.i("@@@ pause->pending:" + status);
            mDownloadStatus = DownloadStatus.PENDING;
            mListener.onPending(mPercent, mTotalBytes, mDownloadedBytes);
        }*/
        return status;
    }

    /**
     * resume download
     *
     * @param context
     * @param token   the IDs of the downloads to be resumed
     * @return the number of downloads actually resumed
     */
    public static int resume(Context context, String token) {
        if (!Utils.isIdEmpty(token)) {
            DownloadItem item = getDownloadItem(context, token);
            if (item != null) {
                return resumeDownload(context, item.getDownloadId());
            } else {
                Log.print("Download item not found");
            }
        } else {
            Log.print("Download token cannot be null");
        }
        //return mResolver.update(mBaseUri, values, getWhereClauseForIds(ids),getWhereArgsForIds(ids));//resume multiple download
        return -1;
    }

    public int resume() {
        int status = resumeDownload(mContext, mDownloadId);
        /*if(status>0){
            Log.i("@@@ resume->pending:" + status);
            mDownloadStatus = DownloadStatus.PENDING;
            mListener.onPending(mPercent, mTotalBytes, mDownloadedBytes);
        }*/
        return status;
    }

    private static int resumeDownload(Context context, long downloadId) {
        if (downloadId >= 0) {
            try {
                ContentResolver mResolver = context.getContentResolver();
                Uri mBaseUri = Uri.parse("content://downloads/my_downloads");
                ContentValues values = new ContentValues();
                //values.put(Downloads.Impl.COLUMN_CONTROL, Downloads.Impl.CONTROL_RUN);
                //values.put(Downloads.Impl.COLUMN_STATUS, Downloads.Impl.STATUS_RUNNING);
                values.put("control", 0);
                values.put("status", 192);
                return mResolver.update(ContentUris.withAppendedId(mBaseUri, downloadId), values,
                        null, null);
            } catch (Exception e) {
                Log.print("Resuming encountered an error");
            }
        } else {
            Log.print("Download id not found");
        }
        return -1;
    }

    private static int pauseDownload(Context context, long downloadId) {
        if (downloadId >= 0) {
            try {
                ContentResolver mResolver = context.getContentResolver();
                Uri mBaseUri = Uri.parse("content://downloads/my_downloads");
                ContentValues values = new ContentValues();
                //values.put(Downloads.Impl.COLUMN_CONTROL, Downloads.Impl.CONTROL_PAUSED);
                //values.put(Downloads.Impl.COLUMN_STATUS, Downloads.Impl.STATUS_PAUSED_BY_APP);
                values.put("control", 1);
                values.put("status", 193);
                return mResolver.update(ContentUris
                                .withAppendedId(mBaseUri, downloadId), values,
                        null, null);
            } catch (Exception e) {
                Log.print("Pausing encountered an error");
            }
        } else {
            Log.print("Download id not found");
        }
        return -1;
    }

    private void resetDownloadValues() {
        mPercent = 0;
        mDownloadedBytes = 0;
        mTotalBytes = -1;
        mDownloadStatus = DownloadStatus.NONE;
    }

    public boolean deleteFile(String token, ActionListener listener) {
        String fileUri = getDownloadedFilePath(token);
        if (fileUri != null) {
            String filePath = Uri.parse(fileUri).getPath();
            if (filePath != null) {
                File downloadedFile = new File(filePath);
                if (downloadedFile.exists()) {
                    boolean deleted = downloadedFile.delete();
                    if (deleted) {
                        cancel(token);
                        if (listener != null)
                            listener.onSuccess();
                        return true;
                    } else {
                        if (listener != null)
                            listener.onFailure(Errors.CAN_NOT_DELETE_FILE);
                        return false;
                    }
                } else {
                    if (listener != null)
                        listener.onFailure(Errors.FILE_NOT_FOUND);
                    return false;
                }
            }
        }
        if (listener != null)
            listener.onFailure(Errors.FILE_PATH_NOT_FOUND);
        return false;
    }

    public DownloadStatus getStatus(String token) {
        if (isIdEmpty(token)) {
            Log.print("token can not be null");
            return DownloadStatus.NONE;
        }
        mToken = token;
        findDownloadHistory();
        getDownloadStatusWithReason();
        return mDownloadStatus;
    }

    public String getDownloadedFilePath(String token) {
        if (isIdEmpty(token)) {
            Log.print("token can not be null");
            return null;
        }
        mToken = token;
        findDownloadHistory();
        getDownloadStatusWithReason();
        return mLocalUri;
    }

    public void showProgress() {

        if (mListener == null)
            return;
        if (findDownloadHistory()) {
            final Thread thread = new Thread(new Runnable() {

                @Override
                public void run() {
                    final boolean[] continuous = {true};
                    mStartMeasureDownloadSpeedTime = System.currentTimeMillis();
                    do {
                        if (mContext != null) {
                            getDownloadStatusWithReason();
                            if (mContext instanceof Activity) {
                                if (((Activity) mContext).isFinishing())
                                    break;
                                ((Activity) mContext).runOnUiThread(new Runnable() {

                                    @Override
                                    public void run() {
                                        if (!continuous[0])
                                            return;
                                        //String message = statusMessage();
                                        returnDownloadResponse(continuous);
                                    }
                                });
                            } else {
                                //continuous[0] = false;
                                if (!continuous[0])
                                    return;
                                returnDownloadResponse(continuous);
                                //Log.print("Use activity context for update ui." +
                                //"Use this in activity or getActivity in fragment");
                            }
                        } else {
                            Log.print("Context is null.");
                            break;
                        }

                    } while (((mDownloadStatus == DownloadStatus.RUNNING)
                            || (mDownloadStatus == DownloadStatus.PAUSED)
                            || (mDownloadStatus == DownloadStatus.PENDING))
                            && (mContext != null)
                            && !Thread.interrupted()
                            && continuous[0]);

                    int index = Utils.getThreadListIndex(Thread.currentThread());
                    if (index >= 0) {
                        Utils.removeFromThreadList(mToken);
                    }

                }
            });
            Utils.removeFromThreadList(mToken);
            Utils.addToThreadList(mToken, thread);
            thread.start();
        }
    }

    private void returnDownloadResponse(boolean[] continuous) {

        switch (mDownloadStatus) {
            case SUCCESSFUL:
                mListener.onComplete(mTotalBytes, mDownloadInfo);
                break;
            case PAUSED:
                mListener.onPause(mPercent, mReason, mTotalBytes, mDownloadedBytes, mDownloadInfo);
                break;
            case PENDING:
                mListener.onPending(mPercent, mTotalBytes, mDownloadedBytes, mDownloadInfo);
                break;
            case FAILED:
                mListener.onFail(mPercent, mReason, mTotalBytes, mDownloadedBytes, mDownloadInfo);
                break;
            case CANCELED:
                continuous[0] = false;
                mListener.onCancel(mTotalBytes, mDownloadedBytes, mDownloadInfo);
                break;
            default://Running
                measureDownloadSpeed();
                mListener.onRunning(mPercent, mTotalBytes, mDownloadedBytes, mDownloadSpeed, mDownloadInfo);
                break;
        }
    }

    private void measureDownloadSpeed() {
        int currentDownloadedBytes = mDownloadedBytes - mLastDownloadedBytes;
        long endMeasureDownloadSpeedTime = System.currentTimeMillis();
        if (endMeasureDownloadSpeedTime > mStartMeasureDownloadSpeedTime) {
            if (currentDownloadedBytes > 0) {
                float downloadSpeed = (currentDownloadedBytes / 1024f)
                        / (((endMeasureDownloadSpeedTime - mStartMeasureDownloadSpeedTime) / 1000f));
                mStartMeasureDownloadSpeedTime = endMeasureDownloadSpeedTime;
                mLastDownloadedBytes = mDownloadedBytes;
                mDownloadSpeed = downloadSpeed;
            }
        }
    }

    private void getDownloadStatusWithReason() {
        int permissionError = Utils.getPermissionsError(mContext);
        if (permissionError != 0) {
            return;
        }
        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(mDownloadId);

        final Cursor cursor = downloadManager.query(q);
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
                        cancel(mToken);
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
        if (!isIdEmpty(mToken)) {
            String query;
            SQLiteDatabase db = Utils.openDatabase(mContext);
            query = "SELECT * FROM "
                    + Constants.DOWNLOAD_DB_TABLE
                    + " WHERE " + Strings.DOWNLOAD_PLUS_ID + " = '"
                    + mToken + "';";

            Cursor cur = null;
            try {
                cur = db.rawQuery(query, null);
                if (cur != null && cur.getCount() > 0) {
                    cur.moveToFirst();
                    existed = true;
                    mToken = Utils.getColumnString(cur, Strings.DOWNLOAD_PLUS_ID);
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
        }
        return existed;
    }

    public static String getToken(Context context, long downloadId) {
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


    public static long getDownloadId(Context context, String token) {
        String query;
        SQLiteDatabase db = Utils.openDatabase(context);
        query = "SELECT * FROM "
                + Constants.DOWNLOAD_DB_TABLE
                + " WHERE " + Strings.DOWNLOAD_PLUS_ID + " = '"
                + token + "';";
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


    public static List<DownloadItem> getDownloadsList(Context context) {
        List<DownloadItem> downloadList = new ArrayList<>();
        DownloadManager.Query q = new DownloadManager.Query();
        //q.setFilterById(downloadId);

        final Cursor cursor = getDownloadManager(context).query(q);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            do {
                DownloadItem downloadItem = fetchDownloadItem(context, cursor);
                Log.printItems(downloadItem);
                if (downloadItem.getToken() == null)
                    downloadManager.remove(downloadItem.getDownloadId());
                else
                    downloadList.add(downloadItem);
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        return downloadList;
    }

    public static DownloadItem getDownloadItem(Context context, String token) {
        if (context == null || Utils.isIdEmpty(token)) {
            return null;
        }
        DownloadItem downloadItem = null;
        long downloadId = Downloader.getDownloadId(context, token);
        if (downloadId < 0)
            return null;
        DownloadManager.Query q = new DownloadManager.Query();
        q.setFilterById(downloadId);

        final Cursor cursor = getDownloadManager(context).query(q);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.moveToFirst();
            downloadItem = fetchDownloadItem(context, cursor);
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
        //downloadItem.setLocalFilePath(Utils.getColumnString(cursor
        //, DownloadManager.COLUMN_LOCAL_FILENAME));
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
        downloadItem.setToken(Downloader.getToken(context, downloadItem.getDownloadId()));
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

    public Downloader setKeptAllDownload(boolean allDownloadKept) {
        mAllDownloadKept = allDownloadKept;
        return this;
    }

    public long getDownloadedBytes() {
        return mDownloadedBytes;
    }

    public long getTotalBytes() {
        return mTotalBytes;
    }

    public long getDownloadPercent() {
        return mPercent;
    }
}
