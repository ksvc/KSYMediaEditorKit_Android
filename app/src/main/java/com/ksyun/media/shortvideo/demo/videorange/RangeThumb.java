package com.ksyun.media.shortvideo.demo.videorange;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

/**
 * thumb for range
 */

public class RangeThumb {
    private boolean mIsPressed = false;
    private boolean mIsLeft = false;

    private final float mY;
    private float mX;

    private float mWidth;
    private float mHeight;
    private int mPointerId;
    private float mThumbBandHeight;

    private float mParentWidth;
    private float mParentHeight;

    private Drawable mImageNormal;
    private Drawable mImagePressed;

    RangeThumb(Context ctx, boolean isLeft, float thumbBandHeight, float x, float y,
               float w, float parentWidth, float parentHeight,
               int thumbImageNormal, int thumbImagePressed) {
        final Resources res = ctx.getResources();
        mIsLeft = isLeft;
        mThumbBandHeight = thumbBandHeight;

        mX = x;
        mY = y;

        mParentWidth = parentWidth;
        mParentHeight = parentHeight;

        mWidth = w;
        mHeight = mParentHeight * 0.97f;

        mImageNormal = res.getDrawable(thumbImageNormal);
        mImagePressed = res.getDrawable(thumbImagePressed);

        if (mX < mWidth) {
            mX = mWidth;
        }
    }

    void setX(float x) {
        mX = x;
    }

    float getX() {
        return mX;
    }

    boolean isPressed() {
        return mIsPressed;
    }

    void press() {
        mIsPressed = true;
    }

    void release() {
        mIsPressed = false;
    }

    int getPointerId() {
        return mPointerId;
    }

    void setPointerId(int id) {
        mPointerId = id;
    }

    void draw(Canvas canvas) {
        final Drawable drawable = (mIsPressed) ? mImagePressed
                : mImageNormal;

        if (mIsLeft) {
            drawable.setBounds((int) (mX - mWidth), (int) mY, (int) (mX),
                    (int) (mHeight));
        } else {
            drawable.setBounds((int) mX, (int) mY, (int) (mX + mWidth),
                    (int) (mHeight));
        }

        drawable.draw(canvas);
    }

    boolean isInTargetZone(float x, float y) {
        RectF rect;
        if (mIsLeft) {
            rect = new RectF((mX - mWidth) * 0.8f, mThumbBandHeight,
                    mX * 1.2f, mHeight * 1.4f);
        } else {
            rect = new RectF(mX * 0.8f, mThumbBandHeight,
                    (mX + mWidth) * 1.2f, mHeight * 1.4f);
        }

        return rect.contains(x, y);
    }
}
