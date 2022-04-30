package ir.siaray.downloadmanagerplus.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import androidx.core.content.FileProvider;
import androidx.core.content.PermissionChecker;

import java.io.File;
import java.text.DecimalFormat;

import ir.siaray.downloadmanagerplus.enums.DownloadReason;
import ir.siaray.downloadmanagerplus.enums.Storage;

/**
 * Created by SIARAY on 08/01/2017.
 */

public class Utils {

    /*public static boolean createDownloadDir() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .mkdirs();
    }*/

    public static String getFileName(String url) {
        if (TextUtils.isEmpty(url))
            return null;

        if (!url.contains("/"))
            return url;

        int lastForwardSlashIndex = url.lastIndexOf("/");
        if (lastForwardSlashIndex == url.length())
            return null;
        return url.substring(lastForwardSlashIndex + 1, url.length());
    }

    public static void openFile(Context context, String path) {
        openFile(context, path, null);
    }

    public static void openFile(Context context, String path, Uri providerUri) {
        if (path == null)
            return;
        path = Uri.parse(path).getPath().toLowerCase();
        //Uri uri = Uri.fromFile(new File(path));
        Uri uri = providerUri;
        if (providerUri == null)
            try {
                uri = FileProvider.getUriForFile(context
                        , context.getPackageName() + ".fileProvider"
                        , new File(path));
            } catch (Exception e) {
                Log.print("FileProvider not found. Please declare your FileProvider in manifest.");
                return;
            }
        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (path.contains(".doc")
                || path.contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if (path.contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if (path.contains(".ppt") || path.contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (path.contains(".xls") || path.contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (path.contains(".zip") || path.contains(".rar")) {
            // zip file
            intent.setDataAndType(uri, "application/zip");
        } else if (path.contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if (path.contains(".wav") || path.contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (path.contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if (path.contains(".jpg") || path.contains(".jpeg") || path.contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if (path.contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if (path.contains(".3gp")
                || path.contains(".mpg")
                || path.contains(".mpeg")
                || path.contains(".mpe")
                || path.contains(".mp4")
                || path.contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean deleteDownload(Context context, String id) {
        if (isIdEmpty(id)) {
            Log.print("id can not be null");
            return false;
        }
        SQLiteDatabase db = openDatabase(context);
        try {
            db.execSQL("delete from " + Constants.DOWNLOAD_DB_TABLE + " WHERE " + Strings.DOWNLOAD_PLUS_ID + "='" + id + "'");
        } catch (Exception e) {
            return false;
        } finally {
            db.close();
        }
        return true;
    }

    public static void createDBTables(Context context) {
        SQLiteDatabase db = null;
        try {
            db = openDatabase(context);

            db.execSQL("CREATE TABLE IF NOT EXISTS ["
                    + Constants.DOWNLOAD_DB_TABLE
                    + "] ("
                    + "[" + Strings.ID + "] INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
                    + "[" + Strings.DOWNLOAD_PLUS_ID + "] NVARCHAR NOT NULL, "
                    + "[" + Strings.URL + "] NVARCHAR, "
                    + "[" + Strings.DOWNLOAD_ID + "] LONG, "
                    + "UNIQUE(" + Strings.DOWNLOAD_PLUS_ID + ") "
                    + ");");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (db != null) {
                db.close();
            }
        }

    }

    public static void updateDB(Context context
            , String id
            , String url
            , long downloadId) {
        if (!isIdEmpty(id)) {
            SQLiteDatabase db = openDatabase(context);
            try {
                db.execSQL("INSERT OR REPLACE INTO "
                        + Constants.DOWNLOAD_DB_TABLE + " ( "
                        + Strings.ID + ", "
                        + Strings.DOWNLOAD_PLUS_ID + ", "
                        + Strings.URL + ", "
                        + Strings.DOWNLOAD_ID + " ) "
                        + "VALUES "
                        + "((SELECT id FROM "
                        + Constants.DOWNLOAD_DB_TABLE
                        + " WHERE "
                        + Strings.DOWNLOAD_PLUS_ID + " = '"
                        + id
                        + "'),'"
                        + id
                        + "', '"
                        + url
                        + "',"
                        + downloadId
                        + ");");
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (db != null)
                    db.close();
            }
        } else {
            Log.print("id can not be null");
        }
    }

    public static void addToThreadList(String id, Thread thread) {
        if (isIdEmpty(id)) {
            Log.print("id can not be null");
            return;
        }
        Constants.fieldList.add(id);
        Constants.threadList.add(thread);
    }

    public static void removeFromThreadList(String id) {
        if (isIdEmpty(id)) {
            Log.print("id can not be null");
            return;
        }
        int index = getIdListIndex(id);
        if (index >= 0) {

            if (index < Constants.fieldList.size()
                    && Constants.fieldList.get(index) != null) {
                Constants.fieldList.remove(index);
            }

            if (index < Constants.threadList.size()
                    && Constants.threadList.get(index) != null) {
                //tn = Constants.threadList.get(index).getName();
                if (Constants.threadList.get(index).isAlive()
                        || !Constants.threadList.get(index).isInterrupted()) {
                    Constants.threadList.get(index).interrupt();
                }

                Constants.threadList.remove(index);
            }

        }
    }

    private static int getIdListIndex(String id) {
        return Constants.fieldList.indexOf(id);
    }

    public static int getThreadListIndex(Thread thread) {
        return Constants.threadList.indexOf(thread);
    }

    public static SQLiteDatabase openDatabase(Context context) {
        return context.openOrCreateDatabase(Constants.DOWNLOAD_DB_NAME, Context.MODE_PRIVATE, null);
    }


    public static int getColumnInt(Cursor cursor, String columnName) {
        return cursor.getInt(cursor
                .getColumnIndex(columnName));
    }

    public static long getColumnLong(Cursor cursor, String columnName) {
        return cursor.getLong(cursor
                .getColumnIndex(columnName));
    }

    public static String getColumnString(Cursor cursor, String columnName) {
        return cursor.getString(cursor
                .getColumnIndex(columnName));
    }

    public static boolean isFileExist(String url) {
        url = Uri.parse(url).getPath();
        return (new File(url).exists());
    }

    public static String readableFileSize(long size) {
        if (size < 0) return "";
        if (size == 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public static boolean isIdEmpty(String id) {
        if (id == null || id.length() == 0) {
            return true;
        }
        return false;
    }

    public static boolean createDirectory(String dir) {
        return dir != null && (new File(dir).mkdirs());
    }

    public static File getCreatedDirectory(String dir) {
        if (dir != null) {
            File file = new File(dir);
            file.mkdirs();
            return file;
        }
        return null;
    }

    public static boolean isValidDirectory(Context context, String dir) {
        Log.i("creating path: " + dir);
        if (!TextUtils.isEmpty(dir)) {
            if (isValidDefaultDirectory(dir)) {
                Log.i("path is default directory");
                return true;
            }
            if (getAppTargetSdkVersion(context) < Build.VERSION_CODES.Q) {
                //File d = new File(dir);
                //d.mkdirs();
                //Log.i("creating path isSuccess: " + d.mkdirs());
                File createdDir = getCreatedDirectory(dir);
                return createdDir != null && createdDir.isDirectory();
            }
        }
        return false;
    }

    public static boolean isValidDefaultDirectory(String dir) {
        return Storage.isValidDownloadDirectory(dir);
    }

    /*static boolean isDownloadEnumContent(final String dir) {
        if (dir == null) {
            return false;
        }
        for (DownloadDirectory c : DownloadDirectory.values()) {
            Log.i("enum: " + c.getValue());
            if (c.getValue().equals(dir)) {
                return true;
            }
        }
        return false;
    }*/

   /*static <E extends Enum<E>> boolean isValidEnum(final Class<E> enumClass, final String enumName) {
        if (enumName == null) {
            return false;
        }
        try {
            Enum.valueOf(enumClass, enumName);
            return true;
        } catch (final IllegalArgumentException ex) {
            Log.i("enum crashed... : "+enumName);
            return false;
        }
    }*/

    public static int getPermissionsError(Context context) {
        //int writeExternalPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int writeExternalPermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        //int internetPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.INTERNET);
        int internetPermission = PermissionChecker.checkSelfPermission(context, Manifest.permission.INTERNET);
        int permissionError = 0;
        boolean externalStorageManagerEnabled = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            externalStorageManagerEnabled = Environment.isExternalStorageManager();
        }

        if (writeExternalPermission < 0 && !externalStorageManagerEnabled) {
            permissionError = DownloadReason.WRITE_EXTERNAL_STORAGE_PERMISSION_REQUIRED.getValue();
            Log.print("Permission required: " + Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (internetPermission < 0) {
            permissionError = DownloadReason.INTERNET_PERMISSION_REQUIRED.getValue();
            Log.print("Permission required: " + Manifest.permission.INTERNET);
        }
        return permissionError;
    }

    public static int getAppTargetSdkVersion(Context context) {
        int version = Build.VERSION_CODES.Q;
        if (context != null) {
            PackageManager pm = context.getPackageManager();
            try {
                if (pm != null) {
                    ApplicationInfo applicationInfo = pm.getApplicationInfo(context.getPackageName(), 0);
                    if (applicationInfo != null) {
                        version = applicationInfo.targetSdkVersion;
                    }
                }
            } catch (Exception e) {
            }
        }
        return version;
    }
}
