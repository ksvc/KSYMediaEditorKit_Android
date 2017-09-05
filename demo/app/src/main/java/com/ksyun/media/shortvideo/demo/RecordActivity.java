package com.ksyun.media.shortvideo.demo;

import com.ksyun.media.shortvideo.demo.adapter.BgmSelectAdapter;
import com.ksyun.media.shortvideo.demo.adapter.ImageTextAdapter;
import com.ksyun.media.shortvideo.demo.adapter.SoundEffectAdapter;
import com.ksyun.media.shortvideo.demo.recordclip.RecordProgressView;
import com.ksyun.media.shortvideo.demo.util.DataFactory;
import com.ksyun.media.shortvideo.demo.util.FileUtils;
import com.ksyun.media.shortvideo.demo.kmc.ApiHttpUrlConnection;
import com.ksyun.media.shortvideo.demo.recordclip.RecordProgressController;
import com.ksyun.media.shortvideo.demo.util.ViewUtils;
import com.ksyun.media.shortvideo.demo.view.CameraHintView;
import com.ksyun.media.shortvideo.demo.view.VerticalSeekBar;
import com.ksyun.media.shortvideo.kit.KSYRecordKit;
import com.ksyun.media.streamer.capture.CameraCapture;
import com.ksyun.media.streamer.capture.camera.CameraTouchHelper;
import com.ksyun.media.streamer.filter.audio.AudioFilterBase;
import com.ksyun.media.streamer.filter.audio.AudioReverbFilter;
import com.ksyun.media.streamer.filter.audio.KSYAudioEffectFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautyProFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautySoftFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautySpecialEffectsFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautyStylizeFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgFilterBase;
import com.ksyun.media.streamer.filter.imgtex.ImgTexFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgTexFilterBase;
import com.ksyun.media.streamer.filter.imgtex.ImgTexFilterMgt;
import com.ksyun.media.streamer.kit.KSYStreamer;
import com.ksyun.media.streamer.kit.StreamerConstants;
import com.ksyun.media.streamer.logstats.StatsLogReport;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ksyun.media.kmcfilter.Constants;
import com.ksyun.media.kmcfilter.KMCArMaterial;
import com.ksyun.media.kmcfilter.KMCAuthManager;
import com.ksyun.media.kmcfilter.KMCFilter;
import com.ksyun.media.kmcfilter.KMCFilterManager;
import com.ksyun.media.shortvideo.demo.kmc.MaterialInfoItem;
import com.ksyun.media.shortvideo.demo.kmc.RecyclerViewAdapter;
import com.ksyun.media.shortvideo.demo.kmc.SpacesItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 短视频录制示例窗口
 * 水印
 * 变焦
 * 美颜
 * 滤镜
 * 断点拍摄
 * 魔方动态贴纸
 * 麦克风音频音量调节
 * 背景音频添加及音量调节
 * 麦克风音频音效处理：变声&混响
 * 背景音频变调
 */
public class RecordActivity extends Activity implements
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static String TAG = "RecordActivity";

    private final static int PERMISSION_REQUEST_CAMERA_AUDIOREC = 1;   //摄像头、麦克风请求授权的请求码
    public static final int MAX_DURATION = 1 * 60 * 1000;  //最长拍摄时长
    public static final int MIN_DURATION = 5 * 1000;  //最短拍摄时长
    private static final int REQUEST_CODE = 10010;
    private static final int AUDIO_FILTER_DISABLE = 0;  //不使用音频滤镜的类型标志

    private static final int INDEX_BEAUTY_TITLE_BASE = 0;  //美颜标题在mRecordTitleArray中的启始位置索引
    private static final int INDEX_BGM_TITLE_BASE = 10;  //音乐标题在mRecordTitleArray中的启始位置索引

    private int mAudioEffectType = AUDIO_FILTER_DISABLE;  //变声类型
    private int mAudioReverbType = AUDIO_FILTER_DISABLE;  //混响类型

    //变声类型数组常量
    private static final int[] SOUND_CHANGE_TYPE = {KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_MALE, KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_FEMALE,
            KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_HEROIC, KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_ROBOT};
    //混响类型数组常量
    private static final int[] REVERB_TYPE = {AudioReverbFilter.AUDIO_REVERB_LEVEL_1, AudioReverbFilter.AUDIO_REVERB_LEVEL_3,
            AudioReverbFilter.AUDIO_REVERB_LEVEL_4, AudioReverbFilter.AUDIO_REVERB_LEVEL_2};

    private static final int FILTER_DISABLE = 0;

    private GLSurfaceView mCameraPreviewView;
    //private TextureView mCameraPreviewView;
    private CameraHintView mCameraHintView;
    private Chronometer mChronometer;
    private View mSwitchCameraView;
    private View mFlashView;
    private View mExposureView;
    private View mNoiseSuppressionView;
    private VerticalSeekBar mExposureSeekBar;
    private VerticalSeekBar mNoiseSeekBar;
    private RecordProgressView mRecordProgressView;
    private ImageView mRecordView;
    private ImageView mBackView;
    private ImageView mNextView;
    private ImageView mBeautyView;
    private ImageView mBgmMusicView;
    private ImageView mSoundEffectView;
    private ImageView mWaterMarkView;
    private View mDefaultRecordBottomLayout;
    private View mBeautyLayout;
    private View mBgmLayout;
    private View mSoundEffectLayout;
    private View mBeautyIndicator;
    private View mStickerIndicator;
    private View mFilterIndicator;
    private TextView mBeauty;
    private TextView mDynSticker;
    private TextView mFilter;
    private ImageView mFilterOriginImage;
    private ImageView mFilterBorder;
    private TextView mFilterOriginText;
    private RecyclerView mFilterRecyclerView;
    //背景音乐和音效标题栏控件
    private TextView mSoundChange;
    private TextView mReverb;
    private View mSoundChangeIndicator;
    private View mReverbIndicator;
    private View mSoundChangeLayout;
    private View mReverbLayout;
    private ImageView mPitchMinus;
    private ImageView mPitchPlus;
    private TextView mPitchText;
    private RecyclerView mBgmRecyclerView;
    private RecyclerView mSoundChangeRecycler;
    private RecyclerView mReverbRecycler;
    private BgmSelectAdapter mBgmAdapter;
    private SoundEffectAdapter mSoundChangeAdapter;
    private SoundEffectAdapter mReverbAdapter;
    private ShortVideoConfig mRecordConfig;

    private AppCompatSeekBar mMicAudioVolumeSeekBar;
    private AppCompatSeekBar mBgmVolumeSeekBar;

    //美颜
    private static final int BEAUTY_DISABLE = 100;
    private static final int BEAUTY_NATURE = 101;
    private static final int BEAUTY_PRO = 102;
    private static final int BEAUTY_FLOWER_LIKE = 103;
    private static final int BEAUTY_DELICATE = 104;
    private View mBeautyChooseView;
    private ImageView mBeautyOriginalView;
    private ImageView mBeautyBorder;
    private TextView mBeautyOriginalText;
    private RecyclerView mBeautyRecyclerView;

    private View mFilterChooseView;

    //断点拍摄进度控制
    private RecordProgressController mRecordProgressCtl;

    private View mStickerChooseView;

    private ButtonObserver mObserverButton;
    private SeekBarChangedObserver mSeekBarChangedObserver;

    //魔方贴纸
    private boolean mKMCAuthorized = false;
    private KMCArMaterial mMaterial = null;
    private List<MaterialInfoItem> mMaterialList = null;
    private Thread mKMCInitThread;
    private KMCFilter mKMCFilter;
    private RecyclerView mKMCRecyclerView = null;
    private RecyclerViewAdapter mKMCAdapter = null;
    private final static int MSG_LOAD_THUMB = 0;
    private final static int MSG_DOWNLOAD_SUCCESS = 1;
    private final static int MSG_START_DOWNLOAD = 2;
    private final static int MSG_GET_LIST_SIZE = 3;
    private static int mMaterialIndex = -1;
    private boolean mIsFirstFetchMaterialList = true;
    private Bitmap mNullBitmap = null;

    //录制kit
    private KSYRecordKit mKSYRecordKit;
    private int mImgBeautyTypeIndex = BEAUTY_DISABLE;  //美颜type
    private int mEffectFilterIndex = FILTER_DISABLE;  //滤镜filter type

    private Handler mMainHandler;

    private boolean mIsFileRecording = false;
    private boolean mIsFlashOpened = false;
    private String mRecordUrl;
    private boolean mHWEncoderUnsupported;  //硬编支持标志位
    private boolean mSWEncoderUnsupported;  //软编支持标志位

    private int mPitchValue = 0;  //音调值
    private int mPreBeautyTitleIndex = 0;  //记录上次选择的美颜标题索引
    private int mPreBgmTitleIndex = 0;  //记录上次选择的背景音乐标题索引
    private View mPreRecordConfigLayout;

    //美颜、背景音乐标题和布局自定义内容集合
    private SparseArray<BottomTitleViewInfo> mRecordTitleArray = new SparseArray<>();

    private String mLogoPath = "assets://KSYLogo/logo.png";

    private int mScreenHeight;
    private int mFilterTypeIndex = -1;

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, RecordActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
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
        if (mRecordConfig.isLandscape) {
            setContentView(R.layout.record_acitvity_landscape);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            //默认设置为竖屏
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setContentView(R.layout.record_acitvity);
        }

        //must set
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //init UI
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        mObserverButton = new ButtonObserver();
        mSeekBarChangedObserver = new SeekBarChangedObserver();
        mSwitchCameraView = findViewById(R.id.switch_cam);
        mSwitchCameraView.setOnClickListener(mObserverButton);
        mCameraHintView = (CameraHintView) findViewById(R.id.camera_hint);
        mFlashView = findViewById(R.id.flash);
        mFlashView.setOnClickListener(mObserverButton);
        mExposureView = findViewById(R.id.exposure);
        mExposureView.setOnClickListener(mObserverButton);
        mNoiseSuppressionView = findViewById(R.id.noise_suppression);
        mNoiseSuppressionView.setOnClickListener(mObserverButton);
        mExposureSeekBar = (VerticalSeekBar) findViewById(R.id.exposure_seekBar);
        mExposureSeekBar.setProgress(50);
        mExposureSeekBar.setSecondaryProgress(50);
        mExposureSeekBar.setOnSeekBarChangeListener(getVerticalSeekListener());
        mNoiseSeekBar = (VerticalSeekBar) findViewById(R.id.noise_seekBar);
        mNoiseSeekBar.setProgress(0);
        mNoiseSeekBar.setSecondaryProgress(0);
        mNoiseSeekBar.setOnSeekBarChangeListener(getVerticalSeekListener());
        mCameraPreviewView = (GLSurfaceView) findViewById(R.id.camera_preview);
        //美颜及背景音乐界面控件
        mDefaultRecordBottomLayout = findViewById(R.id.default_bottom_layout);
        mPreRecordConfigLayout = mDefaultRecordBottomLayout;
        mBeautyView = (ImageView) findViewById(R.id.record_beauty);
        mBeautyView.setOnClickListener(mObserverButton);
        mBgmLayout = findViewById(R.id.item_bgm_select);
        mBgmMusicView = (ImageView) findViewById(R.id.record_bgm);
        mBgmMusicView.setOnClickListener(mObserverButton);
        mSoundEffectView = (ImageView) findViewById(R.id.record_sound_effect);
        mSoundEffectView.setOnClickListener(mObserverButton);
        mBeautyLayout = findViewById(R.id.item_beauty_select);
        mSoundEffectLayout = findViewById(R.id.item_sound_effect);
        mBeauty = (TextView) findViewById(R.id.item_beauty);
        mBeautyIndicator = findViewById(R.id.item_beauty_indicator);
        mDynSticker = (TextView) findViewById(R.id.item_dyn_sticker);
        mStickerIndicator = findViewById(R.id.item_sticker_indicator);
        mFilter = (TextView) findViewById(R.id.item_filter);
        mFilterIndicator = findViewById(R.id.item_filter_indicator);
        //mFrontMirrorCheckBox = (CheckBox) findViewById(R.id.record_front_mirror);
        mWaterMarkView = (ImageView) findViewById(R.id.record_watermark);
        mWaterMarkView.setOnClickListener(mObserverButton);
        mMicAudioVolumeSeekBar = (AppCompatSeekBar) findViewById(R.id.record_mic_audio_volume);
        mMicAudioVolumeSeekBar.setOnSeekBarChangeListener(mSeekBarChangedObserver);
        mBgmVolumeSeekBar = (AppCompatSeekBar) findViewById(R.id.record_music_audio_volume);
        mBgmVolumeSeekBar.setOnSeekBarChangeListener(mSeekBarChangedObserver);
        mChronometer = (Chronometer) findViewById(R.id.chronometer);
        mRecordView = (ImageView) findViewById(R.id.click_to_record);
        mRecordView.getDrawable().setLevel(1);
        mRecordView.setOnClickListener(mObserverButton);
        mBackView = (ImageView) findViewById(R.id.click_to_back);
        mBackView.setOnClickListener(mObserverButton);
        mNextView = (ImageView) findViewById(R.id.click_to_next);
        mNextView.setOnClickListener(mObserverButton);

        mBeautyChooseView = findViewById(R.id.record_beauty_choose);
        BottomTitleViewInfo mBeautyInfo = new BottomTitleViewInfo(mBeauty, mBeautyIndicator,
                mBeautyChooseView, mObserverButton);
        mBeautyInfo.setChosenState(true);
        mRecordTitleArray.put(INDEX_BEAUTY_TITLE_BASE, mBeautyInfo);

        mStickerChooseView = findViewById(R.id.record_sticker_choose);
        BottomTitleViewInfo mStickerInfo = new BottomTitleViewInfo(mDynSticker, mStickerIndicator,
                mStickerChooseView, mObserverButton);
        mRecordTitleArray.put(INDEX_BEAUTY_TITLE_BASE + 1, mStickerInfo);
        mFilterChooseView = findViewById(R.id.record_filter_choose);
        BottomTitleViewInfo mFilterInfo = new BottomTitleViewInfo(mFilter, mFilterIndicator,
                mFilterChooseView, mObserverButton);
        mRecordTitleArray.put(INDEX_BEAUTY_TITLE_BASE + 2, mFilterInfo);

        //断点拍摄UI初始化
        mRecordProgressView = (RecordProgressView) findViewById(R.id.record_progress);
        mRecordProgressCtl = new RecordProgressController(mRecordProgressView, mChronometer);
        //拍摄时长变更回调
        mRecordProgressCtl.setRecordingLengthChangedListener(mRecordLengthChangedListener);
        mRecordProgressCtl.start();

        mBackView.getDrawable().setLevel(1);
        mBackView.setSelected(false);
        //init
        mMainHandler = new Handler();
        mKSYRecordKit = new KSYRecordKit(this);

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
        initBeautyUI(); //初始化美颜界面
        initStickerUI();  //初始化动态贴纸界面
        initFilterUI();  //初始化滤镜界面
        initBgmView();  //初始化背景音乐界面
        initSoundEffectView(); //初始化音效界面
        // touch focus and zoom support
        CameraTouchHelper cameraTouchHelper = new CameraTouchHelper();
        cameraTouchHelper.setCameraCapture(mKSYRecordKit.getCameraCapture());
        mCameraPreviewView.setOnTouchListener(cameraTouchHelper);
        // set CameraHintView to show focus rect and zoom ratio
        cameraTouchHelper.setCameraHintView(mCameraHintView);

        startCameraPreviewWithPermCheck(); //请求授权，成功则开启预览
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

        mBgmAdapter.setOnItemClickListener(null);
        mBgmAdapter.clearTask();
        mKSYRecordKit.stopBgm();

        mRecordProgressCtl.stop();
        mRecordProgressCtl.setRecordingLengthChangedListener(null);
        mRecordProgressCtl.release();
        mKSYRecordKit.setOnLogEventListener(null);
        mKSYRecordKit.release();

        mMaterialIndex = -1;
        if (mKMCInitThread != null) {
            mKMCInitThread.interrupt();
            mKMCInitThread = null;
        }
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

    //start recording to a local file
    private void startRecord() {
        if (mIsFileRecording) {
            // 上一次录制未停止完，不能开启下一次录制
            return;
        }
        String fileFolder = getRecordFileFolder();
        mRecordUrl = fileFolder + "/" + System.currentTimeMillis() + ".mp4";
        Log.d(TAG, "record url:" + mRecordUrl);
        float val = mMicAudioVolumeSeekBar.getProgress() / 100.f;
        mKSYRecordKit.setVoiceVolume(val);
        //设置录制文件的本地存储路径，并开始录制
        if (mKSYRecordKit.startRecord(mRecordUrl)) {
            mIsFileRecording = true;
            //更新录制UI
            mRecordView.getDrawable().setLevel(2);
        }
    }

    /**
     * 停止拍摄
     *
     * @param finished 代表是否结束断点拍摄
     */
    private void stopRecord(boolean finished) {
        //停止录制接口为异步接口，sdk在停止结束后会发送
        // StreamerConstants.KSY_STREAMER_FILE_RECORD_STOPPED消息
        //下一次录制响应最好在接收到消息后再进行
        mRecordView.setEnabled(false);

        //录制完成进入编辑
        //若录制文件大于1则需要在录制结束后触发文件合成
        if (finished) {
            String fileFolder = getRecordFileFolder();
            //合成文件路径
            String outFile = fileFolder + "/" + "merger_" + System.currentTimeMillis() + ".mp4";
            //合成过程为异步，需要block下一步处理
            final MegerFilesAlertDialog dialog = new MegerFilesAlertDialog
                    (RecordActivity.this, R.style.dialog);
            dialog.setCancelable(false);
            dialog.show();
            mKSYRecordKit.stopRecord(outFile, new KSYRecordKit.MergeFilesFinishedListener() {
                @Override
                public void onFinished(final String filePath) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            dialog.dismiss();
                            mRecordUrl = filePath;  //合成文件本地路径
                            updateRecordUI();
                            if (filePath == null) {
                                Log.e(TAG, "Merge file failed");
                                Toast.makeText(RecordActivity.this, "Merge file failed!",
                                        Toast.LENGTH_LONG).show();
                                return;
                            }
                            //可以调用此接口在合成结束后，删除断点录制的所有视频
                            //mKSYRecordKit.deleteAllFiles();

                            //获取合成后视频的时长，thumbnail等 示例代码
//                            ProbeMediaInfoTools probeMediaInfoTools = new ProbeMediaInfoTools();
//                            probeMediaInfoTools.probeMediaInfo(mRecordUrl,
//                                    new ProbeMediaInfoTools.ProbeMediaInfoListener() {
//                                        @Override
//                                        public void probeMediaInfoFinished(ProbeMediaInfoTools.MediaInfo info) {
//                                            if (info != null) {
//                                                Log.e(TAG, "url:" + info.url);
//                                                Log.e(TAG, "duration:" + info.duration);
//                                            }
//                                        }
//                                    });
//                            //get thumbnail for first frame
//                            probeMediaInfoTools.getVideoThumbnailAtTime(mKSYRecordKit.getLastRecordedFiles(), 0, 0, 0);


                            //合成结束启动编辑
                            EditActivity.startActivity(getApplicationContext(), mRecordUrl);
                        }
                    });
                }
            });
        } else {
            //普通录制停止
            mKSYRecordKit.stopRecord();
        }

        //获取当前录制视频时长，thumbnail 示例代码，可以仿照此写法获取任意一段视频的信息
//            ProbeMediaInfoTools probeMediaInfoTools = new ProbeMediaInfoTools();
//            probeMediaInfoTools.probeMediaInfo(mKSYRecordKit.getLastRecordedFiles(),
//                    new ProbeMediaInfoTools.ProbeMediaInfoListener() {
//                        @Override
//                        public void probeMediaInfoFinished(ProbeMediaInfoTools.MediaInfo info) {
//                            if (info != null) {
//                                Log.e(TAG, "url:" + info.url);
//                                Log.e(TAG, "duration:" + info.duration);
//                            }
//                        }
//                    });
//            //get thumbnail for first frame
//            probeMediaInfoTools.getVideoThumbnailAtTime(mKSYRecordKit.getLastRecordedFiles(), 0, 0, 0);
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
                    updateRecordUI();
                    break;
                default:
                    Log.d(TAG, "OnInfo: " + what + " msg1: " + msg1 + " msg2: " + msg2);
                    break;
            }
        }
    };

    private void updateRecordUI() {
        mRecordView.setEnabled(true);
        //更新进度显示
        mRecordProgressCtl.stopRecording();
        mRecordView.getDrawable().setLevel(1);
        updateDeleteView();
        stopChronometer();
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
     * 曝光度调节
     */
    private void onExposureClick() {
        if (mExposureSeekBar.getVisibility() == View.VISIBLE) {
            mExposureSeekBar.setVisibility(View.GONE);
        } else {
            mExposureSeekBar.setVisibility(View.VISIBLE);
        }
    }

    private void onNoiseSuppresionClick() {
        if (mNoiseSeekBar.getVisibility() == View.VISIBLE) {
            mNoiseSeekBar.setVisibility(View.GONE);
        } else {
            mNoiseSeekBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * back按钮作为返回上一级和删除按钮
     * 当录制文件>=1时 作为删除按钮，否则作为返回上一级按钮
     * 作为删除按钮时，初次点击时先设置为待删除状态，在带删除状态下再执行文件回删
     */
    private void onBackoffClick() {
        if (mPreRecordConfigLayout != mDefaultRecordBottomLayout &&
                mPreRecordConfigLayout.getVisibility() == View.VISIBLE) {
            mPreRecordConfigLayout.setVisibility(View.INVISIBLE);
            mDefaultRecordBottomLayout.setVisibility(View.VISIBLE);
            mPreRecordConfigLayout = mDefaultRecordBottomLayout;
        } else {
            if (mKSYRecordKit.getRecordedFilesCount() >= 1) {
                if (!mBackView.isSelected()) {
                    mBackView.setSelected(true);
                    //设置最后一个文件为待删除文件
                    mRecordProgressCtl.setLastClipPending();
                } else {
                    mBackView.setSelected(false);
                    //删除文件时，若文件正在录制，则需要停止录制
                    if (mIsFileRecording) {
                        stopRecord(false);
                    }
                    //删除录制文件
                    mKSYRecordKit.deleteRecordFile(mKSYRecordKit.getLastRecordedFiles());
                    mRecordProgressCtl.rollback();
                    updateDeleteView();
                    mRecordView.setEnabled(true);
                    mRecordView.setClickable(true);
                }
            } else {
                mChronometer.stop();
                mIsFileRecording = false;
                RecordActivity.this.finish();
            }
        }
    }

    /**
     * 开始/停止录制
     */
    private void onRecordClick() {
        if (mIsFileRecording) {
            stopRecord(false);
        } else {
            startRecord();
        }
        //清除back按钮的状态
        clearBackoff();
    }

    /**
     * 进入编辑页面
     */
    private void onNextClick() {
        clearBackoff();
        clearRecordState();
        mRecordView.getDrawable().setLevel(1);
        //进行编辑前需要停止录制，并且结束断点拍摄
        stopRecord(true);
    }

    private VerticalSeekBar.OnSeekBarChangeListener getVerticalSeekListener() {
        VerticalSeekBar.OnSeekBarChangeListener listener = new VerticalSeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(VerticalSeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar == mExposureSeekBar) {
                    Camera.Parameters parameters = mKSYRecordKit.getCameraCapture().getCameraParameters();
                    if (parameters != null) {
                        int minValue = parameters.getMinExposureCompensation();
                        int maxValue = parameters.getMaxExposureCompensation();
                        int range = 0;
                        int value = minValue;
                        if (maxValue > minValue) {
                            range = 100 / (maxValue - minValue);
                            value = progress / range + minValue;
                        }

                        parameters.setExposureCompensation(value);
                    }
                    mKSYRecordKit.getCameraCapture().setCameraParameters(parameters);
                }
                if (seekBar == mNoiseSeekBar) {
                    if (progress == 0) {
                        mKSYRecordKit.setEnableAudioNS(false);
                    } else {
                        mKSYRecordKit.setEnableAudioNS(true);
                        int base = 100 / 4;
                        int range = progress / base;
                        if (range == 0) {
                            mKSYRecordKit.setAudioNSLevel(StreamerConstants.AUDIO_NS_LEVEL_0);
                        } else if (range == 1) {
                            mKSYRecordKit.setAudioNSLevel(StreamerConstants.AUDIO_NS_LEVEL_1);
                        } else if (range == 2) {
                            mKSYRecordKit.setAudioNSLevel(StreamerConstants.AUDIO_NS_LEVEL_2);
                        } else if (range >= 3) {
                            mKSYRecordKit.setAudioNSLevel(StreamerConstants.AUDIO_NS_LEVEL_3);
                        }
                    }

                }
            }

            @Override
            public void onStartTrackingTouch(VerticalSeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(VerticalSeekBar seekBar) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean enable) {

            }
        };
        return listener;
    }

    /**
     * 初始化背景音乐相关的UI界面
     */
    private void initBgmView() {
        mKSYRecordKit.setEnableAudioMix(true);  //支持混音
        mPitchMinus = (ImageView) findViewById(R.id.pitch_minus);
        mPitchMinus.setOnClickListener(mObserverButton);
        mPitchPlus = (ImageView) findViewById(R.id.pitch_plus);
        mPitchPlus.setOnClickListener(mObserverButton);
        mPitchText = (TextView) findViewById(R.id.pitch_text);
        mMicAudioVolumeSeekBar.setProgress((int) (mKSYRecordKit.getVoiceVolume() * 100));
        mBgmVolumeSeekBar.setProgress((int) (mKSYRecordKit.getAudioPlayerCapture().getVolume() * 100));
        setEnableBgmEdit(false);
        mSoundChange = (TextView) findViewById(R.id.bgm_title_soundChange);
        mSoundChangeIndicator = findViewById(R.id.bgm_title_soundChange_indicator);
        mReverb = (TextView) findViewById(R.id.bgm_title_reverberation);
        mReverbIndicator = findViewById(R.id.bgm_title_reverberation_indicator);
        mSoundChangeLayout = findViewById(R.id.soundEffect_change);
        mReverbLayout = findViewById(R.id.soundEffect_reverberation);
        BottomTitleViewInfo soundChangeInfo = new BottomTitleViewInfo(mSoundChange, mSoundChangeIndicator,
                mSoundChangeLayout, mObserverButton);
        soundChangeInfo.setChosenState(true);
        mRecordTitleArray.put(INDEX_BGM_TITLE_BASE, soundChangeInfo);
        BottomTitleViewInfo reverbInfo = new BottomTitleViewInfo(mReverb, mReverbIndicator,
                mReverbLayout, mObserverButton);
        mRecordTitleArray.put(INDEX_BGM_TITLE_BASE + 1, reverbInfo);
        List<BgmSelectAdapter.BgmData> list = DataFactory.getBgmData(getApplicationContext());
        mBgmAdapter = new BgmSelectAdapter(this, list);
        mBgmRecyclerView = (RecyclerView) findViewById(R.id.bgm_recycler);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mBgmRecyclerView.setLayoutManager(manager);
        setEnableBgmEdit(false);
        BgmSelectAdapter.OnItemClickListener listener = new BgmSelectAdapter.OnItemClickListener() {
            @Override
            public void onCancel() {
                setEnableBgmEdit(false);
                clearPitchState();
                mKSYRecordKit.stopBgm();
            }

            @Override
            public boolean onSelected(String path) {
                if (ViewUtils.isForeground(RecordActivity.this, RecordActivity.class.getName())) {
                    setEnableBgmEdit(true);
                    clearPitchState();
                    mKSYRecordKit.startBgm(path, true);
                    return true;
                }
                return false;
            }

            @Override
            public void onImport() {
                clearPitchState();
                importMusicFile();
            }
        };
        mBgmAdapter.setOnItemClickListener(listener);
        mBgmRecyclerView.setAdapter(mBgmAdapter);
    }

    private void initSoundEffectView() {
        mSoundChangeRecycler = (RecyclerView) findViewById(R.id.sound_change_recycler);
        mReverbRecycler = (RecyclerView) findViewById(R.id.reverb_recycler);
        LinearLayoutManager soundChangeManager = new LinearLayoutManager(this);
        soundChangeManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mSoundChangeRecycler.setLayoutManager(soundChangeManager);
        LinearLayoutManager reverbManager = new LinearLayoutManager(this);
        reverbManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mReverbRecycler.setLayoutManager(reverbManager);
        List<SoundEffectAdapter.SoundEffectData> soundChangeData
                = DataFactory.getSoundEffectData(getApplicationContext(), 0);
        SoundEffectAdapter.OnItemClickListener soundChangeListener = new SoundEffectAdapter.OnItemClickListener() {
            @Override
            public void onCancel() {
                mAudioEffectType = AUDIO_FILTER_DISABLE;
                addAudioFilter();
            }

            @Override
            public void onSelected(int index) {
                mAudioEffectType = SOUND_CHANGE_TYPE[index - 1];
                addAudioFilter();
            }
        };
        mSoundChangeAdapter = new SoundEffectAdapter(this, soundChangeData);
        mSoundChangeAdapter.setOnItemClickListener(soundChangeListener);
        List<SoundEffectAdapter.SoundEffectData> reverbData
                = DataFactory.getSoundEffectData(getApplicationContext(), 1);
        SoundEffectAdapter.OnItemClickListener reverbListener = new SoundEffectAdapter.OnItemClickListener() {
            @Override
            public void onCancel() {
                mAudioReverbType = AUDIO_FILTER_DISABLE;
                addAudioFilter();
            }

            @Override
            public void onSelected(int index) {
                mAudioReverbType = REVERB_TYPE[index - 1];
                addAudioFilter();
            }
        };
        mReverbAdapter = new SoundEffectAdapter(this, reverbData);
        mReverbAdapter.setOnItemClickListener(reverbListener);
        mSoundChangeRecycler.setAdapter(mSoundChangeAdapter);
        mReverbRecycler.setAdapter(mReverbAdapter);
    }

    private void addImgFilter() {
        ImgBeautyProFilter proFilter;
        ImgBeautySpecialEffectsFilter specialEffectsFilter;
        ImgTexFilter texFilter;
        List<ImgFilterBase> filters = new LinkedList<>();
        switch (mImgBeautyTypeIndex) {
            case BEAUTY_NATURE:
                ImgBeautySoftFilter softFilter = new ImgBeautySoftFilter(mKSYRecordKit.getGLRender());
                softFilter.setGrindRatio(0.5f);
                filters.add(softFilter);
                break;
            case BEAUTY_PRO:
                proFilter = new ImgBeautyProFilter(mKSYRecordKit.getGLRender(), getApplicationContext());
                proFilter.setGrindRatio(0.5f);
                proFilter.setWhitenRatio(0.5f);
                proFilter.setRuddyRatio(0);
                filters.add(proFilter);
                break;
            case BEAUTY_FLOWER_LIKE:
                proFilter = new ImgBeautyProFilter(mKSYRecordKit.getGLRender(), getApplicationContext(), 3);
                proFilter.setGrindRatio(0.5f);
                proFilter.setWhitenRatio(0.5f);
                proFilter.setRuddyRatio(0.15f);
                filters.add(proFilter);
                break;
            case BEAUTY_DELICATE:
                proFilter = new ImgBeautyProFilter(mKSYRecordKit.getGLRender(), getApplicationContext(), 3);
                proFilter.setGrindRatio(0.5f);
                proFilter.setWhitenRatio(0.5f);
                proFilter.setRuddyRatio(0.3f);
                filters.add(proFilter);
                break;
            case FILTER_DISABLE:
                break;
            default:
                break;
        }
        if (mFilterTypeIndex != -1 && mEffectFilterIndex != FILTER_DISABLE) {
            if (mFilterTypeIndex < 13) {
                specialEffectsFilter = new ImgBeautySpecialEffectsFilter(mKSYRecordKit.getGLRender(),
                        getApplicationContext(), mEffectFilterIndex);
                filters.add(specialEffectsFilter);
            } else {
                texFilter = new ImgBeautyStylizeFilter(mKSYRecordKit.getGLRender(), getApplicationContext(),
                        mEffectFilterIndex);
                filters.add(texFilter);
            }
        }
        if (filters.size() > 0) {
            mKSYRecordKit.getImgTexFilterMgt().setFilter(filters);
        } else {
            mKSYRecordKit.getImgTexFilterMgt().setFilter((ImgTexFilterBase) null);
        }
    }

    private void setEffectFilter(int type) {
        mEffectFilterIndex = type;
        addImgFilter();
    }

    /**
     * 美颜设置
     */
    private void initBeautyUI() {
        final int[] BEAUTY_TYPE = {BEAUTY_NATURE, BEAUTY_PRO, BEAUTY_FLOWER_LIKE, BEAUTY_DELICATE};
        mBeautyOriginalView = (ImageView) findViewById(R.id.iv_beauty_origin);
        mBeautyBorder = (ImageView) findViewById(R.id.iv_beauty_border);
        mBeautyOriginalText = (TextView) findViewById(R.id.tv_beauty_origin);
        mBeautyRecyclerView = (RecyclerView) findViewById(R.id.beauty_recyclerView);
        changeOriginalBeautyState(true);
        List<ImageTextAdapter.Data> beautyData = DataFactory.getBeautyTypeDate(this);
        final ImageTextAdapter beautyAdapter = new ImageTextAdapter(this, beautyData);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mBeautyRecyclerView.setLayoutManager(layoutManager);
        ImageTextAdapter.OnImageItemClickListener listener = new ImageTextAdapter.OnImageItemClickListener() {
            @Override
            public void onClick(int index) {
                if (mBeautyOriginalText.isActivated()) {
                    changeOriginalBeautyState(false);
                }
                mImgBeautyTypeIndex = BEAUTY_TYPE[index];
                addImgFilter();
            }
        };
        mBeautyOriginalView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beautyAdapter.clear();
                changeOriginalBeautyState(true);
                mImgBeautyTypeIndex = BEAUTY_DISABLE;
                addImgFilter();
            }
        });
        beautyAdapter.setOnImageItemClick(listener);
        mBeautyRecyclerView.setAdapter(beautyAdapter);
    }

    private void changeOriginalBeautyState(boolean isSelected) {
        if (isSelected) {
            mBeautyBorder.setVisibility(View.VISIBLE);
            mBeautyOriginalText.setActivated(true);
        } else {
            mBeautyBorder.setVisibility(View.INVISIBLE);
            mBeautyOriginalText.setActivated(false);
        }
    }

    /**
     * 视频滤镜
     * https://github.com/ksvc/KSYStreamer_Android/wiki/style_filter
     */
    private void initFilterUI() {

        final int[] FILTER_TYPE = {ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_FRESHY,
                ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_BEAUTY,
                ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_SWEETY,
                ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_SEPIA,
                ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_BLUE,
                ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_NOSTALGIA,
                ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_SAKURA,
                ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_SAKURA_NIGHT,
                ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_RUDDY_NIGHT,
                ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_SUNSHINE_NIGHT,
                ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_RUDDY,
                ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_SUSHINE,
                ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_NATURE,
                ImgBeautyStylizeFilter.KSY_FILTER_STYLE_AMARO,
                ImgBeautyStylizeFilter.KSY_FILTER_STYLE_BRANNAN,
                ImgBeautyStylizeFilter.KSY_FILTER_STYLE_EARLY_BIRD,
                ImgBeautyStylizeFilter.KSY_FILTER_STYLE_HUDSON,
                ImgBeautyStylizeFilter.KSY_FILTER_STYLE_LOMO,
                ImgBeautyStylizeFilter.KSY_FILTER_STYLE_NASHVILLE,
                ImgBeautyStylizeFilter.KSY_FILTER_STYLE_RISE,
                ImgBeautyStylizeFilter.KSY_FILTER_STYLE_TOASTER,
                ImgBeautyStylizeFilter.KSY_FILTER_STYLE_VALENCIA,
                ImgBeautyStylizeFilter.KSY_FILTER_STYLE_WALDEN,
                ImgBeautyStylizeFilter.KSY_FILTER_STYLE_XPROLL};
        List<ImageTextAdapter.Data> filterData = DataFactory.getImgFilterData(this);
        mFilterOriginImage = (ImageView) findViewById(R.id.iv_filter_origin);
        mFilterBorder = (ImageView) findViewById(R.id.iv_filter_border);
        mFilterOriginText = (TextView) findViewById(R.id.tv_filter_origin);
        changeOriginalImageState(true);
        mFilterRecyclerView = (RecyclerView) findViewById(R.id.filter_recyclerView);
        final ImageTextAdapter adapter = new ImageTextAdapter(this, filterData);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mFilterRecyclerView.setLayoutManager(layoutManager);
        ImageTextAdapter.OnImageItemClickListener listener = new ImageTextAdapter.OnImageItemClickListener() {
            @Override
            public void onClick(int index) {
                if (mFilterOriginText.isActivated()) {
                    changeOriginalImageState(false);
                }
                mFilterTypeIndex = index;
                setEffectFilter(FILTER_TYPE[index]);
            }
        };
        mFilterOriginImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setEffectFilter(FILTER_DISABLE);
                adapter.clear();
                changeOriginalImageState(true);
            }
        });
        adapter.setOnImageItemClick(listener);
        mFilterRecyclerView.setAdapter(adapter);
    }

    public void changeOriginalImageState(boolean isSelected) {
        if (isSelected) {
            mFilterOriginText.setActivated(true);
            mFilterBorder.setVisibility(View.VISIBLE);
        } else {
            mFilterOriginText.setActivated(false);
            mFilterBorder.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * 添加音频滤镜，支持变声和混响同时生效
     * https://github.com/ksvc/KSYStreamer_Android/wiki/Audio_Filter
     */
    private void addAudioFilter() {
        KSYAudioEffectFilter effectFilter;
        AudioReverbFilter reverbFilter;
        List<AudioFilterBase> filters = new LinkedList<>();
        if (mAudioEffectType != AUDIO_FILTER_DISABLE) {
            effectFilter = new KSYAudioEffectFilter
                    (mAudioEffectType);
            filters.add(effectFilter);
        }
        if (mAudioReverbType != AUDIO_FILTER_DISABLE) {
            reverbFilter = new AudioReverbFilter();
            reverbFilter.setReverbLevel(mAudioReverbType);
            filters.add(reverbFilter);
        }
        if (filters.size() > 0) {
            mKSYRecordKit.getAudioFilterMgt().setFilter(filters);
        } else {
            mKSYRecordKit.getAudioFilterMgt().setFilter((AudioFilterBase) null);
        }
    }

    /**
     * 重置录制状态
     */
    private void clearRecordState() {
        mKSYRecordKit.stopBgm();
        mKSYRecordKit.getImgTexFilterMgt().setFilter((ImgFilterBase) null);
        mImgBeautyTypeIndex = BEAUTY_DISABLE;
        mEffectFilterIndex = FILTER_DISABLE;
        mFilterOriginImage.callOnClick();
        mKSYRecordKit.getImgTexFilterMgt().setExtraFilter((ImgFilterBase) null);
        if (mKMCAdapter != null) {
            mKMCAdapter.setSelectIndex(-1);
        }
        addImgFilter();
        if (mBgmAdapter != null) {
            mBgmAdapter.clear();
        }
        if (mSoundChangeAdapter != null) {
            mSoundChangeAdapter.clear();
        }
        if (mReverbAdapter != null) {
            mReverbAdapter.clear();
        }

        setEnableBgmEdit(false);
        mAudioEffectType = AUDIO_FILTER_DISABLE;
        mAudioReverbType = AUDIO_FILTER_DISABLE;
        addAudioFilter();
        clearPitchState();
    }

    /**
     * 根据是否有背景音乐选中来设置相应的编辑控件是否可用
     */
    private void setEnableBgmEdit(boolean enable) {
        if (mPitchMinus != null) {
            mPitchMinus.setEnabled(enable);
        }
        if (mPitchPlus != null) {
            mPitchPlus.setEnabled(enable);
        }
        if (mBgmVolumeSeekBar != null) {
            mBgmVolumeSeekBar.setEnabled(enable);
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
                case R.id.exposure:
                    onExposureClick();
                    break;
                case R.id.noise_suppression:
                    onNoiseSuppresionClick();
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
                case R.id.record_beauty:
                    onBeautyClick();
                    break;
                case R.id.record_bgm:
                    onBgmClick();
                    break;
                case R.id.record_sound_effect:
                    onSoundEffectClick();
                    break;
                case R.id.record_watermark:
                    onWaterMarkClick();
                    break;
                case R.id.item_beauty:
                    onBeautyTitleClick(0);
                    break;
                case R.id.item_dyn_sticker:
                    onBeautyTitleClick(1);
                    break;
                case R.id.item_filter:
                    onBeautyTitleClick(2);
                    break;
                case R.id.pitch_minus:
                    onPitchClick(-1);
                    break;
                case R.id.pitch_plus:
                    onPitchClick(1);
                    break;
                case R.id.bgm_title_soundChange:
                    onBgmTitleClick(0);
                    break;
                case R.id.bgm_title_reverberation:
                    onBgmTitleClick(1);
                default:
                    break;
            }
        }
    }

    /**
     * 清除音调状态，重置为'0'
     */
    private void clearPitchState() {
        mPitchValue = 0;
        mPitchText.setText("0");
        mKSYRecordKit.getBGMAudioFilterMgt().setFilter((AudioFilterBase) null);
    }

    /**
     * 背景音乐音调加减事件处理
     */
    private void onPitchClick(int sign) {
        if (sign < 0) {
            if (mPitchValue > -3) {
                mPitchValue--;
            }
        } else {
            if (mPitchValue < 3) {
                mPitchValue++;
            }
        }
        mPitchText.setText(mPitchValue + "");
        KSYAudioEffectFilter audioFilter = new KSYAudioEffectFilter(KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_PITCH);
        audioFilter.setPitchLevel(mPitchValue);
        mKSYRecordKit.getBGMAudioFilterMgt().setFilter(audioFilter);
    }

    private void onFrontMirrorChecked(boolean isChecked) {
        mKSYRecordKit.setFrontCameraMirror(isChecked);
    }

    private void onBeautyClick() {
        if (!(mRecordConfig.isLandscape && mPreRecordConfigLayout == mDefaultRecordBottomLayout)) {
            mPreRecordConfigLayout.setVisibility(View.INVISIBLE);
        }
        if (mRecordConfig.isLandscape) {
            mBeautyLayout.setBackgroundResource(R.drawable.beauty_bg);
        }
        mPreRecordConfigLayout = mBeautyLayout;
        if (mBeautyLayout.getVisibility() != View.VISIBLE) {
            mBeautyLayout.setVisibility(View.VISIBLE);
        }
    }

    private void onBgmClick() {
        if (!(mRecordConfig.isLandscape && mPreRecordConfigLayout == mDefaultRecordBottomLayout)) {
            mPreRecordConfigLayout.setVisibility(View.INVISIBLE);
        }
        if (mRecordConfig.isLandscape) {
            mBgmLayout.setBackgroundResource(R.drawable.music_bg);
        }
        mPreRecordConfigLayout = mBgmLayout;
        if (mBgmLayout.getVisibility() != View.VISIBLE) {
            mBgmLayout.setVisibility(View.VISIBLE);
        }
    }

    private void onSoundEffectClick() {
        if (!(mRecordConfig.isLandscape && mPreRecordConfigLayout == mDefaultRecordBottomLayout)) {
            mPreRecordConfigLayout.setVisibility(View.INVISIBLE);
        }
        if (mRecordConfig.isLandscape) {
            mSoundEffectLayout.setBackgroundResource(R.drawable.sound_effect_bg);
        }
        mPreRecordConfigLayout = mSoundEffectLayout;
        if (mSoundEffectLayout.getVisibility() != View.VISIBLE) {
            mSoundEffectLayout.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 美颜标题点击事件处理
     */
    private void onBeautyTitleClick(int index) {
        if (index == 1) {
            if (mRecordConfig.isLandscape) {
                Toast.makeText(RecordActivity.this, "横屏录制目前不支持动态特效",Toast.LENGTH_SHORT).show();
                return;
            } else {
                updateKMCFilterView();
            }
        }
        BottomTitleViewInfo curInfo = mRecordTitleArray.get(INDEX_BEAUTY_TITLE_BASE + index);
        BottomTitleViewInfo preInfo = mRecordTitleArray.get(INDEX_BEAUTY_TITLE_BASE + mPreBeautyTitleIndex);
        if (index != mPreBeautyTitleIndex) {
            curInfo.setChosenState(true);
            preInfo.setChosenState(false);
            mPreBeautyTitleIndex = index;
        }
    }

    /**
     * 背景音乐标题的点击事件处理
     */
    private void onBgmTitleClick(int index) {
        BottomTitleViewInfo curInfo = mRecordTitleArray.get(INDEX_BGM_TITLE_BASE + index);
        BottomTitleViewInfo preInfo = mRecordTitleArray.get(INDEX_BGM_TITLE_BASE + mPreBgmTitleIndex);
        if (index != mPreBgmTitleIndex) {
            curInfo.setChosenState(true);  //打开选中的音效界面
            preInfo.setChosenState(false);  //隐藏上次打开的音效界面
            mPreBgmTitleIndex = index;
        }
    }

    /**
     * 打开系统文件夹，导入音频文件作为背景音乐
     */
    private void importMusicFile() {
        Intent target = FileUtils.createGetContentIntent();
        Intent intent = Intent.createChooser(target, "ksy_import_music_file");
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 选中本地背景音乐后返回结果处理
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        Log.i(TAG, "Uri = " + uri.toString());
                        try {
                            // Get the file path from the URI
                            final String path = FileUtils.getPath(this, uri);
                            mKSYRecordKit.startBgm(path, true);
                            setEnableBgmEdit(true);
                        } catch (Exception e) {
                            Log.e(TAG, "File select error:" + e);
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
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

    /**
     * 开始拍摄更新，删除按钮状态
     */
    private void updateDeleteView() {
        if (mKSYRecordKit.getRecordedFilesCount() >= 1) {
            mBackView.getDrawable().setLevel(2);
        } else {
            mBackView.getDrawable().setLevel(1);
        }
    }

    /**
     * 清除back按钮的状态（删除），并设置最后一个录制的文件为普通文件
     *
     * @return
     */
    private boolean clearBackoff() {
        if (mBackView.isSelected()) {
            mBackView.setSelected(false);
            //设置最后一个文件为普通文件
            mRecordProgressCtl.setLastClipNormal();
            return true;
        }
        return false;
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
                        //超过最短时长显示下一步按钮，否则不能进入编辑，最短时长可自行设定，Demo中当前设定为5s
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
                            //到达最大拍摄时长时，需要主动停止录制
                            stopRecord(false);
                            mRecordView.getDrawable().setLevel(1);
                            mRecordView.setClickable(false);
                            mRecordView.setEnabled(false);
                            Toast.makeText(RecordActivity.this, "录制结束，请继续操作",
                                    Toast.LENGTH_SHORT).show();
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
            setContentView(R.layout.merge_record_files_layout);
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

    /**
     * 封装一个TextView和View，TextView用来显示标题，View代表相应的布局
     * 提供setChosenState()方法用来统一设置选中（非选中）下各组件的状态
     */
    public class BottomTitleViewInfo {
        private TextView titleView;
        private View indicator;
        private View relativeLayout;

        public BottomTitleViewInfo(TextView tv, View indicator, View layout,
                                   View.OnClickListener onClickListener) {
            this.titleView = tv;
            this.indicator = indicator;
            this.relativeLayout = layout;
            if (titleView != null) {
                titleView.setOnClickListener(onClickListener);
            }
        }

        public void setChosenState(boolean isChosen) {
            if (isChosen) {
                relativeLayout.setVisibility(View.VISIBLE);
                titleView.setActivated(true);
                if (indicator != null) {
                    indicator.setActivated(true);
                }
            } else {
                relativeLayout.setVisibility(View.GONE);
                titleView.setActivated(false);
                if (indicator != null) {
                    indicator.setActivated(false);
                }
            }
        }
    }

    /**************************魔方动态贴纸集成 begin**************************************/
    private void initStickerUI() {
        mKMCRecyclerView = (RecyclerView) findViewById(R.id.kmc_recycler_view);
        //创建默认的线性LayoutManager
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mKMCRecyclerView.setLayoutManager(mLayoutManager);
        mKMCRecyclerView.addItemDecoration(new SpacesItemDecoration(10));
        //如果可以确定每个item的高度是固定的，设置这个选项可以提高性能
        mKMCRecyclerView.setHasFixedSize(true);
        //view state
        mKMCRecyclerView.setVisibility(View.INVISIBLE);
        mKMCInitThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //魔方贴纸鉴权
                doKMCAuth();
            }
        });
        mKMCInitThread.start();
    }

    private void doKMCAuth() {
        String token = "c60229160e84909f2bfb15705f623988";
        KMCAuthManager.getInstance().authorize(getApplicationContext(),
                token, mCheckAuthResultListener);
    }

    private KMCAuthManager.AuthResultListener mCheckAuthResultListener = new KMCAuthManager
            .AuthResultListener() {
        @Override
        public void onSuccess() {
            mKMCAuthorized = true;

            KMCAuthManager.getInstance().removeAuthResultListener(mCheckAuthResultListener);
            makeToast("鉴权成功，可以使用魔方贴纸功能");
        }

        @Override
        public void onFailure(int errCode) {
            KMCAuthManager.getInstance().removeAuthResultListener(mCheckAuthResultListener);
            makeToast("鉴权失败! 错误码: " + errCode);
        }
    };

    protected Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {

                case MSG_GET_LIST_SIZE:
                    initMaterialTabTypeView();
                    break;
                case MSG_LOAD_THUMB:
                    mKMCAdapter.setItemState(msg.arg2,
                            RecyclerViewAdapter.STATE_DOWNLOADTHUMBNAIL);
                    updateListView(msg.arg2);
                    mKMCAdapter.notifyDataSetChanged();

                    break;
                case MSG_DOWNLOAD_SUCCESS:
                    mKMCAdapter.setItemState(msg.arg1,
                            RecyclerViewAdapter.STATE_DOWNLOADED);
                    updateListView(msg.arg1);
                    mKMCAdapter.notifyDataSetChanged();
                    break;
                case MSG_START_DOWNLOAD:
                    mKMCAdapter.setItemState(msg.arg1,
                            RecyclerViewAdapter.STATE_DOWNLOADING);
                    updateListView(msg.arg1);
                    mKMCAdapter.notifyDataSetChanged();
                    break;
                default:
                    Log.e(TAG, "Invalid message");
                    break;
            }
        }
    };

    private void saveSelectedIndex(int position) {
        mMaterialIndex = position;
    }

    private void updateListView(int position) {
        mKMCAdapter.updateItemView(position);
    }

    /**
     * 单独下载贴纸素材的回调对象
     */
    private KMCFilterManager.DownloadMaterialListener mDownloadListener = new KMCFilterManager.DownloadMaterialListener() {
        /**
         * 下载成功
         * @param material 下载成功的素材
         */
        @Override
        public void onSuccess(KMCArMaterial material) {
            int position = 0;

            for (int j = 0; j < mMaterialList.size(); j++) {
                String stickerid = mMaterialList.get(j).material.id;
                if (stickerid != null && stickerid.equals(material.id)) {
                    position = j;
                    mMaterialList.get(j).setHasDownload(true);
                }
            }
            Log.d(TAG, "download success for position " + position);
            mHandler.sendMessage(mHandler.obtainMessage(MSG_DOWNLOAD_SUCCESS, position, 0));

        }

        /**
         * 下载失败
         * @param material 下载失败的素材
         * @param code 失败原因的错误代码
         * @param message 失败原因的解释
         */
        @Override
        public void onFailure(KMCArMaterial material, int code, String message) {
            if (code == Constants.AUTH_EXPIRED) {
                makeToast("鉴权信息过期，请重新鉴权!");
                doKMCAuth();
            }
            mMaterial = null;

        }

        /**
         * 下载过程中的进度回调
         * @param material  正在下载素材
         * @param progress 当前下载的进度
         * @param size 已经下载素材的大小, 单位byte
         */
        @Override
        public void onProgress(KMCArMaterial material, float progress, int size) {
        }
    };

    private void makeToast(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(RecordActivity.this, str,
                        Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });
    }

    /**
     * 获取贴纸列表
     */
    protected void startGetMaterialList() {
        if (mMaterialList != null &&
                mMaterialList.size() > 1) {
            return;
        }
        mMaterialList = new ArrayList<>();

        if (mNullBitmap == null) {
            mNullBitmap = getNullEffectBitmap();
        }
        MaterialInfoItem nullSticker = new MaterialInfoItem(new KMCArMaterial(), mNullBitmap);
        nullSticker.setHasDownload(true);
        mMaterialList.add(nullSticker);

        fetchMaterial("SE_LIST");
    }

    private Bitmap getNullEffectBitmap() {
        mNullBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.close);
        return mNullBitmap;
    }

    private void initMaterialTabTypeView() {
        updateKMCFilterView();
    }

    private void showMaterialLists() {
        if (mKMCAuthorized &&
                mIsFirstFetchMaterialList) {
            startGetMaterialList();
            mIsFirstFetchMaterialList = false;
        }
        mKMCRecyclerView.setVisibility(View.VISIBLE);
    }

    private void closeMaterialsShowLayer() {
        if (mKMCAdapter != null) {
            mKMCAdapter.notifyDataSetChanged();
        }
        mKMCRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void updateKMCFilterView() {
        if (!mKMCAuthorized) {
            return;
        }

        showMaterialLists();

        initMaterialsRecyclerView();
        mKMCAdapter.setSelectIndex(mMaterialIndex);
    }

    private void initMaterialsRecyclerView() {
        if (mMaterialList == null) {
            Log.e(TAG, "The material list is null");
            return;
        }

        mKMCAdapter = new RecyclerViewAdapter(mMaterialList);
        mKMCAdapter.setRecyclerView(mKMCRecyclerView);
        mKMCRecyclerView.setAdapter(mKMCAdapter);

        mKMCAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnRecyclerViewListener() {
            @Override
            public boolean onItemLongClick(int position) {
                onRecyclerViewItemClick(position);
                return false;
            }

            @Override
            public void onItemClick(int position) {
                onRecyclerViewItemClick(position);
            }
        });
    }

    private void onRecyclerViewItemClick(int position) {
        MaterialInfoItem materialInfoItem = mMaterialList.get(position);

        if (position == 0) {
            mMaterial = null;
            mKMCAdapter.setSelectIndex(position);
            saveSelectedIndex(position);
            mKMCAdapter.notifyDataSetChanged();
            if (mKMCFilter != null) {
                mKMCFilter.startShowingMaterial(null);
            }
            //closeMaterialsShowLayer();
            return;
        }

        mMaterial = materialInfoItem.material;

        if (KMCFilterManager.getInstance().isMaterialDownloaded(getApplicationContext(), materialInfoItem.material)) {
            //closeMaterialsShowLayer();

            //action id !=0 为动作贴纸
            if (mMaterial.actionId != 0) {
                makeToast(materialInfoItem.material.actionTip);
            }
            mKMCFilter = new KMCFilter(getApplicationContext(),
                    mKSYRecordKit.getGLRender());
            mKSYRecordKit.getImgTexFilterMgt().setExtraFilter(mKMCFilter);
            mKMCFilter.startShowingMaterial(mMaterial);
            if (mKMCAdapter.getItemState(position) != MSG_DOWNLOAD_SUCCESS) {
                mHandler.sendMessage(mHandler.obtainMessage(MSG_DOWNLOAD_SUCCESS, position, 0));
            }

            saveSelectedIndex(position);
            mKMCAdapter.setSelectIndex(position);
        } else {
            mHandler.sendMessage(mHandler.obtainMessage(MSG_START_DOWNLOAD, position, 0));
            KMCFilterManager.getInstance().
                    downloadMaterial(getApplicationContext(), materialInfoItem.material, mDownloadListener);
        }
    }

    protected void reportError(final String info) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RecordActivity.this, info, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchMaterial(String groupID) {
        // 从AR服务器获取贴纸列表, 并保存其信息
        KMCFilterManager.getInstance().fetchMaterials(getApplicationContext(),
                groupID, new KMCFilterManager.FetchMaterialListener() {
                    @Override
                    public void onSuccess(List<KMCArMaterial> list) {
                        List<KMCArMaterial> adlist = list;
                        for (int i = 0; i < adlist.size(); i++) {
                            KMCArMaterial material = adlist.get(i);
                            MaterialInfoItem infoItem = new MaterialInfoItem(material, null);

                            if (KMCFilterManager.getInstance().isMaterialDownloaded(getApplicationContext(),
                                    material)) {
                                infoItem.setHasDownload(true);
                            } else {
                                infoItem.setHasDownload(false);
                            }
                            mMaterialList.add(infoItem);

                            Message msg = mHandler.obtainMessage(MSG_GET_LIST_SIZE);
                            mHandler.sendMessage(msg);
                        }


                        for (int i = 1; i < mMaterialList.size(); i++) {
                            MaterialInfoItem infoItem = mMaterialList.get(i);

                            String thumbnailurlStr = infoItem.material.thumbnailURL;
                            Bitmap thumbnail = null;
                            try {
                                thumbnail = ApiHttpUrlConnection.getImageBitmap(thumbnailurlStr);
                            } catch (Exception e) {
                                thumbnail = BitmapFactory.decodeResource(getResources(), R.drawable.love);
                                reportError("get material thumbnail failed");
                            }
                            infoItem.thumbnail = thumbnail;
                            mMaterialList.set(i, infoItem);

                            Message msg = mHandler.obtainMessage(MSG_LOAD_THUMB);
                            msg.arg2 = i + 1;
                            mHandler.sendMessage(msg);
                        }
                    }

                    @Override
                    public void onFailure(int erroCode, String msg) {
                        if (erroCode == Constants.AUTH_EXPIRED) {
                            makeToast("鉴权信息过期，请重新鉴权!");
                            doKMCAuth();
                        }
                        reportError("fetch material list failed");
                    }
                });
    }
    /**************************魔方动态贴纸集成 end**************************************/
}