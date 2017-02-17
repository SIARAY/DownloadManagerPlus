package com.siaray.downloadmanagerplussample;

import java.util.List;

/**
 * Created by Siamak on 28/01/2017.
 */

public class SampleUtils {
    public static FileItem getFirstDownloadItem() {
        FileItem item = new FileItem();
        item.setId("1245");
        //String link = "http://www.dl.downloadsoftware.ir/music/bikalam/Carter%20Burwell%20-%20Fathers%20Gun.zip";
        //String link = "http://hw2.asset.aparat.com/aparat-video/a_b90di71g188j92gm947076g6i3594d032g6457306105-471s__31c96.mp4";
        String link = "http://dolly.roslin.ed.ac.uk/wp-content/uploads/2016/01/DollySideView.jpg";
        item.setLink(link);
        return item;
    }

    public static FileItem getSecondDownloadItem() {
        FileItem item = new FileItem();
        item.setId("1249");
        //String link = "http://www.dl.downloadsoftware.ir/music/bikalam/Carter%20Burwell%20-%20Fathers%20Gun.zip";
        String link = "http://hw2.asset.aparat.com/aparat-video/a_b90di71g188j92gm947076g6i3594d032g6457306105-471s__31c96.mp4";
        item.setLink(link);
        return item;
    }

    ////////////////////////////////////////////////////////////////////////////
    public static void getFileList(List<FileItem> list, int number) {
        for (int i = 0; i < number; i++) {
            FileItem item = new FileItem();
            item.setId("" + i);
            String link = "";
            switch (i % 5) {
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
                    link = "http://dl.pop-music.ir/music/1395/Dey/Mohammad%20Alizadeh%20-%20Goftam%20Naro%20(Teaser%20Album)%20480.mp4";
                    break;
                default:
                    link = "http://dl.pop-music.ir/music/1395/Dey/Saman%20Jalili%20-%20Tars.mp3";
            }
            item.setLink(link);
            list.add(item);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    /*public static String getFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1, url.length());
    }*/
}
