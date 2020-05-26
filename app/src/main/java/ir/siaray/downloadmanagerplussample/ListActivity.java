package ir.siaray.downloadmanagerplussample;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ir.siaray.downloadmanagerplus.classes.Downloader;
import ir.siaray.downloadmanagerplus.model.DownloadItem;

import java.util.ArrayList;
import java.util.List;

public class ListActivity extends AppCompatActivity implements OnItemClickListener<FileItem> {

    List<FileItem> list = new ArrayList<>();
    DownloadListAdapter adapter;
    private RecyclerView rvDownloads;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        initUi();
    }


    private void initUi() {
        //SampleUtils.getFileList(list, 30);
        //list=Downloader.getDownloadsList(MainActivity.downloadManager);
        List<DownloadItem> downloadList = new ArrayList<>();
                //Downloader.getDownloadsList(getApplicationContext());
        SampleUtils.getFileList(downloadList,10);
        copyDownloadListToMyCustomList(downloadList, list);
        TextView tvEmpty = findViewById(R.id.tvEmpty);
        if (downloadList.size() == 0) {
            tvEmpty.setVisibility(View.VISIBLE);
        }
        rvDownloads = (RecyclerView) findViewById(R.id.rv_downloads);
        adapter = new DownloadListAdapter(ListActivity.this, rvDownloads, list, R.layout.download_list_item);
        LinearLayoutManager llm = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        adapter.setOnItemClickListener(this);
        rvDownloads.setLayoutManager(llm);
        rvDownloads.setAdapter(adapter);

    }

    private void copyDownloadListToMyCustomList(List<DownloadItem> downloadList, List<FileItem> fileList) {
        for (DownloadItem item : downloadList) {
            FileItem fileItem = new FileItem(item);
            fileList.add(fileItem);
        }
    }

    @Override
    public void onItemClick(View v, int position, FileItem fileItem) {

    }

    /*class DownloadCallback implements DownloadListener {

        private int mPosition;
        private FileItem mItem;

        public DownloadCallback(int position, FileItem item) {
            mPosition = position;
            mItem = item;
        }

        @Override
        public void onComplete() {

        }

        @Override
        public void onPause(int percent, String msg, String reason) {

        }

        @Override
        public void onPending(int percent, String msg) {

        }

        @Override
        public void onFail(String msg, String reason) {

        }

        @Override
        public void onCancel(String msg) {

        }

        @Override
        public void onRunning(int percent, int mTotalBytes, int mDownloadedBytes) {
            //String downloadPerSize = getDownloadPerSize(finished, total);
            mItem.setPercent(percent);
            //mAppInfo.setDownloadPerSize(downloadPerSize);
            mItem.setDownloadStatus(DownloadStatus.RUNNING);
            if (isCurrentListViewItemVisible(mPosition)) {
                DownloadListAdapter.ViewHolder holder = getViewHolder(mPosition);
                //holder.tvDownloadPerSize.setText(downloadPerSize);
                holder.numberProgressBar.setProgress(percent);
                //holder.tvStatus.setText(mAppInfo.getStatusText());
                //holder.btnDownload.setText(mAppInfo.getButtonText());
            }
        }

        *//*@Override
        public void onMessage(Result results, String msg) {

        }*//*
    }*/

    private boolean isCurrentListViewItemVisible(int position) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) rvDownloads.getLayoutManager();
        int first = layoutManager.findFirstVisibleItemPosition();
        int last = layoutManager.findLastVisibleItemPosition();
        return first <= position && position <= last;
    }

    private DownloadListAdapter.ViewHolder getViewHolder(int position) {
        return (DownloadListAdapter.ViewHolder) rvDownloads.findViewHolderForLayoutPosition(position);
    }
}
