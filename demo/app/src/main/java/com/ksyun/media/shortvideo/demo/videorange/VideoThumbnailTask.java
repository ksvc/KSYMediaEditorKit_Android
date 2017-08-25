package com.ksyun.media.shortvideo.demo.videorange;

import com.ksyun.media.shortvideo.kit.KSYEditKit;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * get thumbnail task
 */

public class VideoThumbnailTask extends AsyncTask<Long, Void, Bitmap> {
    private static final String TAG = VideoThumbnailTask.class.getSimpleName();

    private final WeakReference<ImageView> mImageViewReference;
    private Context mContext;
    private long mMS = 0;
    private VideoThumbnailInfo mVideoThumbnailData;
    private KSYEditKit mRetriever;
    private boolean mH265File;
    private Bitmap mBitmap;

    private final WeakReference<View> mNext;

    public VideoThumbnailTask(Context context, ImageView imageView, long ms,
                              VideoThumbnailInfo videoThumbnailData, KSYEditKit retriever, View
                                      next, boolean h265file) {
        mContext = context;
        mImageViewReference = new WeakReference<>(imageView);
        mMS = ms;
        mNext = new WeakReference<>(next);
        mVideoThumbnailData = videoThumbnailData;
        mRetriever = retriever;
        mH265File = h265file;
    }

    private static int mWorkTaskNum = 0;

    public static int getWorkTaskNum() {
        return mWorkTaskNum;
    }

    public static void loadBitmap(Context context, ImageView imageView, Bitmap defaultBitmap, long ms,
                                  VideoThumbnailInfo videoThumbnailData, KSYEditKit retriever, View
                                          next) {
        if (cancelPotentialWork(ms, imageView)) {
            boolean h265File = false;
            String vcodec = retriever.getVideoCodecMeta();
            if (!TextUtils.isEmpty(vcodec) && vcodec.equals("hevc") || vcodec.equals("h265")) {
                h265File = true;
            }

            final VideoThumbnailTask task = new VideoThumbnailTask(context,
                    imageView, ms, videoThumbnailData, retriever, next, h265File);
            mWorkTaskNum++;
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(context.getResources(), defaultBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ms);
        }
    }

    @Override
    protected Bitmap doInBackground(Long... params) {

        if (!mH265File) {
            //精准seek比较耗时，同时存在获取到黑帧的情况，暂时不使用
            mBitmap = mRetriever.getVideoThumbnailAtTime(mMS, mVideoThumbnailData.mWidth, 0, false);
        } else {
            //h265的视频暂时不支持精准seek
            mBitmap = mRetriever.getVideoThumbnailAtTime(mMS, mVideoThumbnailData.mWidth, 0, false);
        }
        if (mBitmap == null) {
            Log.w(TAG, "can't get frame at" + mMS);

            return null;
        }
        if (mVideoThumbnailData != null) {
            mVideoThumbnailData.mBitmap = mBitmap;
        }

        return mBitmap;
    }

    @Override
    protected void onPostExecute(Bitmap value) {

        if (mWorkTaskNum > 0) {
            mWorkTaskNum--;
        }

        // if cancel was called on this task or the "exit early" flag is set then we're done
        final ImageView imageView = mImageViewReference.get();
        if (value != null && imageView != null && !((Activity) mContext).isFinishing()) {
            imageView.setImageBitmap(value);
        }

    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<VideoThumbnailTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap,
                             VideoThumbnailTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                    new WeakReference<VideoThumbnailTask>(bitmapWorkerTask);
        }

        public VideoThumbnailTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    private static VideoThumbnailTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

    public static boolean cancelPotentialWork(long id, ImageView imageView) {

        final VideoThumbnailTask bitmapWorkerTask = getBitmapWorkerTask(imageView);


        if (bitmapWorkerTask != null) {
            if (id != bitmapWorkerTask.mMS) {

                bitmapWorkerTask.cancel(true);
            } else {

                return false;
            }
        }

        return true;
    }

}