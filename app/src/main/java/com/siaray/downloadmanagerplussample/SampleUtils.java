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
            item.setLink(link);
            list.add(item);
        }
    }

    ////////////////////////////////////////////////////////////////////////////
    /*public static String getFileName(String url) {
        return url.substring(url.lastIndexOf("/") + 1, url.length());
    }*/
}
