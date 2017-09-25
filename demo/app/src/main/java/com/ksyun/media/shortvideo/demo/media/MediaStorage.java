package com.ksyun.media.shortvideo.demo.media;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

/**
 * 媒体信息获取类
 */

public class MediaStorage {

    public static final int MEDIA_TYPE_VIDEO = 100;
    public static final int MEDIA_TYPE_IMAGE = 101;
    public static final int MEDIA_TYPE_MUSIC = 102;
    public static final int MEDIA_NOTIFY_SIZE = 5;

    private Context mContext;
    private List<MediaInfo> mMediaList;
    private int mMediaType;
    private MediaCaptureTask mMediaCaptureTask;
    private OnMediaDataUpdateListener mDataUpdateListener;

    private String[] mThumbColumns = new String[]{
            MediaStore.Video.Thumbnails.DATA,
            MediaStore.Video.Thumbnails.VIDEO_ID
    };

    private String[] mMediaColumns = new String[]{
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.MIME_TYPE
    };

    public interface OnMediaDataUpdateListener {
        void onDateUpdate(List<MediaInfo> data);
    }

    public void setOnMediaDataUpdateListener(OnMediaDataUpdateListener listener) {
        this.mDataUpdateListener = listener;
    }

    public MediaStorage(Context context) {
        this.mContext = context;
        this.mMediaType = MEDIA_TYPE_VIDEO;
        mMediaList = new ArrayList<>();
        mMediaCaptureTask = new MediaCaptureTask(context);
    }

    public void startCaptureMedias(int type) {
        this.mMediaType = type;
        mMediaCaptureTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, type);
    }

    private class MediaCaptureTask extends AsyncTask<Integer, ArrayList<MediaInfo>, Void> {
        private final ContentResolver resolver;

        public MediaCaptureTask(Context context) {
            resolver = context.getContentResolver();
        }

        @Override
        protected Void doInBackground(Integer... params) {
            switch (params[0]) {
                case MEDIA_TYPE_VIDEO:
                    captureVideoFiles();
                    break;
                case MEDIA_TYPE_IMAGE:
                    break;
                case MEDIA_TYPE_MUSIC:
                    break;
            }
            return null;
        }

        private void captureVideoFiles() {
            Cursor videoCursor = resolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    mMediaColumns, null, null, null);
            int totalCount = videoCursor.getCount();
            MediaInfo videoInfo = null;
            ArrayList<MediaInfo> cacheList = new ArrayList<>();
            int column_index_data = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
            int column_index_duration = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
            int column_index_id = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
            int column_index_title = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
            int column_index_mime = videoCursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE);
            for (int i = 0; i < totalCount; i++) {
                while (videoInfo == null && videoCursor.moveToNext()) {
                    videoInfo = new MediaInfo();
                    videoInfo.type = MEDIA_TYPE_VIDEO;
                    videoInfo.filePath = videoCursor.getString(column_index_data);
                    videoInfo.duration = videoCursor.getInt(column_index_duration);
                    videoInfo.id = videoCursor.getInt(column_index_id);
                    videoInfo.title = videoCursor.getString(column_index_title);
                    videoInfo.mimeType = videoCursor.getString(column_index_mime);
                    Cursor thumbCursor = resolver.query(MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI,
                            mThumbColumns, MediaStore.Video.Thumbnails.VIDEO_ID + "=?",
                            new String[]{String.valueOf(videoInfo.id)}, null);
                    if (thumbCursor.moveToFirst()) {
                        videoInfo.thumbPath = thumbCursor.getString(
                                thumbCursor.getColumnIndexOrThrow(MediaStore.Video.Thumbnails.DATA));
                    }
                    thumbCursor.close();
                }
                cacheList.add(videoInfo);
                videoInfo = null;
                if (cacheList.size() == MEDIA_NOTIFY_SIZE) {
                    publishProgress(cacheList);
                    cacheList = new ArrayList<>();
                }
            }
            publishProgress(cacheList);
            if (videoCursor != null) {
                videoCursor.close();
            }
        }

        @Override
        protected void onProgressUpdate(ArrayList<MediaInfo>... values) {
            if (values[0] != null) {
                mMediaList.addAll(values[0]);
                if (mDataUpdateListener != null) {
                    mDataUpdateListener.onDateUpdate(values[0]);
                }
            }
            super.onProgressUpdate(values);
        }
    }

    public void cancelTask() {
        if (mMediaCaptureTask != null) {
            mMediaCaptureTask.cancel(false);
        }
    }

    public List<MediaInfo> getMediaList() {
        return mMediaList;
    }
}
