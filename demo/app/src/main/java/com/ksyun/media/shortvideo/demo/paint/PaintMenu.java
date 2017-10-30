package com.ksyun.media.shortvideo.demo.paint;

import com.ksyun.media.shortvideo.demo.R;
import com.lht.paintview.PaintView;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.util.HashMap;
import java.util.Map;

/**
 * for paint menu
 */

public class PaintMenu {
    private RecyclerView mListView;
    private PaintColorAdapter mColorAdapter;
    private ImageView mCancel, mUndo, mComplete;
    private FrameLayout mPaintOne, mPaintTwo, mPaintThree;
    private OnPaintComplete mOnPaintComplete;
    private View mView;
    private int mCurrentSize = 5;
    private int mCurrentColor = Color.WHITE;
    private Map<Integer, View> mViews = new HashMap<>();
    private Context mContext;
    private PaintView mPainView;

    public PaintMenu(Context context, View baseView, PaintView paintView) {
        mContext = context;
        mView = baseView;
        mPainView = paintView;

        mListView = (RecyclerView) mView.findViewById(R.id.color_list);
        mCancel = (ImageView) mView.findViewById(R.id.cancel);
        mUndo = (ImageView) mView.findViewById(R.id.undo);
        mComplete = (ImageView) mView.findViewById(R.id.complete);
        mPaintOne = (FrameLayout) mView.findViewById(R.id.paint_one);
        mPaintTwo = (FrameLayout) mView.findViewById(R.id.paint_two);
        mPaintThree = (FrameLayout) mView.findViewById(R.id.paint_three);
        //画笔初始化
        initPaint();
        //当前画笔
        mCurrentSize = dipTopx(mContext, 5);

        mPainView.setBgColor(Color.TRANSPARENT);
        mPainView.setStrokeWidth(mCurrentSize);
        mPainView.setGestureEnable(false);
        mPainView.setColor(mCurrentColor);


        mCancel.setOnClickListener(onClickListener);
        mUndo.setOnClickListener(onClickListener);
        mComplete.setOnClickListener(onClickListener);
        mPaintOne.setOnClickListener(onClickListener);
        mPaintTwo.setOnClickListener(onClickListener);
        mPaintThree.setOnClickListener(onClickListener);
        if (mViews.get(mCurrentSize) != null) {
            mViews.get(mCurrentSize).setSelected(true);
        }

        LinearLayoutManager layoutManager = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
        mListView.setLayoutManager(layoutManager);
        mColorAdapter = new PaintColorAdapter(context);
        mColorAdapter.setPaintSelect(mPaintSelect);
        mColorAdapter.setSelectedPos(mCurrentColor);
        mListView.setAdapter(mColorAdapter);
        mListView.scrollToPosition(mColorAdapter.getSelectedPos());
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            int id = v.getId();
            switch (id) {
                case R.id.cancel:
                    mPainView.clear();
                    break;
                case R.id.undo:
                    mPainView.undo();
                    break;
                case R.id.complete:
                    if (mOnPaintComplete != null) {
                        mOnPaintComplete.completePaint();
                    }
                    break;
                case R.id.paint_one:
                    clearPaint();
                    mCurrentSize = dipTopx(mContext, 5);
                    mViews.get(mCurrentSize).setSelected(true);
                    mPainView.setStrokeWidth(mCurrentSize);
                    break;
                case R.id.paint_two:
                    clearPaint();
                    mCurrentSize = dipTopx(mContext, 10);
                    mViews.get(mCurrentSize).setSelected(true);
                    mPainView.setStrokeWidth(mCurrentSize);
                    break;
                case R.id.paint_three:
                    clearPaint();
                    mCurrentSize = dipTopx(mContext, 15);
                    mViews.get(mCurrentSize).setSelected(true);
                    mPainView.setStrokeWidth(mCurrentSize);
                    break;
            }
        }
    };


    private void clearPaint() {
        mPaintOne.setSelected(false);
        mPaintTwo.setSelected(false);
        mPaintThree.setSelected(false);
    }

    private PaintColorSelect mPaintSelect = new PaintColorSelect() {
        @Override
        public void onColorSelected(int color) {
            mCurrentColor = color;
            mPainView.setColor(mCurrentColor);

        }
    };

    public interface PaintColorSelect {
        void onColorSelected(int color);
    }

    public interface OnPaintComplete {
        void completePaint();
    }

    public void setOnPaintOpera(OnPaintComplete onPaintComplete) {
        this.mOnPaintComplete = onPaintComplete;
    }

    private void initPaint() {
        mViews.put(dipTopx(mContext, 5), mPaintOne);
        mViews.put(dipTopx(mContext, 10), mPaintTwo);
        mViews.put(dipTopx(mContext, 15), mPaintThree);
    }

    public int dipTopx(Context paramContext, float paramFloat) {
        return (int) (0.5F + paramFloat * paramContext.getResources().getDisplayMetrics().density);
    }
}
