package com.ksyun.media.shortvideo.demo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.annotation.Nullable;
import android.util.ArrayMap;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.ksyun.media.shortvideo.demo.R;

import java.util.Map;

/**
 * @Author: [xiaoqiang]
 * @Description: [一个多彩的Seekbar的绘制]
 * @CreateDate: [2017/12/22]
 * @UpdateDate: [2017/12/22]
 * @UpdateUser: [xiaoqiang]
 * @UpdateRemark: []
 * 1. 先作出一个最简单的Seekbar。按照默认的Seekbar的功能
 */

public class ColorFulSeekbar extends View {
    private static final String TAG = ColorFulSeekbar.class.getName();
    private Drawable mThumbDrawable;
    private int mBackground;
    private Context mContext;
    protected Paint mLintPaint;
    boolean mIsUserSeekable = true;
    private int mThumbHeight;
    private int mThumbWidth;
    protected Point mStartPoint;
    protected Point mEndPoint;

    int mMinWidth;
    int mMaxWidth;
    int mMinHeight;
    protected int mMaxHeight;
    private boolean mIsTouch;
    private float mTouchX;
    private float mInitTouchX;
    private int mMax;
    private int mProgress;
    private OnSeekBarChangeListener mOnSeekBarChangeListener;
    protected Map<Integer, ColorScope> mColorList;
    protected RectF mLintRect;

    public ColorFulSeekbar(Context context) {
        this(context, null);
    }

    public ColorFulSeekbar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ColorFulSeekbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initParams();
        loadAttribute(attrs);
        init();
    }

    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        mOnSeekBarChangeListener = l;
    }

    private void initParams() {
        mMinWidth = 24;
        mMaxWidth = 48;
        mMinHeight = 24;
        mMaxHeight = 48;
        mMax = 100;
        mProgress = 0;
    }

    /**
     * 加载自定义参数
     *
     * @param attrs
     */
    private final void loadAttribute(AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ColorFulSeekbar);
            Drawable mThumb = typedArray.getDrawable(R.styleable.ColorFulSeekbar_cThumbTint);
            if (mThumb != null) {
                mThumbDrawable = mThumb;
            }
            mBackground = typedArray.getColor(R.styleable.ColorFulSeekbar_cBackground, 0x99000000);
            mMaxHeight = (int) typedArray.getDimension(R.styleable.ColorFulSeekbar_cMaxHeight, mMaxHeight);
            mMinHeight = (int) typedArray.getDimension(R.styleable.ColorFulSeekbar_cMinHeight, mMinHeight);
            mMaxWidth = (int) typedArray.getDimension(R.styleable.ColorFulSeekbar_cMaxWidth, mMaxWidth);
            mMinWidth = (int) typedArray.getDimension(R.styleable.ColorFulSeekbar_cMinWidth, mMinWidth);
            mMax = (int) typedArray.getInteger(R.styleable.ColorFulSeekbar_cMax, mMax);
            mProgress = (int) typedArray.getInteger(R.styleable.ColorFulSeekbar_cProgress, mProgress);
            typedArray.recycle();
        }
    }

    /**
     * 初始化，消除锯齿
     */
    private final void init() {
        mLintPaint = new Paint();
        mLintPaint.setColor(mBackground);
        mLintPaint.setAntiAlias(true); //消除锯齿
        mLintPaint.setStrokeWidth(1f);
        mLintPaint.setStyle(Paint.Style.FILL);

        if (mThumbDrawable == null) {
            mThumbDrawable = new ShapeDrawable(new OvalShape());
            ((ShapeDrawable) mThumbDrawable).getPaint().setColor(0xffffffff);
            ((ShapeDrawable) mThumbDrawable).setIntrinsicWidth(dip2px(20));
            ((ShapeDrawable) mThumbDrawable).setIntrinsicHeight(dip2px(20));
        }
        if (mColorList == null) {
            mColorList = new ArrayMap<>();
        }
    }

    public int getProgress() {
        return mProgress;
    }

    public synchronized ColorScope deleteColor(int index) {
        if (mColorList != null) {
            ColorScope scope = mColorList.get(index);
            if (scope == null) {
                return scope;
            }
            if (scope != null) {
                mColorList.remove(index);
            }

            int progress = 0;
            if (mColorList.size() > 0) {
                Object[] colorScopes = mColorList.values().toArray();
                ColorScope lastScope = (ColorScope) colorScopes[colorScopes.length - 1];
                if (lastScope != null) {
                    progress = lastScope.mEndProgress;
                }
            } else {
                progress = scope.mStartProgress;
            }

            moveThumb(progress);
            return scope;
        }
        return null;
    }

    public void setProgress(int progress) {
        if (progress >= 0 && progress <= mMax) {
            if(progress == mMax) {
                moveThumb(progress);
            } else {
                moveThumb(progress);
            }

            invalidate();
        }
    }

    public int getProgressMax() {
        return mMax;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBack(canvas);
        drawColorLint(canvas);
        drawThumb(canvas);
    }

    protected synchronized void drawBack(Canvas canvas) {
        mLintPaint.setColor(mBackground);
        mLintRect.left = mStartPoint.x;
        mLintRect.right = mEndPoint.x;
        canvas.drawRoundRect(mLintRect, mMaxHeight / 2, mMaxHeight / 2, mLintPaint);
    }

    protected synchronized void drawColorLint(Canvas canvas) {
        if (mColorList != null) {
            for (ColorScope scope : mColorList.values()) {
                mLintRect.left = progressToPx(scope.mStartProgress) + getPaddingLeft();
                mLintRect.right = progressToPx(scope.mEndProgress) + getPaddingLeft();
                mLintPaint.setColor(scope.mColor);
                canvas.drawRoundRect(mLintRect, mMaxHeight / 2,
                        mMaxHeight / 2, mLintPaint);
            }
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int thumbHeight = mThumbDrawable == null ? 0 : mThumbDrawable.getIntrinsicHeight();
        if (mThumbDrawable != null) {
            mThumbWidth = Math.max(mMinWidth, Math.min(mMaxWidth, mThumbDrawable.getIntrinsicWidth()));
            mThumbHeight = Math.max(mMinHeight, Math.min(mMaxHeight, mThumbDrawable.getIntrinsicHeight()));
            mThumbHeight = Math.max(thumbHeight, mThumbHeight);
        }
        int left = mThumbDrawable.getBounds().left + getPaddingLeft();
        int top = mThumbDrawable.getBounds().top + getPaddingTop();
        Rect mThumbRect = new Rect(left,
                top,
                mThumbWidth + left,
                top + mThumbHeight);
        mThumbDrawable.setBounds(mThumbRect);

        setMeasuredDimension(resolveSizeAndState(mThumbWidth + getPaddingLeft() + getPaddingRight(),
                widthMeasureSpec, 0),
                resolveSizeAndState(mThumbHeight + getPaddingTop() + getPaddingBottom(),
                        heightMeasureSpec, 0));
    }


    /**
     * Draw the thumb.
     */
    void drawThumb(Canvas canvas) {
        if (mThumbDrawable != null) {
            final int saveCount = canvas.save();
            canvas.translate(getPaddingLeft(), getPaddingTop());
            mThumbDrawable.draw(canvas);
            canvas.restoreToCount(saveCount);
            mProgress = pxToProgress(mThumbDrawable.getBounds().left - getPaddingLeft()
                    + mThumbWidth / 2);
            onProgressRefresh(mIsTouch, mProgress);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mIsUserSeekable || !isEnabled()) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //判断是否在区间范围内
                //记录当前X范围
                if (isScopeOf(event)) {
                    mIsTouch = true;
                    mTouchX = event.getX();
                    mInitTouchX = mThumbDrawable.getBounds().left;
                    onStartTrackingTouch();
                } else {
                    mIsTouch = false;
                }
                break;

            case MotionEvent.ACTION_MOVE:
                //记录X改变
                if (mIsTouch) {
                    moveThumb(event);
                }
                break;

            case MotionEvent.ACTION_UP:
                if (mIsTouch) {
                    onStopTrackingTouch();
                }
                mIsTouch = false;
                //手指离开
                invalidate();
                break;

            case MotionEvent.ACTION_CANCEL:
                if (mIsTouch) {
                    onStopTrackingTouch();
                }
                mIsTouch = false;
                invalidate(); // see above explanation
                break;
        }
        return true;
    }

    private int dip2px(float dpValue) {
        float scale = mContext.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private boolean isScopeOf(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        //10 是margin
        if (x > (mThumbDrawable.getBounds().left - 10) && x < (mThumbDrawable.getBounds().left + mThumbWidth + 10) && y >= getPaddingTop() &&
                y <= mThumbHeight + getPaddingTop()) {
            return true;
        }
        return false;
    }

    private void moveThumb(MotionEvent event) {
        int left = (int) (event.getX() - mTouchX + mInitTouchX);
        int right = left + mThumbWidth;
        if (right > (getWidth() - getPaddingRight()) || left < getPaddingLeft()) {
            return;
        } else {
            mThumbDrawable.setBounds(left, getPaddingTop(), right,
                    getPaddingTop() + mThumbHeight);
            invalidate();
        }
    }

    private void moveThumb(int progress) {
        //progressToPx(progress) 中心点位置
        int left = (int) (progressToPx(progress) + getPaddingLeft() - mThumbWidth / 2);
        int right = left + mThumbWidth;
        if (right > (getWidth() - getPaddingRight() + mThumbWidth) || left < getPaddingLeft()) {
            return;
        } else {
            mThumbDrawable.setBounds(left, getPaddingTop(), right,
                    getPaddingTop() + mThumbHeight);
        }
    }

    void onStartTrackingTouch() {
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStartTrackingTouch(this);
        }
    }

    void onStopTrackingTouch() {
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onStopTrackingTouch(this);
        }
    }

    void onProgressRefresh(boolean fromUser, int progress) {
        if (mOnSeekBarChangeListener != null) {
            mOnSeekBarChangeListener.onProgressChanged(this, progress, fromUser);
        }
    }

    public interface OnSeekBarChangeListener {
        void onProgressChanged(ColorFulSeekbar seekBar, int progress, boolean fromUser);

        void onStartTrackingTouch(ColorFulSeekbar seekBar);

        void onStopTrackingTouch(ColorFulSeekbar seekBar);
    }

    private int pxToProgress(float px) {
        int progress = (int) ((px - mThumbWidth / 2) / (mEndPoint.x - mStartPoint.x) * mMax + 0.5f);
        if (progress > mMax) {
            return mMax;
        } else if (progress < 0) {
            return 0;
        }
        return progress;
    }

    protected float progressToPx(int progress) {
        if (mEndPoint.x <= mStartPoint.x) return 0;
        float left = (float) progress / mMax * (mEndPoint.x - mStartPoint.x);
        left += mThumbWidth / 2;
        if (left < mThumbWidth / 2) {
            return mThumbWidth / 2;
        } else if (left > (mEndPoint.x - mStartPoint.x)) {
            return (mEndPoint.x - mStartPoint.x + mThumbWidth / 2);
        }
        return left;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        onInitDraw(w);
    }

    protected void onInitDraw(int w) {
        if (w <= 0) return;
        mStartPoint = new Point(getPaddingLeft() + mThumbWidth / 2, mThumbHeight / 2 + getPaddingTop());
        mEndPoint = new Point(w - getPaddingRight() - mThumbWidth / 2, mThumbHeight / 2 + getPaddingTop());

        mLintRect = new RectF(mStartPoint.x, mStartPoint.y - mMaxHeight / 2,
                mEndPoint.x, mEndPoint.y + mMaxHeight / 2);
        moveThumb(mProgress);
    }

    public int getMax() {
        return mMax;
    }

    public void setMax(int mMax) {
        this.mMax = mMax;
    }

    public class ColorScope {
        public ColorScope(int color, int startProgress, int endProgress) {
            this.mColor = color;
            this.mStartProgress = startProgress;
            this.mEndProgress = endProgress;
        }

        public int mColor;
        public int mStartProgress;
        public int mEndProgress;
    }
}

