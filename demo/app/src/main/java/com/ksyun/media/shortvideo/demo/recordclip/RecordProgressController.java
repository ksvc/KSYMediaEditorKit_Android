package com.ksyun.media.shortvideo.demo.recordclip;

import com.ksyun.media.shortvideo.demo.R;
import com.ksyun.media.shortvideo.demo.RecordActivity;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * record progress controller
 */

public class RecordProgressController {
    private static final String TAG = "RecordProgressController";
    private Handler mHandler;
    private RecordProgressView mProgressView;
    private ChangeProgressRunnable mProgressRunnable;
    private RecordProgressTimer mProgressTimer;
    private long mStartRecordingTime;
    private boolean mIsRecording;
    private Chronometer mCmt;
    private LinkedList<RecordClipModel> mProgressClipList;

    private List<RecordingStateChanged> mRecordStateChangedListeners;
    private RecordingLengthChangedListener mRecordingLengthChangedListener;

    /**
     * @param view 拍摄进度显示的控件
     */
    public RecordProgressController(RecordProgressView view, Chronometer cmt) {
        mHandler = new Handler();
        mProgressView = view;
        mCmt = cmt;
        mProgressRunnable = new ChangeProgressRunnable();
        mRecordStateChangedListeners = new ArrayList<>();
        mProgressTimer = new RecordProgressTimer();
        mProgressTimer.setProgressUpdateListener(mProgressUpdateListener);
        mRecordStateChangedListeners.add(mProgressTimer);

        mStartRecordingTime = 0;
        mIsRecording = false;
        mProgressClipList = new LinkedList<>();

        mProgressView.setProgressClipList(mProgressClipList);
        mRecordStateChangedListeners.add(mProgressView);
    }

    private class ChangeProgressRunnable implements Runnable {
        @Override
        public void run() {
            if (getTotalRecordTime() >= RecordActivity.MAX_DURATION) {
                mProgressView.invalidate();
                if (mIsRecording && mRecordingLengthChangedListener != null) {
                    mRecordingLengthChangedListener.passMaxPoint();
                }
                mIsRecording = false;
            }
            mRecordingLengthChangedListener.passMinPoint(isPassMinPoint());
            mProgressView.invalidate();
        }
    }

    public int getChronometerTime() {
        int curTime = 0;
        if (mCmt != null) {
            String str = mCmt.getText().toString();
            String[] split = str.split(":");
            if (str.length() == 5) {
                curTime = Integer.parseInt(split[0]) * 60
                        + Integer.parseInt(split[1]);
            } else if (str.length() == 7) {
                curTime = Integer.parseInt(split[0]) * 60 * 60
                        + Integer.parseInt(split[1]) * 60
                        + Integer.parseInt(split[2]);
            }
        }
        return curTime * 1000;
    }

    private long getTotalRecordTime() {
        long time = 0;
        for (RecordClipModel clip : mProgressClipList) {
            time += clip.timeInterval;
        }
        return time;
    }

    /**
     * 是否到达了最小录制时长
     *
     * @return
     */
    public boolean isPassMinPoint() {
        long recordedTime = 0;
        for (RecordClipModel clip : mProgressClipList) {
            recordedTime += clip.timeInterval;
        }
        return recordedTime >= RecordActivity.MIN_DURATION;
    }

    /**
     * 是否到达了最大录制时长
     *
     * @return
     */
    public boolean isPassMaxPoint() {
        return mProgressView.isPassMaxPoint();
    }

    /**
     * 进入录制页面Timer即可启动，用于随时更新录制的进度
     */
    public void start() {
        mProgressTimer.start();
    }

    public void stop() {
        mProgressTimer.stop();
    }

    /**
     * startRecord
     */
    public void startRecording() {
        if (mIsRecording) return;
        mStartRecordingTime = System.currentTimeMillis();
        mIsRecording = true;
        RecordClipModel clip = new RecordClipModel();
        clip.timeInterval = 0;
        clip.state = 0;
        mProgressClipList.add(clip);

        for (RecordingStateChanged listener : mRecordStateChangedListeners) {
            listener.recordingStart(mStartRecordingTime);
        }
    }

    public void release() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }

        mProgressTimer.stop();
        mProgressTimer.setProgressUpdateListener(null);
        mRecordStateChangedListeners.clear();
        mProgressClipList.clear();
        mProgressView.release();
    }

    /**
     * stop record
     */
    public void stopRecording() {
        mIsRecording = false;
        if (!mProgressClipList.isEmpty()) {
            mProgressClipList.getLast().state = 1;
            mProgressClipList.getLast().timeInterval += 20;
            mHandler.post(mProgressRunnable);
        }

        for (RecordingStateChanged listener : mRecordStateChangedListeners) {
            listener.recordingStop();
        }
    }

    /**
     * 删除最后一个录制的视频
     */
    public void rollback() {
        mIsRecording = false;
        if (!mProgressClipList.isEmpty()) {
            mProgressClipList.removeLast();
            mHandler.post(mProgressRunnable);
        }
    }

    /**
     * 设置最后一个file为待删除文件
     */
    public void setLastClipPending() {
        if (!mProgressClipList.isEmpty()) {
            mProgressClipList.getLast().state = 2;
            mHandler.post(mProgressRunnable);
        }
    }

    /**
     * 设置最后一个file为普通文件
     */
    public void setLastClipNormal() {
        if (!mProgressClipList.isEmpty()) {
            mProgressClipList.getLast().state = 1;
            mHandler.post(mProgressRunnable);
        }
    }

    public int getClipListSize() {
        return mProgressClipList.size();
    }

    /**
     * 只是预估时间，实际录制时长已视频为准
     *
     * @return
     */
    public int getRecordedTime() {
        return mProgressView.mTotalWidth * RecordActivity.MAX_DURATION / mProgressView
                .mScreenWidth;
    }

    public boolean getIsRecording() {
        return mIsRecording;
    }

    public long getStartRecordingTime() {
        return mStartRecordingTime;
    }

    /**
     * 用于通知最短拍摄时长和最长拍摄时长
     *
     * @param listener
     */
    public void setRecordingLengthChangedListener(RecordingLengthChangedListener listener) {
        mRecordingLengthChangedListener = listener;
    }

    private RecordProgressTimer.ProgressUpdateListener mProgressUpdateListener = new RecordProgressTimer.ProgressUpdateListener() {
        @Override
        public void updateProgress(long interval) {
            if (!mProgressClipList.isEmpty()) {
                RecordClipModel clip = mProgressClipList.getLast();
                clip.timeInterval = interval;
                mHandler.post(mProgressRunnable);
            }
        }
    };

    public interface RecordingStateChanged {
        void recordingStart(long startTime);

        void recordingStop();
    }

    public interface RecordingLengthChangedListener {
        void passMinPoint(boolean pass);  //拍摄超过了最短时长

        void passMaxPoint();  //拍摄超过了最长时长
    }
}
