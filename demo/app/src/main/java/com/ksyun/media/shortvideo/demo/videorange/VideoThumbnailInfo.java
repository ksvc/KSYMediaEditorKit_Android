package com.ksyun.media.shortvideo.demo.videorange;

import android.graphics.Bitmap;

/**
 * thumbnail data
 */

public class VideoThumbnailInfo {
    public final static int TYPE_START = 1;
    public final static int TYPE_NORMAL = 2;
    public final static int TYPE_END = 3;

    public float mCurrentTime = 0;
    public Bitmap mBitmap = null;
    public int mWidth = 0;
    public int mType = 1;
}
