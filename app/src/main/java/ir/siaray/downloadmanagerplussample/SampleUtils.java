package ir.siaray.downloadmanagerplussample;

import android.content.ClipboardManager;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by Siamak on 28/01/2017.
 */

public class SampleUtils {
    public static FileItem getDownloadItem(int number) {
        FileItem item = new FileItem();

        if (number == 1) {
            item.setId("id1245");
            String link = "http://wallpaperswide.com/download/friendship_4-wallpaper-1920x1200.jpg";
            item.setUri(link);
        } else if (number == 2) {
            item.setId("id1249");
            //String link = "http://www.sample-videos.com/video/mp4/480/big_buck_bunny_480p_10mb.mp4";
            String link = "https://hw7.cdn.asset.aparat.com/aparat-video/8752c8e2a411ce9e486f37983f21017411515047-360p__95568.mp4";
            item.setUri(link);
        } else {
            item.setId("id1280");
            String link = "http://dl.smusic.ir/saal/95/6/Saman%20Jalili%20-%20Dastkhat.mp3";
            item.setUri(link);
        }
        return item;
    }

    public static void getFileList(List<FileItem> list, int number) {
        for (int i = 0; i < number; i++) {
            FileItem item = new FileItem();
            item.setId("" + i);
            String link = "";
            switch (i % 12) {
                case 0:
                    link = "https://s3-us-west-1.amazonaws.com/powr/defaults/image-slider2.jpg";
                    break;
                case 1:
                    link = "http://dolly.roslin.ed.ac.uk/wp-content/uploads/2016/01/DollySideView.jpg";
                    break;
                case 2:
                    link = "http://www.dl.downloadsoftware.ir/music/bikalam/Carter%20Burwell%20-%20Fathers%20Gun.zip";
                    break;
                case 3:
                    link = "http://dl5.downloadha.com/hosein/NarmAfzaar/April%202017/Firefox.53.0.Final.Win.x64.en-US%20(www.Downloadha.com).zip";
                    break;
                case 4:
                    link = "http://hdwallpaperbackgrounds.net/wp-content/uploads/2016/07/4k-wallpaper-11.jpg";
                    break;
                case 5:
                    link = "http://wallpaperpulse.com/img/2242184.jpg";
                    break;
                case 6:
                    link = "http://free4kwallpaper.com/wp-content/uploads/2016/01/Beautiful-Girl-in-Nature-4K-Wallpaper.jpg";
                    break;
                case 7:
                    link = "https://s-media-cache-ak0.pinimg.com/originals/5a/7a/f6/5a7af672ea944a471b1420e411743461.jpg";
                    break;
                case 8:
                    link = "http://yesofcorsa.com/wp-content/uploads/2016/12/4k-Love-Wallpaper-HQ-1024x576.jpg";
                    break;
                case 9:
                    link = "http://yesofcorsa.com/wp-content/uploads/2017/01/4K-Rain-Wallpaper-Download-1024x640.jpeg";
                    break;
                case 10:
                    link = "http://zonewallpaper.net/wp-content/uploads/2016/11/Best-4K-Nature-Wallpaper-2016.jpeg";
                    break;
                case 11:
                    link = "http://desktopwalls.net/wp-content/uploads/2015/09/" +
                            "Fortress%20Town%20Lake%20Bridge%204K%20Ultra%20HD%20Desktop%20Wallpaper.jpg";
                    break;

                default:
                    link = "http://dl.pop-music.ir/music/1395/Dey/Saman%20Jalili%20-%20Tars.mp3";
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
}
