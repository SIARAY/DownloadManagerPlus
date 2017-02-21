package com.siaray.downloadmanagerplussample;

import java.util.List;

/**
 * Created by Siamak on 28/01/2017.
 */

public class SampleUtils {
    public static FileItem getDownloadItem(int number) {
        FileItem item = new FileItem();

        if(number==1) {
            item.setId("id1245");
            String link = "http://wallpaperswide.com/download/friendship_4-wallpaper-1920x1200.jpg";
            item.setLink(link);
        }else if(number==2){
            item.setId("id1249");
            String link = "http://as3.cdn.asset.aparat.com/aparat-video/6a264ee6d22be3ab6a00fd3a774316424062813-360p__64493.mp4";
            item.setLink(link);
        }else{
            item.setId("id1280");
            String link = "http://dl.smusic.ir/saal/95/6/Saman%20Jalili%20-%20Dastkhat.mp3";
            item.setLink(link);
        }
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
