package com.ksyun.media.shortvideo.demo;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.shortvideo.demo.filter.DemoFilter;
import com.ksyun.media.shortvideo.demo.filter.DemoFilter2;
import com.ksyun.media.shortvideo.demo.filter.DemoFilter3;
import com.ksyun.media.shortvideo.demo.filter.DemoFilter4;
import com.ksyun.media.shortvideo.demo.recordclip.RecordProgressController;
import com.ksyun.media.shortvideo.demo.filter.ImgFaceunityFilter;
import com.ksyun.media.shortvideo.kit.KSYRecordKit;
import com.ksyun.media.streamer.capture.CameraCapture;
import com.ksyun.media.streamer.capture.camera.CameraTouchHelper;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautyProFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautyToneCurveFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgFilterBase;
import com.ksyun.media.streamer.filter.imgtex.ImgTexFilterMgt;
import com.ksyun.media.streamer.kit.KSYStreamer;
import com.ksyun.media.streamer.kit.StreamerConstants;
import com.ksyun.media.streamer.logstats.StatsLogReport;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Chronometer;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Record ShortVideo
 */

public class RecordActivity extends Activity implements
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static String TAG = "RecordActivity";

    public static final int MAX_DURATION = 1 * 60 * 1000;
    public static final int MIN_DURATION = 5 * 1000;

    private GLSurfaceView mCameraPreviewView;
    //private TextureView mCameraPreviewView;
    private CameraHintView mCameraHintView;
    private Chronometer mChronometer;
    private View mSwitchCameraView;
    private View mFlashView;
    private RelativeLayout mPreviewLayout;
    private RelativeLayout mBarBottomLayout;
    private ImageView mRecordView;
    private ImageView mBackView;
    private ImageView mNextView;
    private CheckBox mFrontMirrorCheckBox;
    private CheckBox mMicAudioView;
    private CheckBox mBgmMusicView;
    private CheckBox mBeautyCheckBox;
    private CheckBox mWaterMarkCheckBox;
    private CheckBox mStickerCheckBox;
    private AppCompatSeekBar mMicAudioVolumeSeekBar;
    private AppCompatSeekBar mBgmVolumeSeekBar;

    private View mBeautyChooseView;
    private AppCompatSpinner mBeautySpinner;
    private LinearLayout mBeautyGrindLayout;
    private TextView mGrindText;
    private AppCompatSeekBar mGrindSeekBar;
    private LinearLayout mBeautyWhitenLayout;
    private TextView mWhitenText;
    private AppCompatSeekBar mWhitenSeekBar;
    private LinearLayout mBeautyRuddyLayout;
    private TextView mRuddyText;
    private AppCompatSeekBar mRuddySeekBar;

    //record progress
    private RecordProgressController mRecordProgressCtl;

    private View mStickerChooseview;
    private AppCompatSpinner mStickerSpinner;
    private ImgFaceunityFilter mImgFaceunityFilter;

    private ButtonObserver mObserverButton;
    private RecordActivity.CheckBoxObserver mCheckBoxObserver;
    private SeekBarChangedObserver mSeekBarChangedObsesrver;

    private KSYRecordKit mKSYRecordKit;
    private Handler mMainHandler;

    private boolean mIsFileRecording = false;
    private boolean mIsFlashOpened = false;
    private String mRecordUrl;

    private String mLogoPath = "assets://KSYLogo/logo.png";

    private boolean mHWEncoderUnsupported;
    private boolean mSWEncoderUnsupported;
    private String mBgmPath = "/sdcard/test.mp3";
    private final static int PERMISSION_REQUEST_CAMERA_AUDIOREC = 1;

    public final static String FRAME_RATE = "framerate";
    public final static String VIDEO_BITRATE = "video_bitrate";
    public final static String AUDIO_BITRATE = "audio_bitrate";
    public final static String VIDEO_RESOLUTION = "video_resolution";
    public final static String ENCODE_TYPE = "encode_type";
    public final static String ENCODE_METHOD = "encode_method";
    public final static String ENCODE_PROFILE = "encode_profile";

    public static void startActivity(Context context, int frameRate,
                                     int videoBitrate, int audioBitrate,
                                     int videoResolution,
                                     int encodeType, int encodeMethod, int encodeProfile) {
        Intent intent = new Intent(context, RecordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.putExtra(FRAME_RATE, frameRate);
        intent.putExtra(VIDEO_BITRATE, videoBitrate);
        intent.putExtra(AUDIO_BITRATE, audioBitrate);
        intent.putExtra(VIDEO_RESOLUTION, videoResolution);
        intent.putExtra(ENCODE_TYPE, encodeType);
        intent.putExtra(ENCODE_METHOD, encodeMethod);
        intent.putExtra(ENCODE_PROFILE, encodeProfile);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.record_acitvity);

        //must set
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //默认设置为竖屏，当前暂时只支持竖屏，后期完善
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //init UI
        //录制预览部分宽高1:1比例显示（用户可以按照自己的需求处理）
        //just PORTRAIT
        WindowManager windowManager = (WindowManager) getApplication().
                getSystemService(getApplication().WINDOW_SERVICE);

        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();

        mObserverButton = new ButtonObserver();
        mCheckBoxObserver = new CheckBoxObserver();
        mSeekBarChangedObsesrver = new SeekBarChangedObserver();
        mSwitchCameraView = findViewById(R.id.switch_cam);
        mSwitchCameraView.setOnClickListener(mObserverButton);
        mCameraHintView = (CameraHintView) findViewById(R.id.camera_hint);
        mFlashView = findViewById(R.id.flash);
        mFlashView.setOnClickListener(mObserverButton);
        mPreviewLayout = (RelativeLayout) findViewById(R.id.preview_layout);
        mBarBottomLayout = (RelativeLayout) findViewById(R.id.bar_bottom);
        mCameraPreviewView = (GLSurfaceView) findViewById(R.id.camera_preview);
        mFrontMirrorCheckBox = (CheckBox) findViewById(R.id.record_front_mirror);
        mFrontMirrorCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mBgmMusicView = (CheckBox) findViewById(R.id.record_bgm);
        mBgmMusicView.setOnCheckedChangeListener(mCheckBoxObserver);
        mWaterMarkCheckBox = (CheckBox) findViewById(R.id.record_watermark);
        mWaterMarkCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mMicAudioView = (CheckBox) findViewById(R.id.record_mic_audio);
        mMicAudioView.setOnCheckedChangeListener(mCheckBoxObserver);
        mMicAudioVolumeSeekBar = (AppCompatSeekBar) findViewById(R.id.record_mic_audio_volume);
        mMicAudioVolumeSeekBar.setOnSeekBarChangeListener(mSeekBarChangedObsesrver);
        mBgmVolumeSeekBar = (AppCompatSeekBar) findViewById(R.id.record_music_audio_volume);
        mBgmVolumeSeekBar.setOnSeekBarChangeListener(mSeekBarChangedObsesrver);
        if (!mBgmMusicView.isChecked()) {
            mBgmVolumeSeekBar.setEnabled(false);
        }
        mBeautyCheckBox = (CheckBox) findViewById(R.id.record_beauty);
        mBeautyCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mStickerCheckBox = (CheckBox) findViewById(R.id.record_dynamic_sticker);
        mStickerCheckBox.setOnCheckedChangeListener(mCheckBoxObserver);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mRecordView = (ImageView) findViewById(R.id.click_to_record);
        mRecordView.getDrawable().setLevel(1);
        mRecordView.setOnClickListener(mObserverButton);
        mBackView = (ImageView) findViewById(R.id.click_to_back);
        mBackView.setOnClickListener(mObserverButton);
        mNextView = (ImageView) findViewById(R.id.click_to_next);
        mNextView.setOnClickListener(mObserverButton);

        int width = screenWidth;
        int height = screenWidth;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(screenWidth,
                height);
        mPreviewLayout.setLayoutParams(params);

        params = new LinearLayout.LayoutParams(screenWidth,
                screenHeight - height);
        mBarBottomLayout.setLayoutParams(params);

        mBeautyChooseView = findViewById(R.id.record_beauty_choose);
        mBeautySpinner = (AppCompatSpinner) findViewById(R.id.beauty_spin);
        mBeautyGrindLayout = (LinearLayout) findViewById(R.id.beauty_grind);
        mGrindText = (TextView) findViewById(R.id.grind_text);
        mGrindSeekBar = (AppCompatSeekBar) findViewById(R.id.grind_seek_bar);
        mBeautyWhitenLayout = (LinearLayout) findViewById(R.id.beauty_whiten);
        mWhitenText = (TextView) findViewById(R.id.whiten_text);
        mWhitenSeekBar = (AppCompatSeekBar) findViewById(R.id.whiten_seek_bar);
        mBeautyRuddyLayout = (LinearLayout) findViewById(R.id.beauty_ruddy);
        mRuddyText = (TextView) findViewById(R.id.ruddy_text);
        mRuddySeekBar = (AppCompatSeekBar) findViewById(R.id.ruddy_seek_bar);
        initBeautyUI();

        mStickerChooseview = findViewById(R.id.record_sticker_choose);
        mStickerSpinner = (AppCompatSpinner) findViewById(R.id.sticker_spin);
        initStickerUI();

        mRecordProgressCtl = new RecordProgressController(mBarBottomLayout);
        mRecordProgressCtl.setRecordingLengthChangedListener(mRecordLengthChangedListener);
        mRecordProgressCtl.start();
        mBackView.getDrawable().setLevel(1);
        mBackView.setSelected(false);

        //init
        mMainHandler = new Handler();
        mKSYRecordKit = new KSYRecordKit(this);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            int frameRate = bundle.getInt(FRAME_RATE, 0);
            if (frameRate > 0) {
                mKSYRecordKit.setPreviewFps(frameRate);
                mKSYRecordKit.setTargetFps(frameRate);
            }

            int videoBitrate = bundle.getInt(VIDEO_BITRATE, 0);
            if (videoBitrate > 0) {
                mKSYRecordKit.setVideoKBitrate(videoBitrate);
            }

            int audioBitrate = bundle.getInt(AUDIO_BITRATE, 0);
            if (audioBitrate > 0) {
                mKSYRecordKit.setAudioKBitrate(audioBitrate);
            }

            int videoResolution = bundle.getInt(VIDEO_RESOLUTION, 0);
            mKSYRecordKit.setPreviewResolution(videoResolution);
            mKSYRecordKit.setTargetResolution(videoResolution);

            int encode_type = bundle.getInt(ENCODE_TYPE);
            mKSYRecordKit.setVideoCodecId(encode_type);

            int encode_method = bundle.getInt(ENCODE_METHOD);
            mKSYRecordKit.setEncodeMethod(encode_method);

            int encodeProfile = bundle.getInt(ENCODE_PROFILE);
            mKSYRecordKit.setVideoEncodeProfile(encodeProfile);

            mKSYRecordKit.setRotateDegrees(0);
        }
        mKSYRecordKit.setDisplayPreview(mCameraPreviewView);

        mKSYRecordKit.setEnableRepeatLastFrame(false);
        mKSYRecordKit.setCameraFacing(CameraCapture.FACING_FRONT);
        mKSYRecordKit.setFrontCameraMirror(mFrontMirrorCheckBox.isChecked());
        mKSYRecordKit.setOnInfoListener(mOnInfoListener);
        mKSYRecordKit.setOnErrorListener(mOnErrorListener);
        mKSYRecordKit.setOnLogEventListener(mOnLogEventListener);

        // touch focus and zoom support
        CameraTouchHelper cameraTouchHelper = new CameraTouchHelper();
        cameraTouchHelper.setCameraCapture(mKSYRecordKit.getCameraCapture());
        mCameraPreviewView.setOnTouchListener(cameraTouchHelper);
        // set CameraHintView to show focus rect and zoom ratio
        cameraTouchHelper.setCameraHintView(mCameraHintView);

        startCameraPreviewWithPermCheck();

        if (!mMicAudioView.isChecked()) {
            mMicAudioVolumeSeekBar.setEnabled(false);
        } else {
            mMicAudioVolumeSeekBar.setProgress((int) mKSYRecordKit.getVoiceVolume() * 100);
        }
    }

    private int align(int val, int align) {
        return (val + align - 1) / align * align;
    }

    @Override
    public void onResume() {
        super.onResume();

        mKSYRecordKit.setDisplayPreview(mCameraPreviewView);
        mKSYRecordKit.onResume();
        mCameraHintView.hideAll();

        // camera may be occupied by other app in background
        startCameraPreviewWithPermCheck();
    }

    @Override
    public void onPause() {
        super.onPause();
        mKSYRecordKit.onPause();
        if (!mKSYRecordKit.isRecording() && !mKSYRecordKit.isFileRecording()) {
            mKSYRecordKit.stopCameraPreview();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }

        mRecordProgressCtl.stop();
        mRecordProgressCtl.setRecordingLengthChangedListener(null);
        mRecordProgressCtl.release();
        mKSYRecordKit.setOnLogEventListener(null);
        mKSYRecordKit.release();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                onBackoffClick();
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    //start recording to a local file
    private void startRecord() {
        String fileFolder = "/sdcard/ksy_sv_rec_test";
        File file = new File(fileFolder);
        if (!file.exists()) {
            file.mkdir();
        }
        mRecordUrl = fileFolder + "/" + System.currentTimeMillis() + ".mp4";
        Log.d(TAG, "record url:" + mRecordUrl);
        float val = mMicAudioVolumeSeekBar.getProgress() / 100.f;
        mKSYRecordKit.setVoiceVolume(val);
        mKSYRecordKit.startRecord(mRecordUrl);
        mIsFileRecording = true;
        mRecordView.getDrawable().setLevel(2);
    }

    private void stopRecord(boolean finished) {
        //录制完成进入编辑
        //若录制文件大于1则需要触发文件合成
        if (finished) {
            if (mKSYRecordKit.getRecordedFilesCount() > 1) {
                String fileFolder = getRecordFileFolder();
                //合成文件路径
                final String outFile = fileFolder + "/" + "merger_" + System.currentTimeMillis() + ".mp4";
                //合成过程为异步，需要block下一步处理
                final MegerFilesAlertDialog dialog = new MegerFilesAlertDialog(this, R.style.dialog);
                dialog.setCancelable(false);
                dialog.show();
                mKSYRecordKit.stopRecord(outFile, new KSYRecordKit.MegerFilesFinishedListener() {
                    @Override
                    public void onFinished() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dialog.dismiss();
                                mRecordUrl = outFile;
                                EditActivity.startActivity(getApplicationContext(), mRecordUrl);
                            }
                        });
                    }
                });
            } else {
                mKSYRecordKit.stopRecord();
                EditActivity.startActivity(getApplicationContext(), mRecordUrl);
            }

        } else {
            //普通录制停止
            mKSYRecordKit.stopRecord();
        }
        //更新进度显示
        mRecordProgressCtl.stopRecording();
        mRecordView.getDrawable().setLevel(1);
        updateDeleteView();

        mIsFileRecording = false;
        stopChronometer();
    }

    private void stopChronometer() {
        if (mIsFileRecording) {
            return;
        }

        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.stop();
    }

    // Example to handle camera related operation
    private void setCameraAntiBanding50Hz() {
        Camera.Parameters parameters = mKSYRecordKit.getCameraCapture().getCameraParameters();
        if (parameters != null) {
            parameters.setAntibanding(Camera.Parameters.ANTIBANDING_50HZ);
            mKSYRecordKit.getCameraCapture().setCameraParameters(parameters);
        }
    }

    private KSYStreamer.OnInfoListener mOnInfoListener = new KSYStreamer.OnInfoListener() {
        @Override
        public void onInfo(int what, int msg1, int msg2) {
            switch (what) {
                case StreamerConstants.KSY_STREAMER_CAMERA_INIT_DONE:
                    Log.d(TAG, "KSY_STREAMER_CAMERA_INIT_DONE");
                    setCameraAntiBanding50Hz();
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_FACEING_CHANGED:
                    updateFaceunitParams();
                    break;
                case StreamerConstants.KSY_STREAMER_OPEN_FILE_SUCCESS:
                    Log.d(TAG, "KSY_STREAMER_OPEN_FILE_SUCCESS");
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.start();
                    mRecordProgressCtl.startRecording();
                    break;
                default:
                    Log.d(TAG, "OnInfo: " + what + " msg1: " + msg1 + " msg2: " + msg2);
                    break;
            }
        }
    };

    private void handleEncodeError() {
        int encodeMethod = mKSYRecordKit.getVideoEncodeMethod();
        if (encodeMethod == StreamerConstants.ENCODE_METHOD_HARDWARE) {
            mHWEncoderUnsupported = true;
            if (mSWEncoderUnsupported) {
                mKSYRecordKit.setEncodeMethod(
                        StreamerConstants.ENCODE_METHOD_SOFTWARE_COMPAT);
                Log.e(TAG, "Got HW encoder error, switch to SOFTWARE_COMPAT mode");
            } else {
                mKSYRecordKit.setEncodeMethod(StreamerConstants.ENCODE_METHOD_SOFTWARE);
                Log.e(TAG, "Got HW encoder error, switch to SOFTWARE mode");
            }
        } else if (encodeMethod == StreamerConstants.ENCODE_METHOD_SOFTWARE) {
            mSWEncoderUnsupported = true;
            if (mHWEncoderUnsupported) {
                mKSYRecordKit.setEncodeMethod(
                        StreamerConstants.ENCODE_METHOD_SOFTWARE_COMPAT);
                Log.e(TAG, "Got SW encoder error, switch to SOFTWARE_COMPAT mode");
            } else {
                mKSYRecordKit.setEncodeMethod(StreamerConstants.ENCODE_METHOD_HARDWARE);
                Log.e(TAG, "Got SW encoder error, switch to HARDWARE mode");
            }
        }
    }

    private KSYStreamer.OnErrorListener mOnErrorListener = new KSYStreamer.OnErrorListener() {
        @Override
        public void onError(int what, int msg1, int msg2) {
            switch (what) {
                case StreamerConstants.KSY_STREAMER_ERROR_AV_ASYNC:
                    Log.d(TAG, "KSY_STREAMER_ERROR_AV_ASYNC " + msg1 + "ms");
                    break;
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED:
                    Log.d(TAG, "KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED");
                    break;
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN:
                    Log.d(TAG, "KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN");
                    break;
                case StreamerConstants.KSY_STREAMER_AUDIO_ENCODER_ERROR_UNSUPPORTED:
                    Log.d(TAG, "KSY_STREAMER_AUDIO_ENCODER_ERROR_UNSUPPORTED");
                    break;
                case StreamerConstants.KSY_STREAMER_AUDIO_ENCODER_ERROR_UNKNOWN:
                    Log.d(TAG, "KSY_STREAMER_AUDIO_ENCODER_ERROR_UNKNOWN");
                    break;
                case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED:
                    Log.d(TAG, "KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED");
                    break;
                case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN:
                    Log.d(TAG, "KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN");
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_UNKNOWN:
                    Log.d(TAG, "KSY_STREAMER_CAMERA_ERROR_UNKNOWN");
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_START_FAILED:
                    Log.d(TAG, "KSY_STREAMER_CAMERA_ERROR_START_FAILED");
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_SERVER_DIED:
                    Log.d(TAG, "KSY_STREAMER_CAMERA_ERROR_SERVER_DIED");
                    break;
                //Camera was disconnected due to use by higher priority user.
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_EVICTED:
                    Log.d(TAG, "KSY_STREAMER_CAMERA_ERROR_EVICTED");
                    break;
                default:
                    Log.d(TAG, "what=" + what + " msg1=" + msg1 + " msg2=" + msg2);
                    break;
            }
            switch (what) {
                case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_START_FAILED:
                case StreamerConstants.KSY_STREAMER_AUDIO_RECORDER_ERROR_UNKNOWN:
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_UNKNOWN:
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_START_FAILED:
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_EVICTED:
                case StreamerConstants.KSY_STREAMER_CAMERA_ERROR_SERVER_DIED:
                    mKSYRecordKit.stopCameraPreview();
                    break;
                case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_CLOSE_FAILED:
                case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_ERROR_UNKNOWN:
                case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_OPEN_FAILED:
                case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_FORMAT_NOT_SUPPORTED:
                case StreamerConstants.KSY_STREAMER_FILE_PUBLISHER_WRITE_FAILED:
                    stopRecord(false);
                    rollBackClipForError();
                    break;
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED:
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN: {
                    handleEncodeError();
                    stopRecord(false);
                    rollBackClipForError();
                    mMainHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startRecord();
                        }
                    }, 3000);
                }
                break;
                default:
                    break;
            }
        }
    };

    private StatsLogReport.OnLogEventListener mOnLogEventListener =
            new StatsLogReport.OnLogEventListener() {
                @Override
                public void onLogEvent(StringBuilder singleLogContent) {
                    Log.i(TAG, "***onLogEvent : " + singleLogContent.toString());
                }
            };

    private void onSwitchCamera() {
        mKSYRecordKit.switchCamera();
    }

    private void onFlashClick() {
        if (mIsFlashOpened) {
            mKSYRecordKit.toggleTorch(false);
            mIsFlashOpened = false;
        } else {
            mKSYRecordKit.toggleTorch(true);
            mIsFlashOpened = true;
        }
    }

    private void onBackoffClick() {
        if (mKSYRecordKit.getRecordedFilesCount() >= 1) {
            if (!mBackView.isSelected()) {
                mBackView.setSelected(true);

                mRecordProgressCtl.setLastClipPending();
            } else {
                mBackView.setSelected(false);
                if (mIsFileRecording) {
                    stopRecord(false);
                }
                //删除录制文件
                mKSYRecordKit.deleteRecordFile(mKSYRecordKit.getLastRecordedFiles());
                mRecordProgressCtl.rollback();
                updateDeleteView();
                mRecordView.setEnabled(true);
            }
        } else {
            mChronometer.stop();
            mIsFileRecording = false;
            RecordActivity.this.finish();
        }
    }

    private void onRecordClick() {
        if (mIsFileRecording) {
            stopRecord(false);
        } else {
            startRecord();
        }
        clearBackoff();
    }

    private void onNextClick() {
        clearBackoff();
        mRecordView.getDrawable().setLevel(1);
        stopRecord(true);
        if (mBgmMusicView.isChecked()) {
            mBgmMusicView.setChecked(false);
        }
    }

    private void initBeautyUI() {
        String[] items = new String[]{"DISABLE", "BEAUTY_SOFT", "SKIN_WHITEN", "BEAUTY_ILLUSION",
                "BEAUTY_DENOISE", "BEAUTY_SMOOTH", "BEAUTY_PRO", "DEMO_FILTER", "GROUP_FILTER",
                "ToneCurve", "复古", "胶片"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBeautySpinner.setAdapter(adapter);
        mBeautySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = ((TextView) parent.getChildAt(0));
                if (textView != null) {
                    textView.setTextColor(getResources().getColor(R.color.font_color_35));
                }
                if (position == 0) {
                    mKSYRecordKit.getImgTexFilterMgt().setFilter((ImgFilterBase) null);
                } else if (position <= 5) {
                    mKSYRecordKit.getImgTexFilterMgt().setFilter(
                            mKSYRecordKit.getGLRender(), position + 15);
                } else if (position == 6) {
                    mKSYRecordKit.getImgTexFilterMgt().setFilter(mKSYRecordKit.getGLRender(),
                            ImgTexFilterMgt.KSY_FILTER_BEAUTY_PRO);
                } else if (position == 7) {
                    mKSYRecordKit.getImgTexFilterMgt().setFilter(
                            new DemoFilter(mKSYRecordKit.getGLRender()));
                } else if (position == 8) {
                    List<ImgFilterBase> groupFilter = new LinkedList<>();
                    groupFilter.add(new DemoFilter2(mKSYRecordKit.getGLRender()));
                    groupFilter.add(new DemoFilter3(mKSYRecordKit.getGLRender()));
                    groupFilter.add(new DemoFilter4(mKSYRecordKit.getGLRender()));
                    mKSYRecordKit.getImgTexFilterMgt().setFilter(groupFilter);
                } else if (position == 9) {
                    ImgBeautyToneCurveFilter acvFilter = new ImgBeautyToneCurveFilter(mKSYRecordKit.getGLRender());
                    acvFilter.setFromCurveFileInputStream(
                            RecordActivity.this.getResources().openRawResource(R.raw.tone_cuver_sample));

                    mKSYRecordKit.getImgTexFilterMgt().setFilter(acvFilter);
                } else if (position == 10) {
                    ImgBeautyToneCurveFilter acvFilter = new ImgBeautyToneCurveFilter(mKSYRecordKit
                            .getGLRender());
                    acvFilter.setFromCurveFileInputStream(
                            RecordActivity.this.getResources().openRawResource(R.raw.fugu));

                    mKSYRecordKit.getImgTexFilterMgt().setFilter(acvFilter);
                } else if (position == 11) {
                    ImgBeautyToneCurveFilter acvFilter = new ImgBeautyToneCurveFilter(mKSYRecordKit
                            .getGLRender());
                    acvFilter.setFromCurveFileInputStream(
                            RecordActivity.this.getResources().openRawResource(R.raw.jiaopian));

                    mKSYRecordKit.getImgTexFilterMgt().setFilter(acvFilter);
                }
                List<ImgFilterBase> filters = mKSYRecordKit.getImgTexFilterMgt().getFilter();
                if (filters != null && !filters.isEmpty()) {
                    final ImgFilterBase filter = filters.get(0);
                    mBeautyGrindLayout.setVisibility(filter.isGrindRatioSupported() ?
                            View.VISIBLE : View.GONE);
                    mBeautyWhitenLayout.setVisibility(filter.isWhitenRatioSupported() ?
                            View.VISIBLE : View.GONE);
                    mBeautyRuddyLayout.setVisibility(filter.isRuddyRatioSupported() ?
                            View.VISIBLE : View.GONE);
                    SeekBar.OnSeekBarChangeListener seekBarChangeListener =
                            new SeekBar.OnSeekBarChangeListener() {
                                @Override
                                public void onProgressChanged(SeekBar seekBar, int progress,
                                                              boolean fromUser) {
                                    if (!fromUser) {
                                        return;
                                    }
                                    float val = progress / 100.f;
                                    if (seekBar == mGrindSeekBar) {
                                        filter.setGrindRatio(val);
                                    } else if (seekBar == mWhitenSeekBar) {
                                        filter.setWhitenRatio(val);
                                    } else if (seekBar == mRuddySeekBar) {
                                        if (filter instanceof ImgBeautyProFilter) {
                                            val = progress / 50.f - 1.0f;
                                        }
                                        filter.setRuddyRatio(val);
                                    }
                                }

                                @Override
                                public void onStartTrackingTouch(SeekBar seekBar) {
                                }

                                @Override
                                public void onStopTrackingTouch(SeekBar seekBar) {
                                }
                            };
                    mGrindSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
                    mWhitenSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
                    mRuddySeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
                    mGrindSeekBar.setProgress((int) (filter.getGrindRatio() * 100));
                    mWhitenSeekBar.setProgress((int) (filter.getWhitenRatio() * 100));
                    int ruddyVal = (int) (filter.getRuddyRatio() * 100);
                    if (filter instanceof ImgBeautyProFilter) {
                        ruddyVal = (int) (filter.getRuddyRatio() * 50 + 50);
                    }
                    mRuddySeekBar.setProgress(ruddyVal);
                } else {
                    mBeautyGrindLayout.setVisibility(View.GONE);
                    mBeautyWhitenLayout.setVisibility(View.GONE);
                    mBeautyRuddyLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
        mBeautySpinner.setPopupBackgroundResource(R.color.transparent1);
        mBeautySpinner.setSelection(4);
    }

    private void onStickerChecked(boolean isChecked) {
        if (isChecked) {
            initFaceunity();
            mKSYRecordKit.getCameraCapture().mImgBufSrcPin.connect(mImgFaceunityFilter.getBufSinkPin());
            mStickerChooseview.setVisibility(View.VISIBLE);
        } else {
            mStickerChooseview.setVisibility(View.INVISIBLE);
            if (mImgFaceunityFilter != null) {
                mKSYRecordKit.getCameraCapture().mImgBufSrcPin.disconnect(mImgFaceunityFilter.getBufSinkPin(),
                        false);
                mImgFaceunityFilter.setPropType(-1);
            }
        }
    }

    private void initStickerUI() {
        String[] items = new String[]{"DISABLE", "BEAGLEDOG", "COLORCROWN", "DEER",
                "HAPPYRABBI", "HARTSHORN", "ITEM0204", "ITEM0208",
                "ITEM0210", "ITEM0501", "MOOD", "PRINCESSCROWN", "TIARA", "YELLOWEAR"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mStickerSpinner.setAdapter(adapter);
        mStickerSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                TextView textView = ((TextView) parent.getChildAt(0));
                if (textView != null) {
                    textView.setTextColor(getResources().getColor(R.color.font_color_35));
                }
                if (position == 0) {
                    //disable
                    if (mImgFaceunityFilter != null) {
                        mImgFaceunityFilter.setPropType(-1);
                    }
                } else {
                    mImgFaceunityFilter.setPropType(position - 1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });
        mStickerSpinner.setPopupBackgroundResource(R.color.transparent1);
        mStickerSpinner.setSelection(0);
    }

    private void initFaceunity() {
        if (mImgFaceunityFilter == null) {
            //add faceunity filter
            mImgFaceunityFilter = new ImgFaceunityFilter(this, mKSYRecordKit.getGLRender());
            mKSYRecordKit.getImgTexFilterMgt().setExtraFilter(mImgFaceunityFilter);
        }

        updateFaceunitParams();
    }

    private void updateFaceunitParams() {
        if (mImgFaceunityFilter != null) {
            mImgFaceunityFilter.setTargetSize(mKSYRecordKit.getTargetWidth(),
                    mKSYRecordKit.getTargetHeight());

            if (mKSYRecordKit.isFrontCamera()) {
                mImgFaceunityFilter.setMirror(true);
            } else {
                mImgFaceunityFilter.setMirror(false);
            }
        }
    }

    private class ButtonObserver implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.switch_cam:
                    onSwitchCamera();
                    break;
                case R.id.flash:
                    onFlashClick();
                    break;
                case R.id.click_to_record:
                    onRecordClick();
                    break;
                case R.id.click_to_back:
                    onBackoffClick();
                    break;
                case R.id.click_to_next:
                    onNextClick();
                    break;
                default:
                    break;
            }
        }
    }

    private void onFrontMirrorChecked(boolean isChecked) {
        mKSYRecordKit.setFrontCameraMirror(isChecked);
    }

    private void onBeautyChecked(boolean isChecked) {
        if (mKSYRecordKit.getVideoEncodeMethod() == StreamerConstants.ENCODE_METHOD_SOFTWARE_COMPAT) {
            mKSYRecordKit.getImgTexFilterMgt().setFilter(mKSYRecordKit.getGLRender(), isChecked ?
                    ImgTexFilterMgt.KSY_FILTER_BEAUTY_DENOISE :
                    ImgTexFilterMgt.KSY_FILTER_BEAUTY_DISABLE);
            mKSYRecordKit.setEnableImgBufBeauty(isChecked);
        } else {
            mBeautyChooseView.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void onBgmChecked(boolean isChecked) {
        if (isChecked) {
            mKSYRecordKit.getAudioPlayerCapture().setVolume(0.4f);
            mKSYRecordKit.setEnableAudioMix(true);
            mKSYRecordKit.startBgm(mBgmPath, true);

            mBgmVolumeSeekBar.setProgress((int) (0.4f * 100));
            mBgmVolumeSeekBar.setEnabled(true);
        } else {
            mKSYRecordKit.stopBgm();
            mBgmVolumeSeekBar.setEnabled(false);
        }
    }

    private void onMicAudioChecked(boolean isChecked) {
        mKSYRecordKit.setUseDummyAudioCapture(!isChecked);
        if (isChecked) {
            mMicAudioVolumeSeekBar.setEnabled(true);
            mMicAudioVolumeSeekBar.setProgress((int) (mKSYRecordKit.getVoiceVolume() * 100));
        } else {
            mMicAudioVolumeSeekBar.setEnabled(false);
        }
    }

    private void onWaterMarkChecked(boolean isChecked) {
        if (isChecked) {
            mKSYRecordKit.showWaterMarkLogo(mLogoPath, 0.08f, 0.04f, 0.20f, 0, 0.8f);
        } else {
            mKSYRecordKit.hideWaterMarkLogo();
        }

    }


    private class CheckBoxObserver implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.record_front_mirror:
                    onFrontMirrorChecked(isChecked);
                    break;
                case R.id.record_beauty:
                    onBeautyChecked(isChecked);
                    break;
                case R.id.record_mic_audio:
                    onMicAudioChecked(isChecked);
                    break;
                case R.id.record_bgm:
                    onBgmChecked(isChecked);
                    break;
                case R.id.record_watermark:
                    onWaterMarkChecked(isChecked);
                    break;
                case R.id.record_dynamic_sticker:
                    onStickerChecked(isChecked);
                    break;
                default:
                    break;
            }
        }
    }

    private class SeekBarChangedObserver implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            if (!fromUser) {
                return;

            }
            float val = progress / 100.f;
            switch (seekBar.getId()) {
                case R.id.record_mic_audio_volume:
                    mKSYRecordKit.setVoiceVolume(val);
                    break;
                case R.id.record_music_audio_volume:
                    mKSYRecordKit.getAudioPlayerCapture().getMediaPlayer().setVolume(val, val);
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

    }

    private void startCameraPreviewWithPermCheck() {
        int cameraPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int audioPerm = ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        if (cameraPerm != PackageManager.PERMISSION_GRANTED ||
                audioPerm != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Log.e(TAG, "No CAMERA or AudioRecord permission, please check");
                Toast.makeText(this, "No CAMERA or AudioRecord permission, please check",
                        Toast.LENGTH_LONG).show();
            } else {
                String[] permissions = {Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.READ_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions,
                        PERMISSION_REQUEST_CAMERA_AUDIOREC);
            }
        } else {
            mKSYRecordKit.startCameraPreview();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA_AUDIOREC: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mKSYRecordKit.startCameraPreview();
                } else {
                    Log.e(TAG, "No CAMERA or AudioRecord permission");
                    Toast.makeText(this, "No CAMERA or AudioRecord permission",
                            Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }

    private void updateDeleteView() {
        if (mKSYRecordKit.getRecordedFilesCount() >= 1) {
            mBackView.getDrawable().setLevel(2);
        } else {
            mBackView.getDrawable().setLevel(1);
        }
    }

    private boolean clearBackoff() {
        if (mBackView.isSelected()) {
            mBackView.setSelected(false);
            mRecordProgressCtl.setLastClipNormal();
            return true;
        }
        return false;
    }

    private void rollBackClipForError() {
        int clipCount = mRecordProgressCtl.getClipListSize();
        int fileCount = mKSYRecordKit.getRecordedFilesCount();
        if (clipCount > fileCount) {
            int diff = clipCount - fileCount;
            for (int i = 0; i < diff; i++) {
                mRecordProgressCtl.rollback();
            }
        }
    }

    private RecordProgressController.RecordingLengthChangedListener mRecordLengthChangedListener =
            new RecordProgressController.RecordingLengthChangedListener() {
                @Override
                public void passMinPoint(boolean pass) {
                    if (pass) {
                        mNextView.setVisibility(View.VISIBLE);
                    } else {
                        mNextView.setVisibility(View.GONE);
                    }
                }

                @Override
                public void passMaxPoint() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopRecord(false);
                            mRecordView.getDrawable().setLevel(1);
                            mRecordView.setEnabled(false);
                            Toast.makeText(RecordActivity.this, "录制结束，请继续操作",
                                    Toast
                                            .LENGTH_SHORT).show();
                        }
                    });
                }
            };

    private class MegerFilesAlertDialog extends AlertDialog {

        protected MegerFilesAlertDialog(Context context, int themID) {
            super(context, themID);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            setContentView(R.layout.meger_record_files_layout);
        }
    }

    private String getRecordFileFolder() {
        String fileFolder = "/sdcard/ksy_sv_rec_test";
        File file = new File(fileFolder);
        if (!file.exists()) {
            file.mkdir();
        }
        return fileFolder;
    }
}