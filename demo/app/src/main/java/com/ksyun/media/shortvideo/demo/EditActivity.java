package com.ksyun.media.shortvideo.demo;

import com.ksyun.ks3.exception.Ks3Error;
import com.ksyun.ks3.services.handler.PutObjectResponseHandler;
import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.shortvideo.demo.filter.DemoFilter;
import com.ksyun.media.shortvideo.demo.filter.DemoFilter2;
import com.ksyun.media.shortvideo.demo.filter.DemoFilter3;
import com.ksyun.media.shortvideo.demo.filter.DemoFilter4;
import com.ksyun.media.shortvideo.demo.sticker.ColorPicker;
import com.ksyun.media.shortvideo.demo.sticker.StickerAdapter;
import com.ksyun.media.shortvideo.demo.videorange.HorizontalListView;
import com.ksyun.media.shortvideo.demo.videorange.VideoRangeSeekBar;
import com.ksyun.media.shortvideo.demo.videorange.VideoThumbnailAdapter;
import com.ksyun.media.shortvideo.demo.videorange.VideoThumbnailInfo;
import com.ksyun.media.shortvideo.utils.KS3ClientWrap;
import com.ksyun.media.shortvideo.kit.KSYEditKit;
import com.ksyun.media.shortvideo.utils.ShortVideoConstants;
import com.ksyun.media.shortvideo.view.KSYTextView;
import com.ksyun.media.shortvideo.view.StickerHelpBoxInfo;
import com.ksyun.media.shortvideo.view.KSYStickerView;
import com.ksyun.media.streamer.filter.audio.AudioFilterBase;
import com.ksyun.media.streamer.filter.audio.AudioReverbFilter;
import com.ksyun.media.streamer.filter.audio.KSYAudioEffectFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautyProFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautyToneCurveFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgFilterBase;
import com.ksyun.media.streamer.filter.imgtex.ImgTexFilterMgt;
import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Edit Video
 */

public class EditActivity extends Activity implements
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static String TAG = "EditActivity";
    private static String FILEURL_SERVER = "http://ksvs-demo.ks-live.com:8720/api/upload/ks3/signurl";
    private static int AUDIO_FILTER_DISABLE = 0;

    private static int INDEX_FILTER = 1;
    private static int INDEX_WATERMARKK = 2;
    private static int INDEX_VIDEORANGE = 3;
    private static int INDEX_AUDIO = 4;
    private static int INDEX_STICKER = 5;
    private GLSurfaceView mEditPreviewView;
    private RelativeLayout mPreviewLayout;
    private RelativeLayout mBarBottomLayout;
    private ImageView mBackView;
    private ImageView mNextView;
    private ImageView mPauseView;
    private TextView mFilterView;
    private TextView mWaterMarkView;
    private TextView mVideoChooseView;
    private TextView mAudioView;
    private CheckBox mWaterMartLogoView;
    private CheckBox mOriginAudioView;
    private CheckBox mBgmMusicView;
    private AppCompatSeekBar mOriginAudioVolumeSeekBar;
    private AppCompatSeekBar mBgmVolumeSeekBar;
    private View mFilterLayout;
    private View mSpeedLayout;
    private View mWatermarkLayout;
    private View mVideoRangeLayout;
    private View mAudioEditLayout;
    private View mStickerLayout;
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
    private TextView mStickerTextView;
    private View mRemoveStickers;
    private RecyclerView mStickerList;// 贴图素材列表
    private KSYStickerView mKSYStickerView;  //贴纸预览区域
    private StickerAdapter mStickerAdapter;// 贴图列表适配器
    private Bitmap mStickerDeleteBitmap;  //贴纸辅助区域的删除按钮
    private Bitmap mStickerRotateBitmap;  //贴纸辅助区域的旋转按钮
    private StickerHelpBoxInfo mStickerHelpBoxInfo;  //贴纸辅助区域的画笔
    private Map<Integer, BottomViewInfo> mBottomViews = new HashMap<>();
    private EditText mTextInput;  //字幕输入框
    private ImageView mTextColorSelect; //字母颜色选择
    private KSYTextView mTextView;  //字幕预览显示
    private ColorPicker mColorPicker;  //字体颜色选择
    private InputMethodManager mInputMethodManager;
    private RecyclerView mTextStickerList;
    private StickerAdapter mTextStickerAdapter;
    private AppCompatSpinner mAudioEffectSpinner;  //变声
    private AppCompatSpinner mAudioReverbSpinner;  //混响
    private ImageView mSpeedDown; //减速
    private ImageView mSpeedUp; //加速
    private TextView mSpeedInfo; //速度信息

    private boolean mFirstPlay = true;
    private AudioSeekLayout.OnAudioSeekChecked mAudioSeekListener;
    private float mAudioLength;  //背景音乐时长
    private AudioSeekLayout mAudioSeekLayout;  //音频seek布局

    private int mAudioEffectType = AUDIO_FILTER_DISABLE;
    private int mAudioReverbType = AUDIO_FILTER_DISABLE;

    private ButtonObserver mButtonObserver;
    private CheckBoxObserver mCheckBoxObserver;
    private SeekBarChangedObserver mSeekBarChangedObsesrver;

    public final static String SRC_URL = "srcurl";

    private String mLogoPath = "assets://KSYLogo/logo.png";//"file:///sdcard/test.png";
    private String mBgmPath = "/sdcard/test.mp3";
    private String mStickerPath = "Stickers";  //贴纸加载地址默认在Assets目录，如果修改加载地址需要修改StickerAdapter的图片加载
    private String mTextStickerPath = "TextStickers";

    private KSYEditKit mEditKit;
    private boolean mComposeFinished = false;
    private KS3TokenTask mTokenTask;
    private String mCurObjectKey;
    private Handler mMainHandler;

    private boolean mPaused = false;

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

        //默认设置为横屏，当前暂时只支持横屏，后期完善
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //init UI
        //录制预览部分宽高1:1比例显示（用户可以按照自己的需求处理）
        //just PORTRAIT
        WindowManager windowManager = (WindowManager) getApplication().
                getSystemService(getApplication().WINDOW_SERVICE);

        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();
        mButtonObserver = new EditActivity.ButtonObserver();
        mCheckBoxObserver = new EditActivity.CheckBoxObserver();
        mSeekBarChangedObsesrver = new EditActivity.SeekBarChangedObserver();
        mEditPreviewView = (GLSurfaceView) findViewById(R.id.edit_preview);
        mPreviewLayout = (RelativeLayout) findViewById(R.id.preview_layout);
        mBarBottomLayout = (RelativeLayout) findViewById(R.id.edit_bar_bottom);

        int width = screenWidth;
        int height = screenWidth;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,
                height);
        mPreviewLayout.setLayoutParams(params);

        params = new LinearLayout.LayoutParams(screenWidth,
                screenHeight - height);
        mBarBottomLayout.setLayoutParams(params);

        mPauseView = (ImageView) findViewById(R.id.click_to_pause);
        mPauseView.setOnClickListener(mButtonObserver);
        mPauseView.getDrawable().setLevel(2);

        mFilterLayout = findViewById(R.id.beauty_choose);
        mSpeedLayout = findViewById(R.id.speed_layout);
        mSpeedLayout.setVisibility(View.VISIBLE);
        mFilterView = (TextView) findViewById(R.id.click_to_filter);
        mFilterView.setOnClickListener(mButtonObserver);
        mFilterView.setActivated(true);
        mFilterLayout.setVisibility(View.VISIBLE);
        BottomViewInfo filterView = new BottomViewInfo(mFilterView, mFilterLayout);
        mBottomViews.put(INDEX_FILTER, filterView);

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

        mWatermarkLayout = findViewById(R.id.watermark_choose);
        mWaterMarkView = (TextView) findViewById(R.id.click_to_watermark);
        mWaterMarkView.setOnClickListener(mButtonObserver);
        mWaterMartLogoView = (CheckBox) findViewById(R.id.watermark_logo);
        mWaterMartLogoView.setOnCheckedChangeListener(mCheckBoxObserver);
        BottomViewInfo watermarkView = new BottomViewInfo(mWaterMarkView, mWatermarkLayout);
        mBottomViews.put(INDEX_WATERMARKK, watermarkView);

        mVideoRangeLayout = findViewById(R.id.video_range_choose);
        mVideoChooseView = (TextView) findViewById(R.id.click_to_video_range);
        mVideoChooseView.setOnClickListener(mButtonObserver);
        BottomViewInfo videoRangeView = new BottomViewInfo(mVideoChooseView, mVideoRangeLayout);
        mBottomViews.put(INDEX_VIDEORANGE, videoRangeView);

        mAudioView = (TextView) findViewById(R.id.click_to_audio);
        mAudioView.setOnClickListener(mButtonObserver);
        mAudioEditLayout = findViewById(R.id.audio_choose);
        mAudioSeekLayout = (AudioSeekLayout) findViewById(R.id.audioSeekLayout);
        mOriginAudioView = (CheckBox) findViewById(R.id.origin_audio);
        mOriginAudioView.setOnCheckedChangeListener(mCheckBoxObserver);
        mBgmMusicView = (CheckBox) findViewById(R.id.music_audio);
        mBgmMusicView.setOnCheckedChangeListener(mCheckBoxObserver);
        mOriginAudioVolumeSeekBar = (AppCompatSeekBar) findViewById(R.id.origin_audio_volume);
        mOriginAudioVolumeSeekBar.setOnSeekBarChangeListener(mSeekBarChangedObsesrver);
        mBgmVolumeSeekBar = (AppCompatSeekBar) findViewById(R.id.music_audio_volume);
        mBgmVolumeSeekBar.setOnSeekBarChangeListener(mSeekBarChangedObsesrver);
        if (!mBgmMusicView.isChecked()) {
            mBgmVolumeSeekBar.setEnabled(false);
        }

        if (!mOriginAudioView.isChecked()) {
            mOriginAudioVolumeSeekBar.setEnabled(false);
        }
        BottomViewInfo audioView = new BottomViewInfo(mAudioView, mAudioEditLayout);
        mBottomViews.put(INDEX_AUDIO, audioView);

        mStickerLayout = findViewById(R.id.sticker_choose);
        mStickerTextView = (TextView) findViewById(R.id.click_to_sticker);
        mStickerTextView.setOnClickListener(mButtonObserver);
        BottomViewInfo stickerView = new BottomViewInfo(mStickerTextView, mStickerLayout);
        mBottomViews.put(INDEX_STICKER, stickerView);
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

        //混响&变声
        mAudioEffectSpinner = (AppCompatSpinner) findViewById(R.id.origin_audio_effect);
        mAudioReverbSpinner = (AppCompatSpinner) findViewById(R.id.origin_audio_reverb);
        initAudioFilterUI();

        //变速
        mSpeedDown = (ImageView) findViewById(R.id.speed_down);
        mSpeedDown.setOnClickListener(mButtonObserver);
        mSpeedUp = (ImageView) findViewById(R.id.speed_up);
        mSpeedUp.setOnClickListener(mButtonObserver);
        mSpeedInfo = (TextView) findViewById(R.id.speed_info);

        mBackView = (ImageView) findViewById(R.id.click_to_back);
        mBackView.setOnClickListener(mButtonObserver);
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
        initBeautyUI();
        initVideoRange();
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
                        mEditKit.setBGMRanges(start,end);
                    }
                };
                if (mAudioSeekLayout.getVisibility() != View.VISIBLE) {
                    mAudioSeekLayout.setVisibility(View.VISIBLE);
                    mAudioSeekLayout.setOnAudioSeekCheckedListener(mAudioSeekListener);
                }
                if (mFirstPlay) {
                    mFirstPlay = false;
                    mAudioSeekLayout.updateAudioSeekUI(mAudioLength,mEditPreviewDuration);
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
        if(mInputMethodManager.isActive()) {
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

    private void startEditPreview() {
        //设置预览的音量
        mEditKit.setVolume(0.4f);
        //设置是否循环预览
        mEditKit.setLooping(true);
        //开启预览
        mEditKit.startEditPreview();

        mOriginAudioVolumeSeekBar.setProgress((int) (mEditKit.getOriginAudioVolume() * 100));
    }

    private void onFilterClick() {
        BottomViewInfo filterView = mBottomViews.get(INDEX_FILTER);
        if (!filterView.isVisible()) {
            filterView.setVisible(true);
            hideOtherViews(INDEX_FILTER);
        }
    }

    private void onWatermarkClick() {
        BottomViewInfo waterMarkView = mBottomViews.get(INDEX_WATERMARKK);
        if (!waterMarkView.isVisible()) {
            waterMarkView.setVisible(true);
            mTextView.setVisibility(View.VISIBLE);
            hideOtherViews(INDEX_WATERMARKK);
        }
    }

    private void onVideoRangeClick() {
        BottomViewInfo videoRangeView = mBottomViews.get(INDEX_VIDEORANGE);
        if (!videoRangeView.isVisible()) {
            videoRangeView.setVisible(true);
            hideOtherViews(INDEX_VIDEORANGE);
        }
    }

    private void onAudioEditClick() {
        BottomViewInfo audioView = mBottomViews.get(INDEX_AUDIO);
        if (!audioView.isVisible()) {
            audioView.setVisible(true);
            hideOtherViews(INDEX_AUDIO);
        }
    }

    private void onStickerClick() {
        BottomViewInfo stickerView = mBottomViews.get(INDEX_STICKER);
        if (!stickerView.isVisible()) {
            mKSYStickerView.setVisibility(View.VISIBLE);
            stickerView.setVisible(true);
            hideOtherViews(INDEX_STICKER);
        }
    }

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

    private void onOriginAudioClick(boolean isCheck) {
        //是否删除原始音频
        mEditKit.enableOriginAudio(isCheck);

        mOriginAudioVolumeSeekBar.setEnabled(isCheck);
        mAudioEffectSpinner.setEnabled(isCheck);
        mAudioReverbSpinner.setEnabled(isCheck);
    }

    private void onBgmMusicClick(boolean isCheck) {
        if (!isCheck) {
            mEditKit.changeBgmMusic(null);
            if (mAudioSeekLayout.getVisibility() == View.VISIBLE) {
                mAudioSeekLayout.setVisibility(View.GONE);
            }
        } else {
            mEditKit.changeBgmMusic(mBgmPath);
            mBgmVolumeSeekBar.setProgress((int) (mEditKit.getBgmMusicVolume() * 100));
        }
        mBgmVolumeSeekBar.setEnabled(isCheck);
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
        EditActivity.this.finish();
    }

    private ComposeAlertDialog mComposeAlertDialog;

    private void onNextClick() {
        //配置合成参数
        final ShortVideoConfigDialog configDialog = new ShortVideoConfigDialog(this,
                ShortVideoConfigDialog
                        .SHORTVIDEOCONFIG_TYPE_COMPOSE);
        configDialog.setCancelable(false);
        configDialog.show();
        configDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                ShortVideoConfigDialog.ShortVideoConfig config = configDialog.getShortVideoConfig();
                if (config != null) {
                    //配置合成参数
                    mEditKit.setTargetResolution(config.resolution);
                    mEditKit.setVideoFps(config.fps);
                    mEditKit.setVideoCodecId(config.encodeType);
                    mEditKit.setVideoEncodeProfile(config.encodeProfile);
                    mEditKit.setAudioKBitrate(config.audioBitrate);
                    mEditKit.setVideoKBitrate(config.videoBitrate);
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

                    String composeUrl = fileFolder + "/" + System.currentTimeMillis() + ".mp4";
                    Log.d(TAG, "compose Url:" + composeUrl);
                    //开始合成
                    mEditKit.startCompose(composeUrl);
                }

            }
        });

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
                    initSeekBar();
                    initThumbnailAdapter();
                    break;
                case ShortVideoConstants.SHORTVIDEO_COMPOSE_START: {
                    mEditKit.pauseEditPreview();
                    mBeautySpinner.setSelection(0);
                    if (mComposeAlertDialog != null) {
                        mComposeAlertDialog.setCancelable(false);
                        mComposeAlertDialog.show();
                        mComposeAlertDialog.composeStarted();
                    }
                    return null;
                }
                case ShortVideoConstants.SHORTVIDEO_COMPOSE_FINISHED: {
                    mAudioReverbSpinner.setSelection(0);
                    mAudioEffectSpinner.setSelection(0);
                    if (mComposeAlertDialog != null) {
                        mComposeAlertDialog.composeFinished(msgs[0]);
                        mComposeFinished = true;
                    }
                    //上传必要信息：bucket,objectkey，及PutObjectResponseHandler上传过程回调
                    mCurObjectKey = getPackageName() + "/" + System.currentTimeMillis();
                    KS3ClientWrap.KS3UploadInfo bucketInfo = new KS3ClientWrap.KS3UploadInfo
                            ("ksvsdemo", mCurObjectKey, mPutObjectResponseHandler);
                    return bucketInfo;
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
            if (mComposeAlertDialog != null) {
                mComposeAlertDialog.uploadFinished(false);
            }
        }

        @Override
        public void onTaskSuccess(int statesCode, Header[] responceHeaders) {
            Log.d(TAG, "onTaskSuccess:" + statesCode);
            if (mComposeAlertDialog != null) {
                mComposeAlertDialog.uploadFinished(true);
            }
        }

        @Override
        public void onTaskStart() {
            Log.d(TAG, "onTaskStart");
            if (mComposeAlertDialog != null) {
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
            if (mComposeAlertDialog != null) {
                mComposeAlertDialog.uploadProgress(progress);
            }
        }
    };

    private class ButtonObserver implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.click_to_filter:
                    onFilterClick();
                    break;
                case R.id.click_to_watermark:
                    onWatermarkClick();
                    break;
                case R.id.click_to_video_range:
                    onVideoRangeClick();
                    break;
                case R.id.click_to_audio:
                    onAudioEditClick();
                    break;
                case R.id.click_to_sticker:
                    onStickerClick();
                    break;
                case R.id.click_to_back:
                    onBackoffClick();
                    break;
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
                default:
                    break;
            }
        }
    }

    private class CheckBoxObserver implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.watermark_logo:
                    onWaterMarkLogoClick(isChecked);
                    break;
                case R.id.music_audio:
                    onBgmMusicClick(isChecked);
                    break;
                case R.id.origin_audio:
                    onOriginAudioClick(isChecked);
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
                case R.id.origin_audio_volume:
                    mEditKit.setOriginAudioVolume(val);
                    break;
                case R.id.music_audio_volume:
                    mEditKit.setBgmMusicVolume(val);
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

    /**********************************video range****************************************/
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
    }

    private String formatTimeStr3(float s) {
        int minute = (int) (s / (1000 * 60));
        int second = (int) ((s / 1000) % 60);
        return String.format("%02d:%02d", minute, second);
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
                    mEditKit.getImgTexFilterMgt().setFilter((ImgFilterBase) null);
                } else if (position <= 5) {
                    mEditKit.getImgTexFilterMgt().setFilter(
                            mEditKit.getGLRender(), position + 15);
                } else if (position == 6) {
                    mEditKit.getImgTexFilterMgt().setFilter(mEditKit.getGLRender(),
                            ImgTexFilterMgt.KSY_FILTER_BEAUTY_PRO);
                } else if (position == 7) {
                    mEditKit.getImgTexFilterMgt().setFilter(
                            new DemoFilter(mEditKit.getGLRender()));
                } else if (position == 8) {
                    List<ImgFilterBase> groupFilter = new LinkedList<>();
                    groupFilter.add(new DemoFilter2(mEditKit.getGLRender()));
                    groupFilter.add(new DemoFilter3(mEditKit.getGLRender()));
                    groupFilter.add(new DemoFilter4(mEditKit.getGLRender()));
                    mEditKit.getImgTexFilterMgt().setFilter(groupFilter);
                } else if (position == 9) {
                    ImgBeautyToneCurveFilter acvFilter = new ImgBeautyToneCurveFilter(mEditKit.getGLRender());
                    acvFilter.setFromCurveFileInputStream(
                            EditActivity.this.getResources().openRawResource(R.raw.tone_cuver_sample));

                    mEditKit.getImgTexFilterMgt().setFilter(acvFilter);
                } else if (position == 10) {
                    ImgBeautyToneCurveFilter acvFilter = new ImgBeautyToneCurveFilter(mEditKit
                            .getGLRender());
                    acvFilter.setFromCurveFileInputStream(
                            EditActivity.this.getResources().openRawResource(R.raw.fugu));

                    mEditKit.getImgTexFilterMgt().setFilter(acvFilter);
                } else if (position == 11) {
                    ImgBeautyToneCurveFilter acvFilter = new ImgBeautyToneCurveFilter(mEditKit
                            .getGLRender());
                    acvFilter.setFromCurveFileInputStream(
                            EditActivity.this.getResources().openRawResource(R.raw.jiaopian));

                    mEditKit.getImgTexFilterMgt().setFilter(acvFilter);
                }
                List<ImgFilterBase> filters = mEditKit.getImgTexFilterMgt().getFilter();
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
        mBeautySpinner.setSelection(0);
    }

    private void initAudioFilterUI() {
        //变声
        String[] effectItems = new String[]{"DISABLE", "FEMALE", "MALE",
                "HEROIC", "ROBOT"};
        ArrayAdapter<String> effectAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, effectItems);
        effectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAudioEffectSpinner.setAdapter(effectAdapter);
        mAudioEffectSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long
                    index) {
                if (position == 0) {
                    mAudioEffectType = AUDIO_FILTER_DISABLE;
                } else {
                    switch (position) {
                        case 1:
                            //萝莉
                            mAudioEffectType = KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_FEMALE;

                            break;
                        case 2:
                            //大叔
                            mAudioEffectType = KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_MALE;
                            break;
                        case 3:
                            //庄严
                            mAudioEffectType = KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_HEROIC;
                            break;
                        case 4:
                            //机器人
                            mAudioEffectType = KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_ROBOT;
                            break;
                        default:
                            break;
                    }
                }
                addAudioFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mAudioEffectSpinner.setPopupBackgroundResource(R.color.transparent1);
        mAudioEffectSpinner.setSelection(0);

        //混响
        String[] reverbItems = new String[]{"DISABLE", "RECORDING", "KTV",
                "STAGE", "CONCERT"};

        ArrayAdapter<String> reverbAdapter = new ArrayAdapter<String>(this, android.R.layout
                .simple_spinner_item, reverbItems);
        reverbAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mAudioReverbSpinner.setAdapter(reverbAdapter);
        mAudioReverbSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long
                    index) {

                if (position == 0) {
                    mAudioReverbType = AUDIO_FILTER_DISABLE;
                } else {
                    switch (position) {
                        case 1:
                            //录音棚
                            mAudioReverbType = AudioReverbFilter.AUDIO_REVERB_LEVEL_1;
                            break;
                        case 2:
                            //KTV
                            mAudioReverbType = AudioReverbFilter.AUDIO_REVERB_LEVEL_2;
                            break;
                        case 3:
                            //小舞台
                            mAudioReverbType = AudioReverbFilter
                                    .AUDIO_REVERB_LEVEL_3;
                            break;
                        case 4:
                            //演唱会
                            mAudioReverbType = AudioReverbFilter
                                    .AUDIO_REVERB_LEVEL_4;
                            break;
                        default:
                            break;
                    }
                }

                addAudioFilter();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mAudioReverbSpinner.setPopupBackgroundResource(R.color.transparent1);
        mAudioReverbSpinner.setSelection(0);
    }

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

    private void resumeEditPreview() {
        mEditKit.resumeEditPreview();
    }

    private class ComposeAlertDialog extends AlertDialog {
        private RelativeLayout mProgressLayout;
        private RelativeLayout mComposePreviewLayout;
        private ProgressBar mComposeProgess;
        private TextView mStateTextView;
        private TextView mProgressText;

        private KSYMediaPlayer mMediaPlayer;
        private SurfaceView mVideoSurfaceView;
        private SurfaceHolder mSurfaceHolder;

        private int mScreenWidth;
        private int mScreenHeight;
        private String mFilePath = null;
        private HttpRequestTask mPlayurlGetTask;
        public boolean mNeedResumePlay = false;
        private AlertDialog mConfimDialog;

        private Timer mTimer;

        protected ComposeAlertDialog(Context context, int themeResId) {
            super(context, themeResId);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            Display display = getWindowManager().getDefaultDisplay();
            mScreenWidth = display.getWidth();
            mScreenHeight = display.getHeight();
            ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(mScreenWidth, mScreenHeight);
            LayoutInflater inflater = LayoutInflater.from(EditActivity.this);
            View viewDialog = inflater.inflate(R.layout.compose_layout, null);
            setContentView(viewDialog, layoutParams);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


            mProgressLayout = (RelativeLayout) findViewById(R.id.compose_root);
            mComposePreviewLayout = (RelativeLayout) findViewById(R.id.compose_preview_layout);
            mComposeProgess = (ProgressBar) findViewById(R.id.state_progress);
            mProgressText = (TextView) findViewById(R.id.progress_text);
            mStateTextView = (TextView) findViewById(R.id.state_text);

            mVideoSurfaceView = (SurfaceView) findViewById(R.id.compose_preview);

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
                    break;
                default:
                    break;
            }
            return false;
        }

        public void composeStarted() {
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mConfimDialog != null) {
                        mConfimDialog.dismiss();
                        mConfimDialog = null;
                    }
                    mStateTextView.setText("上传鉴权中");
                }
            });

            mFilePath = path;
            startPreview();

            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
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
                    if (success) {
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

                        mPlayurlGetTask.execute(FILEURL_SERVER + "?objkey=" + mCurObjectKey);
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
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mScreenWidth,
                    mScreenWidth);
            mComposePreviewLayout.setLayoutParams(params);

            params = new LinearLayout.LayoutParams(mScreenWidth,
                    mScreenWidth - mScreenHeight);
            mProgressLayout.setLayoutParams(params);

            mVideoSurfaceView.setVisibility(View.VISIBLE);
            mComposePreviewLayout.setVisibility(View.VISIBLE);

            mComposeProgess.setVisibility(View.GONE);
            mProgressText.setVisibility(View.GONE);
        }

        private void showProgress() {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mScreenWidth,
                    mScreenHeight);
            mProgressLayout.setLayoutParams(params);

            mComposePreviewLayout.setVisibility(View.GONE);
            mVideoSurfaceView.setVisibility(View.GONE);

            mStateTextView.setVisibility(View.VISIBLE);
            mComposeProgess.setVisibility(View.VISIBLE);
            mProgressText.setVisibility(View.VISIBLE);
        }

        private void updateProgress(final int progress) {
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

        private void startPreview() {
            if (mPaused) {
                Log.d(TAG, "Activity paused");
                mNeedResumePlay = true;
                return;
            }

            mNeedResumePlay = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideProgress();
                }
            });

            startPlay(mFilePath);
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
            public boolean onInfo(IMediaPlayer iMediaPlayer, int i, int i1) {
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

    private void hideOtherViews(int showIndex) {
        for (Integer id : mBottomViews.keySet()) {
            if (id != showIndex) {
                BottomViewInfo item = mBottomViews.get(id);
                item.setVisible(false);
            }
            if (showIndex != INDEX_WATERMARKK) {
                if (mInputMethodManager.isActive()) {
                    mInputMethodManager.hideSoftInputFromWindow(mTextInput
                                    .getWindowToken(),
                            InputMethodManager.RESULT_UNCHANGED_SHOWN);
                }
            }
        }
    }

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
            onWatermarkClick();
            mTextInput.requestFocus();
            mInputMethodManager.showSoftInput(mTextInput, InputMethodManager.RESULT_SHOWN);
        }
    };

    public class BottomViewInfo {
        public TextView titleView;
        public View container;

        public BottomViewInfo(TextView titleView, View container) {
            this.titleView = titleView;
            this.container = container;
        }

        public boolean isVisible() {
            return container.getVisibility() == View.VISIBLE;
        }

        public void setVisible(boolean visible) {
            if (visible) {
                container.setVisibility(View.VISIBLE);
                titleView.setActivated(true);
            } else {
                container.setVisibility(View.INVISIBLE);
                titleView.setActivated(false);
            }
        }
    }
}
