## DownloadManagerPlus

Using faster and easier than Android Download Manager

![Screenshot](https://gifyu.com/images/out20c239.gif)

## Usage

##### To start the download.

    Downloader downloader = new Downloader(context, downloadManager, url)
     .setListener(listener)
     .setDownloadId(downloadId)
     .setDestinationDir(path, fileName)
     .setNotificationTitle(notificationTitle));
     
    downloader.start();

##### To view download status that has already started.

    downloader.showProgress();

##### To cancel a download.

    downloader.cancel(downloadId);

##### Detect download status.

    downloader.getStatus(downloadId);

##### Delete the downloaded file.

    downloader.deleteFile(downloadId, deleteListener);

