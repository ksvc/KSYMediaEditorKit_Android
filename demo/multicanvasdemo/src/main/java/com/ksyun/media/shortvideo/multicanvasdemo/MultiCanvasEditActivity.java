package com.ksyun.media.shortvideo.multicanvasdemo;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.shortvideo.kit.KSYEditKit;
import com.ksyun.media.shortvideo.kit.KSYMultiCanvasComposeKit;
import com.ksyun.media.shortvideo.multicanvasdemo.adapter.ImageTextAdapter;
import com.ksyun.media.shortvideo.multicanvasdemo.data.MultiCanvasInfo;
import com.ksyun.media.shortvideo.multicanvasdemo.util.DataFactory;
import com.ksyun.media.shortvideo.multicanvasdemo.util.DensityUtil;
import com.ksyun.media.shortvideo.multicanvasdemo.util.SystemStateObtainUtil;
import com.ksyun.media.shortvideo.multicanvasdemo.view.CanvasViewBase;
import com.ksyun.media.shortvideo.utils.ShortVideoConstants;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautyProFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautySoftFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautySpecialEffectsFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautyStylizeFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgFilterBase;
import com.ksyun.media.streamer.filter.imgtex.ImgTexFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgTexFilterBase;
import com.ksyun.media.streamer.framework.AVConst;
import com.ksyun.media.streamer.framework.VideoCodecFormat;
import com.ksyun.media.streamer.kit.StreamerConstants;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

/**
 * demo for model edit
 */

public class MultiCanvasEditActivity extends Activity {
    private static String TAG = "MultiCanvasEditActivity";
    private static final int FILTER_DISABLE = 0;

    private RelativeLayout mPreviewLayout;  //录制预览区域layout，包括正在录制的预览画面和之前录制好的文件的播放画面
    private View mBottomLayout;
    private View mTopLayout;

    private ImageView mAudioVolume;
    private ImageView mBeauty;
    private ImageView mFilter;
    private ImageView mBack;
    private ImageView mPauseView;
    private ImageView mCompose;
    private AppCompatSeekBar mEditPreviewSeekBar;
    private AppCompatSeekBar mLeftVolumeSeekBar;
    private AppCompatSeekBar mRightVolumeSeekBar;
    private TextView mCurrentPreviewTime;
    private TextView mPreviewDurationTime;

    private View mBaseControlLayout; //控制区域的基础显示区域
    private View mBeautyLayout;  //美颜
    private View mFilterLayout; //滤镜
    private View mAudioVolumeLayout; //音量调整
    private View mPreviewEditLayout;
    private View mEditControlLayout;

    //滤镜
    private ImageView mFilterOriginImage;
    private ImageView mFilterBorder;
    private TextView mFilterOriginText;
    private RecyclerView mFilterRecyclerView;
    private int mFilterTypeIndex = -1;

    //美颜
    private static final int BEAUTY_DISABLE = 100;
    private static final int BEAUTY_NATURE = 101;
    private static final int BEAUTY_PRO = 102;
    private static final int BEAUTY_FLOWER_LIKE = 103;
    private static final int BEAUTY_DELICATE = 104;
    private ImageView mBeautyOriginalView;
    private ImageView mBeautyBorder;
    private TextView mBeautyOriginalText;
    private RecyclerView mBeautyRecyclerView;

    private int mImgBeautyTypeIndex = BEAUTY_DISABLE;  //美颜type
    private int mEffectFilterIndex = FILTER_DISABLE;  //滤镜filter type
    private ImgFilterBase mComposeBeautyFilter;
    private ImgFilterBase mComposeEffectFilter;

    private GLSurfaceView mEditPreviewView;
    private SurfaceView mPlayerPreview;
    private SurfaceHolder mSurfaceHolder;
    private Timer mEditPreviewProgressTimer;
    private volatile boolean mIsPreviewSeekBarChanging;//互斥变量，防止进度条与定时器冲突。

    private Dialog mConfigDialog;
    private ComposeDialog mComposeDialog;
    private ShortVideoConfig mComposeConfig; //输出视频参数配置
    private ButtonObserver mButtonObserver;
    private SeekBarChangedObserver mSeekBarChangedObserver;

    public final static String EDIT_URL = "edit_url";
    public final static String PLAYER_URL = "player_url";
    public final static String MODEL_PATH = "model_path";
    public final static String MODEL_LAYOUT_ID = "model_layout_id";
    public final static String EDIT_MODEL_INFO = "model_info";
    public final static String MODEL_FINISH = "model_finish";

    private KSYEditKit mEditKit; //编辑kit类
    private KSYMultiCanvasComposeKit mComposeKit; //合成kit类
    private KSYMediaPlayer mMediaPlayer;  //用于预览播放

    private String mEditUrl;   //编辑文件路径
    private String mPlayerUrl; //预览播放文件路径
    private String mModelPath;  //模版图片路径
    private MultiCanvasInfo mEditModelPos;  //模版位置信息
    private boolean mModelFinished;  //模版文件录制是否结束，如果结束后合成结束后启动预览模式

    private boolean mComposeFinished = false;
    private int mScreenHeight;
    private int mScreenWidth;
    private int mPreviewWidth;
    private int mPreviewHeight;

    /*******编辑后合成参数配置示例******/
    private TextView mOutRes720p;
    private TextView mOutRes1080p;
    private TextView mOutEncodeWithH264;
    private TextView mOutEncodeWithH265;
    private TextView mOutEncodeByHW;
    private TextView mOutEncodeBySW;
    private TextView mOutDecodeByHW;
    private TextView mOutDecodeBySW;
    private TextView mOutForMP4;
    private TextView[] mOutProfileGroup;
    private TextView[] mOutAudioProfileGroup;
    private EditText mOutFrameRate;
    private EditText mOutVideoBitrate;
    private EditText mOutAudioBitrate;
    private EditText mOutVideoCRF;
    private TextView mOutputConfirm;
    private MultiCanvasEditActivity.ComposeConfigButtonObserver mComposeConfigButtonObserver;

    private static final int[] OUTPUT_PROFILE_ID = {R.id.output_config_low_power,
            R.id.output_config_balance, R.id.output_config_high_performance};
    private static final int[] ENCODE_PROFILE_TYPE = {VideoCodecFormat.ENCODE_PROFILE_LOW_POWER,
            VideoCodecFormat.ENCODE_PROFILE_BALANCE, VideoCodecFormat.ENCODE_PROFILE_HIGH_PERFORMANCE};
    private static final int[] AUDIO_OUTPUT_PROFILE_ID = {R.id.output_config_aac_lc,
            R.id.output_config_aac_he, R.id.output_config_aac_he_v2};
    private static final int[] AUDIO_ENCODE_PROFILE = {AVConst.PROFILE_AAC_LOW, AVConst.PROFILE_AAC_HE,
            AVConst.PROFILE_AAC_HE_V2};

    private Handler mMainHandler;

    public static void startActivity(Activity context, String editUrl, String playerUrl, String modelPath,
                                     int model_layout_id, MultiCanvasInfo edit_model_info, boolean isFinished) {
        Intent intent = new Intent(context, MultiCanvasEditActivity.class);

        intent.putExtra(EDIT_URL, editUrl);
        intent.putExtra(PLAYER_URL, playerUrl);
        intent.putExtra(MODEL_LAYOUT_ID, model_layout_id);
        intent.putExtra(EDIT_MODEL_INFO, edit_model_info);
        intent.putExtra(MODEL_PATH, modelPath);
        intent.putExtra(MODEL_FINISH, isFinished);

        context.startActivityForResult(intent, MultiCanvasRecordActivity.EDIT_REQUEST_CODE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //只做竖屏模型
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.edit_activity);

        //must set
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //init UI
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;

        mButtonObserver = new MultiCanvasEditActivity.ButtonObserver();
        mSeekBarChangedObserver = new MultiCanvasEditActivity.SeekBarChangedObserver();

        mPreviewLayout = findViewById(R.id.edit_preview_layout);
        mEditPreviewView = findViewById(R.id.edit_preview);
        mPlayerPreview = findViewById(R.id.player_preview);
        mBottomLayout = findViewById(R.id.bar_bottom);
        mTopLayout = findViewById(R.id.actionbar);
        mBack = findViewById(R.id.click_to_back);
        mBack.setOnClickListener(mButtonObserver);
        mCompose = findViewById(R.id.start_compose);
        mCompose.setOnClickListener(mButtonObserver);
        mAudioVolume = findViewById(R.id.audio_channel_volume);
        mAudioVolume.setOnClickListener(mButtonObserver);
        mBeauty = findViewById(R.id.beauty);
        mBeauty.setOnClickListener(mButtonObserver);
        mFilter = findViewById(R.id.filter);
        mFilter.setOnClickListener(mButtonObserver);
        mPauseView = findViewById(R.id.play_pause);
        mPauseView.setOnClickListener(mButtonObserver);
        mPauseView.getDrawable().setLevel(2);
        mEditControlLayout = findViewById(R.id.edit_control);
        mBaseControlLayout = findViewById(R.id.edit_base_control);
        mBeautyLayout = findViewById(R.id.edit_beauty_choose);
        mFilterLayout = findViewById(R.id.edit_filter_choose);
        mAudioVolumeLayout = findViewById(R.id.edit_audio_volume);
        mCurrentPreviewTime = findViewById(R.id.current_preview_time);
        mPreviewDurationTime = findViewById(R.id.preview_duration_time);
        mLeftVolumeSeekBar = findViewById(R.id.left_audio_volume);
        mLeftVolumeSeekBar.setOnSeekBarChangeListener(mSeekBarChangedObserver);
        mRightVolumeSeekBar = findViewById(R.id.right_audio_volume);
        mRightVolumeSeekBar.setOnSeekBarChangeListener(mSeekBarChangedObserver);
        mEditPreviewSeekBar = findViewById(R.id.edit_preview_SeekBar);
        mEditPreviewSeekBar.setOnSeekBarChangeListener(mSeekBarChangedObserver);

        initBeautyUI();
        initFilterUI();  //初始化滤镜界面

        //按照1:1的比例设置预览区域,而模版的提供建议按照1080*1920来计算出1:1的模版，此比例可由上层设置
        //模版作为预览区域的背景图显示
        //预览区域size
        mPreviewWidth = mScreenWidth;
        mPreviewHeight = mScreenWidth;
        RelativeLayout.LayoutParams previewParams = (RelativeLayout.LayoutParams) mPreviewLayout
                .getLayoutParams();
        previewParams.height = mPreviewHeight;
        previewParams.width = mPreviewWidth;
        mPreviewLayout.setLayoutParams(previewParams);
        //顶层工具栏size
        RelativeLayout.LayoutParams topParams = (RelativeLayout.LayoutParams) mTopLayout
                .getLayoutParams();
        //底层工具栏size
        RelativeLayout.LayoutParams bottomParams = (RelativeLayout.LayoutParams) mBottomLayout
                .getLayoutParams();
        int topHeight = DensityUtil.dip2px(this, topParams.height);
        bottomParams.height = mScreenHeight - mPreviewHeight - topHeight;
        bottomParams.width = mPreviewWidth;
        mBottomLayout.setLayoutParams(bottomParams);

        Bundle bundle = getIntent().getExtras();
        mEditUrl = bundle.getString(EDIT_URL);
        mPlayerUrl = bundle.getString(PLAYER_URL);
        mModelPath = bundle.getString(MODEL_PATH);
        mEditModelPos = (MultiCanvasInfo) bundle.getSerializable(EDIT_MODEL_INFO);
        mModelFinished = bundle.getBoolean(MODEL_FINISH);
        int model_layout_id = bundle.getInt(MODEL_LAYOUT_ID);

        //添加模版
        CanvasViewBase modelView = new CanvasViewBase(this, model_layout_id, null);
        mPreviewLayout.addView(modelView);

        //设置预览surface的size
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mEditPreviewView.getLayoutParams();
        params.leftMargin = mEditModelPos.x_preview;
        params.topMargin = mEditModelPos.y_preview;
        params.width = mEditModelPos.w_preview;
        params.height = mEditModelPos.h_preview;
        mEditPreviewView.setLayoutParams(params);

        mMainHandler = new Handler();
        //init kit
        mEditKit = new KSYEditKit(this);
        mEditKit.setDisplayPreview(mEditPreviewView);
        mEditKit.setOnInfoListener(mOnInfoListener);
        mEditKit.setOnErrorListener(mOnErrorListener);
        mLeftVolumeSeekBar.setProgress((int) (mEditKit.getOriginAudioVolume() * 100));
        mRightVolumeSeekBar.setProgress((int) (mEditKit.getOriginAudioVolume() * 100));

        mComposeKit = new KSYMultiCanvasComposeKit(this);
        mComposeKit.setOnInfoListener(new KSYMultiCanvasComposeKit.OnInfoListener() {
            @Override
            public void onInfo(int type, String msg) {
                switch (type) {
                    case ShortVideoConstants.SHORTVIDEO_COMPOSE_START: {
                        Log.d(TAG, "compose started");
                        resetPlay();
                        mEditKit.pauseEditPreview();
                        if (mComposeDialog != null && mComposeDialog.isShowing()) {
                            mComposeDialog.composeStarted();
                        }
                        return;
                    }
                    case ShortVideoConstants.SHORTVIDEO_COMPOSE_FINISHED: {
                        Log.d(TAG, "compose finished");
                        if (mComposeDialog != null && mComposeDialog.isShowing()) {
                            mComposeDialog.composeFinished(msg);
                        }
                        mComposeFinished = true;
                        return;
                    }
                    case ShortVideoConstants.SHORTVIDEO_COMPOSE_ABORTED:
                        Log.d(TAG, "compose aborted by user");
                        break;
                }
            }
        });
        mComposeKit.setOnErrorListener(new KSYMultiCanvasComposeKit.OnErrorListener() {
            @Override
            public void onError(int type, long msg) {
                switch (type) {
                    case ShortVideoConstants.SHORTVIDEO_ERROR_COMPOSE_FAILED_UNKNOWN:
                    case ShortVideoConstants.SHORTVIDEO_ERROR_COMPOSE_FILE_CLOSE_FAILED:
                    case ShortVideoConstants.SHORTVIDEO_ERROR_COMPOSE_FILE_FORMAT_NOT_SUPPORTED:
                    case ShortVideoConstants.SHORTVIDEO_ERROR_COMPOSE_FILE_OPEN_FAILED:
                    case ShortVideoConstants.SHORTVIDEO_ERROR_COMPOSE_FILE_WRITE_FAILED:
                        Log.d(TAG, "compose failed:" + type);
                        Toast.makeText(MultiCanvasEditActivity.this,
                                "Compose Failed:" + type, Toast.LENGTH_LONG).show();
                        if (mComposeDialog != null && mComposeDialog.isShowing()) {
                            mComposeDialog.closeDialog();
                            resumeEditPreview();
                        }
                        break;
                    case ShortVideoConstants.SHORTVIDEO_ERROR_SDK_AUTHFAILED:
                        Log.d(TAG, "sdk auth failed:" + type);
                        Toast.makeText(MultiCanvasEditActivity.this,
                                "Auth failed can't start compose:" + type, Toast.LENGTH_LONG).show();
                        if (mComposeDialog != null) {
                            mComposeDialog.closeDialog();
                            resumeEditPreview();
                        }
                        break;
                }
            }
        });
        mMediaPlayer = new KSYMediaPlayer.Builder(this).build();
        mSurfaceHolder = mPlayerPreview.getHolder();
        mSurfaceHolder.addCallback(mSurfaceCallback);

        startEditPreview();
        if (!TextUtils.isEmpty(mPlayerUrl) && !mMediaPlayer.isPlaying()) {
            startPlay(mPlayerUrl);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mEditKit.onResume();

        if (!TextUtils.isEmpty(mPlayerUrl)) {
            mMediaPlayer.start();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mEditKit.onPause();
        if (!TextUtils.isEmpty(mPlayerUrl) && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }

        stopPreviewTimerTask();

        if (mComposeDialog != null) {
            mComposeDialog.closeDialog();
            mComposeDialog = null;
        }

        mEditKit.stopEditPreview();
        mEditKit.release();
        mComposeKit.setOnInfoListener(null);
        mComposeKit.setOnErrorListener(null);
        mComposeKit.release();

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

    private void startEditPreview() {
        mEditKit.setEditPreviewUrl(mEditUrl);
        //设置是否循环预览
        mEditKit.setLooping(true);
        //开启预览
        mEditKit.startEditPreview();
    }

    private void resumeEditPreview() {
        Log.d(TAG, "resumeEditPreview ");
        startPlay(mPlayerUrl);
        //恢复播放的状态
        if (mPauseView.getDrawable().getLevel() == 1) {
            mPauseView.getDrawable().setLevel(2);
        }

        mEditKit.resumeEditPreview();
    }

    private KSYEditKit.OnErrorListener mOnErrorListener = new KSYEditKit.OnErrorListener() {
        @Override
        public void onError(int type, long msg) {
            switch (type) {
                case ShortVideoConstants.SHORTVIDEO_EDIT_PREVIEW_PLAYER_ERROR:
                    Log.d(TAG, "KSYEditKit preview player error:" + type + "_" + msg);
                default:
                    break;
            }
        }
    };

    private KSYEditKit.OnInfoListener mOnInfoListener = new KSYEditKit.OnInfoListener() {
        @Override
        public void onInfo(int type, String... msgs) {
            switch (type) {
                case ShortVideoConstants.SHORTVIDEO_EDIT_PREPARED:
                    Log.d(TAG, "preview player prepared");
                    long editDuration = MultiCanvasEditActivity.this.mEditKit.getEditDuration();
                    mEditPreviewSeekBar.setMax((int) editDuration);
                    SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
                    formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
                    String timer = formatter.format(editDuration);
                    mPreviewDurationTime.setText(timer);

                    startPreviewTimerTask();
                    break;
                case ShortVideoConstants.SHORTVIDEO_EDIT_PREVIEW_PLAYER_INFO:
                    Log.d(TAG, "KSYEditKit preview player info:" + type + "_" + msgs[0]);
                    break;
                default:
                    break;
            }
        }
    };

    /**
     * 美颜设置
     */
    private void initBeautyUI() {
        final int[] BEAUTY_TYPE = {BEAUTY_NATURE, BEAUTY_PRO, BEAUTY_FLOWER_LIKE, BEAUTY_DELICATE};
        mBeautyOriginalView = findViewById(R.id.iv_beauty_origin);
        mBeautyBorder = findViewById(R.id.iv_beauty_border);
        mBeautyOriginalText = findViewById(R.id.tv_beauty_origin);
        mBeautyRecyclerView = findViewById(R.id.beauty_recyclerView);
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

    private void addFilerForCompose() {
        if (mImgBeautyTypeIndex != BEAUTY_DISABLE) {
            ImgFilterBase filterBase = null;
            switch (mImgBeautyTypeIndex) {
                case BEAUTY_NATURE:
                    ImgBeautySoftFilter softFilter = new ImgBeautySoftFilter(mComposeKit.getGLRender());
                    softFilter.setGrindRatio(0.5f);
                    filterBase = softFilter;
                    break;
                case BEAUTY_PRO:
                    ImgBeautyProFilter proFilter = new ImgBeautyProFilter(mComposeKit.getGLRender()
                            , getApplicationContext());
                    proFilter.setGrindRatio(0.5f);
                    proFilter.setWhitenRatio(0.5f);
                    proFilter.setRuddyRatio(0);
                    filterBase = proFilter;
                    break;
                case BEAUTY_FLOWER_LIKE:
                    ImgBeautyProFilter pro1Filter = new ImgBeautyProFilter(mComposeKit.getGLRender()
                            , getApplicationContext(), 3);
                    pro1Filter.setGrindRatio(0.5f);
                    pro1Filter.setWhitenRatio(0.5f);
                    pro1Filter.setRuddyRatio(0.15f);
                    filterBase = pro1Filter;
                    break;
                case BEAUTY_DELICATE:
                    ImgBeautyProFilter pro2Filter = new ImgBeautyProFilter(mComposeKit.getGLRender()
                            , getApplicationContext(), 3);
                    pro2Filter.setGrindRatio(0.5f);
                    pro2Filter.setWhitenRatio(0.5f);
                    pro2Filter.setRuddyRatio(0.3f);
                    filterBase = pro2Filter;
                    break;
                case BEAUTY_DISABLE:
                    break;
                default:
                    break;
            }
            mComposeBeautyFilter = filterBase;
            mComposeKit.getImgTexFilterMgt().addFilter(filterBase);
        } else {
            if (mComposeBeautyFilter != null && mComposeKit.getImgTexFilterMgt().getFilter().
                    contains(mComposeBeautyFilter)) {
                mComposeKit.getImgTexFilterMgt().replaceFilter(mComposeBeautyFilter, null, false);
            }
        }

        if (mEffectFilterIndex != FILTER_DISABLE) {
            ImgTexFilterBase filter;
            if (mFilterTypeIndex < 13) {
                filter = new ImgBeautySpecialEffectsFilter(mComposeKit.getGLRender(),
                        getApplicationContext(), mEffectFilterIndex);
            } else {
                filter = new ImgBeautyStylizeFilter(mComposeKit
                        .getGLRender(), getApplicationContext(), mEffectFilterIndex);
            }
            mComposeEffectFilter = filter;
            mComposeKit.getImgTexFilterMgt().addFilter(filter);
        } else {
            if (mComposeBeautyFilter != null && mComposeKit.getImgTexFilterMgt().getFilter().
                    contains(mComposeEffectFilter)) {
                mComposeKit.getImgTexFilterMgt().replaceFilter(mComposeEffectFilter, null, false);
            }
        }
    }

    private void addImgFilter() {
        ImgBeautyProFilter proFilter;
        ImgBeautySpecialEffectsFilter specialEffectsFilter;
        ImgTexFilter texFilter;
        List<ImgFilterBase> filters = new LinkedList<>();
        switch (mImgBeautyTypeIndex) {
            case BEAUTY_NATURE:
                ImgBeautySoftFilter softFilter = new ImgBeautySoftFilter(mEditKit.getGLRender());
                softFilter.setGrindRatio(0.5f);
                filters.add(softFilter);
                break;
            case BEAUTY_PRO:
                proFilter = new ImgBeautyProFilter(mEditKit.getGLRender(), getApplicationContext());
                proFilter.setGrindRatio(0.5f);
                proFilter.setWhitenRatio(0.5f);
                proFilter.setRuddyRatio(0);
                filters.add(proFilter);
                break;
            case BEAUTY_FLOWER_LIKE:
                proFilter = new ImgBeautyProFilter(mEditKit.getGLRender(), getApplicationContext(), 3);
                proFilter.setGrindRatio(0.5f);
                proFilter.setWhitenRatio(0.5f);
                proFilter.setRuddyRatio(0.15f);
                filters.add(proFilter);
                break;
            case BEAUTY_DELICATE:
                proFilter = new ImgBeautyProFilter(mEditKit.getGLRender(), getApplicationContext(), 3);
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
                specialEffectsFilter = new ImgBeautySpecialEffectsFilter(mEditKit.getGLRender(),
                        getApplicationContext(), mEffectFilterIndex);
                filters.add(specialEffectsFilter);
            } else {
                texFilter = new ImgBeautyStylizeFilter(mEditKit.getGLRender(), getApplicationContext(),
                        mEffectFilterIndex);
                filters.add(texFilter);
            }
        }
        if (filters.size() > 0) {

            mEditKit.getImgTexFilterMgt().setFilter(filters);

        } else {
            mEditKit.getImgTexFilterMgt().setFilter((ImgTexFilterBase) null);
        }
    }

    private void setEffectFilter(int type) {
        mEffectFilterIndex = type;
        addImgFilter();
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
        mFilterOriginImage = findViewById(R.id.iv_filter_origin);
        mFilterBorder = findViewById(R.id.iv_filter_border);
        mFilterOriginText = findViewById(R.id.tv_filter_origin);
        changeOriginalImageState(true);
        mFilterRecyclerView = findViewById(R.id.filter_recyclerView);
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

    private void onAudioVolumeClick() {
        mPreviewEditLayout = mAudioVolumeLayout;
        if (mAudioVolumeLayout.getVisibility() != View.VISIBLE) {
            mAudioVolumeLayout.setVisibility(View.VISIBLE);
        }
        if (mBaseControlLayout.getVisibility() == View.VISIBLE) {
            mBaseControlLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void onBeautyClick() {
        mPreviewEditLayout = mBeautyLayout;
        if (mBeautyLayout.getVisibility() != View.VISIBLE) {
            mBeautyLayout.setVisibility(View.VISIBLE);
        }
        if (mBaseControlLayout.getVisibility() == View.VISIBLE) {
            mBaseControlLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void onFilterClick() {
        mPreviewEditLayout = mFilterLayout;
        if (mFilterLayout.getVisibility() != View.VISIBLE) {
            mFilterLayout.setVisibility(View.VISIBLE);
        }
        if (mBaseControlLayout.getVisibility() == View.VISIBLE) {
            mBaseControlLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void onBackoffClick() {
        if (mPreviewEditLayout != null &&
                mPreviewEditLayout.getVisibility() == View.VISIBLE) {
            mPreviewEditLayout.setVisibility(View.INVISIBLE);
            mPreviewEditLayout = null;
            if (mBaseControlLayout.getVisibility() != View.VISIBLE) {
                mBaseControlLayout.setVisibility(View.VISIBLE);
            }
        } else {
            if (!mModelFinished && !mComposeFinished) {
                if (mEditPreviewProgressTimer != null) {
                    mEditPreviewProgressTimer.cancel();
                    mEditPreviewProgressTimer = null;
                }
                //放弃本次录制视频
                MultiCanvasEditActivity.this.finish();
            } else {
                //直接进入配置页面
                Intent intent = new Intent(MultiCanvasEditActivity.this, ConfigActivity.class);
                startActivity(intent);
            }
        }
    }


    private void onPauseClick() {
        if (mPauseView.getDrawable().getLevel() == 2) {
            mEditKit.pausePlay(true);
            mPauseView.getDrawable().setLevel(1);
            stopPreviewTimerTask();
        } else {
            mEditKit.pausePlay(false);
            mPauseView.getDrawable().setLevel(2);
            startPreviewTimerTask();
        }
    }

    private void startPreviewTimerTask() {
        mEditPreviewProgressTimer = new Timer();
        mEditPreviewProgressTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!mIsPreviewSeekBarChanging) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!mIsPreviewSeekBarChanging) {
                                long currentPosition = 0;
                                if (!mModelFinished) {
                                    if(MultiCanvasEditActivity.this.mEditKit.getMediaPlayer() != null) {
                                        currentPosition = MultiCanvasEditActivity.this.mEditKit.getMediaPlayer().getCurrentPosition();
                                    }
                                } else {
                                    if (mMediaPlayer != null) {
                                        currentPosition = mMediaPlayer.getCurrentPosition();
                                    }
                                }
                                mEditPreviewSeekBar.setProgress((int) currentPosition);
                                SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
                                formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
                                String timer = formatter.format(currentPosition);
                                mCurrentPreviewTime.setText(timer);
                            }
                        }
                    });
                }
            }
        }, 0, 500);
    }

    private void stopPreviewTimerTask() {
        if (mEditPreviewProgressTimer != null) {
            mEditPreviewProgressTimer.cancel();
            mEditPreviewProgressTimer = null;
        }
    }

    private void doStartCompose() {
        showComposeDialog();
        if (mComposeConfig != null) {
            //配置合成参数
            //当前模版示范比例是1:1，需要根据模版比例计算对应的resolution
            //例如若模版比例是4:3，480p,对应的resolution就应该设置为(480,480*3/4)
            if(mComposeConfig.resolution == StreamerConstants.VIDEO_RESOLUTION_720P) {
                mComposeKit.setTargetResolution(720, 720);
            } else if(mComposeConfig.resolution == StreamerConstants.VIDEO_RESOLUTION_1080P) {
                mComposeKit.setTargetResolution(1080, 1080);
            }

            mComposeKit.setVideoFps(mComposeConfig.fps);
            mComposeKit.setEncodeMethod(mComposeConfig.encodeMethod);
            mComposeKit.setVideoCodecId(mComposeConfig.encodeType);
            mComposeKit.setVideoEncodeProfile(mComposeConfig.encodeProfile);
            mComposeKit.setAudioKBitrate(mComposeConfig.audioBitrate);
            mComposeKit.setAudioChannels(mComposeConfig.audioChannel);
            mComposeKit.setAudioSampleRate(mComposeConfig.audioSampleRate);
            mComposeKit.setVideoKBitrate(mComposeConfig.videoBitrate);
            mComposeKit.setVideoDecodeMethod(mComposeConfig.decodeMethod);
            mComposeKit.setAudioEncodeProfile(mComposeConfig.audioEncodeProfile);
            //构造合成文件
            List<KSYMultiCanvasComposeKit.MediaInfo> mediaInfos = new LinkedList<>();

            //在这里需要配置视频或者模版在目标区域的相对位置和size，以1:1的模版为例
            //MediaInfo中需要提供的几个关键信息有：
            //媒体路径path
            //媒体音量信息(leftVolume,rightVolume)
            //媒体相对位置和size(x,y,w,h,alpha),其中alpha为显示透明度
            //媒体在z方向的位置z_order，该值有效范围为：0~7，主视频默认是0，值越大绘制图层越靠上层
            if (!TextUtils.isEmpty(mModelPath)) {
                //模版在目标区域的相关信息为(x:0,y:0,w:1,h:1,alpha:1),即起始位置在目标区域的左上角，占据目标区域整个区域
                //模版绘制在目标区域z方向的最底层，因此z_order设置为0
                KSYMultiCanvasComposeKit.MediaInfo mediaInfoModel = new KSYMultiCanvasComposeKit.MediaInfo(
                        mModelPath, 0, 0,
                        0, 0, 1.0f, 1.0f, 1.0f, 0, KSYMultiCanvasComposeKit.MEDIA_TYPE_BITMAP);
                mediaInfos.add(mediaInfoModel);
            }

            if (!TextUtils.isEmpty(mPlayerUrl)) {
                //编辑好的视频在目标区域的位置信息为(x:0,y:0,w:1,h:1,alpha:1),即其实位置在目标区域的左上角，占据目标区域整个区域
                //音量信息(leftVolume:1.0,rightVolume:1.0)
                KSYMultiCanvasComposeKit.MediaInfo mediaInfoModel = new KSYMultiCanvasComposeKit.MediaInfo(
                        mPlayerUrl, 1.0f, 1.0f,
                        0, 0, 1.0f, 1.0f, 1.0f, 0, KSYMultiCanvasComposeKit.MEDIA_TYPE_VIDEO);
                mediaInfoModel.isEdit = false;
                mediaInfos.add(mediaInfoModel);
            }
            //当前编辑视频在目标区域的位置由录制时决定，z_order在其它视频和模版的上层，设置为1，当然可以根据需要设置z_order
            //用当前调节的具体值作为编辑视频的音量信息
            float leftVolume = mLeftVolumeSeekBar.getProgress() / 100.f;
            float rightVolume = mRightVolumeSeekBar.getProgress() / 100.f;
            float x = (float) mEditModelPos.x_preview / (float) mPreviewWidth;
            float y = (float) mEditModelPos.y_preview / (float) mPreviewHeight;
            float w = (float) mEditModelPos.w_preview / (float) mPreviewWidth;
            float h = (float) mEditModelPos.h_preview / (float) mPreviewHeight;
            KSYMultiCanvasComposeKit.MediaInfo mediaInfo = new KSYMultiCanvasComposeKit.MediaInfo(
                    mEditUrl, leftVolume, rightVolume,
                    x, y, w, h, 1.0f, 1, KSYMultiCanvasComposeKit.MEDIA_TYPE_VIDEO);
            //当前录制视频作为待编辑视频，滤镜和音效调整只在该视频上面生效
            mediaInfo.isEdit = true;
            mediaInfos.add(mediaInfo);


            //设置合成路径
            String fileFolder = Environment.getExternalStorageDirectory().
                    getAbsolutePath() + "/ksy_sv_model_compose_test";
            File file = new File(fileFolder);
            if (!file.exists()) {
                file.mkdir();
            }

            StringBuilder composeUrl = new StringBuilder(fileFolder).append("/").append(System
                    .currentTimeMillis());
            composeUrl.append(".mp4");

            Log.d(TAG, "compose Url:" + composeUrl);
            //开始合成
            mComposeFinished = false;
            addFilerForCompose();
            mComposeKit.start(mediaInfos, composeUrl.toString());
        }
    }

    private void onOutputConfirmClick() {
        confirmConfig();
        if (mConfigDialog.isShowing()) {
            mConfigDialog.dismiss();
        }
        doStartCompose();
    }

    private void onStartComposeClick() {
        if (mModelFinished && mComposeFinished) {
            saveFileToDCIM();
        } else {
            //showConfigDialog();
            //当前交互模式，每次录制完成后必做一次压缩，为防止多次压缩造成的画面损失严重，
            //除最后一次压缩外，前几次压缩均采用高分辨率，并且每次压缩都使用一致的参数，不进行其它参数的变更
            confirmConfig();
            doStartCompose();
        }
    }

    /**
     * 保存视频到相册并发广播进行通知
     */
    private void saveFileToDCIM() {
        String srcPath = mPlayerUrl;
        String desDir = Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)
                ? Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()
                + "/Camera/" : Environment.getExternalStorageDirectory().getAbsolutePath() + "/KSYShortVideo";
        String name = srcPath.substring(srcPath.lastIndexOf('/'));
        String desPath = desDir + name;
        File desFile = new File(desPath);
        try {
            File srcFile = new File(srcPath);
            if (srcFile.exists() && !desFile.exists()) {
                InputStream is = new FileInputStream(srcPath);
                FileOutputStream fos = new FileOutputStream(desFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, length);
                }
                Toast.makeText(MultiCanvasEditActivity.this, "文件保存相册成功", Toast.LENGTH_LONG).show();
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 发送系统广播通知有图片更新
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(desFile);
        intent.setData(uri);
        sendBroadcast(intent);
    }

    private class ButtonObserver implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.play_pause:
                    onPauseClick();
                    break;
                case R.id.audio_channel_volume:
                    onAudioVolumeClick();
                    break;
                case R.id.beauty:
                    onBeautyClick();
                    break;
                case R.id.filter:
                    onFilterClick();
                    break;
                case R.id.click_to_back:
                    onBackoffClick();
                    break;
                case R.id.start_compose:
                    onStartComposeClick();
                default:
                    break;
            }
        }
    }

    private class ComposeConfigButtonObserver implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.output_config_r720p:
                    mOutRes720p.setActivated(true);
                    mOutRes1080p.setActivated(false);
                    break;
                case R.id.output_config_1080p:
                    mOutRes720p.setActivated(false);
                    mOutRes1080p.setActivated(true);
                    break;
                case R.id.output_config_h264:
                    mOutEncodeWithH264.setActivated(true);
                    mOutEncodeWithH265.setActivated(false);
                    break;
                case R.id.output_config_h265:
                    mOutEncodeWithH264.setActivated(false);
                    mOutEncodeWithH265.setActivated(true);
                    break;
                case R.id.output_config_hw:
                    mOutEncodeByHW.setActivated(true);
                    mOutEncodeBySW.setActivated(false);
                    mOutVideoCRF.setEnabled(false);
                    if(Integer.parseInt(mOutVideoBitrate.getText().toString()) == 0) {
                        mOutVideoBitrate.setText(String.valueOf(4000));
                    }
                    break;
                case R.id.output_config_sw:
                    mOutEncodeByHW.setActivated(false);
                    mOutEncodeBySW.setActivated(true);
                    mOutVideoCRF.setEnabled(true);
                    break;
                case R.id.output_config_decode_hw:
                    mOutDecodeByHW.setActivated(true);
                    mOutDecodeBySW.setActivated(false);
                    break;
                case R.id.output_config_decode_sw:
                    mOutDecodeBySW.setActivated(true);
                    mOutDecodeByHW.setActivated(false);
                    break;
                case R.id.output_config_mp4:
                    mOutForMP4.setActivated(true);
                    mOutEncodeWithH264.setEnabled(true);
                    mOutEncodeWithH265.setEnabled(true);
                    mOutEncodeWithH264.setActivated(true);
                    mOutEncodeWithH265.setActivated(false);
                    mOutEncodeByHW.setEnabled(true);
                    break;
                case R.id.output_config_low_power:
                    onOutputEncodeProfileClick(0);
                    break;
                case R.id.output_config_balance:
                    onOutputEncodeProfileClick(1);
                    break;
                case R.id.output_config_high_performance:
                    onOutputEncodeProfileClick(2);
                    break;
                case R.id.output_config_aac_lc:
                    onOutputAudioEncodeProfileClick(0);
                    break;
                case R.id.output_config_aac_he:
                    onOutputAudioEncodeProfileClick(1);
                    break;
                case R.id.output_config_aac_he_v2:
                    onOutputAudioEncodeProfileClick(2);
                default:
                    break;
            }
        }
    }

    private void onOutputEncodeProfileClick(int index) {
        mOutProfileGroup[index].setActivated(true);
        for (int i = 0; i < mOutProfileGroup.length; i++) {
            if (i != index) {
                mOutProfileGroup[i].setActivated(false);
            }
        }
    }

    private void onOutputAudioEncodeProfileClick(int index) {
        mOutAudioProfileGroup[index].setActivated(true);
        for (int i = 0; i < mOutAudioProfileGroup.length; i++) {
            if (i != index) {
                mOutAudioProfileGroup[i].setActivated(false);
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
                case R.id.left_audio_volume:
                    float rightVal = mRightVolumeSeekBar.getProgress() / 100.f;
                    mEditKit.setOriginAudioVolume(val, rightVal);
                    break;
                case R.id.right_audio_volume:
                    float leftVal = mLeftVolumeSeekBar.getProgress() / 100.f;
                    mEditKit.setOriginAudioVolume(leftVal, val);
                    break;
                case R.id.edit_preview_SeekBar:
                    break;
                default:
                    break;
            }
        }

        /*滚动时,应当暂停后台定时器*/
        public void onStartTrackingTouch(SeekBar seekBar) {
            switch (seekBar.getId()) {
                case R.id.edit_preview_SeekBar:
                    stopPreviewTimerTask();
                    mIsPreviewSeekBarChanging = true;
                    break;
                default:
                    break;
            }
        }

        /*滑动结束后，重新设置值*/
        public void onStopTrackingTouch(SeekBar seekBar) {
            switch (seekBar.getId()) {
                case R.id.edit_preview_SeekBar:
                    int progress = seekBar.getProgress();
                    if (!mModelFinished) {
                        MultiCanvasEditActivity.this.mEditKit.getMediaPlayer().seekTo(progress, true);
                    } else {
                        if (mMediaPlayer != null) {
                            mMediaPlayer.seekTo(seekBar.getProgress());
                        }
                    }
                    startPreviewTimerTask();
                    mIsPreviewSeekBarChanging = false;
                    break;
                default:
                    break;
            }
        }
    }

    private void confirmConfig() {
        if(mConfigDialog == null) {
            mComposeConfig = new ShortVideoConfig();
        }
        if(mConfigDialog != null) {
            if (mOutRes720p.isActivated()) {
                mComposeConfig.resolution = StreamerConstants.VIDEO_RESOLUTION_720P;
            } else if (mOutRes1080p.isActivated()) {
                mComposeConfig.resolution = StreamerConstants.VIDEO_RESOLUTION_1080P;
            }
            if (mOutEncodeWithH264.isActivated()) {
                mComposeConfig.encodeType = AVConst.CODEC_ID_AVC;
            } else if (mOutEncodeWithH265.isActivated()) {
                mComposeConfig.encodeType = AVConst.CODEC_ID_HEVC;
            }

            if (mOutEncodeByHW.isActivated()) {
                mComposeConfig.encodeMethod = StreamerConstants.ENCODE_METHOD_HARDWARE;
            } else if (mOutEncodeBySW.isActivated()) {
                mComposeConfig.encodeMethod = StreamerConstants.ENCODE_METHOD_SOFTWARE;
            }

            if (mOutDecodeByHW.isActivated()) {
                mComposeConfig.decodeMethod = StreamerConstants.DECODE_METHOD_HARDWARE;
            } else if (mOutDecodeBySW.isActivated()) {
                mComposeConfig.decodeMethod = StreamerConstants.DECODE_METHOD_SOFTWARE;
            }

            for (int i = 0; i < mOutProfileGroup.length; i++) {
                if (mOutProfileGroup[i].isActivated()) {
                    mComposeConfig.encodeProfile = ENCODE_PROFILE_TYPE[i];
                    break;
                }
            }

            for (int i = 0; i < mOutAudioProfileGroup.length; i++) {
                if (mOutAudioProfileGroup[i].isActivated()) {
                    mComposeConfig.audioEncodeProfile = AUDIO_ENCODE_PROFILE[i];
                    break;
                }
            }
            mComposeConfig.fps = Integer.parseInt(mOutFrameRate.getText().toString());
            mComposeConfig.videoBitrate = Integer.parseInt(mOutVideoBitrate.getText().toString());
            mComposeConfig.audioBitrate = Integer.parseInt(mOutAudioBitrate.getText().toString());
            mComposeConfig.videoCRF = Integer.parseInt(mOutVideoCRF.getText().toString());
        } else {
            //每次都使用推荐参数进行合成
            if(!mModelFinished) {
                mComposeConfig.resolution = StreamerConstants.VIDEO_RESOLUTION_1080P;
            } else {
                //最后一次可适当降低分辨率
                mComposeConfig.resolution = StreamerConstants.VIDEO_RESOLUTION_720P;
            }
            mComposeConfig.encodeMethod = StreamerConstants.ENCODE_METHOD_HARDWARE;
            mComposeConfig.decodeMethod = StreamerConstants.DECODE_METHOD_HARDWARE;
            mComposeConfig.encodeProfile = ENCODE_PROFILE_TYPE[1];
            mComposeConfig.audioEncodeProfile = AUDIO_ENCODE_PROFILE[0];
            mComposeConfig.fps = 30;
            mComposeConfig.videoBitrate = 4000; //kbps
            mComposeConfig.audioBitrate = 48;
            mComposeConfig.videoCRF = 23;
        }
    }

    private void showConfigDialog() {
        if (mConfigDialog != null) {
            mConfigDialog.show();
            return;
        }
        mConfigDialog = new Dialog(this, R.style.dialog);
        mComposeConfigButtonObserver = new MultiCanvasEditActivity.ComposeConfigButtonObserver();
        View contentView = LayoutInflater.from(this).inflate(R.layout.config_popup_layout, null);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mConfigDialog.setContentView(contentView, params);
        mOutRes720p = contentView.findViewById(R.id.output_config_r720p);
        mOutRes720p.setOnClickListener(mComposeConfigButtonObserver);
        mOutRes1080p = contentView.findViewById(R.id.output_config_1080p);
        mOutRes1080p.setOnClickListener(mComposeConfigButtonObserver);
        mOutEncodeWithH264 = contentView.findViewById(R.id.output_config_h264);
        mOutEncodeWithH264.setOnClickListener(mComposeConfigButtonObserver);
        mOutEncodeWithH265 = contentView.findViewById(R.id.output_config_h265);
        mOutEncodeWithH265.setOnClickListener(mComposeConfigButtonObserver);
        mOutEncodeByHW = contentView.findViewById(R.id.output_config_hw);
        mOutEncodeByHW.setOnClickListener(mComposeConfigButtonObserver);
        mOutEncodeBySW = contentView.findViewById(R.id.output_config_sw);
        mOutEncodeBySW.setOnClickListener(mComposeConfigButtonObserver);
        mOutDecodeByHW = contentView.findViewById(R.id.output_config_decode_hw);
        mOutDecodeByHW.setOnClickListener(mComposeConfigButtonObserver);
        mOutDecodeBySW = contentView.findViewById(R.id.output_config_decode_sw);
        mOutDecodeBySW.setOnClickListener(mComposeConfigButtonObserver);
        mOutForMP4 = contentView.findViewById(R.id.output_config_mp4);
        mOutForMP4.setOnClickListener(mComposeConfigButtonObserver);
        mOutProfileGroup = new TextView[3];
        mOutAudioProfileGroup = new TextView[3];
        for (int i = 0; i < mOutProfileGroup.length; i++) {
            mOutProfileGroup[i] = contentView.findViewById(OUTPUT_PROFILE_ID[i]);
            mOutProfileGroup[i].setOnClickListener(mComposeConfigButtonObserver);

            mOutAudioProfileGroup[i] = contentView.findViewById(AUDIO_OUTPUT_PROFILE_ID[i]);
            mOutAudioProfileGroup[i].setOnClickListener(mComposeConfigButtonObserver);
        }
        mOutFrameRate = contentView.findViewById(R.id.output_config_frameRate);
        mOutVideoBitrate = contentView.findViewById(R.id.output_config_video_bitrate);
        mOutAudioBitrate = contentView.findViewById(R.id.output_config_audio_bitrate);
        mOutVideoCRF = contentView.findViewById(R.id.output_config_video_crf);
        mOutputConfirm = contentView.findViewById(R.id.output_confirm);
        mOutputConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOutputConfirmClick();
            }
        });
        mOutRes720p.setActivated(true);
        mOutEncodeWithH264.setActivated(true);
        mOutEncodeByHW.setActivated(true);
        mOutDecodeByHW.setActivated(true);
        mOutForMP4.setActivated(true);
        mOutProfileGroup[1].setActivated(true);
        mOutAudioProfileGroup[0].setActivated(true);
        mConfigDialog.show();

    }

    private void showComposeDialog() {
        if (mComposeDialog != null && mComposeDialog.isShowing()) {
            mComposeDialog.closeDialog();
        }

        if (mComposeDialog == null) {
            mComposeDialog = new ComposeDialog(this, R.style.dialog);
        }
        mComposeDialog.show();
    }

    private class ComposeDialog extends Dialog {
        private TextView mStateTextView;
        private TextView mProgressText;
        private View mSystemState;
        private TextView mCpuRate;
        private AlertDialog mConfimDialog;
        private Timer mTimer;

        protected ComposeDialog(Context context, int themeResId) {
            super(context, themeResId);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            View composeView = LayoutInflater.from(MultiCanvasEditActivity.this).inflate(R.layout.compose_layout, null);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            setContentView(composeView, params);
            setCanceledOnTouchOutside(false);
            mStateTextView = composeView.findViewById(R.id.state_text);
            mProgressText = composeView.findViewById(R.id.progress_text);
            mSystemState = composeView.findViewById(R.id.system_state);
            mSystemState.setVisibility(View.VISIBLE);
            mCpuRate = composeView.findViewById(R.id.cpu_rate);
        }

        public void composeStarted() {
            mStateTextView.setVisibility(View.VISIBLE);
            mStateTextView.setText(R.string.compose_file);
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    final int progress = (int) mComposeKit.getProgress();
                    updateProgress(progress);
                }

            }, 500, 500);
        }

        private void updateProgress(final int progress) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    int rate = (int) SystemStateObtainUtil.getInstance().sampleCPU();
                    mProgressText.setText(String.valueOf(progress) + "%");
                    mCpuRate.setText(rate + "%");
                }
            });
        }

        public void composeFinished(String path) {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
            if (mComposeDialog.isShowing()) {
                mComposeDialog.closeDialog();
            }

            if (!mModelFinished) {

                //启动短视频录制
                Intent intent = new Intent();
                intent.putExtra(MultiCanvasRecordActivity.PLAYER_URL, path);
                setResult(100, intent);

                //若未录制结束继续跳转置录制界面进行录制
                MultiCanvasEditActivity.this.finish();
            } else {
                //启动预览页面
                //显示最终视频时，需要隐藏用于编辑预览的GLSurfaceView
                mEditPreviewView.setVisibility(View.INVISIBLE);
                mPlayerUrl = path;
                startPlay(path);
                mEditControlLayout.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (!mComposeFinished) {
                        mConfimDialog = new AlertDialog.Builder(MultiCanvasEditActivity.this).setCancelable
                                (true)
                                .setTitle("中止合成?")
                                .setNegativeButton("取消", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        mConfimDialog = null;
                                    }
                                })
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        if (!mComposeFinished) {
                                            mComposeKit.stop();
                                            mComposeFinished = false;
                                            closeDialog();
                                            resumeEditPreview();
                                            startPlay(mPlayerUrl);
                                        }
                                        mConfimDialog = null;
                                    }
                                }).show();
                    } else {
                        closeDialog();
                        resumeEditPreview();
                        startPlay(mPlayerUrl);
                    }
                    break;
                default:
                    break;
            }
            return false;
        }

        public void closeDialog() {
            mProgressText.setText(0 + "%");
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }

            if (mConfimDialog != null && mConfimDialog.isShowing()) {
                mConfimDialog.dismiss();
                mConfimDialog = null;
            }

            MultiCanvasEditActivity.ComposeDialog.this.dismiss();
        }
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
                if (mModelFinished) {
                    long editDuration = mMediaPlayer.getDuration();
                    mEditPreviewSeekBar.setMax((int) editDuration);
                    SimpleDateFormat formatter = new SimpleDateFormat("mm:ss");
                    formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
                    String timer = formatter.format(editDuration);
                    mPreviewDurationTime.setText(timer);

                    startPreviewTimerTask();
                }
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
                    break;
                default:
                    break;
            }
            return false;
        }
    };

    private void startPlay(String path) {
        if (!TextUtils.isEmpty(mPlayerUrl)) {
            mMediaPlayer.setLooping(true);
            mMediaPlayer.shouldAutoPlay(false);
            mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
            mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
            mMediaPlayer.setOnInfoListener(mOnMediaInfoListener);
            mMediaPlayer.setOnErrorListener(mOnMediaErrorListener);
            try {
                mMediaPlayer.setDataSource(path);
                mMediaPlayer.prepareAsync();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
