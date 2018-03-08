package com.ksyun.media.shortvideo.demo.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.ksyun.media.shortvideo.demo.R;
import com.ksyun.media.shortvideo.demo.adapter.ImageTextAdapter;
import com.ksyun.media.shortvideo.demo.util.DataFactory;
import com.ksyun.media.shortvideo.demo.videorange.VideoThumbnailTask;
import com.ksyun.media.shortvideo.kit.KSYEditKit;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: [xiaoqiang]
 * @Description: [滤镜特效UI]
 * @CreateDate: [2018/1/8]
 * @UpdateDate: [2018/1/8]
 * @UpdateUser: [xiaoqiang]
 * @UpdateRemark: []
 */

public class FilterEffectsView extends FrameLayout {
    private final static String TAG = FilterEffectsView.class.getName();
    private Context mContext;
    private ColorFulImageSeekBar mSeekbar;
    private ImageTextAdapter mImgTextAdapter;
    private ImageView mCancel;
    private RecyclerView mRecycler;
    private static final int[] EFFECTS_COLOR = {0xAAFFF687, 0xAA8AC0FF, 0xAAFF2E4E, 0xAA9FFF6E, 0xAAFFAE66,
            0xAA9013FE, 0xAA4A4BE2, 0xAA1FA20A};
    private OnEffectsChangeListener mOnChangeListener;
    private int mThumbnailCount = 0;
    private long mEditDuration = 0;
    private Map<Integer, Integer> mFilterMap = new LinkedHashMap<>(); //key:由SDK返回的滤镜的ID; value:滤镜的index，也就是滤镜的类型
    private int mCurrentFilterId = -1;

    public FilterEffectsView(@NonNull Context context) {
        super(context);
    }

    public FilterEffectsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public FilterEffectsView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void initView(long duration, KSYEditKit kit) {
        inflate(getContext(), R.layout.filter_effects_view, this);
        mEditDuration = duration;
        mContext = getContext();
        mSeekbar = findViewById(R.id.effects_seek_bar);
        mRecycler = findViewById(R.id.beauty_recyclerView);
        mCancel = findViewById(R.id.iv_beauty_origin);
        mCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if(mFilterMap.size() > 0 && mCurrentFilterId != -1) {
                    mSeekbar.deleteColor(mCurrentFilterId);
                    if (mOnChangeListener != null) {
                        mOnChangeListener.onDelete(mCurrentFilterId);
                    }
                    mFilterMap.remove(mCurrentFilterId);
                    if (mFilterMap.size() > 0) {
                        Object[] idxs = mFilterMap.keySet().toArray();
                        mCurrentFilterId = (int) idxs[idxs.length - 1];
                    }
                }
            }
        });
        mSeekbar.setOnSeekBarChangeListener(mOnSeekBar);
        initRecycler();

        final Bitmap[] bitmaps = new Bitmap[14];
        long unitTimer = duration / 14;
        for (int i = 0; i < bitmaps.length; i++) {
            VideoThumbnailTask.loadBitmap(mContext, i, unitTimer * i, 100, 100, kit, new VideoThumbnailTask.ThumbnailLoadListener() {
                @Override
                public void onFinished(Bitmap bitmap, int index) {
                    if (bitmap == null) {
                        bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.RGB_565);
                    }
                    bitmaps[index] = bitmap;
                    mThumbnailCount++;
                    if (mThumbnailCount == 14) {
                        setBitmapsOrMax(bitmaps);
                    }

                }
            });
        }
    }

    private void initRecycler() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mRecycler.setLayoutManager(layoutManager);

        List<ImageTextAdapter.Data> beautyData = DataFactory.getEffectsTypeDate(mContext);

        mImgTextAdapter = new ImageTextAdapter(mContext, beautyData);
        mRecycler.setAdapter(mImgTextAdapter);

        mImgTextAdapter.setOnImageLongItemClick(
                new ImageTextAdapter.OnImageLongItemClickListener() {
                    @Override
                    public boolean onLongClick(View view, int index, int state) {
                        if (index < EFFECTS_COLOR.length) {

                            if (state == ImageTextAdapter.LONG_CLICK_STATE_START) {
                                int progress = mSeekbar.getProgress();
                                int max = mSeekbar.getMax();
                                if (progress == max) {
                                    return false;
                                }
                                //开始添加滤镜
                                if (mOnChangeListener != null) {
                                    int id = mOnChangeListener.onAddFilterStart(index);
                                    addFilter(id, index);
                                }
                            } else if (state == ImageTextAdapter.LONG_CLICK_STATE_CLICKING) {
                                mSeekbar.setProgressColor(mCurrentFilterId, EFFECTS_COLOR[index]);
                                //滤镜时长更新中
                                if (mOnChangeListener != null) {
                                    mOnChangeListener.onUpdateFilter(mCurrentFilterId);
                                }
                            } else if (state == ImageTextAdapter.LONG_CLICK_STATE_END) {
                                int progress = mSeekbar.getProgress();
                                int max = mSeekbar.getMax();
                                if (index == -1) {
                                    progress = max;
                                }
                                long position = progress * mEditDuration / max;
                                if (mOnChangeListener != null) {
                                    mOnChangeListener.onAddFilterEnd(mCurrentFilterId, position);
                                }

                            }
                        }
                        return true;
                    }
                });


    }

    public void addFilter(int id, int index) {
        mFilterMap.put(id, index);
        mCurrentFilterId = id;
    }

    public void clear(){
        mSeekbar.clearColor();
        if(mFilterMap != null) {
            mFilterMap.clear();
        }
        mCurrentFilterId = -1;
    }

    /**
     * 设置缩图和SeekBar最大的展示时间
     *
     * @param bitmaps 建议这个为14左右，不能太大也不能太小
     */
    public void setBitmapsOrMax(Bitmap[] bitmaps) {
        if (bitmaps == null || bitmaps.length < 3) return;

        mSeekbar.setBackBitmap(bitmaps);
    }

    /**
     * 进度更新，由外部使用者负责，view内部不负责播放进度的更新
     *
     * @param curPosition
     */
    public void setProgress(long curPosition) {
        int max = mSeekbar.getMax();
        float progress = (float) (curPosition * max / mEditDuration);
        mSeekbar.setProgress((int) progress);
        if (progress == max) {
            //已经到达添加的默认停止时间更新
            mImgTextAdapter.endLongClick();
        }
    }

    public void setOnEffectsChangeListener(OnEffectsChangeListener listener) {
        mOnChangeListener = listener;
    }

    private ColorFulSeekbar.OnSeekBarChangeListener mOnSeekBar =
            new ColorFulSeekbar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(ColorFulSeekbar seekBar, int progress, boolean fromUser) {
                }

                @Override
                public void onStartTrackingTouch(ColorFulSeekbar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(ColorFulSeekbar seekBar) {
                    int progress = seekBar.getProgress();
                    long position = progress * mEditDuration / seekBar.getMax();
                    if (mOnChangeListener != null) {
                        mOnChangeListener.onProgressChanged(position);
                    }
                }
            };

    public interface OnEffectsChangeListener {
        int onAddFilterStart(int type);

        void onUpdateFilter(int index);

        void onAddFilterEnd(int index, long position);

        void onDelete(int index);

        void onProgressChanged(long position);
    }
}
