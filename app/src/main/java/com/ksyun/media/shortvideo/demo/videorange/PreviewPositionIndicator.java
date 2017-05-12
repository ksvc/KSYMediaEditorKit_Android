package com.ksyun.media.shortvideo.demo.videorange;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.drawable.NinePatchDrawable;

/**
 * view for range preview
 */

public class PreviewPositionIndicator {
    private static final String TAG = PreviewPositionIndicator.class.getSimpleName();

    private float mX;
    private float mW;
    private float mH;
    private boolean mVisible = false;

    private final NinePatchDrawable mIndicatorImage;

    PreviewPositionIndicator(Context ctx, float x, float w, float h, int indicatorImage) {
        final Resources res = ctx.getResources();

        mIndicatorImage = (NinePatchDrawable) res.getDrawable(indicatorImage);

        mX = x;
        mW = w;
        mH = h;
    }

    void setX(float x) {
        mX = x;
    }

    float getX() {
        return mX;
    }

    void setVisible(boolean visible) {
        mVisible = visible;
    }

    boolean isVisible() {
        return mVisible;
    }


    void draw(Canvas canvas) {
        if (mVisible) {
            mIndicatorImage.setBounds((int)mX, 0, (int)(mX+mW), (int)mH);

            mIndicatorImage.draw(canvas);
        }
    }
}
