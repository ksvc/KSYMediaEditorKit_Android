package com.ksyun.media.shortvideo.multicanvasdemo.view;

import com.ksyun.media.shortvideo.multicanvasdemo.data.MultiCanvasInfo;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Created by zanxiaofei on 17/12/14.
 */

public class CanvasViewBase extends FrameLayout {
    private View[] mRecordViews;
    private int mLastPreviewIndex = -1;
    private AddRecordClickListener mAddRecordClickListener;
    private MultiCanvasInfo[] mMultiCanvasInfo;

    public CanvasViewBase(@NonNull Context context, int id, MultiCanvasInfo[] modelPos) {
        super(context);
        mMultiCanvasInfo = modelPos;
        initView(context, id, modelPos);
    }

    @SuppressLint("ResourceType")
    private void initView(Context context, int id, final MultiCanvasInfo[] modelPos) {
        inflate(context, id, this);
        //model的layout布局必须以Frame为root，子控件的加载总是从左到右从上到下
        FrameLayout baseLayout = (FrameLayout) this.getChildAt(0);
        int count = baseLayout.getChildCount();
        mRecordViews = new View[count];
        for (int i = 0; i < mRecordViews.length; i++) {
            mRecordViews[i] = baseLayout.getChildAt(i);
            if (modelPos != null && modelPos[i] != null && modelPos[i].showAdd) {
                mRecordViews[i].setVisibility(VISIBLE);
                LayoutParams layoutParams = (LayoutParams) mRecordViews[i].getLayoutParams();
                layoutParams.leftMargin = modelPos[i].x_record_icon;
                layoutParams.topMargin = modelPos[i].y_record_icon;
                mRecordViews[i].setLayoutParams(layoutParams);
                mRecordViews[i].setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        int previewingIndex = -1;
                        for (int j = 0; j < mRecordViews.length; j++) {
                            if (view == mRecordViews[j]) {
                                if (mAddRecordClickListener != null) {
                                    int result = mAddRecordClickListener.onAddClicked(j + 1);
                                    if (result > 0) {
                                        //启动预览成功，隐藏添加预览标记
                                        previewingIndex = j;
                                        mRecordViews[j].setVisibility(INVISIBLE);
                                    } else {
                                        //已经开始录制，清除preview标记
                                        mLastPreviewIndex = -1;
                                    }
                                }
                            }
                        }
                        if(mLastPreviewIndex != -1 && mMultiCanvasInfo[mLastPreviewIndex].showAdd
                                && previewingIndex != mLastPreviewIndex) {
                            mRecordViews[mLastPreviewIndex].setVisibility(VISIBLE);
                        }
                        mLastPreviewIndex = previewingIndex;
                    }
                });
            } else {
                mRecordViews[i].setVisibility(INVISIBLE);
            }
        }
    }

    public void updateRecordView(MultiCanvasInfo[] modelPos) {
        for (int i = 0; i < mRecordViews.length; i++) {
            if (modelPos != null && modelPos[i] != null && modelPos[i].showAdd) {
                mRecordViews[i].setVisibility(VISIBLE);
            } else {
                mRecordViews[i].setVisibility(INVISIBLE);
            }
        }
    }

    public void setAddRecordClickListener(AddRecordClickListener listener) {
        mAddRecordClickListener = listener;
    }

    public interface AddRecordClickListener {
        int onAddClicked(int index);
    }
}
