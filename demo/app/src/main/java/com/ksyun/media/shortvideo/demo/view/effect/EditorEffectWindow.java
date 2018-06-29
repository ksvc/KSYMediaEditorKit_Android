package com.ksyun.media.shortvideo.demo.view.effect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ksyun.media.shortvideo.demo.R;
import com.ksyun.media.shortvideo.demo.videorange.VideoThumbnailTask;
import com.ksyun.media.shortvideo.kit.KSYEditKit;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author: [xiaoqiang]
 * @Description: [用于滤镜特效]
 * @CreateDate: [2018/1/27]
 * @UpdateDate: [2018/1/27]
 * @UpdateUser: [xiaoqiang]
 * @UpdateRemark: []
 */

public class EditorEffectWindow extends RelativeLayout {

    private static final String TAG = EditorEffectWindow.class.getName();

    private static final String[] EFFECTS_NAME = {"抖动", "冲击波", "灵魂出窍", "闪电", "Black magic", "70s", "幻觉", "X-Single"};
    private static final int[] EFFECTS_IMG_ID = {R.drawable.effects_shake, R.drawable.effects_shock_wave,
            R.drawable.effects_soul, R.drawable.effects_lightning, R.drawable.black_magic, R.drawable.effect_70s,
            R.drawable.effect_illusion, R.drawable.effect_xsignal};
    private static final int[] EFFECTS_COLOR = {0xAAFFF687, 0xAA8AC0FF, 0xAAFF2E4E,
            0xAA9FFF6E, 0xAAFFAE66, 0xAA9013FE, 0xAA4A4BE2, 0xAA1FA20A};

    private static final String[] TIME_EFFECTS_NAME = {"无", "倒放", "反复", "慢动作"};
    private static final int[] TIME_EFFECTS_IMG_ID = {R.drawable.time_effect_none, R.drawable.time_effect_reverse,
            R.drawable.time_effect_repeatedly, R.drawable.time_effect_slow};
    private static final int TIME_REVERSE_PLAYER = 0x50F5A623;

    private Context mContext;

    protected LinearLayout mLinearLayout;
    protected EffectSeekBar mSeekbar;
    protected Button mCancel;
    protected TextView mEffectHint;

    protected LinearLayout mTimeLinearLayout;
    protected EffectSeekBar mTimeSeekbar;
    protected TextView mTimeEffectHint;
    protected View mCurrentTimeView;
    protected ImageView mTimeHandle;

    private LinearLayout mEffectView;
    private LinearLayout mTimeEffectView;

    private int mEffectTime = 0;
    private int mCurrentEffectTimeItem = 0;


    private Bitmap[] mBitmaps = new Bitmap[14];
    private KSYEditKit mEditKit;
    private View mIsTouchSelectView;
    private boolean isFromUser;
    private OnEffectsChangeListener mEffectsChangeListener;
    private Map<Integer, Integer> mIndexMap;
    private AtomicBoolean mIsLongClick = new AtomicBoolean(false);
    private Handler mHandler;

    public EditorEffectWindow(Context context) {
        super(context);
        initView();
    }

    public EditorEffectWindow(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public EditorEffectWindow(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
    }

    private void initSeekBar() {
        for (int i = 0; i < mBitmaps.length; i++) {
            mBitmaps[i] = BitmapFactory.decodeResource(getResources(), R.drawable.effect_default);
        }
        mSeekbar.setBackBitmap(mBitmaps);
        mTimeSeekbar.setBackBitmap(mBitmaps);
        mSeekbar.setOnSeekBarChangeListener(mOnSeekBar);
        mTimeSeekbar.setOnSeekBarChangeListener(mOnTimeSeekBar);
    }

    private void loadVideoBitmap() {
        long unitTimer = mEditKit.getEditDuration() / 14;
        for (int i = 0; i < mBitmaps.length; i++) {
            VideoThumbnailTask.loadBitmap(mContext, i, unitTimer * i, 100, 100, mEditKit, mLoadListener);
        }
    }

    private VideoThumbnailTask.ThumbnailLoadListener mLoadListener =
            new VideoThumbnailTask.ThumbnailLoadListener() {
                @Override
                public void onFinished(Bitmap bitmap, int index) {
                    mBitmaps[index] = bitmap;
                    mSeekbar.setBackBitmap(mBitmaps);
                    mTimeSeekbar.setBackBitmap(mBitmaps);
                }
            };


    public void setEffectsChangeListener(OnEffectsChangeListener mEffectsChangeListener) {
        this.mEffectsChangeListener = mEffectsChangeListener;
    }

    public void bindKSYEditKit(KSYEditKit mKit) {
        this.mEditKit = mKit;
    }

    public void openTimeEffect(boolean isOpenTimeEffect) {
        removeAllViews();
        if (isOpenTimeEffect) {
            addView(mTimeEffectView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        } else {
            addView(mEffectView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }
    }

    public void setProgress(int progress) {
        mSeekbar.setProgress(progress);
        mTimeSeekbar.setProgress(progress);
        if (isLastTime(progress)) {
            Log.w(TAG, "达到最后一秒，主动触发长按离开的事件");
            onLongClickUp();
        }
    }

    public void startPlayer() {
        mSeekbar.setMax((int) mEditKit.getEditDuration());
        mTimeSeekbar.setMax((int) mEditKit.getEditDuration());
        loadVideoBitmap();
        mHandler.removeCallbacks(mPlayerRunnable);
        mHandler.postDelayed(mPlayerRunnable, 50);
    }

    private Runnable mPlayerRunnable = new Runnable() {
        @Override
        public void run() {
            mHandler.postDelayed(mPlayerRunnable, 10);
            if (isFromUser) return;
            long curTime = mEditKit.getCurrentPosition();
            if (isLastTime((int) curTime)) {
                Log.w(TAG, "达到最后一秒，主动触发长按离开的事件");
                onLongClickUp();
            }
            mSeekbar.setProgress((int) curTime);
            mTimeSeekbar.setProgress((int) curTime);
        }
    };

    private boolean isLastTime(int time) {
        return ((time == mSeekbar.getMax() && !mEditKit.getReversePlay())
                || (time == 0 && mEditKit.getReversePlay()))
                && mIsTouchSelectView != null;
    }

    public void stopPlayer() {
        mHandler.removeCallbacks(mPlayerRunnable);
    }

    public void clearAllEffect() {
        if (mSeekbar != null) {
            mSeekbar.deleteAll();
            mTimeSeekbar.deleteAll();
        }
        mCancel.setVisibility(GONE);
    }

    private void initView() {
        mContext = getContext();
        mEffectView = (LinearLayout) inflate(mContext, R.layout.layout_effect, null);
        mTimeEffectView = (LinearLayout) inflate(mContext, R.layout.layout_effect, null);
        mHandler = new Handler();


        mLinearLayout = mEffectView.findViewById(R.id.ll_effect_list);
        mSeekbar = mEffectView.findViewById(R.id.effects_seek_bar);
        mCancel = mEffectView.findViewById(R.id.btn_effect_cancel);
        mEffectHint = mEffectView.findViewById(R.id.tv_effect_hint);
        mEffectHint.setText(mContext.getResources().getString(R.string.effect_hint));

        mTimeLinearLayout = mTimeEffectView.findViewById(R.id.ll_effect_list);
        mTimeSeekbar = mTimeEffectView.findViewById(R.id.effects_seek_bar);
        mTimeEffectHint = mTimeEffectView.findViewById(R.id.tv_effect_hint);
        mTimeHandle = mTimeEffectView.findViewById(R.id.imgv_time_handle);
        mTimeEffectHint.setText(mContext.getResources().getString(R.string.effect_time_hint));
        mTimeHandle.setOnTouchListener(mTimeHandleTouch);


        //默认隐藏撤销按钮
        mCancel.setVisibility(GONE);
        mLinearLayout.removeAllViews();
        mTimeLinearLayout.removeAllViews();

        for (int i = 0; i < EFFECTS_NAME.length; i++) {
            View view = addItemView(EFFECTS_IMG_ID[i], EFFECTS_NAME[i]);
            view.setTag(i);
        }
        for (int i = 0; i < TIME_EFFECTS_NAME.length; i++) {
            View view = addTimeItemView(TIME_EFFECTS_IMG_ID[i], TIME_EFFECTS_NAME[i]);
            view.setTag(i);
            if (mCurrentTimeView == null) {
                mCurrentTimeView = view;
                mCurrentTimeView.findViewById(R.id.image_select).setVisibility(VISIBLE);
            }
        }

        mCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsTouchSelectView == null && mSeekbar.getListScope().size() > 0) {
                    final int index = mSeekbar.deleteLast();
                    if (mIndexMap != null
                            && mSeekbar.getListScope().size() < mIndexMap.size()
                            && mEffectsChangeListener != null) {
                        int key = mSeekbar.getListScope().size() + 1;
                        mEffectsChangeListener.onDelete(mIndexMap.get(key));
                        mIndexMap.remove(key);

                        if (mIndexMap.size() <= 0) {
                            mCancel.setVisibility(GONE);
                        }
                    }
                    if (mEditKit != null) {
                        mEditKit.seekTo(index);
                        //FIXME: 在2.2.1版本中，这里的SeekTo需要延时一定的时间才能生效，后期版本中会进行优化
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                mEditKit.seekTo(index);
                            }
                        }, 100);
                    }
                    int in = mSeekbar.getListScope().size();
                    Log.i(TAG, "删除滤镜，滤镜是" + (in + 1));
                }
            }
        });
        initSeekBar();
    }

    private void moveTimeEffectHandle(int size) {
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) mTimeHandle.getLayoutParams();
        if (params == null) {
            params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        int width = mTimeHandle.getWidth() / 2;
        int maxWidth = getWidth(mContext);
        if (size < width) {
            size = width;
        } else if (size > maxWidth) {
            size = maxWidth;
        }
        params.leftMargin = size - width;
        mEffectTime = (int) (size * ((float) mEditKit.getEditDuration()) / maxWidth + 0.5f);
        mTimeHandle.setLayoutParams(params);
    }

    private View addTimeItemView(int imageResult, String str) {
        View view = inflate(mContext, R.layout.layout_effect_item, null);
        ImageView image = view.findViewById(R.id.image_effect);
        image.setImageResource(imageResult);
        TextView effName = view.findViewById(R.id.tv_effect_name);
        effName.setText(str);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mTimeLinearLayout.addView(view, params);
        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v == mCurrentTimeView) return;
                if (mCurrentTimeView != null) {
                    mCurrentTimeView.findViewById(R.id.image_select).setVisibility(GONE);
                }
                mCurrentTimeView = v;
                mTimeEffectHint.setText(mContext.getResources().getString(R.string.effect_time_hint));
                mTimeSeekbar.setDefaultColor(0x00000000);
                mTimeHandle.setVisibility(GONE);
                mEffectTime = 0;
                mCurrentEffectTimeItem = (int) mCurrentTimeView.getTag();
                switch (mCurrentEffectTimeItem) {
                    case 1:
                        mTimeSeekbar.setDefaultColor(TIME_REVERSE_PLAYER);
                        break;
                    case 2:
                        mTimeEffectHint.setText(mContext.getResources().getString(R.string.effect_time_repeatedly_hint));
                        mTimeHandle.setImageResource(R.drawable.time_effect_repeatedly_handle);
                        mTimeHandle.setVisibility(VISIBLE);
                        moveTimeEffectHandle((int) (getWidth(mContext) / 2f));
                        break;
                    case 3:
                        mTimeEffectHint.setText(mContext.getResources().getString(R.string.effect_time_slow_hint));
                        mTimeHandle.setImageResource(R.drawable.time_effect_slow_handle);
                        mTimeHandle.setVisibility(VISIBLE);
                        moveTimeEffectHandle((int) (getWidth(mContext) / 2f));
                        break;

                }
                mCurrentTimeView.findViewById(R.id.image_select).setVisibility(VISIBLE);
                if (mEffectsChangeListener != null) {
                    mEffectsChangeListener.onTimeEffects((Integer) v.getTag(), mEffectTime);
                }
            }
        });
        return view;
    }

    private OnTouchListener mTimeHandleTouch = new OnTouchListener() {
        boolean mIsDown = false;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (mTimeHandle.getVisibility() == VISIBLE) {
                        mIsDown = true;
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mIsDown) {
                        moveTimeEffectHandle((int) event.getRawX());
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    if (mIsDown && mEffectsChangeListener != null) {
                        mEffectsChangeListener.onTimeEffects(mCurrentEffectTimeItem, mEffectTime);
                    }
                    break;
            }
            return true;
        }
    };

    private View addItemView(int imageResult, String str) {
        View view = inflate(mContext, R.layout.layout_effect_item, null);
        ImageView image = view.findViewById(R.id.image_effect);
        image.setImageResource(imageResult);
        TextView effName = view.findViewById(R.id.tv_effect_name);
        effName.setText(str);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mLinearLayout.addView(view, params);
        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        onLongClickDown(v);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        onLongClickUp();
                        break;
                }
                return true;
            }
        });
        return view;
    }


    private void onLongClickUp() {
        mHandler.removeCallbacks(mLongClickRunnable);
        int in = mSeekbar.getListScope().size();
        Log.i(TAG, "stop add filter:" + in);
        if (mEffectsChangeListener != null && mIsLongClick.get()) {
            mIsLongClick.set(false);
            EffectSeekBar.Scope scope =
                    mSeekbar.getListScope().get(mSeekbar.getListScope().size() - 1);
            mEffectsChangeListener.onAddFilterEnd(
                    mIndexMap.get(mSeekbar.getListScope().size()), scope.end);
            mIsTouchSelectView.findViewById(R.id.image_select).setVisibility(GONE);
            mSeekbar.clearColor();
            stopPlayer();

            if (mIndexMap.size() > 0) {
                mCancel.setVisibility(VISIBLE);
            }
        }
        mIsTouchSelectView = null;
    }

    private void onLongClickDown(final View v) {
        if (mIsTouchSelectView != null) {
            return;
        }
        mIsTouchSelectView = v;
        mHandler.removeCallbacks(mLongClickRunnable);
        mHandler.postDelayed(mLongClickRunnable, 500);
    }

    private Runnable mLongClickRunnable = new Runnable() {
        @Override
        public void run() {
            mIsLongClick.set(true);
            mIsTouchSelectView.findViewById(R.id.image_select).setVisibility(VISIBLE);
            int type = (Integer) mIsTouchSelectView.getTag();
            mSeekbar.setColor(EFFECTS_COLOR[type]);
            int in = mSeekbar.getListScope().size();
            Log.i(TAG, "start add filter:" + in);
            if (mEffectsChangeListener != null) {
                int index = mEffectsChangeListener.onAddFilterStart(type);
                if (mIndexMap == null) {
                    mIndexMap = new HashMap<>();
                }
                mIndexMap.put(mSeekbar.getListScope().size(), index);
            }
            startPlayer();
        }
    };

    @Override
    public void setVisibility(int visibility) {
        super.setVisibility(visibility);
        if (View.VISIBLE == visibility) {

        }
    }

    public int getEffectCount() {
        if (mIndexMap == null) {
            return 0;
        }
        return mIndexMap.size();
    }

    private SeekBar.OnSeekBarChangeListener mOnSeekBar =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    isFromUser = fromUser;
                    if (mEffectsChangeListener != null && mIsTouchSelectView != null && mSeekbar != null) {
                        mEffectsChangeListener.onUpdateFilter(mIndexMap.get(mSeekbar.getListScope().size()));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (isFromUser) {
                        mEditKit.seekTo(seekBar.getProgress());
                    }
                    isFromUser = false;
                }
            };
    private SeekBar.OnSeekBarChangeListener mOnTimeSeekBar =
            new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    isFromUser = fromUser;
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (isFromUser) {
                        mEditKit.seekTo(seekBar.getProgress());
                    }
                    isFromUser = false;
                }
            };


    public interface OnEffectsChangeListener {
        int onAddFilterStart(int type);

        void onUpdateFilter(int index);

        void onAddFilterEnd(int index, long position);

        void onDelete(int index);

        void onTimeEffects(int type, int startTime);
    }


    private static int getWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        return display.getWidth();
    }
}
