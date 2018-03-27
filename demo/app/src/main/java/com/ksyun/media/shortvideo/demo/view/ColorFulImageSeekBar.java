package com.ksyun.media.shortvideo.demo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;

/**
 * @Author: [xiaoqiang]
 * @Description: [ColorFulImageSeekBar]
 * @CreateDate: [2018/1/5]
 * @UpdateDate: [2018/1/5]
 * @UpdateUser: [xiaoqiang]
 * @UpdateRemark: []
 */

public class ColorFulImageSeekBar extends ColorFulSeekbar {
    private Bitmap[] mBackBitmaps;
    private Rect mSrc;
    private Rect mDst;
    private int mBitmapWidth;
    private Paint mBitmapPaint;
    private int[] mColorForProgress;
    private int mWidth;
    private boolean mInit = false;

    public ColorFulImageSeekBar(Context context) {
        super(context);
        init();
    }

    public ColorFulImageSeekBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ColorFulImageSeekBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mBitmapPaint = new Paint();
    }

    @Override
    protected synchronized void drawBack(Canvas canvas) {
        if (mBackBitmaps == null || mBackBitmaps.length <= 0 || mDst == null || mSrc == null)
            return;
        for (int i = 0; i < mBackBitmaps.length; i++) {
            mDst.left = i * mBitmapWidth + mStartPoint.x;
            mDst.right = mDst.left + mBitmapWidth;
            mDst.right = (mDst.right > mEndPoint.x) ? mEndPoint.x : mDst.right;
            canvas.drawBitmap(mBackBitmaps[i], mSrc, mDst, mBitmapPaint);
        }
    }

    protected synchronized void setProgressColor(int index, int color) {
        int progress = getProgress();

        if (progress <= getMax()) {
            if (mColorForProgress == null || mColorForProgress.length != getMax()) {
                mColorForProgress = new int[getMax()];
                if (mColorList != null)
                    mColorList.clear();
            }
            int startColorIndex;
            int endColorIndex;
            ColorScope colorScope = mColorList.get(index);
            if (colorScope != null) {
                startColorIndex = colorScope.mEndProgress;
                colorScope.mEndProgress = progress;
                endColorIndex = progress;
            } else {
                colorScope = new ColorScope(color, progress, progress);
                mColorList.put(index, colorScope);
                startColorIndex = progress;
                endColorIndex = progress;
            }
            if (endColorIndex >= mColorForProgress.length) {
                endColorIndex = mColorForProgress.length - 1;
            }

            if(startColorIndex > endColorIndex) {
                int temp = startColorIndex;
                startColorIndex = endColorIndex;
                endColorIndex = temp;
            }
            colorScope.mStartProgress = startColorIndex;
            colorScope.mEndProgress = endColorIndex;

            for (int i = startColorIndex; i <= endColorIndex; i++) {
                mColorForProgress[i] = color;
            }
            invalidate();
        }
    }

    @Override
    public synchronized ColorScope deleteColor(int index) {
        ColorScope scope = super.deleteColor(index);
        mColorForProgress = new int[getMax()];
        for (ColorScope colorScope : mColorList.values()) {
            for (int i = colorScope.mStartProgress; i < colorScope.mEndProgress; i++) {
                mColorForProgress[i] = colorScope.mColor;
            }
        }
        invalidate();
        return scope;
    }

    public void clearColor() {
        mColorList.clear();
        mColorForProgress = new int[getMax()];
        invalidate();
    }

    @Override
    protected synchronized void drawColorLint(Canvas canvas) {
        if (mColorForProgress != null && mColorForProgress.length > 0) {
            int start = 0;
            int end = 1;
            for (; end < mColorForProgress.length; ) {
                if (mColorForProgress[end] != mColorForProgress[start] ||
                        end == mColorForProgress.length - 1) {
                    mLintRect.left = progressToPx(start) + getPaddingLeft();
                    mLintRect.right = progressToPx(end) + getPaddingLeft();
                    mLintPaint.setColor(mColorForProgress[start]);
                    canvas.drawRect(mLintRect, mLintPaint);
                    start = end;
                }
                end++;
            }
        }
    }

    @Override
    protected void onInitDraw(int w) {
        super.onInitDraw(w);

        if (mBackBitmaps == null || mBackBitmaps.length <= 0 || mEndPoint == null || mStartPoint == null) {
            mWidth = w;
            return;
        }

        mBitmapWidth = (mEndPoint.x - mStartPoint.x) / mBackBitmaps.length;
        if (mBitmapWidth > 0 && mMaxHeight > 0) {
            mSrc = new Rect(0, 0, (mBackBitmaps[0].getHeight() / mMaxHeight) * mBitmapWidth,
                    mBackBitmaps[0].getHeight());
            int mHeight = getHeight() - getPaddingTop() - getPaddingBottom();
            int top = getPaddingTop() + (mHeight - mMaxHeight) / 2;
            mDst = new Rect(0, top, mBitmapWidth, top + mMaxHeight);
        }
        mInit = true;

    }


    public void setBackBitmap(Bitmap[] bitmap) {
        if (bitmap == null || bitmap.length <= 0) return;
        mBackBitmaps = bitmap;
        if (!mInit) {
            onInitDraw(mWidth);
        }
        invalidate();
    }
}

