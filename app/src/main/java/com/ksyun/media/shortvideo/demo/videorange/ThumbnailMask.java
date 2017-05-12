package com.ksyun.media.shortvideo.demo.videorange;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.NinePatchDrawable;

/**
 * thumbnail mask
 */

public class ThumbnailMask {
    private static final String TAG = ThumbnailMask.class.getSimpleName();

    private float mWidth;
    private float mHeight;

    private float mRangeStartX;
    private float mRangeEndX;

    private float mMaskWidth;

    private NinePatchDrawable mLeftMaskBitmap;
    private NinePatchDrawable mRightMaskBitmap;
    private NinePatchDrawable mVideoClipMaskBitmap;

    ThumbnailMask(Context ctx, float maskWidth,
                  float width, float height,
                  float rangeStartX, float rangeEndX,
                  int leftMaskBitmap, int rightMaskBitmap, int videoClipMaskBitmap) {

        final Resources res = ctx.getResources();

        mMaskWidth = maskWidth;
        mWidth = width;
        mHeight = height;

        mRangeStartX = rangeStartX;
        mRangeEndX = rangeEndX;

        mLeftMaskBitmap = (NinePatchDrawable) res.getDrawable(leftMaskBitmap);
        mRightMaskBitmap = (NinePatchDrawable) res.getDrawable(rightMaskBitmap);
        mVideoClipMaskBitmap = (NinePatchDrawable) res.getDrawable(videoClipMaskBitmap);
    }

    void setRange(float rangeStartX, float rangeEndX) {
        mRangeStartX = rangeStartX;
        mRangeEndX = rangeEndX;
    }

    void draw(Canvas canvas) {
        mLeftMaskBitmap.setBounds(0, 0, (int) mMaskWidth, (int) mHeight);
        mLeftMaskBitmap.draw(canvas);
        mRightMaskBitmap.setBounds((int) (mWidth - mMaskWidth), 0, (int) mWidth, (int) mHeight);
        mRightMaskBitmap.draw(canvas);


        mVideoClipMaskBitmap.setBounds(0, 0, (int) (mRangeStartX), (int) mHeight);
        mVideoClipMaskBitmap.draw(canvas);

        mVideoClipMaskBitmap.setBounds((int) mRangeEndX, 0, (int) mWidth, (int) mHeight);
        mVideoClipMaskBitmap.draw(canvas);
    }

    boolean isInTargetZone(float x, float y) {
        RectF rect = new RectF(0, 0, mWidth, mHeight);

        return rect.contains(x, y);
    }

}
