package com.ksyun.media.shortvideo.demo.media;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.provider.MediaStore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 缩略图生成器
 */

public class ThumbnailGenerator {
    public interface OnThumbnailGenerateListener {
        void onThumbnailGenerate(int key, Bitmap thumbnail);
    }

    private Map<Integer, OnThumbnailGenerateListener> mListeners = new HashMap<>();

    private Executor mExecutor;
    private ContentResolver mResolver;
    private Handler mHandler = new Handler();

    public ThumbnailGenerator(Context context) {
        mExecutor = Executors.newFixedThreadPool(4);
        mResolver = context.getContentResolver();
    }

    public void generateThumbnail(int type, int id, int resId, OnThumbnailGenerateListener listener) {
        int key = generateKey(type, id);
        ThumbnailGetTask task = new ThumbnailGetTask(type, id, resId);
        mListeners.put(key, listener);
        mExecutor.execute(task);
    }

    private class ThumbnailGetTask implements Runnable {
        private int type;
        private int id;
        private int resId;

        public ThumbnailGetTask(int type, int id, int resId) {
            this.type = type;
            this.id = id;
            this.resId = resId;
        }

        @Override
        public void run() {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inDither = false;
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            final Bitmap bitmap;
            if (type == MediaStorage.MEDIA_TYPE_VIDEO) {
                bitmap = MediaStore.Video.Thumbnails.getThumbnail(mResolver,
                        id == -1 ? resId : id, MediaStore.Video.Thumbnails.MICRO_KIND, options);
            } else {
                bitmap = MediaStore.Images.Thumbnails.getThumbnail(mResolver,
                        id == -1 ? resId : id, MediaStore.Images.Thumbnails.MICRO_KIND, options);
            }
            final int key = generateKey(type, id);
            if (bitmap == null) {
                if (mListeners.containsKey(key)) {
                    mListeners.remove(key);
                }
                return;
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mListeners.containsKey(key)) {
                        OnThumbnailGenerateListener l = mListeners.remove(key);
                        if (l != null) {
                            l.onThumbnailGenerate(key, bitmap);
                        }
                    }
                }
            });
        }
    }

    public static int generateKey(int type, int id) {
        return type << 16 | id;
    }

    public void cancelAllTask() {
        ((ExecutorService) mExecutor).shutdown();
    }
}
