package com.ksyun.media.shortvideo.demo.videorange;

import com.ksyun.media.shortvideo.demo.R;
import com.ksyun.media.shortvideo.kit.KSYEditKit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

/**
 * video thumbnail adapter
 */

public class VideoThumbnailAdapter extends ArrayAdapter<VideoThumbnailInfo> {
    private LayoutInflater mInflater;
    private VideoThumbnailInfo[] mList;
    private Context mContext;
    private KSYEditKit mRetriever;
    private View mNext;
    private Bitmap mDefaultBmp;

    public VideoThumbnailAdapter(Context context, VideoThumbnailInfo[] values, KSYEditKit
            retriever) {
        super(context, R.layout.item_thumbnail, values);
        mList = values;
        mContext = context;
        mRetriever = retriever;
        mNext = null;
        mInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mDefaultBmp = BitmapFactory.decodeResource(context.getResources(),R.drawable.bottom_default);
    }

    public int getTaskNum() {
        return VideoThumbnailTask.getWorkTaskNum();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_thumbnail, parent, false);

            holder = new Holder();
            holder.thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        ViewGroup.LayoutParams param = convertView.getLayoutParams();
        param.width = mList[position].mWidth;
        convertView.setLayoutParams(param);

        holder.thumbnail.setImageBitmap(mDefaultBmp);
        if (mList[position].mBitmap != null) {
            holder.thumbnail.setImageBitmap(mList[position].mBitmap);
        } else {
            if (mList[position].mType == VideoThumbnailInfo.TYPE_NORMAL) {
                VideoThumbnailTask.loadBitmap(mContext, holder.thumbnail,
                        null, (long) (mList[position].mCurrentTime * 1000), mList[position],
                        mRetriever, mNext);
            }
        }

        return convertView;
    }

    /**
     * View holder for the views we need access to
     */
    private static class Holder {
        public ImageView thumbnail;
    }
}