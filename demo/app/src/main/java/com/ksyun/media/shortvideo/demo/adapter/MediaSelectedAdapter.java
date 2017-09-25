package com.ksyun.media.shortvideo.demo.adapter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ksyun.media.shortvideo.demo.R;
import com.ksyun.media.shortvideo.demo.media.MediaInfo;
import com.ksyun.media.shortvideo.demo.media.ThumbnailGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 选择文件列表适配器
 */

public class MediaSelectedAdapter extends RecyclerView.Adapter<MediaSelectedAdapter.MediaSelectedViewHolder>
        implements View.OnClickListener {
    private List<MediaInfo> mDataList;
    private ThumbnailGenerator mGenerator;
    private long mCurrentDuration;
    private ItemCallback mCallback;

    public interface ItemCallback {
        void onDurationUpdate(long duration);
    }

    public void setItemCallback(ItemCallback callback) {
        this.mCallback = callback;
    }

    public MediaSelectedAdapter(ThumbnailGenerator thumbnailGenerator) {
        this.mGenerator = thumbnailGenerator;
        this.mDataList = new ArrayList<>();
    }

    @Override
    public MediaSelectedViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_selected_item, parent, false);
        MediaSelectedViewHolder holder = new MediaSelectedViewHolder(view);
        holder.mDelete.setOnClickListener(this);
        return holder;
    }

    @Override
    public void onBindViewHolder(MediaSelectedViewHolder holder, int position) {
        if (mDataList != null && position < mDataList.size()) {
            bindDate(mDataList.get(position), holder);
        }
    }

    private void bindDate(final MediaInfo info, final MediaSelectedViewHolder holder) {
        if (info == null) {
            return;
        }
        if (info.thumbPath != null && isFileExist(info.thumbPath)) {
            String uri = "file://" + info.thumbPath;
            DisplayImageOptions options = new DisplayImageOptions.Builder()
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .build();
            ImageLoader.getInstance().displayImage(uri, holder.mPhoto, options);
        } else {
            holder.mPhoto.setImageDrawable(new ColorDrawable(Color.GRAY));
            mGenerator.generateThumbnail(info.type, info.id, 0,
                    new ThumbnailGenerator.OnThumbnailGenerateListener() {
                        @Override
                        public void onThumbnailGenerate(int key, Bitmap thumbnail) {
                            int curKey = ThumbnailGenerator.generateKey(info.type, info.id);
                            if (key == curKey) {
                                holder.mPhoto.setImageBitmap(thumbnail);
                            }
                        }
                    });
        }
        int duration = info.duration;
        if (duration > 0) {
            int sec = Math.round((float) duration / 1000);
            int min = sec / 60;
            sec %= 60;
            holder.mDuration.setText(String.format(String.format("%d:%02d", min, sec)));
        }
    }

    private boolean isFileExist(String path) {
        Boolean res = false;
        if (path == null) {
            return res;
        }
        File file = new File(path);
        if (file.exists()) {
            res = true;
        }
        return res;
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }

    public List<MediaInfo> getSelectedList() {
        return mDataList;
    }

    public class MediaSelectedViewHolder extends RecyclerView.ViewHolder {
        private ImageView mPhoto;
        private ImageView mDelete;
        private TextView mDuration;

        public MediaSelectedViewHolder(View view) {
            super(view);
            this.mPhoto = (ImageView) view.findViewById(R.id.iv_photo);
            this.mDelete = (ImageView) view.findViewById(R.id.iv_delete);
            this.mDuration = (TextView) view.findViewById(R.id.tv_duration);
            this.mDelete.setTag(this);
        }
    }

    public void addData(MediaInfo info) {
        MediaInfo copy = new MediaInfo();
        copy.filePath = info.filePath;
        copy.id = info.id;
        copy.thumbPath = info.thumbPath;
        copy.duration = info.duration;
        copy.title = info.title;
        copy.mimeType = info.mimeType;
        mDataList.add(copy);
        notifyDataSetChanged();
        mCurrentDuration += info.duration;
        if (mCallback != null) {
            mCallback.onDurationUpdate(mCurrentDuration);
        }
    }

    private void removeItem(int position) {
        MediaInfo info = mDataList.get(position);
        if (info != null) {
            mCurrentDuration -= info.duration;
            mDataList.remove(position);
            notifyDataSetChanged();
            if (mCallback != null) {
                mCallback.onDurationUpdate(mCurrentDuration);
            }
        }
    }

    @Override
    public void onClick(View v) {
        MediaSelectedViewHolder holder;
        switch (v.getId()) {
            case R.id.iv_delete:
                holder = (MediaSelectedViewHolder) v.getTag();
                int position = holder.getAdapterPosition();
                removeItem(position);
                break;
            default:
                break;
        }
    }

}
