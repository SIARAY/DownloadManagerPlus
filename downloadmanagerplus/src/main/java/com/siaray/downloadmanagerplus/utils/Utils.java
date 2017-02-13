package com.siaray.downloadmanagerplus.utils;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

/**
 * Created by Siamak on 08/01/2017.
 */

public class Utils {

    public static boolean createDownloadDir() {
        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                .mkdirs();
    }

    public static String getFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1, url.length());
    }

    public static void openFile(Context context, String url) {
        if (url == null)
            return;
        Uri uri = Uri.fromFile(new File(url));

        Intent intent = new Intent(Intent.ACTION_VIEW);
        // Check what kind of file you are trying to open, by comparing the url with extensions.
        // When the if condition is matched, plugin sets the correct intent (mime) type,
        // so Android knew what application to use to open the file
        if (url.toString().contains(".doc") || url.toString().contains(".docx")) {
            // Word document
            intent.setDataAndType(uri, "application/msword");
        } else if (url.toString().contains(".pdf")) {
            // PDF file
            intent.setDataAndType(uri, "application/pdf");
        } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {
            // Powerpoint file
            intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
        } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {
            // Excel file
            intent.setDataAndType(uri, "application/vnd.ms-excel");
        } else if (url.toString().contains(".zip") || url.toString().contains(".rar")) {
            // WAV audio file
            intent.setDataAndType(uri, "application/zip");
        } else if (url.toString().contains(".rtf")) {
            // RTF file
            intent.setDataAndType(uri, "application/rtf");
        } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {
            // WAV audio file
            intent.setDataAndType(uri, "audio/x-wav");
        } else if (url.toString().contains(".gif")) {
            // GIF file
            intent.setDataAndType(uri, "image/gif");
        } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {
            // JPG file
            intent.setDataAndType(uri, "image/jpeg");
        } else if (url.toString().contains(".txt")) {
            // Text file
            intent.setDataAndType(uri, "text/plain");
        } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") || url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {
            // Video files
            intent.setDataAndType(uri, "video/*");
        } else {
            //if you want you can also define the intent type for any other file

            //additionally use else clause below, to manage other unknown extensions
            //in this case, Android will show all applications installed on the device
            //so you can choose which application to use
            intent.setDataAndType(uri, "*/*");
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean deleteDownload(Context context, String fieldId) {

        SQLiteDatabase db = context.openOrCreateDatabase(Constants.DOWNLOAD_DB_NAME, Context.MODE_PRIVATE, null);
        try {

            db.execSQL("delete from " + Constants.DOWNLOAD_DB_TABLE + " WHERE field_id='" + fieldId+"'");
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
            db = context.openOrCreateDatabase(Constants.DOWNLOAD_DB_NAME, Context.MODE_PRIVATE, null);

            db.execSQL("CREATE TABLE IF NOT EXISTS ["
                    + Constants.DOWNLOAD_DB_TABLE
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

    public static void updateDB(Context context
            , String fieldId
            , String url
            , long downloadId) {
        if (fieldId != null) {
            SQLiteDatabase db = context.openOrCreateDatabase(Constants.DOWNLOAD_DB_NAME, Context.MODE_PRIVATE, null);
            try {
                db.execSQL("INSERT OR REPLACE INTO "
                        + Constants.DOWNLOAD_DB_TABLE
                        + " ( id, field_id, link, download_id ) "
                        + "VALUES "
                        + "((SELECT id FROM "
                        + Constants.DOWNLOAD_DB_TABLE
                        + " WHERE field_id = '"
                        + fieldId
                        + "'),'"
                        + fieldId
                        + "', '"
                        + url
                        + "',"
                        + downloadId
                        + ");");
            } catch (Exception e) {
            } finally {
                db.close();
            }
        }
    }

    public static void addToThreadList(String fieldId, Thread thread) {
        Constants.fieldList.add(fieldId);
        Constants.threadList.add(thread);
        Log.i("add: " + Constants.fieldList.toString());
        //Log.i("no add: " + Constants.fieldList.toString());

    }

    public static void removeFromThreadList(String fieldId) {
        int index = getIdListIndex(fieldId);
        String tn="";
        if (index >= 0) {

            if (index < Constants.fieldList.size()
                    && Constants.fieldList.get(index) != null) {
                Constants.fieldList.remove(index);
                Log.i("remove f list: " + Constants.fieldList.toString()+" id:"+tn);
            }

            if (index < Constants.threadList.size()
                    && Constants.threadList.get(index) != null) {
                tn=Constants.threadList.get(index).getName();
                if (Constants.threadList.get(index).isAlive()
                        || !Constants.threadList.get(index).isInterrupted()) {
                    Log.i("thread stoping:" + Constants.threadList.get(index).getName()+" id:"+tn);
                    Constants.threadList.get(index).interrupt();
                }

                Constants.threadList.remove(index);

                Log.i("remove t list: " + Constants.threadList.toString()+" id:"+tn);
                return;
            }

            Log.i("no remove:null: " + Constants.fieldList.toString()+" id:"+tn);

            return;
        }
        Log.i("no remove: " + Constants.fieldList.toString()
                + Constants.threadList.toString()+" id:"+tn);
    }

    public static int getIdListIndex(String fieldId) {
        int index = Constants.fieldList.indexOf(fieldId);
        Log.i("index: " + index+" :f:"+fieldId);
        return index;
    }
    public static int getThreadListIndex(Thread thread) {
        int index = Constants.threadList.indexOf(thread);
        Log.i("index: " + index+" :t:"+thread);
        return index;
    }
}
