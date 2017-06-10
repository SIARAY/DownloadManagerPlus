package com.siaray.downloadmanagerplus.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import java.io.File;

/**
 * Created by Siamak on 08/01/2017.
 */

public class Utils {

    /*public static boolean createDownloadDir() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .mkdirs();
    }*/

    public static String getFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1, url.length());
    }

    public static void openFile(Context context, String url) {
        if (url == null)
            return;
        url=Uri.parse(url).getPath();
        Uri uri = Uri.fromFile(new File(url));

        Intent intent = new Intent(Intent.ACTION_VIEW);

        if (url.contains(".doc") || url.contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if (url.contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if (url.contains(".ppt") || url.contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (url.contains(".xls") || url.contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (url.contains(".zip") || url.contains(".rar")) {
            // zip file
            intent.setDataAndType(uri, "application/zip");
        } else if (url.contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if (url.contains(".wav") || url.contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (url.contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if (url.contains(".jpg") || url.contains(".jpeg") || url.contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if (url.contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if (url.contains(".3gp")
                || url.contains(".mpg")
                || url.contains(".mpeg")
                || url.contains(".mpe")
                || url.contains(".mp4")
                || url.contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean deleteDownload(Context context, String id) {

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
        if (id != null) {
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
        }
    }

    public static void addToThreadList(String id, Thread thread) {
        if (id == null)
            return;
        Constants.fieldList.add(id);
        Constants.threadList.add(thread);
    }

    public static void removeFromThreadList(String id) {
        if (id == null)
            return;
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

    public static boolean isFileExist(String path) {
        return (new File(path).exists());
    }
}
