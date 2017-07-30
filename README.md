## DownloadManagerPlus

Using faster and easier than Android Download Manager

## Screenshots

![Screenshot](https://gifyu.com/images/downloadmanagerplus-v1.1.1.gif)

## Getting started

##### Dependency

    dependencies {
        compile 'com.siaray:downloadmanagerplus:1.1.1'
    }

## Usage

##### To start the download.

    Downloader downloader = new Downloader(context, downloadManager)
     .setUrl(url)
     .setListener(listener)
     .setId(id)
     .setAllowedOverRoaming(roamingAllowed)
     .setAllowedOverMetered(meteredAllowed) //Api 16 and higher
     .setVisibleInDownloadsUi(isVisible)
     .setDestinationDir(path, fileName)
     .setNotificationTitle(notificationTitle)
     .setDescription(description)
     .setNotificationVisibility(visibility)
     .setAllowedNetworkTypes(networkTypes);
     
    downloader.start();

##### To view download status and progress that has already started.

    downloader.showProgress();

##### To cancel a download.

    downloader.cancel(downloadId);

##### Detect download status.

    downloader.getStatus(downloadId);

##### Delete the downloaded file.

    downloader.deleteFile(downloadId, deleteListener);

##### Get download id.

    Downloader.getDownloadId(context, id);

##### Get download plus id.

    Downloader.getId(context, downloadId);

##### Get download item.

    Downloader.getDownloadItem(context, downloadManager, id);

##### Get downloads list.

    Downloader.getDownloadsList(context, downloadManager);

