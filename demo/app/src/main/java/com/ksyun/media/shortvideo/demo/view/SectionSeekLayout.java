package com.ksyun.media.shortvideo.demo.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.ksyun.media.shortvideo.demo.R;
import com.ksyun.media.shortvideo.demo.videorange.VideoThumbnailInfo;
import com.ksyun.media.shortvideo.demo.videorange.VideoThumbnailTask;
import com.ksyun.media.shortvideo.kit.KSYEditKit;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


/**
 * 片段编辑时区域选择的交互UI,可滑动选择起始时间、结束时间和片段长度，提供选择的回调接口
 */

public class SectionSeekLayout extends RelativeLayout {

    private static final String TAG = "SectionSeekLayout";
    private static final String STATE_AUTO_SCROLL = "scroll_auto";
    private static final String STATE_PASSIVE_SCROLL = "scroll_passive";
    private static final int ITEM_WIDTH = 120;  //单个缩略图的宽度
    private static final int INTERVAL = 5; // 用于监听惯性滑动的消息间隔，单位毫秒
    private static final int THUMBNAIL_INTERVAL = 1000; //获取缩略图的间隔
    private int mItemCount = 0;
    private int mLastX = 0;  //滚动距离
    private int mSectionId = -1;  //要执行区间选择的事务id
    private String mState = STATE_AUTO_SCROLL;
    private Context mContext;
    private int mScreenWidth;
    private HorizontalScrollView mScrollView;
    private RelativeLayout mScrollParent;  //包裹RecyclerView的父布局，用来添加区间阴影
    private RecyclerView mThumbView;
    private RelativeLayout mSectionTopView;
    private View mSectionTools;  //seek选择框
    private ImageView mSeekRear;
    private View mSeekBody;
    private ImageView mSeekFront;
    private Bitmap mDefaultBmp;

    private int mSectionWidth;  //选择框宽度
    private int mRectLeftPosition;  //选择框可滑动的左边界
    private int mRectRightPosition; //选择框可滑动的右边界
    private int mRearWidth;
    private int mFrontWidth;
    private int mRightBorder;  //滑动左边Button时的右边界，右边Button位置保持不变
    private int mLeftBorder;  //滑动右边Button时的左边界，通过改变中间透明View宽度来实现伸缩
    private float mOriginX;  //seek事件的起始位置
    private long mPreviewLength;  //视频时长
    private OnSectionSeekListener mListener;
    private SimpleImageAdapter mSimpleAdapter;
    private KSYEditKit mKit;
    private SparseArray<SectionInfo> mSectionMap; //所有区间信息集合
    private RelativeLayout.LayoutParams mSectionParams;
    private ViewGroup.LayoutParams mBodyParams;

    public interface OnSectionSeekListener {
        void onRangeChanged(int index, long start, long end);

        void removeSticker(int id);

        void onPausePreview();

        void onSeekTo(long time);
    }

    public void setOnSectionSeekListener(OnSectionSeekListener listener) {
        this.mListener = listener;
    }

    public SectionSeekLayout(Context context) {
        this(context, null);
    }

    public SectionSeekLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SectionSeekLayout(Context context, AttributeSet attrs, final int defStyle) {
        super(context, attrs, defStyle);
        mContext = context;
        mScreenWidth = mContext.getResources().getDisplayMetrics().widthPixels;
        LayoutInflater.from(mContext).inflate(R.layout.section_seek, this, true);
        mScrollView = (HorizontalScrollView) findViewById(R.id.thumbnail_scroll_view);
        mScrollParent = (RelativeLayout) findViewById(R.id.scroll_parent);
        mThumbView = (RecyclerView) findViewById(R.id.section_recycler);
        mSectionTopView = (RelativeLayout) findViewById(R.id.section_top);
        mSectionTools = findViewById(R.id.section_tools);
        mSeekRear = (ImageView) findViewById(R.id.seek_rear);
        mSeekBody = findViewById(R.id.seek_body);
        mSeekFront = (ImageView) findViewById(R.id.seek_front);
        mScrollView.setOnTouchListener(new OnTouchListener() {
            private int touchEventId = -100;
            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (msg.what == touchEventId) {
                        if (mLastX == mScrollView.getScrollX()) { //滚动真正停止
                            if (mLastX < 0) {
                                mLastX = 0;
                            }
                            int max = (mItemCount - 2) * ITEM_WIDTH;
                            if (mLastX > max) {
                                mLastX = max;
                            }
                            updateSeekRange();
                            onScrollSeek(mScrollView.getScrollX());
                        } else {
                            //滚动未停止则继续监听
                            handler.sendMessageDelayed(handler.obtainMessage(touchEventId), INTERVAL);
                            mLastX = mScrollView.getScrollX();
                            Log.d(TAG, "handleMessage: lastX=" + mLastX);
                        }
                    }
                }
            };

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mState.equals(STATE_AUTO_SCROLL)) {
                    mState = STATE_PASSIVE_SCROLL;
                    if (mListener != null) {
                        mListener.onPausePreview();
                    }
                }
                switch (event.getAction()) {
                    case MotionEvent.ACTION_UP:
                        //抬手后发送延迟消息监测惯性滚动
                        handler.sendMessageDelayed(handler.obtainMessage(touchEventId), INTERVAL);
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
    }

    //必须执行，需要传入视频信息及KSYEditKit来执行缩略图获取任务
    public void init(long duration, KSYEditKit kit) {
        mPreviewLength = duration;
        mKit = kit;
        mRectLeftPosition = 0;
        mRectRightPosition = mScreenWidth;
        mSectionMap = new SparseArray<>();
        mDefaultBmp = BitmapFactory.decodeResource(mContext.getResources(),R.drawable.top_default);
        //每1s取一张图片，第一个Item和最后一个Item为空白，宽度为屏幕宽度一半，方便从屏幕中间开始预览
        mItemCount = (int) (duration / THUMBNAIL_INTERVAL) + 2;
        mSimpleAdapter = new SimpleImageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mThumbView.setLayoutManager(layoutManager);
        mThumbView.setAdapter(mSimpleAdapter);
        mSectionParams = (RelativeLayout.LayoutParams) mSectionTopView.getLayoutParams();
        mBodyParams = mSeekBody.getLayoutParams();
        mScrollView.setSmoothScrollingEnabled(true);
        mSeekRear.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onSeekRear(event);
                return true;
            }
        });
        mSeekBody.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onSeekBody(event);
                return true;
            }
        });
        mSeekFront.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                onSeekFront(event);
                return true;
            }
        });
    }

    //选择框左边按钮滑动事件，效果为该按键自己滑动，选择框伸缩
    private void onSeekRear(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mRearWidth = mSeekRear.getWidth();
                mRightBorder = mSectionParams.leftMargin + mRearWidth + mBodyParams.width;
                mOriginX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                int leftMargin = (int) (event.getRawX() - mOriginX);
                if (leftMargin < mRectLeftPosition) {
                    leftMargin = mRectLeftPosition;
                }
                if (leftMargin > mRightBorder - mRearWidth) {
                    leftMargin = mRightBorder - mRearWidth;
                }
                mSectionParams.leftMargin = leftMargin;
                mSectionTopView.setLayoutParams(mSectionParams);
                int bodyWidth = mRightBorder - leftMargin - mRearWidth;
                mBodyParams.width = bodyWidth;
                mSeekBody.setLayoutParams(mBodyParams);
                break;
            default:
                break;
        }
    }

    //选择框中间部分的滑动事件，效果为选择框整体滑动，宽度不变
    private void onSeekBody(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mRearWidth = mSeekRear.getWidth();
                mFrontWidth = mSeekFront.getWidth();
                mOriginX = event.getX() + mRearWidth;
                mSectionWidth = mRearWidth + mBodyParams.width + mFrontWidth;
                break;
            case MotionEvent.ACTION_MOVE:
                int leftMargin = (int) (event.getRawX() - mOriginX);
                if (leftMargin < mRectLeftPosition) {
                    leftMargin = mRectLeftPosition;
                }
                if (leftMargin > mRectRightPosition - mSectionWidth) {
                    leftMargin = mRectRightPosition - mSectionWidth;
                }
                mSectionParams.leftMargin = leftMargin;
                mSectionTopView.setLayoutParams(mSectionParams);
                break;
            default:
                break;
        }
    }

    //选择框右边按钮滑动事件，效果为该按键自己滑动，选择框伸缩
    private void onSeekFront(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mOriginX = event.getX();
                mRearWidth = mSeekRear.getWidth();
                mFrontWidth = mSeekFront.getWidth();
                mLeftBorder = mSectionParams.leftMargin + mRearWidth;
                break;
            case MotionEvent.ACTION_MOVE:
                int leftMargin = (int) (event.getRawX() - mOriginX);
                if (leftMargin < mLeftBorder) {
                    leftMargin = mLeftBorder;
                }
                if (leftMargin > mRectRightPosition - mFrontWidth) {
                    leftMargin = mRectRightPosition - mFrontWidth;
                }
                int bodyWidth = leftMargin - mLeftBorder;
                mBodyParams.width = bodyWidth;
                mSeekBody.setLayoutParams(mBodyParams);
                break;
            default:
                break;
        }
    }

    public class SimpleImageAdapter extends RecyclerView.Adapter<SimpleImageAdapter.ImageHolder> {

        @Override
        public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.item_thumbnail, null);
            ImageHolder holder = new ImageHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(ImageHolder holder, int position) {
            if (position == 0 || position == mItemCount - 1) {
                ViewGroup.LayoutParams params = holder.image.getLayoutParams();
                params.width = mScreenWidth / 2;
                holder.image.setLayoutParams(params);
            } else {
                ViewGroup.LayoutParams params = holder.image.getLayoutParams();
                params.width = ITEM_WIDTH;
                holder.image.setLayoutParams(params);
                holder.image.setImageBitmap(mDefaultBmp);
                long currentTime = (long) ((position - 1) * THUMBNAIL_INTERVAL);
                if (currentTime < 0) {
                    currentTime = 0;
                }
                if (currentTime > mPreviewLength) {
                    currentTime = mPreviewLength;
                }
                VideoThumbnailInfo info = new VideoThumbnailInfo();
                info.mWidth = ITEM_WIDTH * 3;
                VideoThumbnailTask.loadBitmap(mContext, holder.image,
                        null, currentTime, info, mKit, null);
            }
        }

        @Override
        public int getItemCount() {
            return mItemCount;
        }

        public class ImageHolder extends RecyclerView.ViewHolder {
            public ImageView image;

            public ImageHolder(View view) {
                super(view);
                image = (ImageView) view.findViewById(R.id.thumbnail);
            }
        }
    }

    private void updateSeekRange() {
        mLastX = mScrollView.getScrollX();
        if (mLastX >= mScreenWidth / 2) {
            mRectLeftPosition = 0;
        } else {
            mRectLeftPosition = mScreenWidth / 2 - mLastX;
        }
        mRectRightPosition = mScreenWidth / 2 + ITEM_WIDTH * (mItemCount - 2) - mLastX;
        if (mRectRightPosition > mScreenWidth) {
            mRectRightPosition = mScreenWidth;
        }
    }

    //预留接口，滚动时间条时同步更新播放器预览的图片
    private void onScrollSeek(int scrollX) {
        long time = (long) ((float) (scrollX * mPreviewLength) / ((mItemCount - 2) * ITEM_WIDTH));
        if (time < 0) {
            time = 0;
        }
        if (time > mPreviewLength) {
            time = mPreviewLength;
        }
        if (mListener != null) {
            mListener.onSeekTo(time);
        }
    }

    //更新区间信息并存储，需要立即生效直接调用此方法
    public void calculateRange() {
        if (mSectionTools.getVisibility() != VISIBLE) {
            return;
        }
        int start = mSectionParams.leftMargin + mLastX - mScreenWidth / 2;
        int end = start + mSectionTopView.getWidth();
        int total = (mItemCount - 2) * ITEM_WIDTH;
        long startTime = (long) start * mPreviewLength / total;
        long endTime = (long) end * mPreviewLength / total;
        int left = mSectionParams.leftMargin + mLastX;
        int right = left + mSectionTopView.getWidth();
        if (startTime < 0) {
            startTime = 0;
            left = mScreenWidth / 2;
        }
        if (endTime > mPreviewLength) {
            endTime = mPreviewLength;
            right = mScreenWidth / 2 + (mItemCount - 2) * ITEM_WIDTH;
        }
        int width = right - left;
        if (startTime > mPreviewLength || endTime < 0) {
            startTime = 0;
            endTime = 0;
            width = 0;
        }
        SectionInfo info = mSectionMap.get(mSectionId);
        if (info == null && width > 0) {
            info = new SectionInfo(mSectionId, left, width, startTime, endTime);
            mSectionMap.put(mSectionId, info);
        } else if (info != null && width > 0) {
            info.leftMargin = left;
            info.width = width;
            info.startTime = startTime;
            info.endTime = endTime;
        } else if (info != null && width == 0) {
            mSectionMap.remove(info.id);
            mListener.removeSticker(mSectionId);
        } else {
            mSectionTools.setVisibility(INVISIBLE);
            mListener.removeSticker(mSectionId);
        }
        if (info != null && info.view.getParent() == null) {
            info.show();
        }
        if (mListener != null) {
            mListener.onRangeChanged(mSectionId, startTime, endTime);
        }
    }

    public void startPreview() {
        mState = STATE_AUTO_SCROLL;
        mSectionTools.setVisibility(INVISIBLE);
        for (int i = 0; i < mSectionMap.size(); i++) {
            int key = mSectionMap.keyAt(i);
            SectionInfo info = mSectionMap.get(key);
            if (info.view.getParent() == null) {
                info.show();
            }
        }
        if (mSectionMap.get(mSectionId) == null) {
            if (mListener != null) {
                mListener.removeSticker(mSectionId);
            }
        }
    }

    public void stopPreview() {
        mState = STATE_PASSIVE_SCROLL;
    }

    //根据主界面的当前预览时间让时间条滚动到相应位置
    public void scrollAuto(long time) {
        int totalLength = (mItemCount - 2) * ITEM_WIDTH;
        int curMargin = (int) ((float) time * totalLength / mPreviewLength);
        if (curMargin != mScrollView.getScrollX()) {
            mScrollView.smoothScrollTo(curMargin, 0);
            updateSeekRange();
        }
    }

    //开始区间选择，使区间选择框可见
    public void startSeek(int id) {
        mSectionId = id;
        updateSeekRange();
        mSectionParams.leftMargin = mScreenWidth / 2;
        mSectionTopView.setLayoutParams(mSectionParams);
        mSectionTools.setVisibility(VISIBLE);
        SectionInfo info = mSectionMap.get(mSectionId);
        if (info != null) {
            mScrollParent.removeView(info.view);
            mSectionParams.leftMargin = info.leftMargin - mLastX;
            mSectionTopView.setLayoutParams(mSectionParams);
            mBodyParams.width = info.width - mSeekFront.getWidth() - mSeekFront.getWidth();
            mSeekBody.setLayoutParams(mBodyParams);
        }
    }

    //删除区间
    public void delete(List<Integer> list) {
        for (int i = 0; i < list.size(); i++) {
            int id = list.get(i);
            SectionInfo info = mSectionMap.get(id);
            if (info != null) {
                mScrollParent.removeView(info.view);
                mSectionMap.remove(id);
                mSectionMap.delete(id);
            }
        }
        if (mSectionTools.getVisibility() == VISIBLE) {
            mSectionTools.setVisibility(INVISIBLE);
        }
    }

    // 判断是否处于seek状态
    public boolean isSeeking() {
        return mSectionTools.getVisibility() == VISIBLE;
    }

    // 存储区间信息的数据类
    public class SectionInfo {
        public int id;
        public int leftMargin;
        public int width;
        public int height;
        public long startTime;
        public long endTime;
        public View view; //区间阴影

        public SectionInfo(int id, int margin, int width, long start, long end) {
            this.id = id;
            this.leftMargin = margin;
            this.width = width;
            this.height = mSectionTopView.getHeight();
            this.startTime = start;
            this.endTime = end;
            this.view = new View(mContext);
            this.view.setAlpha(0.5f);
            this.view.setBackgroundColor(Color.parseColor("#BFEFFF"));
        }

        //显示区间阴影
        public void show() {
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(width, height);
            params.leftMargin = leftMargin;
            mScrollParent.addView(view, params);
            mSectionTools.setVisibility(GONE);
        }
    }

}
