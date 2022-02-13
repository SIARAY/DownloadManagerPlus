package ir.siaray.downloadmanagerplussample;

import android.app.Activity;
import android.app.DownloadManager;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.akexorcist.roundcornerprogressbar.RoundCornerProgressBar;
import com.akexorcist.roundcornerprogressbar.indeterminate.IndeterminateRoundCornerProgressBar;

import java.util.List;

import ir.siaray.downloadmanagerplus.classes.Downloader;
import ir.siaray.downloadmanagerplus.enums.DownloadReason;
import ir.siaray.downloadmanagerplus.enums.DownloadStatus;
import ir.siaray.downloadmanagerplus.enums.Errors;
import ir.siaray.downloadmanagerplus.enums.Storage;
import ir.siaray.downloadmanagerplus.interfaces.ActionListener;
import ir.siaray.downloadmanagerplus.interfaces.DownloadListener;
import ir.siaray.downloadmanagerplus.model.DownloadItem;
import ir.siaray.downloadmanagerplus.utils.Log;
import ir.siaray.downloadmanagerplus.utils.Utils;

import static ir.siaray.downloadmanagerplussample.SampleUtils.DOWNLOAD_DIRECTORY;
import static ir.siaray.downloadmanagerplussample.SampleUtils.NOTIFICATION_VISIBILITY;
import static ir.siaray.downloadmanagerplussample.SampleUtils.setDownloadBackgroundColor;
import static ir.siaray.downloadmanagerplussample.SampleUtils.showPopUpMenu;


public class DownloadListAdapter extends RecyclerView.Adapter<DownloadListAdapter.ViewHolder> {

    private List<FileItem> items;
    private int itemLayout;
    private Activity activity;
    private OnItemClickListener mListener;
    private RecyclerView mRecyclerviewDownloads;

    public DownloadListAdapter(Activity activity, RecyclerView rvDownloads, List<FileItem> items, int itemLayout) {
        this.items = items;
        this.itemLayout = itemLayout;
        this.activity = activity;
        mRecyclerviewDownloads = rvDownloads;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(itemLayout, parent, false);
        final ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final FileItem item = items.get(position);
        holder.tvName.setText(Utils.getFileName(item.getUri()));
        holder.btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickOnActionButton(holder, item);
            }
        });
        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //deleteDownload(holder, item, position);
                final ActionListener deleteListener = getDeleteListener(holder
                        , holder.downloadProgressBar
                        , item
                        , position);
                showPopUpMenu(activity, view, item, deleteListener);

            }
        });
        showProgress(holder, item, position);
        Log.print("i: " + position + " item: " + item);
        Log.print("status: " + position + " : " + item.getDownloadStatus());

        initItem(holder, item);
        holder.itemView.setTag(item);
    }

    private void initItem(ViewHolder holder, FileItem item) {
        switch (item.getDownloadStatus()) {
            case NONE:
            case CANCELED:
            case FAILED:
                holder.ivAction.setImageResource(R.mipmap.ic_start);
                break;
            case PAUSED:
                holder.ivAction.setImageResource(R.mipmap.ic_cancel);
                break;
            case PENDING:
                holder.ivAction.setImageResource(R.mipmap.ic_cancel);
                break;
            case SUCCESSFUL:
                holder.ivAction.setImageResource(R.mipmap.ic_complete);
                break;
            case RUNNING:
                holder.ivAction.setImageResource(R.mipmap.ic_cancel);
                break;
            default:
                holder.ivAction.setImageResource(R.mipmap.ic_start);
        }
        holder.downloadProgressBar.setProgress(item.getPercent());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView ivAction;
        private RoundCornerProgressBar downloadProgressBar;
        private ViewGroup btnAction;
        private ViewGroup btnDelete;
        private IndeterminateRoundCornerProgressBar loading;
        private TextView tvName;
        private DownloadListener listener;
        private TextView tvSize;
        private TextView tvSpeed;
        private TextView tvPercent;

        private ViewHolder(View itemView) {
            super(itemView);
            ivAction = (ImageView) itemView.findViewById(R.id.iv_image);
            btnAction = (ViewGroup) itemView.findViewById(R.id.btn_action);
            tvName = (TextView) itemView.findViewById(R.id.tv_name);
            downloadProgressBar = (RoundCornerProgressBar) itemView.findViewById(R.id.progressbar);
            btnDelete = (ViewGroup) itemView.findViewById(R.id.btn_delete);
            loading = (IndeterminateRoundCornerProgressBar) itemView.findViewById(R.id.loading);
            tvSize = itemView.findViewById(R.id.tv_size);
            tvSpeed = itemView.findViewById(R.id.tv_speed);
            tvPercent = itemView.findViewById(R.id.tv_percent);
        }
    }

    private void showProgress(ViewHolder holder, FileItem item, int position) {
        initListener(holder, item, position);

        Downloader.getInstance(activity)
                .setUrl(item.getUri())
                .setListener(holder.listener)
                .setToken(item.getToken())
                .setDestinationDir(Storage.DIRECTORY_DOWNLOADS
                        , Utils.getFileName(item.getUri()))
                .setNotificationTitle(Utils.getFileName(item.getUri()))
                .showProgress();
    }

    private void initListener(final ViewHolder holder, final FileItem item, final int position) {
        Log.print("position visible: " + position);
        holder.listener = new DownloadListener() {
//            DownloadStatus lastStatus = DownloadStatus.NONE;
//            long startTime = 0;
//            int lastDownloadedBytes = 0;
//            int lastPercent = 0;

            @Override
            public void onComplete(int totalBytes, DownloadItem downloadInfo) {
                Log.print("onComplete: " + position);
                item.setDownloadStatus(DownloadStatus.SUCCESSFUL);
                item.setPercent(100);
                if (isCurrentListViewItemVisible(position)) {
                    holder.ivAction.setImageResource(R.mipmap.ic_complete);
                    holder.downloadProgressBar.setProgress(100);
                    holder.loading.setVisibility(View.GONE);
                    holder.tvPercent.setText("100%");
                    holder.tvSize.setText(Utils.readableFileSize(totalBytes)
                            + "/" + Utils.readableFileSize(totalBytes) + " - Completed");
                    setDownloadBackgroundColor(holder.btnAction, DownloadStatus.SUCCESSFUL);
                }
            }

            @Override
            public void onPause(int percent, DownloadReason reason, int totalBytes, int downloadedBytes, DownloadItem downloadInfo) {
                Log.print("onPause: " + position);
                if (isCurrentListViewItemVisible(position)) {
                    if (item.getDownloadStatus() != DownloadStatus.PAUSED) {
                        holder.ivAction.setImageResource(R.mipmap.ic_cancel);
                        holder.downloadProgressBar.setProgress(percent);
                        holder.loading.setVisibility(View.VISIBLE);
                        holder.tvPercent.setText(item.getPercent() + "%");
                        holder.tvSize.setText(Utils.readableFileSize(downloadedBytes)
                                + "/" + Utils.readableFileSize(totalBytes) + " - Paused");
                        setDownloadBackgroundColor(holder.btnAction, DownloadStatus.PAUSED);
                    }
                }
                item.setDownloadStatus(DownloadStatus.PAUSED);
            }

            @Override
            public void onPending(int percent, int totalBytes, int downloadedBytes, DownloadItem downloadInfo) {
                Log.print("onPending: " + position + " : " + isCurrentListViewItemVisible(position));
                if (isCurrentListViewItemVisible(position)) {
                    if (item.getDownloadStatus() != DownloadStatus.PENDING) {
                        holder.ivAction.setImageResource(R.mipmap.ic_cancel);
                        holder.downloadProgressBar.setProgress(percent);
                        holder.loading.setVisibility(View.VISIBLE);
                        holder.tvPercent.setText(item.getPercent() + "%");
                        holder.tvSize.setText(Utils.readableFileSize(downloadedBytes)
                                + "/" + Utils.readableFileSize(totalBytes) + " - Pending");
                        setDownloadBackgroundColor(holder.btnAction, DownloadStatus.PENDING);
                    }
                }
                item.setDownloadStatus(DownloadStatus.PENDING);
            }

            @Override
            public void onFail(int percent, DownloadReason reason, int totalBytes, int downloadedBytes, DownloadItem downloadInfo) {
                Log.print("onFail: " + position);
                item.setDownloadStatus(DownloadStatus.FAILED);
                item.setPercent(0);
                if (isCurrentListViewItemVisible(position)) {
                    holder.ivAction.setImageResource(R.mipmap.ic_start);
                    holder.downloadProgressBar.setProgress(item.getPercent());
                    holder.loading.setVisibility(View.GONE);
                    holder.tvPercent.setText(item.getPercent() + "%");
                    holder.tvSize.setText(Utils.readableFileSize(downloadedBytes)
                            + "/" + Utils.readableFileSize(totalBytes) + " - Failed");
                    setDownloadBackgroundColor(holder.btnAction, DownloadStatus.FAILED);
                }
                holder.loading.setVisibility(View.GONE);
            }

            @Override
            public void onCancel(int totalBytes, int downloadedBytes, DownloadItem downloadInfo) {
                Log.print("onCancel: " + position);
                item.setDownloadStatus(DownloadStatus.CANCELED);
                item.setPercent(0);
                if (isCurrentListViewItemVisible(position)) {
                    holder.ivAction.setImageResource(R.mipmap.ic_start);
                    holder.downloadProgressBar.setProgress(item.getPercent());
                    holder.loading.setVisibility(View.GONE);
                    holder.tvPercent.setText(item.getPercent() + "%");
                    holder.tvSize.setText(Utils.readableFileSize(downloadedBytes)
                            + "/" + Utils.readableFileSize(totalBytes) + " - Canceled");
                    setDownloadBackgroundColor(holder.btnAction, DownloadStatus.CANCELED);
                }
            }

            @Override
            public void onRunning(int percent, int totalBytes, int downloadedBytes, float downloadSpeed, DownloadItem downloadInfo) {
                Log.print("onRunning: " + position);
                item.setDownloadStatus(DownloadStatus.RUNNING);
                item.setPercent(percent);
                if (isCurrentListViewItemVisible(position)) {
                    holder.ivAction.setImageResource(R.mipmap.ic_cancel);
                    holder.downloadProgressBar.setProgress(item.getPercent());
                    holder.loading.setVisibility(View.GONE);
                    holder.tvPercent.setText(item.getPercent() + "%");
                    if (totalBytes < 0 || downloadedBytes < 0)
                        holder.tvSize.setText("loading...");
                    else
                        holder.tvSize.setText(Utils.readableFileSize(downloadedBytes)
                                + "/" + Utils.readableFileSize(totalBytes));
                    holder.tvSpeed.setText(Math.round(downloadSpeed) + " KB/sec");
                    setDownloadBackgroundColor(holder.btnAction, DownloadStatus.RUNNING);
                }


                Log.i("Title: " + downloadInfo.getTitle());
            }

        };
    }

    private void clickOnActionButton(ViewHolder holder, FileItem item) {
        if (!SampleUtils.isStoragePermissionGranted(activity))
            return;
        final Downloader downloader = getDownloader(holder, item);
        if (downloader.getStatus(item.getToken()) == DownloadStatus.RUNNING
                || downloader.getStatus(item.getToken()) == DownloadStatus.PAUSED
                || downloader.getStatus(item.getToken()) == DownloadStatus.PENDING)
            downloader.cancel(item.getToken());
        else if (downloader.getStatus(item.getToken()) == DownloadStatus.SUCCESSFUL) {
            Utils.openFile(activity, downloader.getDownloadedFilePath(item.getToken()));
        } else
            downloader.start();
    }

    private Downloader getDownloader(ViewHolder holder, FileItem item) {
        Downloader request = Downloader.getInstance(activity)
                .setListener(holder.listener)
                .setUrl(item.getUri())
                .setToken(item.getToken())
                .setKeptAllDownload(false)//if true: canceled download token keep in db
                .setAllowedOverRoaming(true)
                .setVisibleInDownloadsUi(true)
                .setDescription(Utils.readableFileSize(item.getFileSize()))
                .setScanningByMediaScanner(true)
                .setNotificationVisibility(NOTIFICATION_VISIBILITY)
                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                //.setCustomDestinationDir(DOWNLOAD_DIRECTORY, Utils.getFileName(item.getUri()))//TargetApi 28 and lower
                .setDestinationDir(DOWNLOAD_DIRECTORY, Utils.getFileName(item.getUri()))
                .setNotificationTitle(SampleUtils.getFileShortName(Utils.getFileName(item.getUri())));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            request.setAllowedOverMetered(true); //Api 16 and higher
        }
        return request;
    }
    /*private void startDownload(ViewHolder holder, FileItem item) {
        Log.i("start");
        Downloader downloader = Downloader.getInstance(context)
                .setUrl(item.getUri())
                .setListener(holder.listener)
                .setToken(item.getToken())
                .setDestinationDir(Environment.DIRECTORY_DOWNLOADS
                        , Utils.getFileName(item.getUri()))
                .setNotificationTitle(Utils.getFileName(item.getUri()));
        if (downloader.getStatus(item.getToken()) == DownloadStatus.RUNNING)
            downloader.cancel(item.getToken());
        else if (downloader.getStatus(item.getToken()) == DownloadStatus.SUCCESSFUL) {
            Utils.openFile(context, downloader.getDownloadedFilePath(item.getToken()));
        } else
            downloader.start();
    }*/

    /*private void deleteDownload(ViewHolder holder, FileItem item, int position) {
        Log.print("delete");
        *//*Downloader downloader = new Downloader(context, MainActivity.downloadManager, item.getUri())
                .setListener(holder.listener)
                .setToken(item.getToken())
                .setDestinationDir(Environment.DIRECTORY_DOWNLOADS
                        , Utils.getFileName(item.getUri()))
                .setNotificationTitle(Utils.getFileName(item.getUri()));*//*
        //if (downloader.getStatus(item.getToken()) == DownloadStatus.RUNNING)
        //downloader.deleteFile(item.getToken());
        final ActionListener deleteListener = getDeleteListener(holder.ivAction
                , holder.btnAction
                , holder.downloadProgressBar
                , item
                , position);

        Downloader downloader = Downloader.getInstance(activity)
                .setUrl(item.getUri())
                .setListener(holder.listener);

        downloader.deleteFile(item.getToken(), deleteListener);
    }*/

    private ActionListener getDeleteListener(final ViewHolder holder
            , final RoundCornerProgressBar numberProgressBar
            , final FileItem item
            , final int position) {
        return new ActionListener() {
            @Override
            public void onSuccess() {
                item.setPercent(0);
                //item.setDownloadStatus(DownloadStatus.NONE);
                //if (isCurrentListViewItemVisible(position)) {
                holder.ivAction.setImageResource(R.mipmap.ic_start);
                numberProgressBar.setProgress(item.getPercent());
                Toast.makeText(activity, "Deleted", Toast.LENGTH_SHORT).show();
                holder.downloadProgressBar.setProgress(item.getPercent());
                holder.loading.setVisibility(View.GONE);
                holder.tvPercent.setText(item.getPercent() + "%");
                holder.tvSize.setText("Deleted");
                setDownloadBackgroundColor(holder.btnAction, DownloadStatus.CANCELED);
                // }
            }

            @Override
            public void onFailure(Errors error) {
                Toast.makeText(activity, "" + error, Toast.LENGTH_SHORT).show();
            }
        };
    }

    private boolean isCurrentListViewItemVisible(int position) {
        LinearLayoutManager layoutManager = (LinearLayoutManager) mRecyclerviewDownloads.getLayoutManager();
        int first = layoutManager.findFirstVisibleItemPosition();
        int last = layoutManager.findLastVisibleItemPosition();
        return first <= position && position <= last;
    }

    private DownloadListAdapter.ViewHolder getViewHolder(int position) {
        return (DownloadListAdapter.ViewHolder) mRecyclerviewDownloads.findViewHolderForLayoutPosition(position);
    }
}
