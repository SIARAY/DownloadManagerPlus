## DownloadManagerPlus

Using faster and easier than Android Download Manager

## Screenshots

![Screenshot](https://raw.githubusercontent.com/SIARAY/DownloadManagerPlus/master/art/downloadmanagerplus-v1.1.1.gif)

## Getting started

##### Dependency
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}

    dependencies {
        //implementation 'ir.siaray:downloadmanagerplus:1.4.1' //last version
        implementation 'com.github.SIARAY:DownloadManagerPlus:1.4.2' // new version
    }

## Usage

##### To start the download.

    Downloader downloader = Downloader.getInstance(context)
     .setUrl(url)
     .setListener(listener)
     .setToken(token)
     .setAllowedOverRoaming(roamingAllowed)
     .setAllowedOverMetered(meteredAllowed) //Api 16 and higher
     .setVisibleInDownloadsUi(isVisible)
     .setDestinationDir(directory, fileName)
     .setNotificationTitle(notificationTitle)
     .setDescription(description)
     .setNotificationVisibility(visibility)
     .setAllowedNetworkTypes(networkTypes)
     .setKeptAllDownload(allDownloadKept); 
     
    downloader.start();

##### To view download status and progress that has already started.

    downloader.showProgress();

##### To cancel a download.

    downloader.cancel(token);
    
##### To pause a download.

    downloader.pause();
    or
    Downloader.pause(context, token);
    
##### To resume a download.

    downloader.resume();
    or
    Downloader.resume(context, token);

##### Detect download status.

    downloader.getStatus(token);

##### Delete the downloaded file.

    downloader.deleteFile(token, deleteListener);

##### Get download id.

    Downloader.getDownloadId(context, token);

##### Get download plus token.
> `downloadId parameter is android download manager id`

    Downloader.getToken(context, downloadId);

##### Get download item.

    Downloader.getDownloadItem(context, token);

##### Get downloads list.

    Downloader.getDownloadsList(context);

##### Download Notification BroadcastReceiver.
    class YourNotificationBroadcastReceiver extends NotificationBroadcastReceiver{
        @Override
        public void onCompleted(Context context, Intent intent, long downloadId) {
            super.onCompleted(context, intent, downloadId);
        }

        @Override
        public void onClicked(Context context, Intent intent, long[] downloadIdList) {
            super.onClicked(context, intent, downloadIdList);
        }

        @Override
        public void onFailed(Context context, Intent intent, long downloadId) {
            super.onFailed(context, intent, downloadId);
        }
    }    
    
> `To register YourBroadcastReceiver add to your app Manifest`

    <receiver android:name=".YourNotificationBroadcastReceiver">
        <intent-filter>
            <action android:name="android.intent.action.DOWNLOAD_COMPLETE"/>
            <action android:name="android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED" />
        </intent-filter>
    </receiver>

## License

    Copyright 2017 Siamak Rayeji

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.