package com.ksyun.media.shortvideo.demo.audiorange;

import com.ksyun.media.shortvideo.demo.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AudioSeekLayout extends LinearLayout {
    private Context mContext;
    private Drawable mOriginDraw;  //原始音频波浪图
    private Bitmap mCoverBmp;   //上层覆盖的纯色Bitmap
    private Paint mPaint;  //绘制音频选择区域画笔
    private int mScreenWidth;
    private float mScreenDensity;
    private int mWaveImageWidth;
    private int mWaveImageHeight;
    private int mCoverImageWidth;
    private float mBgmLength = -1;
    private float mPreviewLength = -1;
    private long mStartTime = 0;
    private float mRate = .0f;
    private float mOriginX = .0f;  //触摸位置相对控件的位置
    private TextView mAudioStartTime;
    private TextView mAudioEndTime;
    private ImageView mAudioSeekBg;
    private ImageView mAudioSeekBar;
    private OnAudioSeekChecked mListener;

    public interface OnAudioSeekChecked {
        void onActionUp(long start, long end);
    }

    public void setOnAudioSeekCheckedListener(OnAudioSeekChecked listener) {
        mListener = listener;
    }

    public AudioSeekLayout(Context context) {
        this(context, null);
    }

    public AudioSeekLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AudioSeekLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mScreenWidth = context.getResources().getDisplayMetrics().widthPixels;
        mScreenDensity = context.getResources().getDisplayMetrics().density;
        LayoutInflater.from(context).inflate(R.layout.audio_seek, this, true);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //获取子控件
        mAudioStartTime = (TextView) findViewById(R.id.bgm_start_time);
        mAudioEndTime = (TextView) findViewById(R.id.bgm_end_time);
        mAudioSeekBg = (ImageView) findViewById(R.id.origin_audio_wave);
        mAudioSeekBar = (ImageView) findViewById(R.id.bgm_seek_bar);
        mAudioSeekBar.setEnabled(false);
        mOriginDraw = ContextCompat.getDrawable(mContext, R.drawable.audio_wave);
        mWaveImageWidth = ((BitmapDrawable) mOriginDraw).getBitmap().getWidth();
        mWaveImageHeight = ((BitmapDrawable) mOriginDraw).getBitmap().getHeight();
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        PorterDuff.Mode mode = PorterDuff.Mode.SRC_ATOP;
        mPaint.setXfermode(new PorterDuffXfermode(mode));
    }

    public void updateAudioSeekUI(float bgmLength, float previewLength) {
        if (bgmLength == -1 || previewLength == -1)
            return;
        mBgmLength = bgmLength;
        mPreviewLength = previewLength;
        if (mBgmLength != 0) {
            mRate = mPreviewLength / mBgmLength;
        }
        if (mRate > 1) {
            mRate = 1;
        }
        mCoverImageWidth = (int) (mWaveImageWidth * mRate);
        mCoverBmp = Bitmap.createBitmap(mCoverImageWidth, mWaveImageHeight, Bitmap.Config.ARGB_8888);
        mCoverBmp.eraseColor(Color.parseColor("#DC143C"));
        if (mRate < 1) {
            mAudioEndTime.setText(formatTimeStr(mStartTime + mPreviewLength));
        } else {
            mAudioEndTime.setText(formatTimeStr(mBgmLength));
        }
        final int minMarginLeft = (int) (12 * mScreenDensity);
        final int maxMarginLeft = (int) (mScreenWidth - mCoverImageWidth - 58 * mScreenDensity);
        int offset = ((LayoutParams) mAudioSeekBar.getLayoutParams()).leftMargin - minMarginLeft;
        updateAudioSeekUI(offset);
        mAudioSeekBar.setEnabled(true);
        mAudioSeekBar.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mOriginX = event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        LinearLayout.LayoutParams para = (LinearLayout.LayoutParams) mAudioSeekBar.getLayoutParams();
                        float offsetX = event.getRawX() - mOriginX;
                        if (minMarginLeft <= offsetX && offsetX <= maxMarginLeft) {
                            para.leftMargin = (int) offsetX;
                        }
                        mAudioSeekBar.setLayoutParams(para);
                        updateAudioSeekUI(para.leftMargin - minMarginLeft);
                        break;
                    case MotionEvent.ACTION_UP:
                        long startTime = 0;
                        long endTime = (long) mBgmLength;
                        if (mRate < 1) {
                            float rate = (event.getRawX() - mOriginX - minMarginLeft) / mWaveImageWidth;
                            startTime = (long) (mBgmLength * rate);
                            if (startTime < 0)
                                startTime = 0;
                            endTime = startTime + (long) mPreviewLength;
                            if (endTime > mBgmLength) {
                                endTime = (long) mBgmLength;
                                startTime = endTime - (long) mPreviewLength;
                            }
                        }
                        mStartTime = startTime;
                        mAudioStartTime.setText(formatTimeStr(startTime));
                        mAudioEndTime.setText(formatTimeStr(endTime));
                        if (mListener != null) {
                            mListener.onActionUp(startTime, endTime);
                        }
                }
                return true;
            }
        });
    }

    private void updateAudioSeekUI(int startX) {
        Bitmap originBmp = ((BitmapDrawable) mOriginDraw).getBitmap().copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas = new Canvas(originBmp);
        canvas.drawBitmap(mCoverBmp, startX, 0, mPaint);
        mAudioSeekBg.setImageBitmap(originBmp);
    }

    private String formatTimeStr(float s) {
        int minute = (int) (s / (1000 * 60));
        int second = (int) ((s / 1000) % 60);
        return String.format("%02d:%02d", minute, second);
    }

}
