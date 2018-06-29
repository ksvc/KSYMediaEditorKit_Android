package com.ksyun.media.shortvideo.demo.view.effect;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;


import com.ksyun.media.shortvideo.demo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: [xiaoqiang]
 * @Description: [带图形的SeekBar，可以实现随时设置seekBar颜色]
 * @CreateDate: [2018/1/27]
 * @UpdateDate: [2018/1/27]
 * @UpdateUser: [xiaoqiang]
 * @UpdateRemark: []
 */

public class EffectSeekBar extends android.support.v7.widget.AppCompatSeekBar {
    private static final String TAG = EffectSeekBar.class.getName();
    private int mImageHeight;
    private Bitmap[] mBackBitmaps;
    private Rect mSrc;
    private RectF mDst;
    private RectF mColorRectf;
    private float mProgressSize;
    private float mBitmapWidth;
    private Paint mBitmapPaint;
    private List<Scope> mListScope = new ArrayList<Scope>();
    private List<Scope> mRepeatScope = new ArrayList<Scope>();
    private Scope mCurrentScope;
    protected Paint mLintPaint;

    public EffectSeekBar(Context context) {
        super(context);
        initView();
    }

    public EffectSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
        loadAttribute(attrs);
    }

    public EffectSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        loadAttribute(attrs);
    }

    private void initView() {
        mBitmapPaint = new Paint();
        mLintPaint = new Paint();
        mLintPaint.setAntiAlias(true); //消除锯齿
        mLintPaint.setStrokeWidth(1f);
        mLintPaint.setStyle(Paint.Style.FILL);
    }

    private final void loadAttribute(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.EffectSeekBar);
            mImageHeight = typedArray.getDimensionPixelSize(R.styleable.EffectSeekBar_EffectImageHeight, 38);
            typedArray.recycle();
        }
    }

    public void setColor(int color) {
        mCurrentScope = new Scope();
        mCurrentScope.color = color;
        mCurrentScope.start = getProgress();
        mListScope.add(mCurrentScope);
    }

    public void setDefaultColor(int color) {
        mRepeatScope.clear();
        Scope scope = new Scope();
        scope.color = color;
        scope.start = 0;
        scope.end = getMax();
        mRepeatScope.add(scope);
    }

    public List<Scope> getListScope() {
        return mListScope;
    }

    public int deleteLast() {
        int index = getProgress();
        if (mListScope.size() > 0 && mCurrentScope == null) {
            Scope sopen = mListScope.remove(mListScope.size() - 1);
            index = sopen.start;
            setProgress(index);
            repeatScope();
            invalidate();
        }
        return index;
    }

    public void deleteAll() {
        mListScope.clear();
        mRepeatScope.clear();
        invalidate();
    }

    @Override
    public synchronized void setProgress(int progress) {
        if (mCurrentScope != null) {
            mCurrentScope.end = progress;
        }
        super.setProgress(progress);
    }

    public void clearColor() {
        mCurrentScope = null;
        repeatScope();
    }

    private void repeatScope() {
        mRepeatScope.clear();
        Scope scope = null;
        //TODO: 这里去重算法不会，只能这样
        for (int i = 0; i < getMax(); i++) {
            int color = 0x00000000;
            for (Scope t : mListScope) {
                if ((t.start <= i && t.end >= i) || (t.start >= i && t.end <= i)) {
                    color = t.color;
                }
            }
            if (scope != null && scope.color == color && Math.abs(i - scope.end) <= 1
                    && scope.end >= 0) {
                scope.end = i;
            } else if (color != 0x00000000) {
                scope = new Scope();
                scope.color = color;
                scope.start = i;
                scope.end = i;
                mRepeatScope.add(scope);
            }
        }
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        if (mBackBitmaps != null && mSrc != null && mDst != null) {
            for (int i = 0; i < mBackBitmaps.length; i++) {
                mDst.left = i * mBitmapWidth;
                mDst.right = mDst.left + mBitmapWidth;
                canvas.drawBitmap(mBackBitmaps[i], mSrc, mDst, mBitmapPaint);
            }
        } else {
            Log.e(TAG, "绘制特效滤镜状态不对");
        }
        for (Scope scope : mRepeatScope) {
            if (scope.start > scope.end) {
                mColorRectf.left = scope.end * mProgressSize;
                mColorRectf.right = scope.start * mProgressSize;
            } else {
                mColorRectf.left = scope.start * mProgressSize;
                mColorRectf.right = scope.end * mProgressSize;
            }
            mLintPaint.setColor(scope.color);
            canvas.drawRect(mColorRectf, mLintPaint);
        }
        if (mCurrentScope != null && mCurrentScope.end >= 0) {
            mLintPaint.setColor(mCurrentScope.color);
            if (mCurrentScope.start > mCurrentScope.end) {
                mColorRectf.left = mCurrentScope.end * mProgressSize;
                mColorRectf.right = mCurrentScope.start * mProgressSize;
            } else {
                mColorRectf.left = mCurrentScope.start * mProgressSize;
                mColorRectf.right = mCurrentScope.end * mProgressSize;
            }
            canvas.drawRect(mColorRectf, mLintPaint);
        }
        super.onDraw(canvas);
    }

    public void setBackBitmap(Bitmap[] bitmaps) {
        mBackBitmaps = bitmaps;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        computeImage(w, h);
        computeProgressSize(w);
    }

    private void computeImage(int w, int h) {
        if (mBackBitmaps != null && mBackBitmaps.length > 0 && mBackBitmaps[0] != null) {
            mSrc = new Rect(0, 0, mBackBitmaps[0].getWidth(),
                    mBackBitmaps[0].getHeight());
            mBitmapWidth = w / (float) mBackBitmaps.length;
            float top = (h - mImageHeight) / 2.0f + getTop() + 10;
            mDst = new RectF(0, top, mBitmapWidth, mImageHeight);
            mColorRectf = new RectF(0, top, mBitmapWidth, mImageHeight);
        }
    }

    @Override
    public synchronized void setMax(int max) {
        super.setMax(max);
        if (getWidth() >= 0) {
            computeProgressSize(getWidth());
        }
    }

    private void computeProgressSize(int w) {
        mProgressSize = w / (float) getMax();
    }

    class Scope {
        int color;
        int start;
        int end = -1;
    }
}
