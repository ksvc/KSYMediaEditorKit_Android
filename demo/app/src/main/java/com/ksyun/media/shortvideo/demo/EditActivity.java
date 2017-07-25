package com.ksyun.media.shortvideo.demo;

import com.ksyun.ks3.exception.Ks3Error;
import com.ksyun.ks3.services.handler.PutObjectResponseHandler;
import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.shortvideo.demo.adapter.BgmSelectAdapter;
import com.ksyun.media.shortvideo.demo.adapter.BottomTitleAdapter;
import com.ksyun.media.shortvideo.demo.adapter.ImageTextAdapter;
import com.ksyun.media.shortvideo.demo.adapter.SoundEffectAdapter;
import com.ksyun.media.shortvideo.demo.audiorange.AudioSeekLayout;
import com.ksyun.media.shortvideo.demo.sticker.ColorPicker;
import com.ksyun.media.shortvideo.demo.sticker.StickerAdapter;
import com.ksyun.media.shortvideo.demo.util.DataFactory;
import com.ksyun.media.shortvideo.demo.util.HttpRequestTask;
import com.ksyun.media.shortvideo.demo.util.KS3TokenTask;
import com.ksyun.media.shortvideo.demo.util.SystemStateObtainUtil;
import com.ksyun.media.shortvideo.demo.videorange.HorizontalListView;
import com.ksyun.media.shortvideo.demo.videorange.VideoRangeSeekBar;
import com.ksyun.media.shortvideo.demo.videorange.VideoThumbnailAdapter;
import com.ksyun.media.shortvideo.demo.videorange.VideoThumbnailInfo;
import com.ksyun.media.shortvideo.utils.FileUtils;
import com.ksyun.media.shortvideo.utils.KS3ClientWrap;
import com.ksyun.media.shortvideo.kit.KSYEditKit;
import com.ksyun.media.shortvideo.utils.ProbeMediaInfoTools;
import com.ksyun.media.shortvideo.utils.ShortVideoConstants;
import com.ksyun.media.shortvideo.view.KSYTextView;
import com.ksyun.media.shortvideo.view.StickerHelpBoxInfo;
import com.ksyun.media.shortvideo.view.KSYStickerView;
import com.ksyun.media.streamer.encoder.VideoEncodeFormat;
import com.ksyun.media.streamer.filter.audio.AudioFilterBase;
import com.ksyun.media.streamer.filter.audio.AudioReverbFilter;
import com.ksyun.media.streamer.filter.audio.KSYAudioEffectFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautyProFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautySpecialEffectsFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgFilterBase;
import com.ksyun.media.streamer.filter.imgtex.ImgTexFilterBase;
import com.ksyun.media.streamer.framework.AVConst;
import com.ksyun.media.streamer.kit.StreamerConstants;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 编辑合成示例窗口
 * 水印
 * 美颜
 * 滤镜
 * 变速
 * 视频裁剪
 * 背景音：音量调节、裁剪
 * 原始音频：音量调节、变声、混响
 * 字幕
 * 静态贴纸
 * 合成后文件上传ks3
 */

public class EditActivity extends Activity implements
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static String TAG = "EditActivity";
    //获取ks3播放地址，仅供demo使用，不提供上线服务
    private static String FILE_URL_SERVER = "http://ksvs-demo.ks-live.com:8720/api/upload/ks3/signurl";
    private static final int REQUEST_CODE = 10010;
    private static final int FILTER_DISABLE = 0;

    private static final int BEAUTY_LAYOUT_INDEX = 0;
    private static final int FILTER_LAYOUT_INDEX = 1;
    private static final int WATER_MARK_INDEX = 2;
    private static final int SPEED_LAYOUT_INDEX = 3;
    private static final int VIDEO_RANGE_INDEX = 4;
    private static final int MUSIC_LAYOUT_INDEX = 5;
    private static final int SOUND_CHANGE_INDEX = 6;
    private static final int REVERB_LAYOUT_INDEX = 7;
    private static final int STICKER_LAYOUT_INDEX = 8;
    private static final int SUBTITLE_LAYOUT_INDEX = 9;

    private GLSurfaceView mEditPreviewView;
    private RelativeLayout mBarBottomLayout;
    private ImageView mNextView;
    private ImageView mPauseView;
    private List<String> mTitleData;
    private RecyclerView mTitleView;
    private BottomTitleAdapter mTitleAdapter;
    private AppCompatSeekBar mOriginAudioVolumeSeekBar;
    private AppCompatSeekBar mBgmVolumeSeekBar;
    private View mBeautyLayout;  //美颜
    private View mFilterLayout; //滤镜
    private View mSpeedLayout; //变速
    private View mVideoRangeLayout;  //视频裁剪
    private View mAudioEditLayout;  //bgm音频裁剪
    private View mSoundChangeLayout;  //原始音频变声
    private View mReverbLayout;  //原始音频混响
    private View mSubtitleLayout;
    private RecyclerView mBgmRecyclerView;
    private RecyclerView mSoundChangeRecycler;
    private RecyclerView mReverbRecycler;
    //滤镜
    private ImageView mFilterOriginImage;
    private ImageView mFilterBorder;
    private TextView mFilterOriginText;
    private RecyclerView mFilterRecyclerView;
    private View mStickerLayout;
    private View[] mBottomViewList;

    //美颜
    private LinearLayout mBeautyGrindLayout;
    private TextView mGrindText;
    private AppCompatSeekBar mGrindSeekBar;
    private LinearLayout mBeautyWhitenLayout;
    private TextView mWhitenText;
    private AppCompatSeekBar mWhitenSeekBar;
    private LinearLayout mBeautyRuddyLayout;
    private TextView mRuddyText;
    private AppCompatSeekBar mRuddySeekBar;

    private View mRemoveStickers;
    private RecyclerView mStickerList;// 贴图素材列表
    private KSYStickerView mKSYStickerView;  //贴纸预览区域
    private StickerAdapter mStickerAdapter;// 贴图列表适配器
    private Bitmap mStickerDeleteBitmap;  //贴纸辅助区域的删除按钮
    private Bitmap mStickerRotateBitmap;  //贴纸辅助区域的旋转按钮
    private StickerHelpBoxInfo mStickerHelpBoxInfo;  //贴纸辅助区域的画笔
    private EditText mTextInput;  //字幕输入框
    private ImageView mTextColorSelect; //字母颜色选择
    private KSYTextView mTextView;  //字幕预览显示
    private ColorPicker mColorPicker;  //字体颜色选择
    private InputMethodManager mInputMethodManager;
    private RecyclerView mTextStickerList;
    private StickerAdapter mTextStickerAdapter;

    private ImageView mSpeedDown; //减速
    private ImageView mSpeedUp; //加速
    private TextView mSpeedInfo; //速度信息

    private boolean mFirstPlay = true;
    private boolean mWaterMarkChecked;
    private AudioSeekLayout.OnAudioSeekChecked mAudioSeekListener;
    private float mAudioLength;  //背景音乐时长
    private float mPreviewLength; //视频裁剪后的时长
    private AudioSeekLayout mAudioSeekLayout;  //音频seek布局
    private PopupWindow mConfigWindow;
    private ShortVideoConfig mComposeConfig; //输出视频参数配置
    private ButtonObserver mButtonObserver;
    private SeekBarChangedObserver mSeekBarChangedObserver;

    public final static String SRC_URL = "srcurl";

    private static final int AUDIO_FILTER_DISABLE = 0;  //不使用音频滤镜的类型标志
    private int mAudioEffectType = AUDIO_FILTER_DISABLE;  //变声类型缓存变量
    private int mAudioReverbType = AUDIO_FILTER_DISABLE;  //混响类型缓存变量
    //变声类型数组常量
    private static final int[] SOUND_CHANGE_TYPE = {KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_MALE, KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_FEMALE,
            KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_HEROIC, KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_ROBOT};
    //混响类型数组常量
    private static final int[] REVERB_TYPE = {AudioReverbFilter.AUDIO_REVERB_LEVEL_1, AudioReverbFilter.AUDIO_REVERB_LEVEL_3,
            AudioReverbFilter.AUDIO_REVERB_LEVEL_4, AudioReverbFilter.AUDIO_REVERB_LEVEL_2};

    private static final int BOTTOM_VIEW_NUM = 10;
    private String mLogoPath = "assets://KSYLogo/logo.png";
    private String mStickerPath = "Stickers";  //贴纸加载地址默认在Assets目录，如果修改加载地址需要修改StickerAdapter的图片加载
    private String mTextStickerPath = "TextStickers";

    private KSYEditKit mEditKit; //编辑合成kit类
    private ImgBeautyProFilter mImgBeautyProFilter;  //美颜filter
    private int mEffectFilterIndex = FILTER_DISABLE;  //滤镜filter type

    private boolean mComposeFinished = false;
    /*******编辑后合成参数配置示例******/
    private TextView mOutRes480p;
    private TextView mOutRes540p;
    private TextView mOutEncodeWithH264;
    private TextView mOutEncodeWithH265;
    private TextView mOutForMP4;
    private TextView mOutForGIF;
    private TextView[] mOutProfileGroup;
    private EditText mOutFrameRate;
    private EditText mOutVideoBitrate;
    private EditText mOutAudioBitrate;
    private TextView mOutputConfirm;
    private static final int[] OUTPUT_PROFILE_ID = {R.id.output_config_low_power,
            R.id.output_config_balance, R.id.output_config_high_performance};
    private static final int[] ENCODE_PROFILE_TYPE = {VideoEncodeFormat.ENCODE_PROFILE_LOW_POWER,
            VideoEncodeFormat.ENCODE_PROFILE_BALANCE, VideoEncodeFormat.ENCODE_PROFILE_HIGH_PERFORMANCE};

    //合成后文件上传ks3
    private KS3TokenTask mTokenTask;
    private String mCurObjectKey;
    private Handler mMainHandler;
    private ComposeAlertDialog mComposeAlertDialog;
    private boolean mPaused = false;
    private int mBottomViewPreIndex;

    //for video range
    private HorizontalListView mVideoThumbnailList;
    private VideoRangeSeekBar mVideoRangeSeekBar;
    private VideoThumbnailAdapter mVideoThumbnailAdapter;
    private static final int LONG_VIDEO_MAX_LEN = 300000;
    private int mMaxClipSpanMs = LONG_VIDEO_MAX_LEN;  //默认的最大裁剪时长
    private float mHLVOffsetX = 0.0f;
    private long mEditPreviewDuration;
    private TextView mVideoRangeStart;
    private TextView mVideoRange;
    private TextView mVideoRangeEnd;
    private float mLastX = 0;

    public static void startActivity(Context context, String srcurl) {
        Intent intent = new Intent(context, EditActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.putExtra(SRC_URL, srcurl);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.edit_activity);

        //must set
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //默认设置为横屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        mButtonObserver = new EditActivity.ButtonObserver();
        mSeekBarChangedObserver = new EditActivity.SeekBarChangedObserver();
        mEditPreviewView = (GLSurfaceView) findViewById(R.id.edit_preview);
        mBarBottomLayout = (RelativeLayout) findViewById(R.id.edit_bar_bottom);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mBarBottomLayout.getLayoutParams();
        params.height = screenHeight / 3;
        mBarBottomLayout.setLayoutParams(params);

        mBottomViewList = new View[BOTTOM_VIEW_NUM];
        mPauseView = (ImageView) findViewById(R.id.click_to_pause);
        mPauseView.setOnClickListener(mButtonObserver);
        mPauseView.getDrawable().setLevel(2);
        mBeautyLayout = findViewById(R.id.beauty_choose);
        mBottomViewList[BEAUTY_LAYOUT_INDEX] = mBeautyLayout;
        mFilterLayout = findViewById(R.id.edit_filter_choose);
        mBottomViewList[FILTER_LAYOUT_INDEX] = mFilterLayout;
        mSpeedLayout = findViewById(R.id.speed_layout);
        mBottomViewList[SPEED_LAYOUT_INDEX] = mSpeedLayout;
        mBeautyGrindLayout = (LinearLayout) findViewById(R.id.beauty_grind);
        mGrindText = (TextView) findViewById(R.id.grind_text);
        mGrindSeekBar = (AppCompatSeekBar) findViewById(R.id.grind_seek_bar);
        mBeautyWhitenLayout = (LinearLayout) findViewById(R.id.beauty_whiten);
        mWhitenText = (TextView) findViewById(R.id.whiten_text);
        mWhitenSeekBar = (AppCompatSeekBar) findViewById(R.id.whiten_seek_bar);
        mBeautyRuddyLayout = (LinearLayout) findViewById(R.id.beauty_ruddy);
        mRuddyText = (TextView) findViewById(R.id.ruddy_text);
        mRuddySeekBar = (AppCompatSeekBar) findViewById(R.id.ruddy_seek_bar);

        mVideoRangeLayout = findViewById(R.id.video_range_choose);
        mBottomViewList[VIDEO_RANGE_INDEX] = mVideoRangeLayout;
        mAudioEditLayout = findViewById(R.id.audio_choose);
        mBottomViewList[MUSIC_LAYOUT_INDEX] = mAudioEditLayout;
        mAudioSeekLayout = (AudioSeekLayout) findViewById(R.id.audioSeekLayout);
        mSoundChangeLayout = findViewById(R.id.edit_sound_change);
        mBottomViewList[SOUND_CHANGE_INDEX] = mSoundChangeLayout;
        mReverbLayout = findViewById(R.id.edit_reverb);
        mBottomViewList[REVERB_LAYOUT_INDEX] = mReverbLayout;
        mOriginAudioVolumeSeekBar = (AppCompatSeekBar) findViewById(R.id.record_mic_audio_volume);
        mOriginAudioVolumeSeekBar.setOnSeekBarChangeListener(mSeekBarChangedObserver);
        mBgmVolumeSeekBar = (AppCompatSeekBar) findViewById(R.id.record_music_audio_volume);
        mBgmVolumeSeekBar.setOnSeekBarChangeListener(mSeekBarChangedObserver);
        mStickerLayout = findViewById(R.id.sticker_choose);
        mBottomViewList[STICKER_LAYOUT_INDEX] = mStickerLayout;
        mRemoveStickers = findViewById(R.id.click_to_delete_stickers);
        mRemoveStickers.setOnClickListener(mButtonObserver);
        mKSYStickerView = (KSYStickerView) findViewById(R.id.sticker_panel);
        //初始化贴纸选择List
        mStickerList = (RecyclerView) findViewById(R.id.stickers_list);
        LinearLayoutManager stickerListLayoutManager = new LinearLayoutManager(
                this);
        stickerListLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mStickerList.setLayoutManager(stickerListLayoutManager);
        mStickerAdapter = new StickerAdapter(this);
        mStickerList.setAdapter(mStickerAdapter);
        //Adapter中设置贴纸的路径，默认支持的是assets目录下面的，其它目录需要自行修改Adapter
        mStickerAdapter.addStickerImages(mStickerPath);
        //添加Item选择事件用于添加贴纸
        mStickerAdapter.setOnStickerItemClick(mOnStickerItemClick);

        mSubtitleLayout = findViewById(R.id.subtitle_choose);
        mBottomViewList[SUBTITLE_LAYOUT_INDEX] = mSubtitleLayout;
        mTextView = (KSYTextView) findViewById(R.id.text_panel);
        mTextInput = (EditText) findViewById(R.id.text_input);
        mTextInput.addTextChangedListener(mTextInputChangedListener);
        mTextColorSelect = (ImageView) findViewById(R.id.text_color);
        initStickerHelpBox();
        mTextStickerList = (RecyclerView) findViewById(R.id.text_stickers_list);
        LinearLayoutManager textstickerListLayoutManager = new LinearLayoutManager(
                this);
        textstickerListLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mTextStickerList.setLayoutManager(textstickerListLayoutManager);
        mTextStickerAdapter = new StickerAdapter(this);
        mTextStickerList.setAdapter(mTextStickerAdapter);
        mTextStickerAdapter.addStickerImages(mTextStickerPath);
        mTextStickerAdapter.setOnStickerItemClick(mOnTextStickerItemClick);

        KSYTextView.DrawTextParams textParams = new KSYTextView.DrawTextParams();
        textParams.textPaint = new TextPaint();
        textParams.textPaint.setTextSize(mTextView.getTextSize());
        textParams.textPaint.setColor(Color.WHITE);
        textParams.textPaint.setTextAlign(Paint.Align.LEFT);
        textParams.textPaint.setStyle(Paint.Style.FILL);
        textParams.textPaint.setAntiAlias(true);
        textParams.autoNewLine = true;
        textParams.bitmap = null;
        mTextView.initView(textParams, mStickerHelpBoxInfo,
                mTextInput);
        mTextView.setTextRectSelected(mTextRectSelected);
        mColorPicker = new ColorPicker(this, 255, 255, 255);
        mTextColorSelect.setOnClickListener(mButtonObserver);
        //变速
        mSpeedDown = (ImageView) findViewById(R.id.speed_down);
        mSpeedDown.setOnClickListener(mButtonObserver);
        mSpeedUp = (ImageView) findViewById(R.id.speed_up);
        mSpeedUp.setOnClickListener(mButtonObserver);
        mSpeedInfo = (TextView) findViewById(R.id.speed_info);

        mNextView = (ImageView) findViewById(R.id.click_to_next);
        mNextView.setOnClickListener(mButtonObserver);

        mMainHandler = new Handler();
        mEditKit = new KSYEditKit(this);
        mEditKit.setDisplayPreview(mEditPreviewView);
        mEditKit.setOnErrorListener(mOnErrorListener);
        mEditKit.setOnInfoListener(mOnInfoListener);
        mEditKit.addStickerView(mKSYStickerView);
        mEditKit.addTextStickerView(mTextView);

        mSpeedInfo.setText(String.valueOf(mEditKit.getNomalSpeed()));

        Bundle bundle = getIntent().getExtras();
        String url = bundle.getString(SRC_URL);
        if (!TextUtils.isEmpty(url)) {
            mEditKit.setEditPreviewUrl(url);
        }

        initTitleRecycleView();
        initFilterUI();
        initVideoRange();
        initBgmView();
        initSoundEffectView();
        startEditPreview();

        mInputMethodManager = (InputMethodManager) this.getSystemService(Context
                .INPUT_METHOD_SERVICE);

        mEditKit.getAudioPlayerCapture().setOnPreparedListener(new IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(IMediaPlayer iMediaPlayer) {
                mAudioLength = iMediaPlayer.getDuration();
                mAudioSeekListener = new AudioSeekLayout.OnAudioSeekChecked() {
                    @Override
                    public void onActionUp(long start, long end) {
                        mEditKit.setBGMRanges(start, end);
                    }
                };
                if (mAudioSeekLayout.getVisibility() != View.VISIBLE) {
                    mAudioSeekLayout.setVisibility(View.VISIBLE);
                    mAudioSeekLayout.setOnAudioSeekCheckedListener(mAudioSeekListener);
                }
                if (mFirstPlay) {
                    mFirstPlay = false;
                    mAudioSeekLayout.updateAudioSeekUI(mAudioLength, mPreviewLength);
                }
            }
        });
    }

    public void onResume() {
        super.onResume();
        mPaused = false;
        mEditKit.onResume();
        if (mComposeAlertDialog != null && mComposeAlertDialog.mNeedResumePlay) {
            mComposeAlertDialog.startPreview();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
        mEditKit.onPause();
        if (mInputMethodManager.isActive()) {
            mInputMethodManager.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
            if (getCurrentFocus() != null) {
                getCurrentFocus().clearFocus();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }
        mPutObjectResponseHandler = null;

        if (mComposeAlertDialog != null) {
            mComposeAlertDialog.closeDialog();
            mComposeAlertDialog = null;
        }
        mTextView.setTextRectSelected(null);
        mEditKit.stopEditPreview();
        mEditKit.release();
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int x = (int) event.getRawX();
                int y = (int) event.getRawY();
                if (!isTouchPointInView(mBarBottomLayout, x, y)) {
                    if (mBottomViewPreIndex != WATER_MARK_INDEX) {
                        mBottomViewList[mBottomViewPreIndex].setVisibility(View.INVISIBLE);
                        if (mTitleAdapter != null) {
                            mTitleAdapter.clear();
                        }
                    }
                }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 打开系统文件夹，导入音频文件作为背景音乐
     */
    private void importMusicFile() {
        Intent target = com.ksyun.media.shortvideo.demo.util.FileUtils.createGetContentIntent();
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
                    mFirstPlay = true;
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        Log.i(TAG, "Uri = " + uri.toString());
                        try {
                            // Get the file path from the URI
                            final String path = com.ksyun.media.shortvideo.demo.util.FileUtils.getPath(this, uri);
                            mEditKit.startBgm(path, true);
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

    private boolean isTouchPointInView(View view, int x, int y) {
        if (view == null) {
            return false;
        }
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        int left = location[0];
        int top = location[1];
        int right = left + view.getMeasuredWidth();
        int bottom = top + view.getMeasuredHeight();
        if (y >= top && y <= bottom && x >= left
                && x <= right) {
            return true;
        }
        return false;
    }

    private void startEditPreview() {
        //设置预览的原始音频的音量
        mEditKit.setOriginAudioVolume(0.4f);
        //设置是否循环预览
        mEditKit.setLooping(true);
        //开启预览
        mEditKit.startEditPreview();

        mOriginAudioVolumeSeekBar.setProgress((int) (mEditKit.getOriginAudioVolume() * 100));
    }

    /*********************************字幕 begin***************************************/
    /**
     * 字幕区域被选中
     */
    private void onTextColorSelected() {
        mColorPicker.show();
        Button okColor = (Button) mColorPicker.findViewById(R.id.okColorButton);
        okColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeTextColor(mColorPicker.getColor());
                mColorPicker.dismiss();
            }
        });
    }

    /**
     * 修改字体颜色
     *
     * @param newColor
     */
    private void changeTextColor(int newColor) {
        mTextColorSelect.setBackgroundColor(newColor);
        mTextView.setTextColor(newColor);
    }

    private void onDeleteStickers() {
        mKSYStickerView.removeStickers();
    }

    private StickerAdapter.OnStickerItemClick mOnStickerItemClick = new StickerAdapter.OnStickerItemClick() {
        @Override
        public void selectedStickerItem(String path) {
            //辅助区域的删除按钮
            if (mStickerDeleteBitmap == null) {
                mStickerDeleteBitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.sticker_delete);
            }

            //辅助区域的旋转按钮
            if (mStickerRotateBitmap == null) {
                mStickerRotateBitmap = BitmapFactory.decodeResource(getResources(),
                        R.drawable.sticker_rotate);
            }

            //辅助区域信息
            if (mStickerHelpBoxInfo == null) {
                mStickerHelpBoxInfo = new StickerHelpBoxInfo();
                mStickerHelpBoxInfo.deleteBit = mStickerDeleteBitmap;  //删除按钮
                mStickerHelpBoxInfo.rotateBit = mStickerRotateBitmap;  //旋转按钮
                //辅助区域画笔
                Paint helpBoxPaint = new Paint();
                helpBoxPaint.setColor(Color.BLACK);
                helpBoxPaint.setStyle(Paint.Style.STROKE);
                helpBoxPaint.setAntiAlias(true);  //抗锯齿
                helpBoxPaint.setStrokeWidth(4);     //宽度
                mStickerHelpBoxInfo.helpBoxPaint = helpBoxPaint;
            }

            mKSYStickerView.addSticker(getImageFromAssetsFile(path), mStickerHelpBoxInfo);

        }
    };

    private StickerAdapter.OnStickerItemClick mOnTextStickerItemClick = new StickerAdapter
            .OnStickerItemClick() {
        @Override
        public void selectedStickerItem(String path) {
            KSYTextView.DrawTextParams textParams = new KSYTextView.DrawTextParams();
            textParams.textPaint = null;
            textParams.autoNewLine = false;
            //字幕范围设置，不太精准，后续完善
            if (path.contains("3")) {
                textParams.text_left_padding = 40;
                textParams.text_right_padding = 41;
                textParams.text_top_padding = 82;
                textParams.text_bottom_padding = 28;
            }
            if (path.contains("1")) {
                textParams.text_left_padding = 33;
                textParams.text_right_padding = 35;
                textParams.text_top_padding = 61;
                textParams.text_bottom_padding = 69;
            }

            if (path.contains("2")) {
                textParams.text_left_padding = 185;
                textParams.text_right_padding = 27;
                textParams.text_top_padding = 81;
                textParams.text_bottom_padding = 18;
            }

            if (path.contains("4")) {
                textParams.text_left_padding = 42;
                textParams.text_right_padding = 108;
                textParams.text_top_padding = 43;
                textParams.text_bottom_padding = 24;
            }

            if (path.contains("5")) {
                textParams.text_left_padding = 105;
                textParams.text_right_padding = 104;
                textParams.text_top_padding = 89;
                textParams.text_bottom_padding = 66;
            }
            textParams.bitmap = getImageFromAssetsFile(path);

            mTextView.updateTextParams(textParams);
        }
    };

    /**
     * 提示的辅助区域
     */
    private void initStickerHelpBox() {
        if (mStickerDeleteBitmap == null) {
            mStickerDeleteBitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.sticker_delete);
        }

        if (mStickerRotateBitmap == null) {
            mStickerRotateBitmap = BitmapFactory.decodeResource(getResources(),
                    R.drawable.sticker_rotate);
        }

        if (mStickerHelpBoxInfo == null) {
            mStickerHelpBoxInfo = new StickerHelpBoxInfo();
            mStickerHelpBoxInfo.deleteBit = mStickerDeleteBitmap;
            mStickerHelpBoxInfo.rotateBit = mStickerRotateBitmap;
            Paint helpBoxPaint = new Paint();
            helpBoxPaint.setColor(Color.BLACK);
            helpBoxPaint.setStyle(Paint.Style.STROKE);
            helpBoxPaint.setAntiAlias(true);
            helpBoxPaint.setStrokeWidth(4);
            mStickerHelpBoxInfo.helpBoxPaint = helpBoxPaint;
        }
    }

    private TextWatcher mTextInputChangedListener = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

        }

        @Override
        public void afterTextChanged(Editable editable) {
            String text = editable.toString().trim();
            mTextView.setText(text);
        }
    };

    public KSYTextView.OnTextRectSelected mTextRectSelected = new KSYTextView.OnTextRectSelected() {
        @Override
        public void textRectSelected() {
            //显示输入框
            mTextInput.requestFocus();
            mInputMethodManager.showSoftInput(mTextInput, InputMethodManager.RESULT_SHOWN);
        }
    };
    /*********************************字幕 end***************************************/

    /**
     * 从Assert文件夹中读取位图数据
     *
     * @param fileName
     * @return
     */
    private Bitmap getImageFromAssetsFile(String fileName) {
        Bitmap image = null;
        AssetManager am = getResources().getAssets();
        try {
            InputStream is = am.open(fileName);
            image = BitmapFactory.decodeStream(is);
            is.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }


    private void onWaterMarkLogoClick(boolean isCheck) {
        if (isCheck) {
            mEditKit.showWaterMarkLogo(mLogoPath, 0.08f, 0.04f, 0.20f, 0, 0.8f);
        } else {
            mEditKit.hideWaterMarkLogo();
        }
    }

    private void onPauseClick() {
        if (mPauseView.getDrawable().getLevel() == 2) {
            mEditKit.pausePlay(true);
            mPauseView.getDrawable().setLevel(1);
        } else {
            mEditKit.pausePlay(false);
            mPauseView.getDrawable().setLevel(2);
        }
    }

    private void onSpeedClick(boolean plus) {
        mEditKit.updateSpeed(plus);
        DecimalFormat decimalFormat = new DecimalFormat(".0");
        String text = decimalFormat.format(mEditKit.getSpeed());
        mSpeedInfo.setText(text);
    }

    private void onBackoffClick() {
        if ((mBottomViewPreIndex == WATER_MARK_INDEX) ||
                (mBottomViewPreIndex != WATER_MARK_INDEX &&
                        mBottomViewList[mBottomViewPreIndex].getVisibility() != View.VISIBLE)) {
            EditActivity.this.finish();
        } else {
            mBottomViewList[mBottomViewPreIndex].setVisibility(View.INVISIBLE);
            if (mTitleAdapter != null) {
                mTitleAdapter.clear();
            }
        }
    }

    private void onNextClick() {
        showPopupWindow();
    }

    private void showPopupWindow() {
        View contentView = LayoutInflater.from(this).inflate(R.layout.config_popup_layout, null);
        mConfigWindow = new PopupWindow(contentView,
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT, true);
        mConfigWindow.setContentView(contentView);
        mOutRes480p = (TextView) contentView.findViewById(R.id.output_config_r480p);
        mOutRes480p.setOnClickListener(mButtonObserver);
        mOutRes540p = (TextView) contentView.findViewById(R.id.output_config_r540p);
        mOutRes540p.setOnClickListener(mButtonObserver);
        mOutEncodeWithH264 = (TextView) contentView.findViewById(R.id.output_config_h264);
        mOutEncodeWithH264.setOnClickListener(mButtonObserver);
        mOutEncodeWithH265 = (TextView) contentView.findViewById(R.id.output_config_h265);
        mOutEncodeWithH265.setOnClickListener(mButtonObserver);
        mOutForMP4 = (TextView) contentView.findViewById(R.id.output_config_mp4);
        mOutForMP4.setOnClickListener(mButtonObserver);
        mOutForGIF = (TextView) contentView.findViewById(R.id.output_config_gif);
        mOutForGIF.setOnClickListener(mButtonObserver);
        mOutProfileGroup = new TextView[3];
        for (int i = 0; i < mOutProfileGroup.length; i++) {
            mOutProfileGroup[i] = (TextView) contentView.findViewById(OUTPUT_PROFILE_ID[i]);
            mOutProfileGroup[i].setOnClickListener(mButtonObserver);
        }
        mOutFrameRate = (EditText) contentView.findViewById(R.id.output_config_frameRate);
        mOutVideoBitrate = (EditText) contentView.findViewById(R.id.output_config_video_bitrate);
        mOutAudioBitrate = (EditText) contentView.findViewById(R.id.output_config_audio_bitrate);
        mComposeConfig = new ShortVideoConfig();
        mOutputConfirm = (TextView) contentView.findViewById(R.id.output_confirm);
        mOutputConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOutputConfirmClick();
            }
        });
        mOutRes480p.setActivated(true);
        mOutEncodeWithH264.setActivated(true);
        mOutForMP4.setActivated(true);
        mOutProfileGroup[1].setActivated(true);
        View rootView = LayoutInflater.from(this).inflate(R.layout.edit_activity, null);
        mConfigWindow.showAtLocation(rootView, Gravity.CENTER, 0, 0);

    }

    private void onOutputConfirmClick() {
        confirmConfig();
        if (mConfigWindow.isShowing()) {
            mConfigWindow.dismiss();
        }
        //配置合成参数
        if (mComposeConfig != null) {
            //配置合成参数
            mEditKit.setTargetResolution(mComposeConfig.resolution);
            mEditKit.setVideoFps(mComposeConfig.fps);
            mEditKit.setVideoCodecId(mComposeConfig.encodeType);
            mEditKit.setVideoEncodeProfile(mComposeConfig.encodeProfile);
            mEditKit.setAudioKBitrate(mComposeConfig.audioBitrate);
            mEditKit.setVideoKBitrate(mComposeConfig.videoBitrate);
            //关闭上一次合成窗口
            if (mComposeAlertDialog != null) {
                mComposeAlertDialog.closeDialog();
            }
            mComposeAlertDialog = new ComposeAlertDialog(EditActivity.this, R.style.dialog);

            //设置合成路径
            String fileFolder = "/sdcard/ksy_sv_compose_test";
            File file = new File(fileFolder);
            if (!file.exists()) {
                file.mkdir();
            }

            StringBuilder composeUrl = new StringBuilder(fileFolder).append("/").append(System
                    .currentTimeMillis());
            if (mComposeConfig.encodeType == AVConst.CODEC_ID_GIF) {
                composeUrl.append(".gif");
            } else {
                composeUrl.append(".mp4");
            }
            Log.d(TAG, "compose Url:" + composeUrl);
            //开始合成
            mComposeFinished = false;
            mEditKit.startCompose(composeUrl.toString());
        }
    }

    private void confirmConfig() {
        if (mOutRes480p.isActivated()) {
            mComposeConfig.resolution = StreamerConstants.VIDEO_RESOLUTION_480P;
        } else if (mOutRes540p.isActivated()) {
            mComposeConfig.resolution = StreamerConstants.VIDEO_RESOLUTION_540P;
        }
        if (mOutEncodeWithH264.isActivated()) {
            mComposeConfig.encodeType = AVConst.CODEC_ID_AVC;
        } else if (mOutEncodeWithH265.isActivated()) {
            mComposeConfig.encodeType = AVConst.CODEC_ID_HEVC;
        }
        if (mOutForGIF.isActivated()) {
            mComposeConfig.encodeType = AVConst.CODEC_ID_GIF;
        }
        for (int i = 0; i < mOutProfileGroup.length; i++) {
            if (mOutProfileGroup[i].isActivated()) {
                mComposeConfig.encodeProfile = ENCODE_PROFILE_TYPE[i];
                break;
            }
        }
        mComposeConfig.fps = Integer.parseInt(mOutFrameRate.getText().toString());
        mComposeConfig.videoBitrate = Integer.parseInt(mOutVideoBitrate.getText().toString());
        mComposeConfig.audioBitrate = Integer.parseInt(mOutAudioBitrate.getText().toString());
    }

    private KSYEditKit.OnErrorListener mOnErrorListener = new KSYEditKit.OnErrorListener() {
        @Override
        public void onError(int type, long msg) {
            switch (type) {
                case ShortVideoConstants.SHORTVIDEO_ERROR_COMPOSE_FAILED_UNKNOWN:
                case ShortVideoConstants.SHORTVIDEO_ERROR_COMPOSE_FILE_CLOSE_FAILED:
                case ShortVideoConstants.SHORTVIDEO_ERROR_COMPOSE_FILE_FORMAT_NOT_SUPPORTED:
                case ShortVideoConstants.SHORTVIDEO_ERROR_COMPOSE_FILE_OPEN_FAILED:
                case ShortVideoConstants.SHORTVIDEO_ERROR_COMPOSE_FILE_WRITE_FAILED:
                    Log.d(TAG, "compose failed:" + type);
                    Toast.makeText(EditActivity.this,
                            "Compose Failed:" + type, Toast.LENGTH_LONG).show();
                    if (mComposeAlertDialog != null) {
                        mComposeAlertDialog.closeDialog();
                        resumeEditPreview();
                    }
                    break;
                case ShortVideoConstants.SHORTVIDEO_ERROR_SDK_AUTHFAILED:
                    Log.d(TAG, "sdk auth failed:" + type);
                    Toast.makeText(EditActivity.this,
                            "Auth failed can't start compose:" + type, Toast.LENGTH_LONG).show();
                    break;
                case ShortVideoConstants.SHORTVIDEO_ERROR_UPLOAD_KS3_TOKEN_ERROR:
                    Log.d(TAG, "ks3 upload token error, upload to ks3 failed");
                    Toast.makeText(EditActivity.this,
                            "Auth failed can't start upload:" + type, Toast.LENGTH_LONG).show();
                    if (mComposeAlertDialog != null) {
                        mComposeAlertDialog.uploadFinished(false);
                    }
                    break;


            }
        }
    };

    private KSYEditKit.OnInfoListener mOnInfoListener = new KSYEditKit.OnInfoListener() {
        @Override
        public Object onInfo(int type, String... msgs) {
            Log.d(TAG, "on info:" + type);
            switch (type) {
                case ShortVideoConstants.SHORTVIDEO_EDIT_PREPARED:
                    mEditPreviewDuration = mEditKit.getEditDuration();
                    mPreviewLength = mEditPreviewDuration;
                    initSeekBar();
                    initThumbnailAdapter();
                    break;
                case ShortVideoConstants.SHORTVIDEO_COMPOSE_START: {
                    mEditKit.pauseEditPreview();
                    if (mComposeAlertDialog != null) {
                        mComposeAlertDialog.setCancelable(false);
                        mComposeAlertDialog.show();

                        //部分机型dialog显示不能全屏
                        WindowManager windowManager = getWindowManager();
                        Display display = windowManager.getDefaultDisplay();
                        WindowManager.LayoutParams lp = mComposeAlertDialog.getWindow()
                                .getAttributes();
                        lp.width = display.getWidth(); //设置宽度
                        lp.height = display.getHeight();
                        mComposeAlertDialog.getWindow().setAttributes(lp);
                        mComposeAlertDialog.getWindow().getDecorView().setPadding(0, 0, 0, 0);

                        mComposeAlertDialog.composeStarted();
                    }
                    return null;
                }
                case ShortVideoConstants.SHORTVIDEO_COMPOSE_FINISHED: {
                    //合成结束需要置为null，再次预览时重新创建
                    clearImgFilter();
                    if (mComposeAlertDialog != null && mComposeAlertDialog.isShowing()) {
                        mComposeAlertDialog.composeFinished(msgs[0]);
                        mComposeFinished = true;
                    }

                    //通过ProbeMediaInfoTools获取合成后文件信息
//                    ProbeMediaInfoTools probeMediaInfoTools = new ProbeMediaInfoTools();
//                    probeMediaInfoTools.probeMediaInfo(msgs[0],
//                            new ProbeMediaInfoTools.ProbeMediaInfoListener() {
//                        @Override
//                        public void probeMediaInfoFinished(ProbeMediaInfoTools.MediaInfo info) {
//                            if(info != null) {
//                                Log.e(TAG, "url:" + info.url);
//                                Log.e(TAG, "duration:" + info.duration);
//                            }
//                        }
//                    });
//                    //get thumbnail for first frame
//                    probeMediaInfoTools.getVideoThumbnailAtTime(msgs[0],0,0,0);

                    // 可在此处触发sdk将合成后文件上传到ks3，示例代码如下：
//                    //上传必要信息：bucket,objectkey，及PutObjectResponseHandler上传过程回调
//                    String mineType = FileUtils.getMimeType(new File(msgs[0]));
//                    StringBuilder objectKey = new StringBuilder(getPackageName() +
//                            "/" + System.currentTimeMillis());
//                    if (mineType == FileUtils.MINE_TYPE_MP4) {
//                        objectKey.append(".mp4");
//                    } else if (mineType == FileUtils.MINE_TYPE_GIF) {
//                        objectKey.append(".gif");
//                    }
//                    mCurObjectKey = objectKey.toString();
//                    KS3ClientWrap.KS3UploadInfo bucketInfo = new KS3ClientWrap.KS3UploadInfo
//                            ("ksvsdemo", mCurObjectKey, mPutObjectResponseHandler);
//                    return bucketInfo;
                    return null;
                }
                case ShortVideoConstants.SHORTVIDEO_COMPOSE_ABORTED:
                    break;
                case ShortVideoConstants.SHORTVIDEO_GET_KS3AUTH: {
                    if (msgs.length == 6) {
                        if (mTokenTask == null) {
                            mTokenTask = new KS3TokenTask(getApplicationContext());
                        }

                        return mTokenTask.requsetTokenToAppServer(msgs[0], msgs[1],
                                msgs[2], msgs[3], msgs[4], msgs[5]);
                    } else {
                        return null;
                    }
                }
                default:
                    return null;
            }
            return null;
        }
    };

    private PutObjectResponseHandler mPutObjectResponseHandler = new PutObjectResponseHandler() {
        @Override
        public void onTaskFailure(int statesCode, Ks3Error error, Header[] responceHeaders, String response, Throwable paramThrowable) {
            Log.e(TAG, "onTaskFailure:" + statesCode);
            if (mComposeAlertDialog != null && mComposeAlertDialog.isShowing()) {
                mComposeAlertDialog.uploadFinished(false);
            }
        }

        @Override
        public void onTaskSuccess(int statesCode, Header[] responceHeaders) {
            Log.d(TAG, "onTaskSuccess:" + statesCode);
            if (mComposeAlertDialog != null && mComposeAlertDialog.isShowing()) {
                mComposeAlertDialog.uploadFinished(true);
            }
        }

        @Override
        public void onTaskStart() {
            Log.d(TAG, "onTaskStart");
            if (mComposeAlertDialog != null && mComposeAlertDialog.isShowing()) {
                mComposeAlertDialog.uploadStarted();
            }
        }

        @Override
        public void onTaskFinish() {
            Log.d(TAG, "onTaskFinish");
        }

        @Override
        public void onTaskCancel() {
            Log.d(TAG, "onTaskCancel");
        }

        @Override
        public void onTaskProgress(double progress) {
            if (mComposeAlertDialog != null && mComposeAlertDialog.isShowing()) {
                mComposeAlertDialog.uploadProgress(progress);
            }
        }
    };

    private class ButtonObserver implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.click_to_next:
                    onNextClick();
                    break;
                case R.id.click_to_pause:
                    onPauseClick();
                    break;
                case R.id.click_to_delete_stickers:
                    onDeleteStickers();
                    break;
                case R.id.text_color:
                    onTextColorSelected();
                    break;
                case R.id.speed_up:
                    onSpeedClick(true);
                    break;
                case R.id.speed_down:
                    onSpeedClick(false);
                    break;
                case R.id.output_config_r480p:
                    mOutRes480p.setActivated(true);
                    mOutRes540p.setActivated(false);
                    break;
                case R.id.output_config_r540p:
                    mOutRes480p.setActivated(false);
                    mOutRes540p.setActivated(true);
                    break;
                case R.id.output_config_h264:
                    mOutEncodeWithH264.setActivated(true);
                    mOutEncodeWithH265.setActivated(false);
                    break;
                case R.id.output_config_h265:
                    mOutEncodeWithH264.setActivated(false);
                    mOutEncodeWithH265.setActivated(true);
                    break;
                case R.id.output_config_mp4:
                    mOutForMP4.setActivated(true);
                    mOutForGIF.setActivated(false);
                    mOutEncodeWithH264.setEnabled(true);
                    mOutEncodeWithH265.setEnabled(true);
                    break;
                case R.id.output_config_gif:
                    mOutForMP4.setActivated(false);
                    mOutForGIF.setActivated(true);
                    mOutEncodeWithH264.setActivated(false);
                    mOutEncodeWithH265.setActivated(false);
                    mOutEncodeWithH264.setEnabled(false);
                    mOutEncodeWithH265.setEnabled(false);
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

    private class SeekBarChangedObserver implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) {
                return;
            }
            float val = progress / 100.f;
            switch (seekBar.getId()) {
                case R.id.record_mic_audio_volume:
                    mEditKit.setOriginAudioVolume(val);
                    break;
                case R.id.record_music_audio_volume:
                    mEditKit.setBgmVolume(val);
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

    /**********************************video range begin*************************************/
    /**
     * init video range ui
     */
    private void initVideoRange() {
        mVideoRangeStart = (TextView) findViewById(R.id.range_start);  //裁剪开始位置
        mVideoRange = (TextView) findViewById(R.id.range);    //裁剪时长
        mVideoRangeEnd = (TextView) findViewById(R.id.range_end);  //裁剪结束位置

        //裁剪bar
        mVideoRangeSeekBar = (VideoRangeSeekBar) findViewById(R.id.seekbar);
        mVideoRangeSeekBar.setOnVideoMaskScrollListener(mVideoMaskScrollListener);
        mVideoRangeSeekBar.setOnRangeBarChangeListener(onRangeBarChangeListener);

        //缩略图显示
        mVideoThumbnailList = (HorizontalListView) findViewById(R.id.hlistview);
        mVideoThumbnailList.setOnScrollListener(mVideoThumbnailScrollListener);
    }

    /**
     * init video seek range
     */
    private void initSeekBar() {
        long durationMS = mEditKit.getEditDuration();
        float durationInSec = durationMS * 1.0f / 1000;
        if (durationMS > mMaxClipSpanMs) {
            mVideoRangeSeekBar.setMaxRange(mMaxClipSpanMs * 1.0f / 1000);
        } else {
            mVideoRangeSeekBar.setMaxRange(durationInSec);
        }

        mVideoRangeSeekBar.setMinRange(1.0f);

        if (durationInSec > 300.0f) {
            mVideoRangeSeekBar.setRange(0.0f, 300.0f);
        } else {
            mVideoRangeSeekBar.setRange(0.0f, durationInSec);
        }
    }

    /**
     * init video thumbnail
     */
    private void initThumbnailAdapter() {
        float picWidth;  //每个thumbnail显示的宽度
        if (mVideoRangeSeekBar == null) {
            picWidth = 60;
        } else {
            picWidth = mVideoRangeSeekBar.getFrameWidth();
        }
        long durationMS = mEditKit.getEditDuration();

        //list区域需要显示的item个数
        int totalFrame;
        //比最大裁剪时长大的视频,每长mMaxClipSpanMs长度,则增加8个thumbnail
        //比最大裁剪时长小的视频,最多显示8个thumbnail
        if (durationMS > mMaxClipSpanMs) {
            totalFrame = (int) (durationMS * 8) / mMaxClipSpanMs;
        } else {
            totalFrame = 10;
        }

        int mm = totalFrame;

        VideoThumbnailInfo[] listData = new VideoThumbnailInfo[totalFrame];
        for (int i = 0; i < totalFrame; i++) {
            listData[i] = new VideoThumbnailInfo();
            if (durationMS > mMaxClipSpanMs) {
                listData[i].mCurrentTime = i * ((float) durationMS / 1000) * (1.0f / mm);
            } else {
                if (i > 0 && i < 9) {
                    listData[i].mCurrentTime = (i - 1) * ((float) durationMS / 1000) * (1.0f / 8);
                }
            }

            if (i == 0 && mVideoRangeSeekBar != null) {
                listData[i].mType = VideoThumbnailInfo.TYPE_START;
                listData[i].mWidth = (int) mVideoRangeSeekBar.getMaskWidth();
            } else if (i == totalFrame - 1 && mVideoRangeSeekBar != null) {
                listData[i].mType = VideoThumbnailInfo.TYPE_END;
                listData[i].mWidth = (int) mVideoRangeSeekBar.getMaskWidth();
            } else {
                listData[i].mType = VideoThumbnailInfo.TYPE_NORMAL;
                listData[i].mWidth = (int) picWidth;
            }
        }

        mVideoThumbnailAdapter = new VideoThumbnailAdapter(this, listData, mEditKit);
        mVideoThumbnailList.setAdapter(mVideoThumbnailAdapter);
    }

    VideoRangeSeekBar.OnRangeBarChangeListener onRangeBarChangeListener = new VideoRangeSeekBar.OnRangeBarChangeListener() {

        @Override
        public void onIndexChangeListener(VideoRangeSeekBar rangeBar,
                                          float rangeStart, float rangeEnd, final int change, boolean toEnd) {

            float toLen = (mVideoRangeSeekBar.getRangeEnd() + mHLVOffsetX) * 1000;
            if (toEnd && toLen >= mMaxClipSpanMs && mMaxClipSpanMs > 0 && toLen <= mEditPreviewDuration) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(EditActivity.this, "视频总长不能超过" + mMaxClipSpanMs / 1000 + "秒 " +
                                "T_T", Toast.LENGTH_LONG);
                    }
                });
            }

            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mHLVOffsetX >= 7.5f && mHLVOffsetX <= 8.5f
                            && !mVideoRangeSeekBar.isTouching()) {
                        mHLVOffsetX = 8.0f;
                        mVideoRangeSeekBar.setRange(mVideoRangeSeekBar.getRangeStart(),
                                mVideoRangeSeekBar.getRangeStart() + mHLVOffsetX);
                    }
                    setRangeTextView(mHLVOffsetX);
                    //seek bug
//                    if (change == VideoRangeSeekBar.OnRangeBarChangeListener.LEFT_CHANGE) {
//                        seekToPreview(mVideoRangeSeekBar.getRangeStart() + mHLVOffsetX);
//                    } else if (change == VideoRangeSeekBar.OnRangeBarChangeListener.RIGHT_CHANGE) {
//                        seekToPreview(mVideoRangeSeekBar.getRangeEnd() + mHLVOffsetX);
//                        mMainHandler.postDelayed(new Runnable() {
//                            @Override
//                            public void run() {
//                                seekToPreview(mVideoRangeSeekBar.getRangeStart() + mHLVOffsetX);
//                            }
//                        }, 500);
//                    }
                }
            });
        }

        @Override
        public void onActionUp() {
            rangeLoopPreview();
        }
    };

    /**
     * loop preview duraing range
     */
    private void rangeLoopPreview() {
        long startTime = (long) ((mVideoRangeSeekBar.getRangeStart() + mHLVOffsetX) * 1000);
        long endTime = (long) ((mVideoRangeSeekBar.getRangeEnd() + mHLVOffsetX) * 1000);

        mEditKit.setEditPreviewRanges(startTime, endTime);
    }

    /**
     * seek to preview
     *
     * @param second
     */
    private void seekToPreview(float second) {
        if (mVideoRangeSeekBar != null) {
            mVideoRangeSeekBar.setIndicatorVisible(false);
        }

        long seekTo = (long) (second * 1000);
        if (seekTo > mEditPreviewDuration) {
            seekTo = mEditPreviewDuration;
        }

        if (seekTo < 0) {
            seekTo = 0;
        }

        Log.d(TAG, "seekto:" + seekTo);
        mEditKit.seekEditPreview(seekTo);

        if (mVideoRangeSeekBar != null) {
            mVideoRangeSeekBar.setIndicatorOffsetSec((mEditKit.getEditPreviewCurrentPosition() * 1.0f - mHLVOffsetX * 1000) /
                    1000);
        }

        Log.d(TAG, "seek currentpostion:" + mEditKit.getEditPreviewCurrentPosition());
    }

    private void setRangeTextView(float offset) {
        Log.d(TAG, "setRangeTextView offset:" + offset);
        Log.d(TAG, "setRangeTextView:" + mVideoRangeSeekBar.getRangeStart() + ","
                + mVideoRangeSeekBar.getRangeEnd());
        mVideoRangeStart.setText(formatTimeStr(mVideoRangeSeekBar.getRangeStart() + offset));
        mVideoRangeEnd.setText(formatTimeStr(mVideoRangeSeekBar.getRangeEnd() + offset));

        mVideoRange.setText(formatTimeStr2(((int) (10 * mVideoRangeSeekBar.getRangeEnd()))
                - (int) (10 * mVideoRangeSeekBar.getRangeStart())));
        mPreviewLength = (mVideoRangeSeekBar.getRangeEnd() - mVideoRangeSeekBar.getRangeStart()) * 1000;
        if (mAudioSeekLayout != null && mAudioLength != 0 &&
                mPreviewLength < mAudioLength) {
            mAudioSeekLayout.updateAudioSeekUI(mAudioLength, mPreviewLength);
        }
    }

    private String formatTimeStr2(int s) {
        int second = s / 10;
        int left = s % 10;

        return String.format("%d.%d", second, left);
    }

    private String formatTimeStr(float s) {
        int minute = ((int) s) / 60;
        int second = ((int) s) % 60;
        int left = ((int) (s * 10)) % 10;

        return String.format("%02d:%02d.%d", minute, second, left);
    }

    VideoRangeSeekBar.OnVideoMaskScrollListener mVideoMaskScrollListener = new VideoRangeSeekBar.OnVideoMaskScrollListener() {

        @Override
        public void onVideoMaskScrollListener(VideoRangeSeekBar rangeBar,
                                              MotionEvent event) {
            mVideoThumbnailList.dispatchTouchEvent(event);
        }
    };

    HorizontalListView.OnScrollListener mVideoThumbnailScrollListener = new HorizontalListView.OnScrollListener() {

        @Override
        public void onScroll(final int currentX) {
            final String msg = String.format("currentXX: %d", currentX);
            Log.d(TAG, msg);

            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "currentX:" + currentX);
                    mHLVOffsetX = mVideoRangeSeekBar.getRange(currentX);

                    if (mEditPreviewDuration > mMaxClipSpanMs) {
                        if ((mVideoRangeSeekBar.getRangeEnd() + mHLVOffsetX) * 1000 >= mEditPreviewDuration) {
                            mHLVOffsetX = (mEditPreviewDuration / 1000 - mVideoRangeSeekBar.getRangeEnd());
                        }
                    }

                    setRangeTextView(mHLVOffsetX);

                    if (mLastX != mVideoRangeSeekBar.getRangeStart() + mHLVOffsetX) {
                        rangeLoopPreview();
                        mLastX = mVideoRangeSeekBar.getRangeStart() + mHLVOffsetX;
                    }
                }
            });
        }
    };

    /***********************************
     * video range end
     *****************************************/

    private void initTitleRecycleView() {
        View pitchLayout = findViewById(R.id.bgm_pitch);
        pitchLayout.setVisibility(View.GONE);
        String[] items = {"美颜", "滤镜", "水印", "变速", "裁剪", "音乐", "变声", "混响", "贴纸", "字幕"};
        mTitleData = Arrays.asList(items);
        mTitleView = (RecyclerView) findViewById(R.id.edit_title_recyclerView);
        mTitleAdapter = new BottomTitleAdapter(this, mTitleData);
        BottomTitleAdapter.OnItemClickListener listener = new BottomTitleAdapter.OnItemClickListener() {
            @Override
            public void onClick(int curIndex, int preIndex) {
                mBottomViewPreIndex = curIndex;
                if (curIndex != WATER_MARK_INDEX) {
                    mBottomViewList[curIndex].setVisibility(View.VISIBLE);
                    if (curIndex == STICKER_LAYOUT_INDEX && mKSYStickerView.getVisibility() != View.VISIBLE) {
                        mKSYStickerView.setVisibility(View.VISIBLE);
                    }
                    if (curIndex == SUBTITLE_LAYOUT_INDEX && mTextView.getVisibility() != View.VISIBLE) {
                        mTextView.setVisibility(View.VISIBLE);
                    }
                } else {
                    if (curIndex != preIndex) {
                        mWaterMarkChecked = true;
                        onWaterMarkLogoClick(mWaterMarkChecked);
                    } else {
                        mWaterMarkChecked = !mWaterMarkChecked;
                        onWaterMarkLogoClick(mWaterMarkChecked);
                    }
                }
                if (preIndex != WATER_MARK_INDEX && preIndex != -1 &&
                        curIndex != preIndex) {
                    mBottomViewList[preIndex].setVisibility(View.GONE);
                }

                initBeautyUI();
            }
        };
        mTitleAdapter.setOnItemClickListener(listener);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mTitleView.setLayoutManager(layoutManager);
        mTitleView.setAdapter(mTitleAdapter);
    }

    private void clearImgFilter() {
        mImgBeautyProFilter = null;
        mEffectFilterIndex = FILTER_DISABLE;
    }

    private void addImgFilter() {
        ImgBeautyProFilter proFilter;
        ImgBeautySpecialEffectsFilter specialEffectsFilter;
        List<ImgFilterBase> filters = new LinkedList<>();

        if (mImgBeautyProFilter != null) {
            proFilter = new ImgBeautyProFilter(mEditKit.getGLRender(), getApplicationContext());
            proFilter.setGrindRatio(mImgBeautyProFilter.getGrindRatio());
            proFilter.setRuddyRatio(mImgBeautyProFilter.getRuddyRatio());
            proFilter.setWhitenRatio(mImgBeautyProFilter.getWhitenRatio());
            mImgBeautyProFilter = proFilter;
            filters.add(proFilter);
        }

        if (mEffectFilterIndex != FILTER_DISABLE) {
            specialEffectsFilter = new ImgBeautySpecialEffectsFilter(mEditKit.getGLRender(),
                    getApplicationContext(), mEffectFilterIndex);
            filters.add(specialEffectsFilter);
        }

        if (filters.size() > 0) {

            mEditKit.getImgTexFilterMgt().setFilter(filters);

        } else {
            mEditKit.getImgTexFilterMgt().setFilter((ImgTexFilterBase) null);
        }
    }

    private void setBeautyFilter() {
        if (mImgBeautyProFilter == null) {
            //Demo中当前演示该美颜被设置后，未演示取消，后续完善，更多美颜参考：
            //https://github.com/ksvc/KSYStreamer_Android/wiki/Video_Filter_Inner
            //注意：该filter只能被set一次，若调用用过mKSYRecordKit.getImgTexFilterMgt().setFilter(null)
            //后不能再使用该filter，需要重新new
            mImgBeautyProFilter = new ImgBeautyProFilter(mEditKit.getGLRender(), EditActivity.this);
            addImgFilter();
        }
    }

    private void setEffectFilter(int type) {
        mEffectFilterIndex = type;
        addImgFilter();
    }

    private void initBeautyUI() {
        if (mBeautyLayout.getVisibility() == View.VISIBLE) {
            setBeautyFilter();
            mGrindSeekBar.setProgress((int) (mImgBeautyProFilter.getGrindRatio() * 100));
            mWhitenSeekBar.setProgress((int) (mImgBeautyProFilter.getWhitenRatio() * 100));
            int ruddyVal = (int) (mImgBeautyProFilter.getRuddyRatio() * 50 + 50);
            mRuddySeekBar.setProgress(ruddyVal);
        }

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
                            mImgBeautyProFilter.setGrindRatio(val);
                        } else if (seekBar == mWhitenSeekBar) {
                            mImgBeautyProFilter.setWhitenRatio(val);
                        } else if (seekBar == mRuddySeekBar) {
                            val = progress / 50.f - 1.0f;
                            mImgBeautyProFilter.setRuddyRatio(val);
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
    }

    /**
     * 视频滤镜
     * https://github.com/ksvc/KSYStreamer_Android/wiki/style_filter
     */
    private void initFilterUI() {
        final String[] NAME_LIST = {"小清新", "靓丽", "甜美可人", "怀旧"};
        final int[] IMAGE_ID = {R.drawable.filter_fresh, R.drawable.filter_beautiful,
                R.drawable.filter_sweet, R.drawable.filter_old_photo};
        final int[] FILTER_TYPE = {ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_FRESHY,
                ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_BEAUTY,
                ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_SWEETY,
                ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_SEPIA};
        List<ImageTextAdapter.Data> filterData = new ArrayList<>();
        for (int i = 0; i < NAME_LIST.length; i++) {
            Drawable image = getResources().getDrawable(IMAGE_ID[i]);
            String type = NAME_LIST[i];
            ImageTextAdapter.Data data = new ImageTextAdapter.Data(image, type);
            filterData.add(data);
        }
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

    private void resumeEditPreview() {
        Log.d(TAG, "resumeEditPreview ");
        mEditKit.resumeEditPreview();
        initBeautyUI();
        mFilterOriginImage.callOnClick();
        initBeautyUI();
        mFilterOriginImage.callOnClick();
    }

    private void initBgmView() {
        List<BgmSelectAdapter.BgmData> list = DataFactory.getBgmData(getApplicationContext());
        final BgmSelectAdapter adapter = new BgmSelectAdapter(this, list);
        mBgmRecyclerView = (RecyclerView) findViewById(R.id.bgm_recycler);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mBgmRecyclerView.setLayoutManager(manager);
        setEnableBgmEdit(false);
        BgmSelectAdapter.OnItemClickListener listener = new BgmSelectAdapter.OnItemClickListener() {
            @Override
            public void onCancel() {
                mFirstPlay = true;
                setEnableBgmEdit(false);
                mEditKit.stopBgm();
                mAudioSeekLayout.setVisibility(View.GONE);
            }

            @Override
            public void onSelected(String path) {
                mFirstPlay = true;
                setEnableBgmEdit(true);
                mEditKit.startBgm(path, true);
            }

            @Override
            public void onImport() {
                mFirstPlay = true;
                importMusicFile();
            }
        };
        adapter.setOnItemClickListener(listener);
        mBgmRecyclerView.setAdapter(adapter);
    }

    /**
     * 根据是否有背景音乐选中来设置相应的编辑控件是否可用
     */
    private void setEnableBgmEdit(boolean enable) {
        if (mBgmVolumeSeekBar != null) {
            mBgmVolumeSeekBar.setEnabled(enable);
        }
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
        SoundEffectAdapter soundChangeAdapter = new SoundEffectAdapter(this, soundChangeData);
        soundChangeAdapter.setOnItemClickListener(soundChangeListener);
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
        SoundEffectAdapter reverbAdapter = new SoundEffectAdapter(this, reverbData);
        reverbAdapter.setOnItemClickListener(reverbListener);
        mSoundChangeRecycler.setAdapter(soundChangeAdapter);
        mReverbRecycler.setAdapter(reverbAdapter);
    }

    /**
     * 添加音频滤镜，支持变声和混响同时生效
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
            mEditKit.getAudioFilterMgt().setFilter(filters);
        } else {
            mEditKit.getAudioFilterMgt().setFilter((AudioFilterBase) null);
        }
    }

    private class ComposeAlertDialog extends AlertDialog {
        private View mProgressLayout;
        private View mSystemState;
        private View mCoverSeekLayout;
        private View mComposePreviewLayout;
        private ProgressBar mComposeProgess;
        private TextView mStateTextView;
        private TextView mProgressText;
        private TextView mCpuRate;

        private ImageView mCoverBack;
        private TextView mCoverComplete;
        private ImageView mCoverImage;
        private AppCompatSeekBar mCoverSeekBar;
        private ImageView mPreviewBack;
        private ImageView mSaveToDCIM;

        private KSYMediaPlayer mMediaPlayer;
        private SurfaceView mVideoSurfaceView;
        private SurfaceHolder mSurfaceHolder;
        private WebView mGifView;

        private int mScreenWidth;
        private int mScreenHeight;
        private String mLocalPath = null;
        private String mFilePath = null;
        private String mFileMineType = FileUtils.MINE_TYPE_MP4;
        private HttpRequestTask mPlayurlGetTask;
        public boolean mNeedResumePlay = false;
        private AlertDialog mConfimDialog;
        private ProbeMediaInfoTools mImageSeekTools;
        private Bitmap mBitmap;
        private Timer mTimer;
        private Timer mSeekTimer;
        private long mSeekTime = 0L;

        protected ComposeAlertDialog(Context context, int themeResId) {
            super(context, themeResId);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            mScreenWidth = getResources().getDisplayMetrics().widthPixels;
            mScreenHeight = getResources().getDisplayMetrics().heightPixels;
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(mScreenWidth, mScreenHeight);
            LayoutInflater inflater = LayoutInflater.from(EditActivity.this);
            View viewDialog = inflater.inflate(R.layout.compose_layout, null);
            setContentView(viewDialog, layoutParams);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

            mProgressLayout = findViewById(R.id.compose_root);
            mSystemState = findViewById(R.id.system_state);
            mCoverSeekLayout = findViewById(R.id.cover_dialog);

            mComposePreviewLayout = findViewById(R.id.compose_preview_layout);
            mComposeProgess = (ProgressBar) findViewById(R.id.state_progress);
            mProgressText = (TextView) findViewById(R.id.progress_text);
            mCpuRate = (TextView) findViewById(R.id.cpu_rate);
            mStateTextView = (TextView) findViewById(R.id.state_text);
            mCoverBack = (ImageView) findViewById(R.id.cover_back);
            mCoverComplete = (TextView) findViewById(R.id.cover_complete);
            mCoverImage = (ImageView) findViewById(R.id.cover_image);
            mCoverSeekBar = (AppCompatSeekBar) findViewById(R.id.cover_seekBar);
            mImageSeekTools = new ProbeMediaInfoTools();
            mCoverBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeDialog();
                    resumeEditPreview();
                }
            });
            mCoverComplete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startUpload();
                }
            });
            mCoverSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    float rate = progress / 100.f;
                    mSeekTime = (long) (mPreviewLength * rate);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    mSeekTimer = new Timer();
                    mSeekTimer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            mBitmap = mImageSeekTools.getVideoThumbnailAtTime(mLocalPath, mSeekTime, 0, 0);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (mBitmap != null) {
                                        mCoverImage.setImageBitmap(mBitmap);
                                    }
                                }
                            });
                        }
                    }, 500, 500);
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if (mSeekTimer != null) {
                        mSeekTimer.cancel();
                        mSeekTimer = null;
                    }
                    mBitmap = mImageSeekTools.getVideoThumbnailAtTime(mLocalPath, mSeekTime, 0, 0);
                    if (mBitmap != null) {
                        mCoverImage.setImageBitmap(mBitmap);
                    }
                }
            });
            mVideoSurfaceView = (SurfaceView) findViewById(R.id.compose_preview);
            mGifView = (WebView) findViewById(R.id.gif_view);
            WebSettings webSettings = mGifView.getSettings();
            webSettings.setUseWideViewPort(true);
            webSettings.setLoadWithOverviewMode(true);
            webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
            mPreviewBack = (ImageView) findViewById(R.id.preview_back);
            mPreviewBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(EditActivity.this, ConfigActivity.class);
                    startActivity(intent);
                }
            });
            mSaveToDCIM = (ImageView) findViewById(R.id.save_to_album);
            mSaveToDCIM.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    saveFileToDCIM();
                }
            });
            getMediaPlayer();
            mSurfaceHolder = mVideoSurfaceView.getHolder();
            mSurfaceHolder.addCallback(mSurfaceCallback);
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (!mComposeFinished) {
                        mConfimDialog = new AlertDialog.Builder(EditActivity.this).setCancelable
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
                                            mEditKit.stopCompose();
                                            mComposeFinished = false;
                                            closeDialog();
                                            resumeEditPreview();
                                        }
                                        mConfimDialog = null;
                                    }
                                }).show();
                    } else {
                        closeDialog();
                        resumeEditPreview();
                    }
                    if (mComposePreviewLayout.getVisibility() == View.VISIBLE) {
                        Intent intent = new Intent(EditActivity.this, ConfigActivity.class);
                        startActivity(intent);
                    }
                    break;
                default:
                    break;
            }
            return false;
        }

        public void startUpload() {
            if (!TextUtils.isEmpty(mLocalPath)) {

                String mineType = FileUtils.getMimeType(new File(mLocalPath));
                StringBuilder objectKey = new StringBuilder(getPackageName() +
                        "/" + System.currentTimeMillis());
                if (mineType == FileUtils.MINE_TYPE_MP4) {
                    objectKey.append(".mp4");
                } else if (mineType == FileUtils.MINE_TYPE_GIF) {
                    objectKey.append(".gif");
                }
                mCurObjectKey = objectKey.toString();
                //上传必要信息：bucket,objectkey，及PutObjectResponseHandler上传过程回调
                KS3ClientWrap.KS3UploadInfo bucketInfo = new KS3ClientWrap.KS3UploadInfo
                        ("ksvsdemo", mCurObjectKey, mPutObjectResponseHandler);
                //调用SDK内部接口触发上传
                mEditKit.getKS3ClientWrap().putObject(bucketInfo, mLocalPath,
                        mPutObjectResponseHandler, new KS3ClientWrap.OnGetAuthInfoListener() {
                            @Override
                            public KS3ClientWrap.KS3AuthInfo onGetAuthInfo(String s, String s1, String s2, String s3, String s4, String s5) {

                                if (mTokenTask == null) {
                                    mTokenTask = new KS3TokenTask(getApplicationContext());
                                }

                                KS3ClientWrap.KS3AuthInfo authInfo = mTokenTask.requsetTokenToAppServer(s, s1,
                                        s2, s3, s4, s5);

                                return authInfo;
                            }
                        });

            }
        }

        public void composeStarted() {
            mProgressLayout.setVisibility(View.VISIBLE);
            mSystemState.setVisibility(View.VISIBLE);
            mStateTextView.setVisibility(View.VISIBLE);
            mStateTextView.setText(R.string.compose_file);
            mTimer = new Timer();
            mTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    final int progress = mEditKit.getProgress();
                    updateProgress(progress);
                }

            }, 500, 500);
        }

        public void composeFinished(String path) {
            mLocalPath = path;
            mFilePath = path;
            mFileMineType = FileUtils.getMimeType(new File(path));
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
            if (mFileMineType.equals(FileUtils.MINE_TYPE_GIF)) {
                startUpload();
            } else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgress();
                        mBitmap = mImageSeekTools.getVideoThumbnailAtTime(mLocalPath, mSeekTime, 0, 0);
                        mCoverImage.setImageBitmap(mBitmap);
                    }
                });
            }
        }

        public void uploadStarted() {
            mStateTextView.setText(R.string.upload_file);
            resetPlay();
            showProgress();
        }

        public void uploadFinished(final boolean success) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mComposePreviewLayout.getVisibility() != View.VISIBLE) {
                        mComposePreviewLayout.setVisibility(View.VISIBLE);
                    }
                    mCoverSeekLayout.setVisibility(View.GONE);
                    if (success) {
                        mProgressText.setVisibility(View.INVISIBLE);
                        mStateTextView.setVisibility(View.VISIBLE);
                        mStateTextView.setText(R.string.upload_file_success);

                        if (mPlayurlGetTask != null) {
                            mPlayurlGetTask.cancel(true);
                            mPlayurlGetTask.release();
                            mPlayurlGetTask = null;
                        }

                        mPlayurlGetTask = new HttpRequestTask(new HttpRequestTask.HttpResponseListener() {
                            @Override
                            public void onHttpResponse(int responseCode, String response) {
                                if (responseCode == 200) {
                                    if (!TextUtils.isEmpty(response)) {
                                        try {
                                            JSONObject data = new JSONObject(response);

                                            if (data.getInt("errno") == 0) {
                                                String url = data.getString("presigned_url");
                                                if (!url.contains("http")) {
                                                    url = "http://" + url;
                                                }
                                                mFilePath = url;
                                                Log.e(TAG, "play url:" + mFilePath);
                                                EditActivity.this.mComposeAlertDialog.startPreview();
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                            //播放合成后的视频
                                            EditActivity.this.mComposeAlertDialog.startPreview();
                                        }

                                    }
                                }
                            }
                        });
                        mStateTextView.setText(R.string.get_file_url);
                        mPlayurlGetTask.execute(FILE_URL_SERVER + "?objkey=" + mCurObjectKey);
                    } else {
                        mStateTextView.setVisibility(View.VISIBLE);
                        mStateTextView.setText(R.string.upload_file_fail);
                        startPreview();
                    }

                }
            });
        }

        public void uploadProgress(double progress) {
            updateProgress((int) progress);
        }

        private void hideProgress() {
            mComposeProgess.setVisibility(View.GONE);
            mProgressText.setVisibility(View.GONE);
            if (mProgressLayout.getVisibility() == View.VISIBLE) {
                mProgressLayout.setVisibility(View.INVISIBLE);
            }
            if (mCoverSeekLayout.getVisibility() != View.VISIBLE) {
                mCoverSeekLayout.setVisibility(View.VISIBLE);
            }
        }

        private void showProgress() {
            mProgressLayout.setVisibility(View.VISIBLE);
            mSystemState.setVisibility(View.INVISIBLE);
            mStateTextView.setVisibility(View.VISIBLE);
            mComposeProgess.setVisibility(View.VISIBLE);
            mProgressText.setVisibility(View.VISIBLE);
        }

        private void updateProgress(final int progress) {
            if (mSystemState.getVisibility() == View.VISIBLE) {
                final int rate = (int) SystemStateObtainUtil.getInstance().sampleCPU();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCpuRate.setText(rate + "%");
                    }
                });
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mProgressText.getVisibility() == View.VISIBLE) {
                        mProgressText.setText(String.valueOf(progress) + "%");
                    }
                }
            });

        }

        public void closeDialog() {
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }

            if (mPlayurlGetTask != null) {
                mPlayurlGetTask.cancel(true);
                mPlayurlGetTask.release();
                mPlayurlGetTask = null;
            }

            releasePlay();

            EditActivity.ComposeAlertDialog.this.dismiss();
            EditActivity.this.mComposeAlertDialog = null;
        }

        private void saveFileToDCIM() {
            String srcPath = mLocalPath;
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
                        fos.write(buffer,0,length);
                    }
                    Toast.makeText(EditActivity.this,"文件保存成功",Toast.LENGTH_SHORT);
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

        private void startPreview() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "start compose file Preview:");
                    if (mFileMineType.equals(FileUtils.MINE_TYPE_GIF)) {
                        mProgressLayout.setVisibility(View.GONE);
                        mGifView.setVisibility(View.VISIBLE);
                        mGifView.loadUrl("file://" + mLocalPath);
//                        if (mFilePath.startsWith("http")) {
//                            mGifView.loadUrl(mFilePath);
//                        } else {
//                            mGifView.loadUrl("file://" + mFilePath);
//                        }
                    } else {
                        mVideoSurfaceView.setVisibility(View.VISIBLE);
                        if (mPaused) {
                            Log.d(TAG, "Activity paused");
                            mNeedResumePlay = true;
                            return;
                        }
                        mNeedResumePlay = false;
                        startPlay(mFilePath);
                    }
                }
            });
        }

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

        private IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
                Log.d(TAG, "mediaplayer error:" + i);
                return false;
            }
        };

        private IMediaPlayer.OnInfoListener mOnInfoListener = new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
                switch (what) {
                    case KSYMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(mComposeAlertDialog != null && mComposeAlertDialog.isShowing()) {
                                    mProgressLayout.setVisibility(View.GONE);
                                }
                            }
                        });
                        break;
                    default:
                        break;
                }
                return false;
            }
        };

        public KSYMediaPlayer getMediaPlayer() {
            if (mMediaPlayer == null) {
                mMediaPlayer = new KSYMediaPlayer.Builder(
                        EditActivity.this.getApplicationContext()).build();
            }
            return mMediaPlayer;
        }

        private void startPlay(String path) {
            mMediaPlayer.setLooping(true);
            mMediaPlayer.shouldAutoPlay(false);
            mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
            mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
            mMediaPlayer.setOnInfoListener(mOnInfoListener);
            mMediaPlayer.setOnErrorListener(mOnErrorListener);
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
    }
}
