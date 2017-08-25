package com.ksyun.media.shortvideo.demo.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ksyun.media.shortvideo.demo.R;
import com.ksyun.media.shortvideo.demo.util.DownloadAndHandleTask;
import com.ksyun.media.shortvideo.demo.util.FileUtils;

import java.io.File;
import java.util.List;

/**
 * 背景音乐选择的适配器类
 */

public class BgmSelectAdapter extends RecyclerView.Adapter<BgmSelectAdapter.MyViewHolder> {
    private Context mContext;
    private List<BgmData> mData;
    private DownloadAndHandleTask mBgmLoadTask;
    private MyViewHolder mPreHolder;
    private OnItemClickListener mListener;
    private volatile String mCurrentFilePath;
    //背景音乐下载地址
    private String[] mBgmLoadPath = {"https://ks3-cn-beijing.ksyun.com/ksy.vcloud.sdk/ShortVideo/faded.mp3",
            "https://ks3-cn-beijing.ksyun.com/ksy.vcloud.sdk/ShortVideo/Hotel_California.mp3",
            "https://ks3-cn-beijing.ksyun.com/ksy.vcloud.sdk/ShortVideo/Immortals.mp3"};
    public static final int INDEX_CANCEL = 0;
    public static final int INDEX_IMPORT = 4;

    public interface OnItemClickListener {
        void onCancel();

        boolean onSelected(String path);

        void onImport();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public BgmSelectAdapter(Context context, List<BgmData> data) {
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.bgm_item_view, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        BgmData data = mData.get(position);
        if (position != 0 && position != mData.size() - 1) {
            holder.mText.setText(data.text);
            File file = new File(getFilePath(position - 1));
            if (file.exists()) {
                holder.mDownload.setVisibility(View.GONE);
            }
        } else {
            holder.mDownload.setVisibility(View.GONE);
        }
        holder.mContent.setImageDrawable(data.drawable);
        holder.mContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onImageClick(holder, position);
            }
        });
    }

    public void onImageClick(final MyViewHolder holder, int index) {
        if (index == INDEX_CANCEL) {
            mCurrentFilePath = null;
            clear();
            if (mListener != null) {
                mListener.onCancel();
            }
        } else if (index == INDEX_IMPORT) {
            mCurrentFilePath = null;
            clear();
            if (mListener != null) {
                mListener.onImport();
            }
        } else {
            if (mPreHolder != null) {
                mPreHolder.setActivated(false);
            }
            final String filePath = getFilePath(index - 1);
            File file = new File(filePath);
            if (!file.exists()) {
                if (mBgmLoadTask != null && mBgmLoadTask.getStatus() == AsyncTask.Status.RUNNING) {
                    clear();
                    mBgmLoadTask.cancel(true);
                }
                DownloadAndHandleTask.DownloadListener listener = new DownloadAndHandleTask.DownloadListener() {
                    @Override
                    public void onCompleted(String downloadFilePath) {
                        holder.mDownload.setVisibility(View.GONE);
                        holder.mProgress.setVisibility(View.GONE);

                        if (!TextUtils.isEmpty(mCurrentFilePath) && mCurrentFilePath.equals
                                (downloadFilePath)) {
                            if (mListener != null) {
                                boolean isShow = mListener.onSelected(mCurrentFilePath);
                                if(isShow) {
                                    holder.setActivated(true);
                                }
                            }
                        }
                    }
                };
                mBgmLoadTask = new DownloadAndHandleTask(filePath, listener);
                mBgmLoadTask.execute(mBgmLoadPath[index - 1]);
                holder.mProgress.setVisibility(View.VISIBLE);
            } else {
                holder.mDownload.setVisibility(View.GONE);
                holder.mProgress.setVisibility(View.GONE);
                holder.setActivated(true);
                if (mListener != null) {
                    mListener.onSelected(filePath);
                }
            }
            mCurrentFilePath = filePath;
        }

        mPreHolder = holder;
    }

    public String getFilePath(int index) {
        if (index >= 0 && index < 3) {
            String fileName = mBgmLoadPath[index].substring(mBgmLoadPath[index].lastIndexOf('/'));
            String filePath = FileUtils.getCacheDirectory(mContext.getApplicationContext()) + fileName;
            return filePath;
        } else {
            return null;
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void clear() {
        if (mPreHolder != null) {
            mPreHolder.mDownload.setVisibility(View.GONE);
            mPreHolder.mProgress.setVisibility(View.GONE);
            mPreHolder.setActivated(false);
        }
    }

    public void clearTask() {
        //同时取消下载任务
        if (mBgmLoadTask != null && mBgmLoadTask.getStatus() == AsyncTask.Status.RUNNING) {
            mBgmLoadTask.cancel(true);
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView mContent;
        public ImageView mBorder;
        public ImageView mDownload;
        public ProgressBar mProgress;
        public TextView mText;

        public MyViewHolder(View view) {
            super(view);
            mContent = (ImageView) view.findViewById(R.id.bgm_image_content);
            mBorder = (ImageView) view.findViewById(R.id.bgm_image_border);
            mDownload = (ImageView) view.findViewById(R.id.bgm_image_download);
            mProgress = (ProgressBar) view.findViewById(R.id.download_progress);
            mText = (TextView) view.findViewById(R.id.bgm_name);
        }

        public void setActivated(boolean active) {
            if (active) {
                mBorder.setVisibility(View.VISIBLE);
                mText.setActivated(true);
            } else {
                mBorder.setVisibility(View.GONE);
                mText.setActivated(false);
            }
        }
    }

    public static class BgmData {
        public Drawable drawable;
        public String text;

        public BgmData(Drawable image, String type) {
            this.drawable = image;
            this.text = type;
        }
    }
}
