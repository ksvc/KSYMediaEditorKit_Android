package com.ksyun.media.shortvideo.demo.videorange;

import com.ksyun.media.shortvideo.kit.KSYEditKit;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * get thumbnail task
 */

public class VideoThumbnailTask extends AsyncTask<Long, Void, Bitmap> {
    private static final String TAG = VideoThumbnailTask.class.getSimpleName();

    private final WeakReference<ImageView> imageViewReference;
    private Context mContext;
    private long mMS = 0;
    private VideoThumbnailInfo mVideoThumbnailData;
    private KSYEditKit mRetriever;

    private final WeakReference<View> mNext;

    public VideoThumbnailTask(Context context, ImageView imageView, long ms,
                              VideoThumbnailInfo videoThumbnailData, KSYEditKit retriever, View
                                      next) {
        mContext = context;
        imageViewReference = new WeakReference<ImageView>(imageView);
        mMS = ms;
        mNext = new WeakReference<View>(next);
        mVideoThumbnailData = videoThumbnailData;
        mRetriever = retriever;
    }

    private static int mWorkTaskNum = 0;

    public static int getWorkTaskNum() {
        return mWorkTaskNum;
    }

    public static void loadBitmap(Context context, ImageView imageView, Bitmap defaultBitmap, long ms,
                                  VideoThumbnailInfo videoThumbnailData, KSYEditKit retriever, View
                                          next) {

        if (cancelPotentialWork(ms, imageView)) {
            final VideoThumbnailTask task = new VideoThumbnailTask(context,
                    imageView, ms, videoThumbnailData, retriever, next);
            mWorkTaskNum++;
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(context.getResources(), defaultBitmap, task);
            imageView.setImageDrawable(asyncDrawable);
            task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, ms);
        }
    }

    @Override
    protected Bitmap doInBackground(Long... params) {
//        if(mContext == null || mContext.isFinishing()){
//            return null;
//        }
//        if (mContext.mBeginClip) {
//            return null;
//        }
        Bitmap bitmap = mRetriever.getVideoThumbnailAtTime(mMS, 0, 0);
        if (bitmap == null) {
            Log.w("test", "can't get frame at" + mMS);

            return null;
        }

        mVideoThumbnailData.mBitmap = bitmap;

        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap value) {

        if (mWorkTaskNum > 0) {
            mWorkTaskNum--;
            //if(mNext != null) ((TextView) mNext).setVisibility(View.GONE);
        }

//        if(mContext == null || mContext.isFinishing()){
//            return;
//        }

        // if cancel was called on this task or the "exit early" flag is set then we're done
        final ImageView imageView = imageViewReference.get();
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