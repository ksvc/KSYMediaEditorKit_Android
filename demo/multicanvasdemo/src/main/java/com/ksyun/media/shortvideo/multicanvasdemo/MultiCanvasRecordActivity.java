package com.ksyun.media.shortvideo.multicanvasdemo;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.shortvideo.kit.KSYRecordKit;
import com.ksyun.media.shortvideo.multicanvasdemo.data.MultiCanvasInfo;
import com.ksyun.media.shortvideo.multicanvasdemo.util.DensityUtil;
import com.ksyun.media.shortvideo.multicanvasdemo.util.MultiCanvasFactory;
import com.ksyun.media.shortvideo.multicanvasdemo.view.CameraHintView;
import com.ksyun.media.shortvideo.multicanvasdemo.view.CanvasViewBase;
import com.ksyun.media.shortvideo.multicanvasdemo.view.RecordProgressController;
import com.ksyun.media.shortvideo.multicanvasdemo.view.RecordProgressView;
import com.ksyun.media.streamer.capture.CameraCapture;
import com.ksyun.media.streamer.capture.camera.CameraTouchHelper;
import com.ksyun.media.streamer.kit.KSYStreamer;
import com.ksyun.media.streamer.kit.StreamerConstants;
import com.ksyun.media.streamer.logstats.StatsLogReport;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;

/**
 * demo for model record
 */

public class MultiCanvasRecordActivity extends Activity implements
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static String TAG = "MultiCanvasRecordActivity";
    public final static String PLAYER_URL = "play_url";
    private final static int PERMISSION_REQUEST_CAMERA_AUDIOREC = 1;   //摄像头、麦克风请求授权的请求码
    public static final int MAX_DURATION = 1 * 60 * 1000;  //最长拍摄时长
    public static final int MIN_DURATION = 5 * 1000;  //最短拍摄时长
    public static final int EDIT_REQUEST_CODE = 100;

    private GLSurfaceView mCameraPreviewView;
    private SurfaceView mPlayerPreview;
    private SurfaceHolder mSurfaceHolder;
    private CameraHintView mCameraHintView;
    private View mSwitchCameraView;
    private View mFlashView;
    private ImageView mTimingRecordView;
    private RelativeLayout mPreviewLayout;  //录制预览区域layout，包括正在录制的预览画面和之前录制好的文件的播放画面
    private View mBottomLayout;
    private View mTopLayout;
    private ImageView mWaterMarkView;
    private ImageView mRecordView;
    private ImageView mBackView;
    private ImageView mCountDownImage;
    private Chronometer mChronometer;
    private RecordProgressView mRecordProgressView;
    private CanvasViewBase mCanvasView;
    //拍摄进度显示
    private RecordProgressController mRecordProgressCtl;

    public MultiCanvasInfo[] mModelPos;  //模版中添加视频按钮和视频显示区域的位置信息
    private ShortVideoConfig mRecordConfig;
    private AnimatorSet mAnimatorSet;
    private MultiCanvasRecordActivity.ButtonObserver mObserverButton;

    //录制kit
    private KSYRecordKit mKSYRecordKit;
    private KSYMediaPlayer mMediaPlayer;

    private Handler mMainHandler;

    private boolean mIsFileRecording = false;
    private boolean mIsRecordFailed = false;
    private boolean mIsFlashOpened = false;
    private boolean mCameraPreviewing = false;
    private String mCurrentRecordUrl;
    private String mPlayUrl;
    private String mModelUrl;
    private boolean mHWEncoderUnsupported;  //硬编支持标志位
    private boolean mSWEncoderUnsupported;  //软编支持标志位

    private String mLogoPath = "assets://KSYLogo/logo.png";

    private int mScreenHeight;
    private int mScreenWidth;

    public int mCurrentRecordIndex = -1;

    public static void startActivity(Context context, String playerUrl) {
        Intent intent = new Intent(context, MultiCanvasRecordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(PLAYER_URL, playerUrl);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //录制参数配置
        if (ConfigActivity.getRecordConfig() != null) {
            mRecordConfig = ConfigActivity.getRecordConfig();
        } else {
            mRecordConfig = new ShortVideoConfig();
        }

        //只做竖屏模型展示
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.record_activity);


        //must set
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //init UI
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;

        mObserverButton = new MultiCanvasRecordActivity.ButtonObserver();

        mPreviewLayout = findViewById(R.id.record_preview);
        mCameraPreviewView = findViewById(R.id.camera_preview);
        mCameraPreviewView.setZOrderOnTop(true);
        mPlayerPreview = findViewById(R.id.player_preview);
        mBottomLayout = findViewById(R.id.bar_bottom);
        mTopLayout = findViewById(R.id.actionbar);

        //按照1:1的比例设置预览区域,而模版的提供建议按照1080*1920来计算出1:1的模版
        //模版作为预览区域的背景图显示
        //预览区域size
        int previewWidth = mScreenWidth;
        final int previewHeight = mScreenWidth;
        RelativeLayout.LayoutParams previewParams = (RelativeLayout.LayoutParams) mPreviewLayout
                .getLayoutParams();
        previewParams.height = previewHeight;
        previewParams.width = previewWidth;
        mPreviewLayout.setLayoutParams(previewParams);
        //顶层工具栏size
        RelativeLayout.LayoutParams topParams = (RelativeLayout.LayoutParams) mTopLayout
                .getLayoutParams();
        //底层工具栏size
        RelativeLayout.LayoutParams bottomParams = (RelativeLayout.LayoutParams) mBottomLayout
                .getLayoutParams();
        int topHeight = DensityUtil.dip2px(this, topParams.height);
        bottomParams.height = mScreenHeight - previewHeight - topHeight;
        bottomParams.width = previewWidth;
        mBottomLayout.setLayoutParams(bottomParams);

        //默认展示以下形式模型，更换模型只需要创建不同的layout
        //-record1- -record2-
        //-record3- -record4-

        //计算可录制区域及录制录制区域选择按钮的位置，可以由上级页面提供
        if (mModelPos == null) {
            mModelPos = MultiCanvasFactory.getModel4Info(previewWidth, previewHeight);
            mModelUrl = "assets://Model/model_4.png";
        }

        mCanvasView = new CanvasViewBase(this, R.layout.canvas_layout_4, mModelPos);

        mPreviewLayout.addView(mCanvasView);
        mCanvasView.setAddRecordClickListener(new CanvasViewBase.AddRecordClickListener() {
            @Override
            public int onAddClicked(int index) {
                if (mIsFileRecording) {
                    //正在录制中不能触发下一次录制
                    return -1;
                }
                mRecordView.setEnabled(true);
                //设置预览GLSurfaceView的大小和区域
                mCurrentRecordIndex = index;
                RelativeLayout.LayoutParams previewParams = (RelativeLayout.LayoutParams) mCameraPreviewView.getLayoutParams();
                previewParams.leftMargin = mModelPos[index - 1].x_preview;
                previewParams.topMargin = mModelPos[index - 1].y_preview;
                previewParams.width = mModelPos[index - 1].w_preview;
                previewParams.height = mModelPos[index - 1].h_preview;
                mCameraPreviewView.setLayoutParams(previewParams);

                RelativeLayout.LayoutParams cameraHitParams = (RelativeLayout.LayoutParams) mCameraHintView.getLayoutParams();
                cameraHitParams.leftMargin = mModelPos[index - 1].x_preview;
                cameraHitParams.topMargin = mModelPos[index - 1].y_preview;
                cameraHitParams.width = mModelPos[index - 1].w_preview;
                cameraHitParams.height = mModelPos[index - 1].h_preview;
                mCameraHintView.setLayoutParams(cameraHitParams);

                //开始预览
                mKSYRecordKit.startCameraPreview();
                //mCameraPreviewView.setVisibility(View.VISIBLE);
                return index;
            }
        });

        mSwitchCameraView = findViewById(R.id.switch_cam);
        mSwitchCameraView.setOnClickListener(mObserverButton);
        mCameraHintView = findViewById(R.id.camera_hint);
        mFlashView = findViewById(R.id.flash);
        mFlashView.setOnClickListener(mObserverButton);

        mTimingRecordView = findViewById(R.id.timing_record);
        mTimingRecordView.setOnClickListener(mObserverButton);
        mWaterMarkView = findViewById(R.id.record_watermark);
        mWaterMarkView.setOnClickListener(mObserverButton);

        mRecordView = findViewById(R.id.click_to_record);
        mRecordView.getDrawable().setLevel(1);
        mRecordView.setOnClickListener(mObserverButton);
        mRecordView.setEnabled(false);
        mBackView = findViewById(R.id.click_to_back);
        mBackView.setOnClickListener(mObserverButton);
        mCountDownImage = findViewById(R.id.count_down_image);

        //拍摄进度初始化
        mChronometer = findViewById(R.id.chronometer);
        mRecordProgressView = findViewById(R.id.record_progress);
        mRecordProgressCtl = new RecordProgressController(mRecordProgressView, mChronometer);
        //拍摄时长变更回调
        mRecordProgressCtl.setRecordingLengthChangedListener(mRecordLengthChangedListener);
        mRecordProgressCtl.start();


        mBackView.getDrawable().setLevel(1);
        mBackView.setSelected(false);

        //init
        mMainHandler = new Handler();
        mKSYRecordKit = new KSYRecordKit(this);
        mMediaPlayer = new KSYMediaPlayer.Builder(this).build();
        mSurfaceHolder = mPlayerPreview.getHolder();
        mSurfaceHolder.addCallback(mSurfaceCallback);

        float frameRate = mRecordConfig.fps;
        if (frameRate > 0) {
            mKSYRecordKit.setPreviewFps(frameRate);
            mKSYRecordKit.setTargetFps(frameRate);
        }

        int videoBitrate = mRecordConfig.videoBitrate;
        if (videoBitrate > 0) {
            mKSYRecordKit.setVideoKBitrate(videoBitrate);
        }

        int audioBitrate = mRecordConfig.audioBitrate;
        if (audioBitrate > 0) {
            mKSYRecordKit.setAudioKBitrate(audioBitrate);
        }

        int videoResolution = mRecordConfig.resolution;
        mKSYRecordKit.setPreviewResolution(videoResolution);
        mKSYRecordKit.setTargetResolution(videoResolution);

        int encode_type = mRecordConfig.encodeType;
        mKSYRecordKit.setVideoCodecId(encode_type);

        int encode_method = mRecordConfig.encodeMethod;
        mKSYRecordKit.setEncodeMethod(encode_method);

        int encodeProfile = mRecordConfig.encodeProfile;
        mKSYRecordKit.setVideoEncodeProfile(encodeProfile);

        if (mRecordConfig.isLandscape) {
            mKSYRecordKit.setRotateDegrees(90);
        } else {
            mKSYRecordKit.setRotateDegrees(0);
        }
        mKSYRecordKit.setDisplayPreview(mCameraPreviewView);
        mKSYRecordKit.setEnableRepeatLastFrame(false);
        mKSYRecordKit.setCameraFacing(CameraCapture.FACING_FRONT);
        mKSYRecordKit.setFrontCameraMirror(true);
        mKSYRecordKit.setOnInfoListener(mOnInfoListener);
        mKSYRecordKit.setOnErrorListener(mOnErrorListener);
        mKSYRecordKit.setOnLogEventListener(mOnLogEventListener);
        mKSYRecordKit.setAudioChannels(2);

        // touch focus and zoom support
        CameraTouchHelper cameraTouchHelper = new CameraTouchHelper();
        cameraTouchHelper.setCameraCapture(mKSYRecordKit.getCameraCapture());
        mCameraPreviewView.setOnTouchListener(cameraTouchHelper);
        // set CameraHintView to show focus rect and zoom ratio
        cameraTouchHelper.setCameraHintView(mCameraHintView);

        checkPermission(); //请求授权

        Bundle bundle = getIntent().getExtras();
        mPlayUrl = bundle.getString(PLAYER_URL);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case EDIT_REQUEST_CODE:
                //从编辑窗口返回，判断是否有编辑后的视频
                if (data != null) {
                    Bundle bundle = data.getExtras();
                    mPlayUrl = bundle.getString(PLAYER_URL);
                } else {
                    if (mCurrentRecordIndex > 0) {
                        //取消编辑的场景需要撤销之前的录制文件
                        mModelPos[mCurrentRecordIndex - 1].showAdd = true;
                    }
                }
                //此时不能确定录制的视频的位置，需要将size设置为1，否则会对正在播放的视频产生影响
                RelativeLayout.LayoutParams previewParams = (RelativeLayout.LayoutParams) mCameraPreviewView.getLayoutParams();
                previewParams.width = 1;
                previewParams.height = 1;
                mCameraPreviewView.setLayoutParams(previewParams);
                //更新添加录制icon的显示
                mCanvasView.updateRecordView(mModelPos);
                if (!TextUtils.isEmpty(mPlayUrl) && !mMediaPlayer.isPlaying()) {
                    startPlay(mPlayUrl);
                }
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        mKSYRecordKit.setDisplayPreview(mCameraPreviewView);
        mKSYRecordKit.onResume();
        mCameraHintView.hideAll();

        // camera may be occupied by other app in background
        checkPermission();

        //如果当前正在录制，恢复播放
        if (mIsFileRecording && !TextUtils.isEmpty(mPlayUrl)) {
            mMediaPlayer.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mKSYRecordKit.onPause();
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }

        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }

        mRecordProgressCtl.stop();
        mRecordProgressCtl.setRecordingLengthChangedListener(null);
        mRecordProgressCtl.release();
        mKSYRecordKit.setOnLogEventListener(null);
        mKSYRecordKit.release();
        releasePlay();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                onBackoffClick();  //覆盖系统返回键进行个性化处理
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void startCountDownAnimation() {
        //预览未开始，不能开始录制的倒计时
        if (!mCameraPreviewing) {
            Toast.makeText(this,
                    "please select canvas to preview", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mIsFileRecording) {
            Toast.makeText(this,
                    "recording now", Toast.LENGTH_SHORT).show();
            return;
        }
        final int[] resId = new int[]{R.drawable.num_three, R.drawable.num_two, R.drawable.num_one};
        //沿x轴缩小
        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(mCountDownImage, "scaleX", 1f, 0.5f, 0.2f);
        scaleXAnimator.setRepeatCount(2);
        //沿y轴缩小
        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(mCountDownImage, "scaleY", 1f, 0.5f, 0.2f);
        scaleYAnimator.setRepeatCount(2);
        //透明度动画
        ObjectAnimator alphaAnimator = ObjectAnimator.ofFloat(mCountDownImage, "alpha", 1f, 0.5f);
        alphaAnimator.setRepeatCount(2);
        if (mAnimatorSet != null) {
            mAnimatorSet.cancel();
        }
        mAnimatorSet = new AnimatorSet();
        mAnimatorSet.play(scaleXAnimator).with(scaleYAnimator).with(alphaAnimator);
        scaleXAnimator.addListener(new Animator.AnimatorListener() {
            int index = 0;

            @Override
            public void onAnimationStart(Animator animation) {
                if (mCountDownImage.getVisibility() != View.VISIBLE) {
                    mCountDownImage.setVisibility(View.VISIBLE);
                }
                mCountDownImage.setImageDrawable(getResources().getDrawable(resId[0]));
                index++;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mCountDownImage.setVisibility(View.GONE);
                if(!MultiCanvasRecordActivity.this.isFinishing()) {
                    startRecord(mCurrentRecordIndex);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                if (index >= 0 && index < 3) {
                    mCountDownImage.setImageDrawable(getResources().getDrawable(resId[index]));
                    index++;
                }
            }
        });
        mAnimatorSet.setDuration(1000).start();
    }

    //start recording to a local file
    private void startRecord(int index) {
        if (mIsFileRecording) {
            // 上一次录制未停止完，不能开启下一次录制
            return;
        }

        String fileFolder = getRecordFileFolder();
        mCurrentRecordUrl = fileFolder + "/" + System.currentTimeMillis() + String.valueOf(index) + ".mp4";
        Log.d(TAG, "record url:" + mCurrentRecordUrl);
        //超过最短时长后才可以停止录制
        mRecordView.setEnabled(false);

        //设置录制文件的本地存储路径，并开始录制
        if (mKSYRecordKit.startRecord(mCurrentRecordUrl)) {
            mIsFileRecording = true;
            //更新录制UI
            mRecordView.getDrawable().setLevel(2);

            //开始之前视频的播放
            if (!TextUtils.isEmpty(mPlayUrl)) {
                mMediaPlayer.start();
            }
        }
    }

    /**
     * 停止拍摄
     */
    private void stopRecord() {
        //停止录制接口为异步接口，sdk在停止结束后会发送
        // StreamerConstants.KSY_STREAMER_FILE_RECORD_STOPPED消息
        //下一次录制响应最好在接收到消息后再进行
        mRecordView.setEnabled(false);
        //一个录制完成，进入编辑页面
        mKSYRecordKit.stopRecord();
        //停止录制后直接进入编辑界面，因此停止播放
        resetPlay();
    }

    /**
     * 停止录制计时
     */
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
                    mCameraPreviewing = true;
                    break;
                case StreamerConstants.KSY_STREAMER_CAMERA_FACEING_CHANGED:
                    break;
                case StreamerConstants.KSY_STREAMER_OPEN_FILE_SUCCESS:
                    Log.d(TAG, "KSY_STREAMER_OPEN_FILE_SUCCESS");
                    mChronometer.setBase(SystemClock.elapsedRealtime());
                    mChronometer.start();
                    mRecordProgressCtl.startRecording();
                    break;
                case StreamerConstants.KSY_STREAMER_FILE_RECORD_STOPPED:
                    Log.d(TAG, "KSY_STREAMER_FILE_RECORD_STOPPED");
                    //未停止结束，最好不要操作开始录制，否则在某些机型上容易造成录制开始失败的case
                    mIsFileRecording = false;
                    mCameraPreviewing = false;
                    updateRecordUI();
                    if (!mIsRecordFailed) {
                        //录制结束进入编辑页面，录制好的视频的添加icon不需要显示
                        if (mCurrentRecordIndex > 0) {
                            mModelPos[mCurrentRecordIndex - 1].showAdd = false;
                            if (TextUtils.isEmpty(mPlayUrl)) {
                                MultiCanvasEditActivity.startActivity(MultiCanvasRecordActivity.this, mCurrentRecordUrl, mPlayUrl,
                                        mModelUrl,
                                        R.layout.canvas_layout_4, mModelPos[mCurrentRecordIndex - 1],
                                        isRecordFinished());
                            } else {
                                MultiCanvasEditActivity.startActivity(MultiCanvasRecordActivity.this, mCurrentRecordUrl,
                                        mPlayUrl, null,
                                        R.layout.canvas_layout_4,
                                        mModelPos[mCurrentRecordIndex - 1],
                                        isRecordFinished());
                            }
                        }
                    } else {
                        //由于录制失败触发的录制停止，不需要进入编辑页面，在此处恢复录制失败的标记
                        mIsRecordFailed = false;
                    }
                    break;
                default:
                    Log.d(TAG, "OnInfo: " + what + " msg1: " + msg1 + " msg2: " + msg2);
                    break;
            }
        }
    };

    private void updateRecordUI() {
        mRecordView.setEnabled(true);
        mRecordView.setClickable(true);
        //更新进度显示
        mRecordProgressCtl.stopRecording();
        mRecordProgressCtl.rollback();
        mRecordView.getDrawable().setLevel(1);
        stopChronometer();
    }

    private boolean isRecordFinished() {
        int finishCount = 0;
        for (int i = 0; i < mModelPos.length; i++) {
            if (!mModelPos[i].showAdd) {
                finishCount++;
            }
        }
        if (finishCount == mModelPos.length) {
            return true;
        }
        return false;
    }

    /**
     * 不支持硬编的设备，fallback到软编
     */
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
                    mIsRecordFailed = true;
                    stopRecord();
                    rollBackClipForError();
                    break;
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED:
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN: {
                    handleEncodeError();
                    mIsRecordFailed = true;
                    stopRecord();
                    rollBackClipForError();
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

    /**
     * 前后置摄像头切换
     */
    private void onSwitchCamera() {
        mKSYRecordKit.switchCamera();
    }

    /**
     * 闪光灯开关处理
     */
    private void onFlashClick() {
        if (mIsFlashOpened) {
            mKSYRecordKit.toggleTorch(false);
            mIsFlashOpened = false;
        } else {
            mKSYRecordKit.toggleTorch(true);
            mIsFlashOpened = true;
        }
    }

    /**
     * 返回上一级
     */
    private void onBackoffClick() {
        if (mIsFileRecording) {
            //返回不代表录制成功结束，录制结束后不需要跳转置编辑页面
            mIsRecordFailed = true;
            stopRecord();
        }

        stopPlay();
        mChronometer.stop();
        mIsFileRecording = false;
        mCameraPreviewing = false;
        MultiCanvasRecordActivity.this.finish();
    }

    /**
     * 开始/停止录制
     */
    private void onRecordClick() {
        if (mIsFileRecording) {
            stopRecord();
        } else {
            startRecord(mCurrentRecordIndex);
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
                case R.id.timing_record:
                    startCountDownAnimation();
                    break;
                case R.id.click_to_record:
                    onRecordClick();
                    break;
                case R.id.click_to_back:
                    onBackoffClick();
                    break;
                case R.id.record_watermark:
                    onWaterMarkClick();
                    break;
                default:
                    break;
            }
        }
    }


    private void onWaterMarkClick() {
        if (mWaterMarkView.isActivated()) {
            mWaterMarkView.setActivated(false);
            mKSYRecordKit.hideWaterMarkLogo();
        } else {
            mWaterMarkView.setActivated(true);
            mKSYRecordKit.showWaterMarkLogo(mLogoPath, 0.08f, 0.04f, 0.20f, 0, 0.8f);
        }

    }


    private void checkPermission() {
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
                } else {
                    Log.e(TAG, "No CAMERA or AudioRecord permission");
                    Toast.makeText(this, "No CAMERA or AudioRecord permission",
                            Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }


    /**
     * 拍摄错误停止后，删除多余文件的进度
     */
    private void rollBackClipForError() {
        //当拍摄异常停止时，SDk内部会删除异常文件，如果ctl比SDK返回的文件小，则需要更新ctl中的进度信息
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
                        if (mIsFileRecording) {
                            //超过最短时长录制停止按钮才可以使用
                            mRecordView.setEnabled(true);
                        }
                    } else {
                        if (mIsFileRecording) {
                            mRecordView.setEnabled(false);
                        }
                    }
                }

                @Override
                public void passMaxPoint() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //到达最大拍摄时长时，需要主动停止录制
                            stopRecord();
                            mRecordView.getDrawable().setLevel(1);
                            mRecordView.setClickable(false);
                            mRecordView.setEnabled(false);
                            Toast.makeText(MultiCanvasRecordActivity.this, "录制结束，请继续操作",
                                    Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            };


    private String getRecordFileFolder() {
        String fileFolder = "/sdcard/ksy_sv_model_rec_test";
        File file = new File(fileFolder);
        if (!file.exists()) {
            file.mkdir();
        }
        return fileFolder;
    }

    /**************************player begin**************************************/
    private final SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Log.d(TAG, "mediaplayer surfaceChanged");

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "mediaplayer surfaceCreated");
            if (mMediaPlayer != null) {
                mMediaPlayer.setDisplay(holder);
                mMediaPlayer.setScreenOnWhilePlaying(true);
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "mediaplayer surfaceDestroyed");
            // 此处非常重要，必须调用!!!
            if (mMediaPlayer != null) {
                mMediaPlayer.setDisplay(null);
            }
        }
    };
    private IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer mp) {
            Log.d(TAG, "mediaplayer onCompletion");
        }
    };

    private IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer mp) {
            if (mMediaPlayer != null) {
                Log.d(TAG, "mediaplayer onPrepared");
                // 设置视频伸缩模式，此模式为填充模式
                mMediaPlayer.setVideoScalingMode(KSYMediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT);
                // 开始播放视频
                mMediaPlayer.start();
            }
        }
    };

    private IMediaPlayer.OnErrorListener mOnMediaErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            Log.d(TAG, "mediaplayer error:" + i);
            return false;
        }
    };

    private IMediaPlayer.OnInfoListener mOnMediaInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
            switch (what) {
                case KSYMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    //首帧视频来以后，暂停，开始录制后在启动播放
                    mMediaPlayer.pause();
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    private void startPlay(String path) {
        mMediaPlayer.setLooping(true);
        mMediaPlayer.shouldAutoPlay(false);
        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mMediaPlayer.setOnInfoListener(mOnMediaInfoListener);
        mMediaPlayer.setOnErrorListener(mOnMediaErrorListener);
        mMediaPlayer.setDisplay(mSurfaceHolder);
        try {
            mMediaPlayer.setDataSource(path);
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void stopPlay() {
        if (mMediaPlayer != null) {
            mMediaPlayer.setOnCompletionListener(null);
            mMediaPlayer.setOnPreparedListener(null);
            mMediaPlayer.setOnInfoListener(null);
            mMediaPlayer.setOnErrorListener(null);
            mMediaPlayer.stop();
        }
    }

    private void resetPlay() {
        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.setDisplay(mSurfaceHolder);
        }
    }

    private void releasePlay() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    /**************************player end**************************************/
}
