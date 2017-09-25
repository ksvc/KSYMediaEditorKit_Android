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
import com.ksyun.media.shortvideo.demo.media.MediaStorage;
import com.ksyun.media.shortvideo.demo.media.ThumbnailGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.List;

/**
 * 媒体信息适配器类
 */

public class MediaGalleryAdapter extends RecyclerView.Adapter<MediaGalleryAdapter.GalleryItemViewHolder>
        implements View.OnClickListener {
    private List<MediaInfo> mMediaList;
    private MediaStorage mStorage;
    private ThumbnailGenerator mGenerator;
    private ViewGroup.LayoutParams mParams;
    private OnItemClickListener mItemListener;

    public interface OnItemClickListener {
        void onItemClick(MediaInfo info);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemListener = listener;
    }

    public MediaGalleryAdapter(MediaStorage storage, ThumbnailGenerator generator) {
        this.mStorage = storage;
        this.mGenerator = generator;
        this.mMediaList = mStorage.getMediaList();
    }

    @Override
    public int getItemCount() {
        return mMediaList.size();
    }

    @Override
    public GalleryItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.media_item_layout, parent, false);
        int length = parent.getContext().getResources().getDisplayMetrics().widthPixels / 4;
        mParams = view.getLayoutParams();
        mParams.width = length;
        mParams.height = length;
        view.setLayoutParams(mParams);
        view.setOnClickListener(this);
        GalleryItemViewHolder holder = new GalleryItemViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(GalleryItemViewHolder holder, int position) {
        bindDate(mMediaList.get(position), holder);
    }

    private void bindDate(final MediaInfo info, final GalleryItemViewHolder holder) {
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
            ImageLoader.getInstance().displayImage(uri, holder.thumbImage, options);
        } else {
            holder.thumbImage.setImageDrawable(new ColorDrawable(Color.GRAY));
            mGenerator.generateThumbnail(info.type, info.id, 0,
                    new ThumbnailGenerator.OnThumbnailGenerateListener() {
                        @Override
                        public void onThumbnailGenerate(int key, Bitmap thumbnail) {
                            int curKey = ThumbnailGenerator.generateKey(info.type, info.id);
                            if (key == curKey) {
                                holder.thumbImage.setImageBitmap(thumbnail);
                            }
                        }
                    });
        }
        int duration = info.duration;
        if (duration > 0) {
            int sec = Math.round((float) duration / 1000);
            int min = sec / 60;
            sec %= 60;
            holder.duration.setText(String.format(String.format("%d:%02d", min, sec)));
        }
    }

    public class GalleryItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView thumbImage;
        private TextView duration;

        public GalleryItemViewHolder(View view) {
            super(view);
            this.thumbImage = (ImageView) view.findViewById(R.id.gallery_item_view);
            this.duration = (TextView) view.findViewById(R.id.gallery_item_duration);
            view.setTag(this);
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
    public void onClick(View v) {
        GalleryItemViewHolder holder = (GalleryItemViewHolder) v.getTag();
        int curPosition = holder.getAdapterPosition();
        if (mItemListener != null) {
            mItemListener.onItemClick(mMediaList.get(curPosition));
        }
    }

}
