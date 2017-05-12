package com.ksyun.media.shortvideo.demo.videorange;

import com.ksyun.media.shortvideo.demo.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * video range seekbar
 */

public class VideoRangeSeekBar extends View {
    private static final String TAG = VideoRangeSeekBar.class.getSimpleName();

    static final int DEFAULT_GRADIENT_WIDTH_DP = 20;
    static final int DEFAULT_THUMB_BAND_HEIGHT_DP = 70;
    static final int DEFAULT_THUMB_WIDTH_DP = 18;
    static final int DEFAULT_INDICATOR_WIDTH_DP = 5;

    static final int DEFAULT_LEFT_THUMB_NORMAL_RES_ID = R.drawable.seekbar_left;
    static final int DEFAULT_LEFT_THUMB_PRESS_RES_ID = R.drawable.seekbar_left;
    static final int DEFAULT_RIGHT_THUMB_NORMAL_RES_ID = R.drawable.seekbar_right;
    static final int DEFAULT_RIGHT_THUMB_PRESS_RES_ID = R.drawable.seekbar_right;
    static final int DEFAULT_LEFT_GRADIENT_RES_ID = R.drawable.seekbar_left_mask;
    static final int DEFAULT_RIGHT_GRADIENT_RES_ID = R.drawable.seekbar_right_mask;
    static final int DEFAULT_VIDEO_CLIP_MASK_RES_ID = R.drawable.seekbar_mask;
    static final int DEFAULT_INDICATOR_IMAGE_RES_ID = R.drawable.seekbar_pointer;

    // config {{
    private float mGradientWidth;
    private float mThumbBandHeight;
    private float mThumbWidth;
    private float mIndicatorWidth;

    private int mLeftThumbNormalResId;
    private int mLeftThumbPressResId;
    private int mRightThumbNormalResId;
    private int mRightThumbPressResId;
    private int mLeftGradientResId;
    private int mRightGradientResId;
    private int mVideoClipMaskResId;
    private int mIndicatorImageResId;

    // }}

    private int mDefaultWidth = 500;
    private int mDefaultHeight = 120;
    private int mWidth = mDefaultWidth;
    private int mHeight = mDefaultHeight;

    private RangeThumb mLeftThumb;
    private RangeThumb mRightThumb;

    private ThumbnailMask mMask;
    private PreviewPositionIndicator mIndicator;

    protected float MAX_RANGE = 300.0f;
    protected float MIN_RANGE = 1.0f;
    // 0.0 ~ 300.0
    protected float mRangeStart = 0.0f;
    protected float mRangeEnd = MAX_RANGE;

    protected float mMaxRange = MAX_RANGE;

    protected Context mContext;

    // callback
    private OnRangeBarChangeListener mRangeBarChangeListener = null;
    private OnVideoMaskScrollListener mVideoMaskScrollListener = null;

    public VideoRangeSeekBar(Context context) {
        super(context);
        mContext = context;
    }

    public VideoRangeSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        rangeSeekBarInit(context, attrs);
        mContext = context;
    }

    public VideoRangeSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        rangeSeekBarInit(context, attrs);
        mContext = context;
    }

    protected void rangeSeekBarInit(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.VideoRangeSeekBar, 0, 0);
        mContext = context;
        try {
            mGradientWidth = ta.getDimension(R.styleable.VideoRangeSeekBar_gradientWidth,
                    DEFAULT_GRADIENT_WIDTH_DP);
            mThumbBandHeight = ta.getDimension(R.styleable.VideoRangeSeekBar_thumbBandHeight,
                    DEFAULT_THUMB_BAND_HEIGHT_DP);
            mThumbWidth = ta.getDimension(R.styleable.VideoRangeSeekBar_thumbWidth,
                    DEFAULT_THUMB_WIDTH_DP);
            mIndicatorWidth = ta.getDimension(R.styleable.VideoRangeSeekBar_indicatorWidth,
                    DEFAULT_INDICATOR_WIDTH_DP);

            mLeftThumbNormalResId = ta.getResourceId(R.styleable.VideoRangeSeekBar_leftThumbNormal,
                    DEFAULT_LEFT_THUMB_NORMAL_RES_ID);
            mLeftThumbPressResId = ta.getResourceId(R.styleable.VideoRangeSeekBar_leftThumbPress,
                    DEFAULT_LEFT_THUMB_PRESS_RES_ID);
            mRightThumbNormalResId = ta.getResourceId(R.styleable.VideoRangeSeekBar_rightThumbNormal,
                    DEFAULT_RIGHT_THUMB_NORMAL_RES_ID);
            mRightThumbPressResId = ta.getResourceId(R.styleable.VideoRangeSeekBar_rightThumbPress,
                    DEFAULT_RIGHT_THUMB_PRESS_RES_ID);
            mLeftGradientResId = ta.getResourceId(R.styleable.VideoRangeSeekBar_leftGradient,
                    DEFAULT_LEFT_GRADIENT_RES_ID);
            mRightGradientResId = ta.getResourceId(R.styleable.VideoRangeSeekBar_rightGradient,
                    DEFAULT_RIGHT_GRADIENT_RES_ID);
            mVideoClipMaskResId = ta.getResourceId(R.styleable.VideoRangeSeekBar_videoClipMask,
                    DEFAULT_VIDEO_CLIP_MASK_RES_ID);

            mIndicatorImageResId = ta.getResourceId(R.styleable.VideoRangeSeekBar_indicatorImage,
                    DEFAULT_INDICATOR_IMAGE_RES_ID);
        } finally {
            ta.recycle();
        }
    }

    public void setOnRangeBarChangeListener(OnRangeBarChangeListener listener) {
        mRangeBarChangeListener = listener;
    }

    public void setOnVideoMaskScrollListener(OnVideoMaskScrollListener listener) {
        mVideoMaskScrollListener = listener;
    }

    public void setMaxRange(float maxRange) {
        MAX_RANGE = maxRange;
    }

    public void setMinRange(float minRange) {
        MIN_RANGE = minRange;
    }

    /**
     * warning: 必须首先执行setMaxRange()
     *
     * @param rangeStart
     * @param rangeEnd
     */
    public void setRange(float rangeStart, float rangeEnd) {
        mRangeStart = rangeStart;
        mRangeEnd = rangeEnd;

        if (rangeStart < 0.0f) {
            mRangeStart = 0.0f;
        }

        if (rangeStart > 7.0f) {
            mRangeStart = 7.0f;
        }

        if (rangeEnd < MIN_RANGE) {
            mRangeEnd = MIN_RANGE;
        }

        if (rangeEnd > MAX_RANGE) {
            mRangeEnd = MAX_RANGE;
        }

        if (mRangeEnd < (mRangeStart + MIN_RANGE)) {
            mRangeEnd = mRangeStart + MIN_RANGE;
        }
    }

    public float setRangeStartX(float rangeStartX) {
        float tmp = rangeStartX;
        float totalRange = mWidth - 2 * mGradientWidth;

        if (tmp < mGradientWidth) {
            rangeStartX = mGradientWidth;
        }

        float maxRangeStartX = getRangeEndX() - MIN_RANGE / MAX_RANGE * totalRange;

        if (tmp > maxRangeStartX) {
            rangeStartX = maxRangeStartX;
        }

        if (getRangeEndX() - tmp > getMaxRangeWidth(mMaxRange)) {
            rangeStartX = getRangeEndX() - getMaxRangeWidth(mMaxRange);
        }

        mRangeStart = (rangeStartX - mGradientWidth) / totalRange * MAX_RANGE;

        return rangeStartX;
    }

    public boolean setRangeEndX(float rangeEndX) {
        float tmp = rangeEndX;
        float totalRange = mWidth - 2 * mGradientWidth;
        boolean ret = false;
        float minRandEndX = getRangeStartX() + MIN_RANGE / MAX_RANGE * totalRange;
        if (tmp < minRandEndX) {
            rangeEndX = minRandEndX;
        }

        if (tmp > (mWidth - mGradientWidth)) {
            rangeEndX = mWidth - mGradientWidth;
            ret = true;
        }

        if (tmp - getRangeStartX() > getMaxRangeWidth(mMaxRange)) {
            rangeEndX = getRangeStartX() + getMaxRangeWidth(mMaxRange);
        }

        mRangeEnd = (rangeEndX - mGradientWidth) / totalRange * MAX_RANGE;

        //return rangeEndX;
        return ret;
    }

    public float getRangeStart() {
        return mRangeStart;
    }

    public float getRangeEnd() {
        return mRangeEnd;
    }

    private float getRangeStartX() {
        float totalRange = mWidth - 2 * mGradientWidth;

        return mRangeStart / MAX_RANGE * totalRange + mGradientWidth;
    }

    private float getRangeEndX() {
        float totalRange = mWidth - 2 * mGradientWidth;

        return mRangeEnd / MAX_RANGE * totalRange + mGradientWidth;
    }

    private float getRangeX(float value) {
        float totalRange = mWidth - 2 * mGradientWidth;

        return value / MAX_RANGE * totalRange + mGradientWidth;
    }

    private float getMaxRangeWidth(float value) {
        float totalRange = mWidth - 2 * mGradientWidth;
        return value / MAX_RANGE * totalRange;
    }

    /**
     * each frame width
     *
     * @return
     */
    public float getFrameWidth() {
        float totalRange = mWidth - 2 * mGradientWidth;

        return 1.0f / Math.min(MAX_RANGE, 8) * totalRange;
    }

    public float getMaskWidth() {
        return mGradientWidth;
    }

    public float getRange(float x) {
        float totalRange = mWidth - 2 * mGradientWidth;

        return x / totalRange * MAX_RANGE;
    }

    @Override
    public Parcelable onSaveInstanceState() {

        final Bundle bundle = new Bundle();

        bundle.putParcelable("instanceState", super.onSaveInstanceState());

        return bundle;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {

        if (state instanceof Bundle) {

            final Bundle bundle = (Bundle) state;

            super.onRestoreInstanceState(bundle.getParcelable("instanceState"));

        } else {

            super.onRestoreInstanceState(state);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mWidth = w;
        mHeight = h;

        final Context ctx = getContext();

        mLeftThumb = new RangeThumb(ctx, true, mThumbBandHeight, getRangeStartX(), 0.0f, mThumbWidth, w,
                h, mLeftThumbNormalResId, mLeftThumbPressResId);

        mRightThumb = new RangeThumb(ctx, false, mThumbBandHeight, getRangeEndX(), 0.0f, mThumbWidth, w,
                h, mRightThumbNormalResId, mRightThumbPressResId);

        mMask = new ThumbnailMask(ctx, mGradientWidth, w, mThumbBandHeight,
                getRangeStartX(), getRangeEndX(), mLeftGradientResId, mRightGradientResId,
                mVideoClipMaskResId);

        mIndicator = new PreviewPositionIndicator(ctx, getRangeStartX(), mIndicatorWidth, mThumbBandHeight,
                mIndicatorImageResId);
    }

    public void setIndicatorVisible(boolean visible) {
        if (mIndicator != null) {
            mIndicator.setVisible(visible);
        }

        if (!visible) {
            invalidate();
        }
    }

    public void setIndicatorOffsetSec(float second) {
        if (second < 0) {
            second = 0.0f;
        }
        float offsetX = getRangeX(second);
        if (mIndicator != null) {
            mIndicator.setX(offsetX);
        }
        invalidate();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width;
        int height;

        final int measureWidthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int measureHeightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int measureWidth = MeasureSpec.getSize(widthMeasureSpec);
        final int measureHeight = MeasureSpec.getSize(heightMeasureSpec);

        if (measureWidthMode == MeasureSpec.AT_MOST) {
            width = measureWidth;
        } else if (measureWidthMode == MeasureSpec.EXACTLY) {
            width = measureWidth;
        } else {
            width = mDefaultWidth;
        }

        if (measureHeightMode == MeasureSpec.AT_MOST) {
            height = Math.min(mDefaultHeight, measureHeight);
        } else if (measureHeightMode == MeasureSpec.EXACTLY) {
            height = measureHeight;
        } else {
            height = mDefaultHeight;
        }

        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        mMask.draw(canvas);

        mLeftThumb.draw(canvas);
        mRightThumb.draw(canvas);

        if (mIndicator != null) {
            mIndicator.draw(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) {
            return false;
        }

        if (null != mVideoMaskScrollListener && mMask.isInTargetZone(event.getX(), event.getY())) {
            mVideoMaskScrollListener.onVideoMaskScrollListener(this, event);
        }

        int index;

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                onActionDown(event.getPointerId(0), event.getX(), event.getY());

                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                index = event.getActionIndex();
                onActionDown(event.getPointerId(index), event.getX(index), event.getY(index));

                return true;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                this.getParent().requestDisallowInterceptTouchEvent(false);
                onActionUp(event.getPointerId(0), event.getX(), event.getY());

                return true;
            case MotionEvent.ACTION_POINTER_UP:
                this.getParent().requestDisallowInterceptTouchEvent(false);
                index = event.getActionIndex();
                onActionUp(event.getPointerId(index), event.getX(), event.getY());

                return true;

            case MotionEvent.ACTION_MOVE:
                onActionMove(event);

                this.getParent().requestDisallowInterceptTouchEvent(true);
                return true;

            default:
                return false;
        }
    }

    private boolean mInTouching = false;

    public boolean isTouching() {
        return mInTouching;
    }

    private void onActionDown(int pointerId, float x, float y) {
        if (mLeftThumb.isInTargetZone(x, y)) {
            mLeftThumb.press();
            mLeftThumb.setPointerId(pointerId);

            invalidate();
        } else if (mRightThumb.isInTargetZone(x, y)) {
            mRightThumb.press();
            mRightThumb.setPointerId(pointerId);

            invalidate();
        }
        mInTouching = true;
    }

    private void onActionUp(int pointerId, float x, float y) {
        mInTouching = false;
        if (mLeftThumb.isPressed() && mLeftThumb.getPointerId() == pointerId) {
            mLeftThumb.release();

            invalidate();
        }

        if (mRightThumb.isPressed() && mRightThumb.getPointerId() == pointerId) {
            mRightThumb.release();
            invalidate();
        }

        if (mContext != null) {
            if (((mRangeEnd - mRangeStart) >= 7.5f) && ((mRangeEnd - mRangeStart) <= 8.5f)) {
                setRange(mRangeStart, mRangeStart + 8.0f);
            }
        }

        if (null != mRangeBarChangeListener && (mLeftThumb.isInTargetZone(x, y) || mRightThumb
                .isInTargetZone(x, y))) {
            mRangeBarChangeListener.onActionUp();
        }
    }

    private void onActionMove(MotionEvent event) {
        int pointerId = 0;
        boolean needRefresh = false;
        boolean toEnd = false;
        int change = 0;
        for (int i = 0; i < event.getPointerCount(); ++i) {
            pointerId = event.getPointerId(i);
            if (mLeftThumb.isPressed() && mLeftThumb.getPointerId() == pointerId) {
                setRangeStartX(event.getX(i));
                mLeftThumb.setX(getRangeStartX());

                mMask.setRange(getRangeStartX(), getRangeEndX());
                needRefresh = true;

                change = OnRangeBarChangeListener.LEFT_CHANGE;
            }

            if (mRightThumb.isPressed() && mRightThumb.getPointerId() == pointerId) {
                toEnd = setRangeEndX(event.getX(i));
                mRightThumb.setX(getRangeEndX());
                mMask.setRange(getRangeStartX(), getRangeEndX());
                needRefresh = true;

                change = OnRangeBarChangeListener.RIGHT_CHANGE;
            }
        }

        if (needRefresh) {
            if (null != mRangeBarChangeListener) {
                mRangeBarChangeListener.onIndexChangeListener(this, mRangeStart, mRangeEnd, change, toEnd);
            }
            invalidate();
        }
    }

    public static interface OnRangeBarChangeListener {
        public static int LEFT_CHANGE = 1;
        public static int RIGHT_CHANGE = 2;
        public static int BOTH_CHANGE = 3;

        public void onIndexChangeListener(VideoRangeSeekBar rangeBar, float rangeStart, float rangeEnd,
                                          int change, boolean toEnd);

        public void onActionUp();
    }

    public static interface OnVideoMaskScrollListener {
        public void onVideoMaskScrollListener(VideoRangeSeekBar rangeBar, MotionEvent event);
    }
}
