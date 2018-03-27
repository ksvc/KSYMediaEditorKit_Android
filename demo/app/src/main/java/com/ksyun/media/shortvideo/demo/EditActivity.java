package com.ksyun.media.shortvideo.demo;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.shortvideo.demo.adapter.BgmSelectAdapter;
import com.ksyun.media.shortvideo.demo.adapter.BottomTitleAdapter;
import com.ksyun.media.shortvideo.demo.adapter.ImageTextAdapter;
import com.ksyun.media.shortvideo.demo.adapter.SoundEffectAdapter;
import com.ksyun.media.shortvideo.demo.audiorange.AudioSeekLayout;
import com.ksyun.media.shortvideo.demo.filter.ImgTexGPUImageFilter;
import com.ksyun.media.shortvideo.demo.paint.PaintMenu;
import com.ksyun.media.shortvideo.demo.sticker.ColorPicker;
import com.ksyun.media.shortvideo.demo.sticker.StickerAdapter;
import com.ksyun.media.shortvideo.demo.util.DataFactory;
import com.ksyun.media.shortvideo.demo.util.DownloadAndHandleTask;
import com.ksyun.media.shortvideo.demo.util.SystemStateObtainUtil;
import com.ksyun.media.shortvideo.demo.util.ViewUtils;
import com.ksyun.media.shortvideo.demo.videorange.HorizontalListView;
import com.ksyun.media.shortvideo.demo.videorange.VideoRangeSeekBar;
import com.ksyun.media.shortvideo.demo.videorange.VideoThumbnailAdapter;
import com.ksyun.media.shortvideo.demo.videorange.VideoThumbnailInfo;
import com.ksyun.media.shortvideo.demo.view.FilterEffectsView;
import com.ksyun.media.shortvideo.demo.view.SectionSeekLayout;
import com.ksyun.media.shortvideo.timereffect.TimerEffectFilter;
import com.ksyun.media.shortvideo.timereffect.TimerEffectInfo;
import com.ksyun.media.shortvideo.utils.FileUtils;
import com.ksyun.media.shortvideo.kit.KSYEditKit;
import com.ksyun.media.shortvideo.utils.ShortVideoConstants;
import com.ksyun.media.shortvideo.sticker.KSYStickerView;
import com.ksyun.media.shortvideo.sticker.StickerHelpBoxInfo;
import com.ksyun.media.shortvideo.sticker.DrawTextParams;
import com.ksyun.media.shortvideo.sticker.KSYStickerInfo;
import com.ksyun.media.streamer.filter.audio.AudioFilterBase;
import com.ksyun.media.streamer.filter.audio.AudioReverbFilter;
import com.ksyun.media.streamer.filter.audio.KSYAudioEffectFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautyProFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautySoftFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautySpecialEffectsFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgBeautyStylizeFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgFilterBase;
import com.ksyun.media.streamer.filter.imgtex.ImgShaderXSingleFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgShake70sFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgShakeColorFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgShakeIllusionFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgShakeShockWaveFilter;
import com.ksyun.media.streamer.filter.imgtex.ImgShakeZoomFilter;
import com.ksyun.media.streamer.framework.AVConst;
import com.ksyun.media.streamer.framework.VideoCodecFormat;
import com.ksyun.media.streamer.kit.StreamerConstants;
import com.lht.paintview.PaintView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
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
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.cyberagent.android.gpuimage.GPUImageSobelEdgeDetection;

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

    private static final int REQUEST_CODE = 10010;
    private static final int FILTER_DISABLE = 0;

    private static final int BEAUTY_LAYOUT_INDEX = 0;
    private static final int FILTER_LAYOUT_INDEX = 1;
    private static final int FILTER_EFFECTS_INDEX = 2;
    private static final int WATER_MARK_INDEX = 3;
    private static final int SPEED_LAYOUT_INDEX = 4;
    private static final int VIDEO_RANGE_INDEX = 5;
    private static final int VIDEO_SCALE_INDEX = 6;
    private static final int MUSIC_LAYOUT_INDEX = 7;
    private static final int SOUND_CHANGE_INDEX = 8;
    private static final int REVERB_LAYOUT_INDEX = 9;
    private static final int PAINT_MENU_LAYOUT_INDEX = 10;
    private static final int ANIMATED_STICKER_LAYOUT_INDEX = 11;
    private static final int STICKER_LAYOUT_INDEX = 12;
    private static final int SUBTITLE_LAYOUT_INDEX = 13;
    private static final int REVERSE_PLAY_INDEX = 14;

    private RelativeLayout mPreviewLayout;
    private GLSurfaceView mEditPreviewView;
    private RelativeLayout mBarBottomLayout;
    private ImageView mNextView;
    private ImageView mPauseView;
    private ImageView mRotateView;
    private List<String> mTitleData;
    private RecyclerView mTitleView;
    private BottomTitleAdapter mTitleAdapter;
    private AppCompatSeekBar mOriginAudioVolumeSeekBar;
    private AppCompatSeekBar mBgmVolumeSeekBar;
    private View mBeautyLayout;  //美颜
    private View mFilterLayout; //滤镜
    private View mSpeedLayout; //变速
    private View mVideoScaleLayout;  //视频画布裁剪
    private View mVideoRangeLayout;  //视频时长裁剪
    private View mAudioEditLayout;  //bgm音频裁剪
    private View mSoundChangeLayout;  //原始音频变声
    private View mReverbLayout;  //原始音频混响
    private View mPaintMenuLayout; //涂鸦画笔信息选择布局
    private View mStickerLayout;  //图片贴纸
    private View mAnimatedStickerLayout; //动态贴纸
    private View mSubtitleLayout;  //字幕
    private RecyclerView mBgmRecyclerView;
    private BgmSelectAdapter mBgmAdapter;
    private RecyclerView mSoundChangeRecycler;
    private RecyclerView mReverbRecycler;
    private View mVideoScale9_16;
    private View mVideoScale3_4;
    private View mVideoScale1_1;
    private View mVideoScaleFit;
    private View mVideoScaleCrop;

    //滤镜
    private ImageView mFilterOriginImage;
    private ImageView mFilterBorder;
    private TextView mFilterOriginText;
    private RecyclerView mFilterRecyclerView;
    private ImageTextAdapter mFilterAdapter;
    private View[] mBottomViewList;
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
    private ImageTextAdapter mBeautyAdapter;

    //涂鸦
    private PaintView mPaintView;
    private PaintMenu mPaintMenuView;
    private boolean mIsPainting = false;

    //贴纸
    private KSYStickerView mKSYStickerView;  //贴纸预览区域（图片贴纸和字幕贴纸公用）
    private Bitmap mStickerDeleteBitmap;  //贴纸辅助区域的删除按钮（图片贴纸和字幕贴纸公用）
    private Bitmap mStickerRotateBitmap;  //贴纸辅助区域的旋转按钮（图片贴纸和字幕贴纸公用）
    private StickerHelpBoxInfo mStickerHelpBoxInfo;  //贴纸辅助区域的画笔（图片贴纸和字幕贴纸公用）

    private RecyclerView mAnimatedStickerList; //动态贴纸素材列表
    private StickerAdapter mAnimatedStickerAdapter; //动态贴纸适配器
    private RecyclerView mStickerList;// 图片贴纸素材列表
    private StickerAdapter mStickerAdapter;// 图片贴纸列表适配器
    private RecyclerView mTextStickerList;  //字幕贴纸素材列表
    private StickerAdapter mTextStickerAdapter; //字幕贴纸列表适配器
    private EditText mTextInput;  //字幕贴纸文字输入框
    private ImageView mTextColorSelect; //字幕贴纸颜色选择按钮
    private ColorPicker mColorPicker;  //字幕贴纸字体颜色选择器
    private InputMethodManager mInputMethodManager;  //输入
    private FilterEffectsView mEffectsView;

    private SectionSeekLayout mSectionView;  //片段编辑UI
    private Timer mPreviewRefreshTimer;
    private TimerTask mPreviewRefreshTask;  //跟随播放预览的缩略图自动滚动任务

    private ImageView mSpeedDown; //减速
    private ImageView mSpeedUp; //加速
    private TextView mSpeedInfo; //速度信息

    private boolean mFirstPlay = true;
    private boolean mWaterMarkChecked;
    private boolean mReversePlayChecked;
    private AudioSeekLayout.OnAudioSeekChecked mAudioSeekListener;
    private long mAudioLength;  //背景音乐时长
    private long mPreviewLength; //视频裁剪后的时长
    private AudioSeekLayout mAudioSeekLayout;  //音频seek布局
    private Dialog mConfigDialog;
    private ComposeDialog mComposeDialog;
    private ShortVideoConfig mComposeConfig; //输出视频参数配置
    private ButtonObserver mButtonObserver;
    private SeekBarChangedObserver mSeekBarChangedObserver;

    public final static String SRC_URL = "src_url";
    public final static String COMPOSE_PATH = "compose_path";
    public final static String MIME_TYPE = "mime_type";
    public final static String PREVIEW_LEN = "preview_length";

    private static final int AUDIO_FILTER_DISABLE = 0;  //不使用音频滤镜的类型标志
    private int mAudioEffectType = AUDIO_FILTER_DISABLE;  //变声类型缓存变量
    private int mAudioReverbType = AUDIO_FILTER_DISABLE;  //混响类型缓存变量
    //变声类型数组常量
    private static final int[] SOUND_CHANGE_TYPE = {KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_MALE, KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_FEMALE,
            KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_HEROIC, KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_ROBOT};
    //混响类型数组常量
    private static final int[] REVERB_TYPE = {AudioReverbFilter.AUDIO_REVERB_LEVEL_1, AudioReverbFilter.AUDIO_REVERB_LEVEL_3,
            AudioReverbFilter.AUDIO_REVERB_LEVEL_4, AudioReverbFilter.AUDIO_REVERB_LEVEL_2};

    private static final int BOTTOM_VIEW_NUM = 14;
    private String mLogoPath = "assets://KSYLogo/logo.png";
    private String mAnimateStickerPath = "AnimatedStickers/Thumbnail";
    private String mStickerPath = "Stickers";  //贴纸加载地址默认在Assets目录，如果修改加载地址需要修改StickerAdapter的图片加载
    private String mTextStickerPath = "TextStickers";

    private KSYEditKit mEditKit; //编辑合成kit类
    private int mImgBeautyTypeIndex = BEAUTY_DISABLE;  //美颜type
    private int mEffectFilterIndex = FILTER_DISABLE;  //滤镜filter type
    private int mLastImgBeautyTypeIndex = BEAUTY_DISABLE;  //美颜type
    private int mLastEffectFilterIndex = FILTER_DISABLE;  //滤镜filter type
    private Map<Integer, ImgFilterBase> mBeautyFilters;
    private Map<Integer, ImgFilterBase> mEffectFilters;

    private boolean mComposeFinished = false;
    /*******编辑后合成参数配置示例******/
    private TextView mOutRes480p;
    private TextView mOutRes540p;
    private TextView mOutEncodeWithH264;
    private TextView mOutEncodeWithH265;
    private TextView mOutEncodeByHW;
    private TextView mOutEncodeBySW;
    private TextView mOutDecodeByHW;
    private TextView mOutDecodeBySW;
    private TextView mOutForMP4;
    private TextView mOutForGIF;
    private TextView[] mOutProfileGroup;
    private TextView[] mOutAudioProfileGroup;
    private EditText mOutFrameRate;
    private EditText mOutVideoBitrate;
    private EditText mOutAudioBitrate;
    private EditText mOutVideoCRF;
    private TextView mOutputConfirm;
    private String mTailVideoUrl = "https://ks3-cn-beijing.ksyun.com/ksy.vcloud.sdk/Android/short_video/TailLeader.mp4";
    private String mTailVideoPath = Environment.getExternalStorageDirectory().
            getAbsolutePath() + "/tailvideo.mp4";
    private DownloadAndHandleTask mTailLoadTask;
    private static final int[] OUTPUT_PROFILE_ID = {R.id.output_config_low_power,
            R.id.output_config_balance, R.id.output_config_high_performance};
    private static final int[] ENCODE_PROFILE_TYPE = {VideoCodecFormat.ENCODE_PROFILE_LOW_POWER,
            VideoCodecFormat.ENCODE_PROFILE_BALANCE, VideoCodecFormat.ENCODE_PROFILE_HIGH_PERFORMANCE};
    private static final int[] AUDIO_OUTPUT_PROFILE_ID = {R.id.output_config_aac_lc,
            R.id.output_config_aac_he, R.id.output_config_aac_he_v2};
    private static final int[] AUDIO_ENCODE_PROFILE = {AVConst.PROFILE_AAC_LOW, AVConst.PROFILE_AAC_HE,
            AVConst.PROFILE_AAC_HE_V2};

    private Handler mMainHandler;
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

    private boolean mHWEncoderUnsupported;  //硬编支持标志位
    private boolean mSWEncoderUnsupported;  //软编支持标志位

    //for scale
    private int mScaleMode = KSYEditKit.SCALING_MODE_BEST_FIT;
    private int mScaleType = KSYEditKit.SCALE_TYPE_9_16;
    private float mMinCrop;
    private float mMaxCrop;
    private float mPreviewTouchStartX;
    private float mPreviewTouchStartY;
    private float mLastRawX;
    private float mLastRawY;
    private float mTouchLastX;
    private float mTouchLastY;
    private boolean mIsPreviewMoved = false;  //是否移动过了，如果移动过了，ACTION_UP时不触发bottom区域隐藏
    private int PREVIEW_TOUCH_MOVE_MARGIN = 30;  //触发移动的最小距离

    private int mScreenWidth;
    private int mScreenHeight;

    private int mRotateDegrees = 0;

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
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        mButtonObserver = new EditActivity.ButtonObserver();
        mSeekBarChangedObserver = new EditActivity.SeekBarChangedObserver();

        mPreviewLayout = (RelativeLayout) findViewById(R.id.preview_layout);
        mEditPreviewView = (GLSurfaceView) findViewById(R.id.edit_preview);
        mBarBottomLayout = (RelativeLayout) findViewById(R.id.edit_bar_bottom);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mBarBottomLayout.getLayoutParams();
        params.height = mScreenHeight / 3;
        mBarBottomLayout.setLayoutParams(params);

        mBottomViewList = new View[BOTTOM_VIEW_NUM];
        mPauseView = (ImageView) findViewById(R.id.click_to_pause);
        mPauseView.setOnClickListener(mButtonObserver);
        mPauseView.getDrawable().setLevel(2);
        mRotateView = findViewById(R.id.click_to_rotate);
        mRotateView.setOnClickListener(mButtonObserver);
        mBeautyLayout = findViewById(R.id.beauty_choose);
        mBottomViewList[BEAUTY_LAYOUT_INDEX] = mBeautyLayout;
        mFilterLayout = findViewById(R.id.edit_filter_choose);
        mBottomViewList[FILTER_LAYOUT_INDEX] = mFilterLayout;
        mSpeedLayout = findViewById(R.id.speed_layout);
        mBottomViewList[SPEED_LAYOUT_INDEX] = mSpeedLayout;

        mVideoScaleLayout = findViewById(R.id.video_scale_choose);
        mBottomViewList[VIDEO_SCALE_INDEX] = mVideoScaleLayout;
        mVideoRangeLayout = findViewById(R.id.video_range_choose);
        mVideoScale9_16 = findViewById(R.id.click_to_9_16);
        mVideoScale9_16.setOnClickListener(mButtonObserver);
        mVideoScale3_4 = findViewById(R.id.click_to_3_4);
        mVideoScale3_4.setOnClickListener(mButtonObserver);
        mVideoScale1_1 = findViewById(R.id.click_to_1_1);
        mVideoScale1_1.setOnClickListener(mButtonObserver);
        mVideoScaleFit = findViewById(R.id.video_scale_fit);
        mVideoScaleFit.setOnClickListener(mButtonObserver);
        mVideoScaleCrop = findViewById(R.id.video_scale_crop);
        mVideoScaleCrop.setOnClickListener(mButtonObserver);

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
        mAnimatedStickerLayout = findViewById(R.id.animate_sticker_choose);
        mBottomViewList[ANIMATED_STICKER_LAYOUT_INDEX] = mAnimatedStickerLayout;
        mSubtitleLayout = findViewById(R.id.subtitle_choose);
        mBottomViewList[SUBTITLE_LAYOUT_INDEX] = mSubtitleLayout;
        mPaintMenuLayout = findViewById(R.id.paint_menu_choose);
        mBottomViewList[PAINT_MENU_LAYOUT_INDEX] = mPaintMenuLayout;

        mEffectsView = findViewById(R.id.edit_filter_effects);
        mBottomViewList[FILTER_EFFECTS_INDEX] = mEffectsView;

        mEffectsView.setOnEffectsChangeListener(new FilterEffectsView.OnEffectsChangeListener() {
            @Override
            public int onAddFilterStart(int type) {
                ImgFilterBase filterBase = null;
                switch (type) {
                    case 0:
                        filterBase = new ImgShakeColorFilter(mEditKit.getGLRender());
                        break;
                    case 1:
                        filterBase = new ImgShakeShockWaveFilter(mEditKit.getGLRender());
                        break;
                    case 2:
                        filterBase = new ImgShakeZoomFilter(mEditKit.getGLRender());
                        break;
                    case 3:
                        filterBase = new ImgBeautySpecialEffectsFilter(mEditKit.getGLRender(), EditActivity.this,
                                ImgBeautySpecialEffectsFilter.KSY_SPECIAL_EFFECT_LIGHTING);
                        break;
                    case 4:
                        GPUImageSobelEdgeDetection sobelEdgeDetection = new GPUImageSobelEdgeDetection();
                        filterBase = new ImgTexGPUImageFilter(mEditKit.getGLRender(), sobelEdgeDetection);
                        break;
                    case 5:
                        filterBase = new ImgShake70sFilter(mEditKit.getGLRender());
                        break;
                    case 6:
                        filterBase = new ImgShakeIllusionFilter(mEditKit.getGLRender());
                        break;
                    case 7:
                        filterBase = new ImgShaderXSingleFilter(mEditKit.getGLRender());
                        break;
                    default:
                        break;
                }
                TimerEffectInfo effectInfo = new TimerEffectInfo(mEditKit.getEditPreviewCurrentPosition(),
                        mEditPreviewDuration, new TimerEffectFilter(filterBase));

                int filterId = mEditKit.addTimerEffectFilter(effectInfo);
                return filterId;
            }

            @Override
            public void onUpdateFilter(int index) {
                resumePreview();
            }

            @Override
            public void onAddFilterEnd(int index, long position) {
                //pause
                pausePreview();
                mEditKit.updateTimerEffectEndTime(index, position);
            }

            @Override
            public void onDelete(int index) {
                TimerEffectInfo info = mEditKit.getTimerEffectInfo(index);
                if (mEditKit.getReversePlay()) {
                    //在倒放状态添加时间特效时，由于ui 是seek到正序的时间，因此需要配合seek到结束时间,并且需要把结束时间算折算一下
                    long endTime = mEditKit.getEditDuration() - info.endTime;
                    mEditKit.seekTo(endTime);
                } else {
                    mEditKit.seekTo(info.startTime);
                }

                //pause
                pausePreview();
                mEditKit.removeTimerEffectFilter(index);
            }

            @Override
            public void onProgressChanged(long position) {
                mEditKit.seekTo(position);
                mSectionView.scrollAuto(position);
            }
        });

        mPaintView = findViewById(R.id.edit_paint_view);
        mPaintView.setBgColor(Color.TRANSPARENT);
        mPaintView.setPaintEnable(false);
        mIsPainting = false;
        mPaintMenuView = new PaintMenu(this, mPaintMenuLayout, mPaintView);
        mPaintMenuView.setOnPaintOpera(new PaintMenu.OnPaintComplete() {
            @Override
            public void completePaint() {
                mBottomViewList[mBottomViewPreIndex].setVisibility(View.GONE);
                mPaintView.setPaintEnable(false);
                mTitleAdapter.clear();
            }
        });

        mKSYStickerView = (KSYStickerView) findViewById(R.id.sticker_panel);

        //初始化动态贴纸UI
        mAnimatedStickerList = (RecyclerView) findViewById(R.id.animated_stickers_list);
        LinearLayoutManager listLayoutManager = new LinearLayoutManager(
                this);
        listLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mAnimatedStickerList.setLayoutManager(listLayoutManager);
        mAnimatedStickerAdapter = new StickerAdapter(this);
        mAnimatedStickerList.setAdapter(mAnimatedStickerAdapter);
        //Adapter中设置贴纸的路径，默认支持的是assets目录下面的，其它目录需要自行修改Adapter
        mAnimatedStickerAdapter.addStickerImages(mAnimateStickerPath);
        //添加Item选择事件用于添加动态贴纸
        mAnimatedStickerAdapter.setOnStickerItemClick(mOnAnimatedStickerItemClick);

        //初始化图片贴纸UI
        mStickerList = (RecyclerView) findViewById(R.id.stickers_list);
        LinearLayoutManager stickerListLayoutManager = new LinearLayoutManager(
                this);
        stickerListLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mStickerList.setLayoutManager(stickerListLayoutManager);
        mStickerAdapter = new StickerAdapter(this);
        mStickerList.setAdapter(mStickerAdapter);
        //Adapter中设置贴纸的路径，默认支持的是assets目录下面的，其它目录需要自行修改Adapter
        mStickerAdapter.addStickerImages(mStickerPath);
        //添加Item选择事件用于添加图片贴纸
        mStickerAdapter.setOnStickerItemClick(mOnStickerItemClick);

        //init 字幕贴纸UI
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
        //Adapter中设置贴纸的路径，默认支持的是assets目录下面的，其它目录需要自行修改Adapter
        mTextStickerAdapter.addStickerImages(mTextStickerPath);
        //添加Item选择事件用于添加字幕贴纸
        mTextStickerAdapter.setOnStickerItemClick(mOnTextStickerItemClick);
        //字幕贴纸的颜色选择器
        mColorPicker = new ColorPicker(this, 255, 255, 255);
        mTextColorSelect.setOnClickListener(mButtonObserver);
        //片段编辑
        mSectionView = (SectionSeekLayout) findViewById(R.id.session_layout);

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
        //添加贴纸View到SDK
        mEditKit.addStickerView(mKSYStickerView);
        mEditKit.setTimerEffectOverlying(false);

        mSpeedInfo.setText(String.valueOf(mEditKit.getNomalSpeed()));
        Bundle bundle = getIntent().getExtras();
        String url = bundle.getString(SRC_URL);
        if (!TextUtils.isEmpty(url)) {
            mEditKit.setEditPreviewUrl(url);
        }
        mEditKit.setTimerEffectOverlying(false);

        initTitleRecycleView();
        initBeautyUI();
        initFilterUI();
        initVideoRange();
        initBgmView();
        initSoundEffectView();
        initSticker();
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
                        mEditKit.setBGMRanges(start, end, true);
                    }
                };
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
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
        });

        mEditPreviewView.setOnTouchListener(mPreviewViewTouchListener);

        //下载片尾视频，在startCompose前执行即可
        File tailMp4 = new File(mTailVideoPath);
        if (!tailMp4.exists()) {
            mTailLoadTask = new DownloadAndHandleTask(mTailVideoPath, new DownloadAndHandleTask.DownloadListener() {
                @Override
                public void onCompleted(String filePath) {
                    mTailVideoPath = filePath;
                }
            });
            mTailLoadTask.execute(mTailVideoUrl);
        }
    }

    private View.OnTouchListener mPreviewViewTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent event) {
            //获取相对屏幕的坐标，即以屏幕左上角为原点
            mLastRawX = event.getRawX();
            mLastRawY = event.getRawY();
            // 预览区域的大小
            RelativeLayout.LayoutParams previewParams = (RelativeLayout.LayoutParams) mPreviewLayout
                    .getLayoutParams();
            int previewHeight = previewParams.height;
            int previewWidth = previewParams.width;

            //预览的crop信息
            int left = (int) (previewParams.leftMargin -
                    mEditKit.getPreviewCropRect().left * previewWidth);
            int right = previewWidth;
            int top = (int) (previewParams.topMargin -
                    mEditKit.getPreviewCropRect().top * previewHeight);
            int bottom = previewHeight;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (isPreviewScreenArea(event.getX(), event.getY(), left, right, top, bottom,
                            false, true)) {
                        //获取相对preview区域的坐标，即以preview左上角为原点
                        mPreviewTouchStartX = event.getX() - left;
                        mPreviewTouchStartY = event.getY() - top;
                        mTouchLastX = event.getX();
                        mTouchLastY = event.getY();
                    }
                    break;
                case MotionEvent.ACTION_MOVE:
                    int moveX = (int) Math.abs(event.getX() - mTouchLastX);
                    int moveY = (int) Math.abs(event.getY() - mTouchLastY);
                    if (mPreviewTouchStartX > 0 && mPreviewTouchStartY > 0 && ((moveX >
                            PREVIEW_TOUCH_MOVE_MARGIN) ||
                            (moveY > PREVIEW_TOUCH_MOVE_MARGIN))) {
                        //触发移动
                        mIsPreviewMoved = true;
                        updatePreviewView();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    //未移动
                    if (!mIsPreviewMoved) {
                        updateBottomVisible((int) mLastRawX, (int) mLastRawY);
                        //对编辑状态的贴纸生效
                        mSectionView.calculateRange();
                    }

                    mIsPreviewMoved = false;
                    mPreviewTouchStartX = 0f;
                    mPreviewTouchStartY = 0f;
                    mTouchLastX = 0f;
                    mTouchLastY = 0f;
                    break;
                default:
                    break;
            }
            return true;
        }
    };

    /**
     * 是否在小窗区域移动
     *
     * @param x      当前点击的相对屏幕左上角的x坐标
     * @param y      当前点击的相对屏幕左上角的y坐标
     * @param left   预览左上角距离屏幕区域左上角的x轴距离
     * @param right  预览右上角距离屏幕区域左上角的x轴距离
     * @param top    预览左上角距离屏幕区域左上角的y轴距离
     * @param bottom 预览右上角距离屏幕区域左上角的y轴距离
     * @return
     */
    private boolean isPreviewScreenArea(float x, float y, int left, int right, int top, int
            bottom, boolean enableX, boolean enableY) {
        if (enableX && enableY) {
            if (x > left && x < right &&
                    y > top && y < bottom) {
                return true;
            }
        } else if (enableX) {
            if (x > left && x < right) {
                return true;
            }
        } else if (enableY) {
            if (y > top && y < bottom) {
                return true;
            }
        }

        return false;
    }

    /**
     * 根据手指滑动的距离对预览区域进行裁剪显示
     */
    public void updatePreviewView() {
        //裁剪模式下，并且有多余的区域需要裁剪才进行裁剪，否则不尽兴
        if (mEditKit.getCropScale() <= 0 || mScaleMode != KSYEditKit.SCALING_MODE_CROP) {
            return;
        }
        RelativeLayout.LayoutParams previewParams = (RelativeLayout.LayoutParams) mPreviewLayout
                .getLayoutParams();
        int previewHeight = previewParams.height;
        int previewWidth = previewParams.width;
        //只有裁剪模式才需要更新预览的显示区域

        if (!mEditKit.getIsLandscape()) {
            //竖屏模式，上下裁剪，
            //更新窗口位置参数
            float newY = (mLastRawY - mPreviewTouchStartY);
            mMaxCrop = previewParams.topMargin;
            mMinCrop = 0.f - mEditKit.getCropScale() * (float) previewHeight + previewParams
                    .topMargin;
            //不能超出可裁剪范围
            if (newY > mMaxCrop) {
                newY = mMaxCrop;
            }

            if (newY < mMinCrop) {
                newY = mMinCrop;
            }

            float top = newY / previewHeight;
            mEditKit.setPreviewCrop(0.f, 0.f - top, 1.f, 0.f);
        } else {
            //横屏模式左右裁剪
            float newX = (mLastRawX - mPreviewTouchStartX);
            mMaxCrop = 0.f;
            mMinCrop = 0.f - mEditKit.getCropScale() * (float) previewWidth + previewParams
                    .leftMargin;
            //不能超出可裁剪范围
            if (newX > mMaxCrop) {
                newX = mMaxCrop;
            }

            if (newX < mMinCrop) {
                newX = mMinCrop;
            }

            float left = newX / previewWidth;
            mEditKit.setPreviewCrop(0.f - left, 0.f, 0.f, 1.f);
        }
    }

    public void resizePreview(int type, int mode) {
        mScaleType = type;
        mScaleMode = mode;

        mEditKit.setScaleType(type);
        mEditKit.setScalingMode(mScaleMode);

        //默认全屏显示预览
        int previewWidth = mScreenWidth;
        int previewHeight = mScreenHeight;

        //根据不同比例来更新预览区域大小
        //to 3:4
        if (mScaleType == KSYEditKit.SCALE_TYPE_3_4) {
            previewHeight = previewWidth * 4 / 3;
        }
        //to 1:1
        if (mScaleType == KSYEditKit.SCALE_TYPE_1_1) {
            previewHeight = previewWidth;
        }

        RelativeLayout.LayoutParams previewParams = (RelativeLayout.LayoutParams) mPreviewLayout
                .getLayoutParams();
        previewParams.height = previewHeight;
        previewParams.width = previewWidth;

        mPreviewLayout.setLayoutParams(previewParams);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPaused = false;
        mEditKit.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPaused = true;
        mEditKit.onPause();
        if (mInputMethodManager.isActive(mTextInput)) {
            mInputMethodManager.hideSoftInputFromWindow(mTextInput.getWindowToken(), 0);
            getCurrentFocus().clearFocus();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }

        if (mComposeDialog != null) {
            mComposeDialog.closeDialog();
            mComposeDialog = null;
        }
        mBgmAdapter.setOnItemClickListener(null);
        mBgmAdapter.clearTask();
        mKSYStickerView.setOnStickerSelected(null);
        stopPreviewTimerTask();
        mSectionView.stopPreview();
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
                updateBottomVisible(x, y);
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:

                break;
            default:
                break;
        }

        return super.onTouchEvent(event);
    }

    private void updateBottomVisible(int x, int y) {
        if (!isTouchPointInView(mBarBottomLayout, x, y)) {
            if (mBottomViewPreIndex != WATER_MARK_INDEX && mBottomViewPreIndex != REVERSE_PLAY_INDEX) {
                mBottomViewList[mBottomViewPreIndex].setVisibility(View.INVISIBLE);
                if (mBottomViewPreIndex == PAINT_MENU_LAYOUT_INDEX) {
                    mPaintView.setPaintEnable(false);
                    mIsPainting = false;
                }
                if (mTitleAdapter != null) {
                    mTitleAdapter.clear();
                }
            }
        }
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

    /*********************************贴纸 begin***************************************/
    private void initSticker() {
        //片段信息变更回调
        mSectionView.setOnSectionSeekListener(new SectionSeekLayout.OnSectionSeekListener() {
            @Override
            public void onRangeChanged(int index, long start, long end) {
                // 更新贴纸显示时间区间
                Log.d(TAG, "update sticker range:" + start + "~" + (end - start));
                mKSYStickerView.updateStickerInfo(index, start, end - start);
            }

            @Override
            public void removeSticker(int id) {
                // 删除贴纸
                mKSYStickerView.removeSticker(id);
            }

            @Override
            public void onPausePreview() {
                onPauseClick();
            }

            @Override
            public void onSeekTo(long time) {
                mEditKit.seekTo(time);
                mEditKit.updateStickerDraw();
                if (mEffectsView.getVisibility() == View.VISIBLE) {
                    mEffectsView.setProgress(mEditKit.getCurrentPosition());
                }
            }
        });

        //贴纸信息变更回调
        mKSYStickerView.setOnStickerSelected(new KSYStickerView.OnStickerStateChanged() {
            /**
             * 某一个贴纸被选择
             * @param index  被选择的贴纸的index
             * @param text  被选择的贴纸的text信息，若为非null说明为字幕贴纸
             */
            @Override
            public void selected(int index, String text) {
                //重新选择某一个区间
                //进入贴纸编辑状态，需要先暂停预览播放
                pausePreview();
                mSectionView.startSeek(index);
                //带字幕的贴纸
                if (!TextUtils.isEmpty(text)) {
                    mTextInput.setText(text);
                    mTextInput.setSelection(text.length());
                    //显示输入框
                    mTextInput.requestFocus();
                    mInputMethodManager.showSoftInput(mTextInput, InputMethodManager.RESULT_SHOWN);
                }
            }

            /**
             * 某一个贴纸被删除
             * @param list 被删除的贴纸的index集合
             * @param text 删除贴纸的text信息，若为非null说明为当前编辑贴纸并且是字幕贴纸
             */
            @Override
            public void deleted(List<Integer> list, String text) {
                //带字幕的当前贴纸
                mSectionView.delete(list);
                if (!TextUtils.isEmpty(text)) {
                    mTextInput.setText(null);
                }
            }
        });
    }

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
        //只有字幕贴纸该设置才会生效
        mKSYStickerView.setCurrentTextColor(newColor);
    }

    private StickerAdapter.OnStickerItemClick mOnAnimatedStickerItemClick = new StickerAdapter.OnStickerItemClick() {
        @Override
        public void selectedStickerItem(String path) {
            StringBuilder urlInfo = new StringBuilder("assets://AnimatedStickers/gif/");
            if (path.contains("1")) {
                urlInfo.append("1.gif");
            } else if (path.contains("2")) {
                urlInfo.append("2.gif");
            } else if (path.contains("3")) {
                urlInfo.append("3.gif");
            } else if (path.contains("4")) {
                urlInfo.append("4.gif");
            } else if (path.contains("5")) {
                urlInfo.append("5.gif");
            } else if (path.contains("6")) {
                urlInfo.append("6.webp");
            } else if (path.contains("7")) {
                urlInfo.append("7.webp");
            } else if (path.contains("8")) {
                urlInfo.append("8.webp");
            } else if (path.contains("9")) {
                urlInfo.append("9.webp");
            }

            KSYStickerInfo info = new KSYStickerInfo();
            initStickerHelpBox();

            info.startTime = Long.MIN_VALUE;
            info.duration = mEditKit.getEditDuration();
            info.animateUrl = urlInfo.toString();
            info.stickerType = KSYStickerInfo.STICKER_TYPE_IMAGE_ANIMATE; //是否是字幕贴纸
            //添加一个贴纸

            int index = mKSYStickerView.addSticker(info, mStickerHelpBoxInfo);
            if (index >= 0) {
                //进入贴纸编辑状态，需要先暂停预览播放
                pausePreview();
                // 选择下一个贴纸时让前一个贴纸生效（如果之前已选择一个贴纸）
                if (mSectionView.isSeeking()) {
                    mSectionView.calculateRange();
                }
                //开始当前贴纸的片段编辑
                mSectionView.startSeek(index);
            } else {
                Log.e(TAG, "add sticker failed");
            }

        }
    };

    private StickerAdapter.OnStickerItemClick mOnStickerItemClick = new StickerAdapter.OnStickerItemClick() {
        @Override
        public void selectedStickerItem(String path) {
            if (path.contains("0")) {
                //删除所有图片贴纸
                mKSYStickerView.removeBitmapStickers();
                return;
            }

            KSYStickerInfo info = new KSYStickerInfo();
            initStickerHelpBox();

            info.bitmap = getImageFromAssetsFile(path);
            info.startTime = Long.MIN_VALUE;
            info.duration = mEditKit.getEditDuration();
            info.stickerType = KSYStickerInfo.STICKER_TYPE_IMAGE; //是否是字幕贴纸
            //添加一个贴纸
            int index = mKSYStickerView.addSticker(info, mStickerHelpBoxInfo);
            if (index >= 0) {
                //进入贴纸编辑状态，需要先暂停预览播放
                pausePreview();
                // 选择下一个贴纸时让前一个贴纸生效（如果之前已选择一个贴纸）
                if (mSectionView.isSeeking()) {
                    mSectionView.calculateRange();
                }
                //开始当前贴纸的片段编辑
                mSectionView.startSeek(index);
            } else {
                Log.e(TAG, "add sticker failed");
            }

        }
    };

    private StickerAdapter.OnStickerItemClick mOnTextStickerItemClick = new StickerAdapter
            .OnStickerItemClick() {
        @Override
        public void selectedStickerItem(String path) {
            if (path.contains("0")) {
                //移除所有字幕
                mKSYStickerView.removeTextStickers();
                return;
            }
            KSYStickerInfo params = new KSYStickerInfo();

            //字幕贴纸的文字相关信息
            DrawTextParams textParams = new DrawTextParams();
            textParams.textPaint = new TextPaint();
            textParams.textPaint.setTextSize(DrawTextParams.DEFAULT_TEXT_SIZE);
            textParams.textPaint.setColor(mKSYStickerView.getCurrentTextColor());
            textParams.textPaint.setTextAlign(Paint.Align.LEFT);
            textParams.textPaint.setStyle(Paint.Style.FILL);
            textParams.textPaint.setAntiAlias(true);
            textParams.text = mTextInput.getText().toString().trim();
            textParams.autoNewLine = false;

            //字幕贴纸的文字有效范围限制
            if (path.contains("3")) {
                textParams.text_left_padding = 0.11f;
                textParams.text_right_padding = 0.17f;
                textParams.text_top_padding = 0.35f;
                textParams.text_bottom_padding = 0.23f;
            }
            if (path.contains("6")) {
                textParams.text_left_padding = 0.159f;
                textParams.text_right_padding = 0.22f;
                textParams.text_top_padding = 0.23f;
                textParams.text_bottom_padding = 0.36f;
            }

            if (path.contains("2")) {
                textParams.text_left_padding = 0.362f;
                textParams.text_right_padding = 0.1f;
                textParams.text_top_padding = 0.461f;
                textParams.text_bottom_padding = 0.09f;
            }

            if (path.contains("4")) {
                textParams.text_left_padding = 0.121f;
                textParams.text_right_padding = 0.32f;
                textParams.text_top_padding = 0.19f;
                textParams.text_bottom_padding = 0.202f;
            }

            if (path.contains("5")) {
                textParams.text_left_padding = 0.276f;
                textParams.text_right_padding = 0.271f;
                textParams.text_top_padding = 0.265f;
                textParams.text_bottom_padding = 0.245f;
            }

            if (path.contains("1")) {
                //添加无背景的字幕
                params.bitmap = null;
                if (TextUtils.isEmpty(textParams.text)) {
                    mTextInput.setText("input text");
                    textParams.text = mTextInput.getText().toString().trim();
                }
            } else {
                params.bitmap = getImageFromAssetsFile(path);
            }

            params.textParams = textParams;
            params.startTime = Long.MIN_VALUE;
            params.duration = mEditKit.getEditDuration();
            params.stickerType = KSYStickerInfo.STICKER_TYPE_TEXT;  //是否是字幕贴纸

            int index = mKSYStickerView.addSticker(params, mStickerHelpBoxInfo);

            if (index >= 0) {
                //进入贴纸编辑状态，需要先暂停预览播放
                pausePreview();
                // 选择下一个贴纸时让前一个贴纸生效（如果之前已选择一个贴纸）
                if (mSectionView.isSeeking()) {
                    mSectionView.calculateRange();
                }
                //开始当前贴纸的片段编辑
                mSectionView.startSeek(index);
            } else {
                Log.e(TAG, "add sticker failed");
            }
        }
    };

    /**
     * 暂停预览，方便片段编辑
     */
    private void pausePreview() {
        if (mPauseView.getDrawable().getLevel() == 2) {
            mEditKit.pausePlay(true);
            mPauseView.getDrawable().setLevel(1);
            stopPreviewTimerTask();
        }
    }

    private void resumePreview() {
        if (mPauseView.getDrawable().getLevel() != 2) {
            mEditKit.pausePlay(false);
            mPauseView.getDrawable().setLevel(2);
            mSectionView.calculateRange();
            startPreviewTimerTask();
            //恢复播放的时候，需要调用setDrawHelpTool隐藏当前编辑态的贴纸的辅助绘制区域
            mKSYStickerView.setDrawHelpTool(false);
        }
    }

    /**
     * 贴纸的辅助区域
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
            if (!mKSYStickerView.setCurrentText(text)) {
                Toast.makeText(EditActivity.this, "请先选择字幕类型", Toast.LENGTH_SHORT).show();
            }
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

    /*********************************sticker end***************************************/

    private void onWaterMarkLogoClick(boolean isCheck) {
        if (isCheck) {
            mEditKit.showWaterMarkLogo(mLogoPath, 0.08f, 0.04f, 0.20f, 0, 0.8f);
        } else {
            mEditKit.hideWaterMarkLogo();
        }
    }

    private void onReversePlayClick(boolean isCheck) {
        mEditKit.stopEditPreview();
        if (isCheck) {
            mEditKit.enableReversePlay(true);
        } else {
            mEditKit.enableReversePlay(false);
        }
        mEditKit.setLooping(true);
        mEditKit.startEditPreview();
    }

    private void onPauseClick() {
        if (mPauseView.getDrawable().getLevel() == 2) {
            pausePreview();
        } else {
            resumePreview();
        }
    }

    private void onSpeedClick(boolean plus) {
        mEditKit.updateSpeed(plus);
        DecimalFormat decimalFormat = new DecimalFormat(".0");
        String text = decimalFormat.format(mEditKit.getSpeed());
        mSpeedInfo.setText(text);
    }

    private void onBackoffClick() {
        if ((mBottomViewPreIndex == WATER_MARK_INDEX) || mBottomViewPreIndex == REVERSE_PLAY_INDEX ||
                (mBottomViewPreIndex != WATER_MARK_INDEX && mBottomViewPreIndex != REVERSE_PLAY_INDEX &&
                        mBottomViewList[mBottomViewPreIndex].getVisibility() != View.VISIBLE)) {
            EditActivity.this.finish();
        } else {
            mBottomViewList[mBottomViewPreIndex].setVisibility(View.INVISIBLE);
            if (mBottomViewPreIndex == PAINT_MENU_LAYOUT_INDEX) {
                mPaintView.setPaintEnable(false);
                mIsPainting = false;
            }
            if (mTitleAdapter != null) {
                mTitleAdapter.clear();
            }
        }
    }

    private void onNextClick() {
        mBgmAdapter.clearTask();
        showConfigDialog();
        if (mSectionView.isSeeking()) {
            mSectionView.calculateRange();
        }
    }

    private void showConfigDialog() {
        if (mConfigDialog != null) {
            mConfigDialog.show();
            return;
        }
        mConfigDialog = new Dialog(this, R.style.dialog);
        View contentView = LayoutInflater.from(this).inflate(R.layout.config_popup_layout, null);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mConfigDialog.setContentView(contentView, params);
        mOutRes480p = (TextView) contentView.findViewById(R.id.output_config_r480p);
        mOutRes480p.setOnClickListener(mButtonObserver);
        mOutRes540p = (TextView) contentView.findViewById(R.id.output_config_r540p);
        mOutRes540p.setOnClickListener(mButtonObserver);
        mOutEncodeWithH264 = (TextView) contentView.findViewById(R.id.output_config_h264);
        mOutEncodeWithH264.setOnClickListener(mButtonObserver);
        mOutEncodeWithH265 = (TextView) contentView.findViewById(R.id.output_config_h265);
        mOutEncodeWithH265.setOnClickListener(mButtonObserver);
        mOutEncodeByHW = (TextView) contentView.findViewById(R.id.output_config_hw);
        mOutEncodeByHW.setOnClickListener(mButtonObserver);
        mOutEncodeBySW = (TextView) contentView.findViewById(R.id.output_config_sw);
        mOutEncodeBySW.setOnClickListener(mButtonObserver);
        mOutDecodeByHW = (TextView) contentView.findViewById(R.id.output_config_decode_hw);
        mOutDecodeByHW.setOnClickListener(mButtonObserver);
        mOutDecodeBySW = (TextView) contentView.findViewById(R.id.output_config_decode_sw);
        mOutDecodeBySW.setOnClickListener(mButtonObserver);
        mOutForMP4 = (TextView) contentView.findViewById(R.id.output_config_mp4);
        mOutForMP4.setOnClickListener(mButtonObserver);
        mOutForGIF = (TextView) contentView.findViewById(R.id.output_config_gif);
        mOutForGIF.setOnClickListener(mButtonObserver);
        mOutProfileGroup = new TextView[3];
        mOutAudioProfileGroup = new TextView[3];
        for (int i = 0; i < mOutProfileGroup.length; i++) {
            mOutProfileGroup[i] = (TextView) contentView.findViewById(OUTPUT_PROFILE_ID[i]);
            mOutProfileGroup[i].setOnClickListener(mButtonObserver);

            mOutAudioProfileGroup[i] = (TextView) contentView.findViewById(AUDIO_OUTPUT_PROFILE_ID[i]);
            mOutAudioProfileGroup[i].setOnClickListener(mButtonObserver);
        }
        mOutFrameRate = (EditText) contentView.findViewById(R.id.output_config_frameRate);
        mOutVideoBitrate = (EditText) contentView.findViewById(R.id.output_config_video_bitrate);
        mOutAudioBitrate = (EditText) contentView.findViewById(R.id.output_config_audio_bitrate);
        mOutVideoCRF = (EditText) contentView.findViewById(R.id.output_config_video_crf);
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
        mOutEncodeBySW.setActivated(true);
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

    private void onOutputConfirmClick() {
        confirmConfig();
        if (mConfigDialog.isShowing()) {
            mConfigDialog.dismiss();
        }
        showComposeDialog();
        //配置合成参数
        if (mComposeConfig != null) {
            //配置合成参数
            mEditKit.setTargetResolution(mComposeConfig.resolution);
            mEditKit.setVideoFps(mComposeConfig.fps);
            mEditKit.setEncodeMethod(mComposeConfig.encodeMethod);
            mEditKit.setVideoCodecId(mComposeConfig.encodeType);
            mEditKit.setVideoEncodeProfile(mComposeConfig.encodeProfile);
            mEditKit.setAudioKBitrate(mComposeConfig.audioBitrate);
            mEditKit.setVideoKBitrate(mComposeConfig.videoBitrate);
            mEditKit.setVideoDecodeMethod(mComposeConfig.decodeMethod);
            mEditKit.setTailUrl(mTailVideoPath);
            mEditKit.addPaintView(mPaintView);
            mEditKit.setAudioEncodeProfile(mComposeConfig.audioEncodeProfile);
            startCompose();
        }
    }

    private void startCompose() {
        //设置合成路径
        String fileFolder = Environment.getExternalStorageDirectory().
                getAbsolutePath() + "/ksy_sv_compose_test";
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

        if (mOutForGIF.isActivated()) {
            mComposeConfig.encodeType = AVConst.CODEC_ID_GIF;
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
    }

    /**
     * 不支持硬编的设备，fallback到软编
     */
    private boolean handleEncodeError() {
        int encodeMethod = mEditKit.getVideoEncodeMethod();
        if (encodeMethod == StreamerConstants.ENCODE_METHOD_HARDWARE) {
            mHWEncoderUnsupported = true;
            if (mSWEncoderUnsupported) {
                Log.e(TAG, "Got HW and SW encoder error, compose failed");
                return false;
            } else {
                mEditKit.setEncodeMethod(StreamerConstants.ENCODE_METHOD_SOFTWARE);
                Log.e(TAG, "Got HW encoder error, switch to SOFTWARE mode");
            }
        } else if (encodeMethod == StreamerConstants.ENCODE_METHOD_SOFTWARE) {
            mSWEncoderUnsupported = true;
            if (mHWEncoderUnsupported) {
                Log.e(TAG, "Got SW and HW encoder error, compose failed");
                return false;
            } else {
                mEditKit.setEncodeMethod(StreamerConstants.ENCODE_METHOD_HARDWARE);
                Log.e(TAG, "Got SW encoder error, switch to HARDWARE mode");
            }
        }
        return true;
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
                    if (mComposeDialog != null && mComposeDialog.isShowing()) {
                        mComposeDialog.closeDialog();
                        resumeEditPreview();
                    }
                    break;
                case ShortVideoConstants.SHORTVIDEO_ERROR_SDK_AUTHFAILED:
                    Log.d(TAG, "sdk auth failed:" + type);
                    Toast.makeText(EditActivity.this,
                            "Auth failed can't start compose:" + type, Toast.LENGTH_LONG).show();
                    if (mComposeDialog != null) {
                        mComposeDialog.closeDialog();
                        resumeEditPreview();
                    }
                    break;
                case ShortVideoConstants.SHORTVIDEO_EDIT_PREVIEW_PLAYER_ERROR:
                    Log.d(TAG, "KSYEditKit preview player error:" + type + "_" + msg);
                    break;
                case ShortVideoConstants.SHORTVIDEO_ERROR_EDIT_FEATURE_NOT_SUPPORTED:
                    Toast.makeText(EditActivity.this,
                            "This Feature not support:" + type, Toast.LENGTH_LONG).show();
                    break;
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNSUPPORTED:
                case StreamerConstants.KSY_STREAMER_VIDEO_ENCODER_ERROR_UNKNOWN:
                    boolean result = handleEncodeError();
                    if(result) {
                        mMainHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startCompose();
                            }
                        }, 200);
                    }
                    break;
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
                    mEditPreviewDuration = mEditKit.getEditDuration();
                    mPreviewLength = mEditPreviewDuration;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initSeekBar();
                            initThumbnailAdapter();
                            // 启动预览后，开始片段编辑UI初始化
                            mSectionView.init(mEditPreviewDuration, mEditKit);
                            mEffectsView.initView(mEditPreviewDuration, mEditKit);
                            startPreviewTimerTask();
                        }
                    });
                    break;
                case ShortVideoConstants.SHORTVIDEO_EDIT_PREVIEW_ONE_LOOP_END:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mEffectsView.getVisibility() == View.VISIBLE) {
                                mEffectsView.setProgress(mEditKit.getEditDuration());
                            }
                        }
                    });
                    break;
                case ShortVideoConstants.SHORTVIDEO_COMPOSE_START: {
                    Log.d(TAG, "compose started");
                    //合成开始null，再次预览时重新创建
                    clearImgFilter();
                    mEditKit.pauseEditPreview();
                    if (mComposeDialog != null && mComposeDialog.isShowing()) {
                        mComposeDialog.composeStarted();
                    }
                    break;
                }
                case ShortVideoConstants.SHORTVIDEO_COMPOSE_FINISHED: {
                    Log.d(TAG, "compose finished");
                    if (mComposeDialog != null && mComposeDialog.isShowing()) {
                        mComposeDialog.composeFinished(msgs[0]);
                    }
                    mComposeFinished = true;

                    //通过ProbeMediaInfoTools获取合成后文件信息
//                    ProbeMediaInfoTools probeMediaInfoTools = new ProbeMediaInfoTools();
//                    probeMediaInfoTools.probeMediaInfo(msgs[0],
//                            new ProbeMediaInfoTools.ProbeMediaInfoListener() {
//                        @Override
//                        public void probeMediaInfoFinished(ProbeMediaInfoTools.MediaInfo info) {
//                            if(info != null) {
//                                Log.d(TAG, "url:" + info.url);
//                                Log.d(TAG, "duration:" + info.duration);
//                            }
//                        }
//                    });
//                    //get thumbnail for first frame
//                    probeMediaInfoTools.getVideoThumbnailAtTime(msgs[0],0,0,0);
                    break;
                }
                case ShortVideoConstants.SHORTVIDEO_COMPOSE_ABORTED:
                    Log.d(TAG, "compose aborted by user");
                    break;
                case ShortVideoConstants.SHORTVIDEO_EDIT_PREVIEW_PLAYER_INFO:
                    Log.d(TAG, "KSYEditKit preview player info:" + type + "_" + msgs[0]);
                    break;
                case ShortVideoConstants.SHORTVIDEO_COMPOSE_TAIL_STARTED:
                    Log.d(TAG, "tail video compose started");
                    break;
                case ShortVideoConstants.SHORTVIDEO_COMPOSE_TITLE_STARTED:
                    Log.d(TAG, "title video compose started");
                default:
                    break;
            }
            return;
        }
    };

    private void startPreviewTimerTask() {
        mSectionView.startPreview();
        mPreviewRefreshTimer = new Timer();
        mPreviewRefreshTask = new TimerTask() {
            @Override
            public void run() {
                refreshUiOnUiThread();
            }
        };
        // 定义顶部滚动view的刷新频率为20fps
        mPreviewRefreshTimer.schedule(mPreviewRefreshTask, 50, 50);
    }

    private void stopPreviewTimerTask() {
        if (mPreviewRefreshTimer != null) {
            mPreviewRefreshTimer.cancel();
            mPreviewRefreshTimer = null;
        }
        if (mPreviewRefreshTask != null) {
            mPreviewRefreshTask.cancel();
            mPreviewRefreshTask = null;
        }
        mSectionView.stopPreview();
    }

    private void refreshUiOnUiThread() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                long curTime = mEditKit.getEditPreviewCurrentPosition();
                mSectionView.scrollAuto(curTime);
                if (mEffectsView.getVisibility() == View.VISIBLE) {
                    mEffectsView.setProgress(curTime);
                }
            }
        });
    }

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
                case R.id.output_config_hw:
                    mOutEncodeByHW.setActivated(true);
                    mOutEncodeBySW.setActivated(false);
                    mOutVideoCRF.setEnabled(false);
                    if (Integer.parseInt(mOutVideoBitrate.getText().toString()) == 0) {
                        mOutVideoBitrate.setText(String.valueOf(2000));
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
                    mOutForGIF.setActivated(false);
                    mOutEncodeWithH264.setEnabled(true);
                    mOutEncodeWithH265.setEnabled(true);
                    mOutEncodeWithH264.setActivated(true);
                    mOutEncodeWithH265.setActivated(false);
                    mOutEncodeByHW.setEnabled(true);
                    break;
                case R.id.output_config_gif:
                    mOutForMP4.setActivated(false);
                    mOutForGIF.setActivated(true);
                    mOutEncodeWithH264.setActivated(false);
                    mOutEncodeWithH265.setActivated(false);
                    mOutEncodeWithH264.setEnabled(false);
                    mOutEncodeWithH265.setEnabled(false);
                    //gif 不支持硬编
                    mOutEncodeByHW.setEnabled(false);
                    mOutEncodeByHW.setActivated(false);
                    mOutEncodeBySW.setActivated(true);
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
                    break;
                case R.id.click_to_9_16:
                    mVideoScale9_16.setActivated(true);
                    mVideoScale3_4.setActivated(false);
                    mVideoScale1_1.setActivated(false);
                    if (!mVideoScaleCrop.isActivated() && !mVideoScaleFit.isActivated()) {
                        mVideoScaleFit.setActivated(true);
                    }
                    resizePreview(KSYEditKit.SCALE_TYPE_9_16, mScaleMode);
                    break;
                case R.id.click_to_3_4:
                    mVideoScale3_4.setActivated(true);
                    mVideoScale9_16.setActivated(false);
                    mVideoScale1_1.setActivated(false);
                    if (!mVideoScaleCrop.isActivated() && !mVideoScaleFit.isActivated()) {
                        mVideoScaleFit.setActivated(true);
                    }
                    resizePreview(KSYEditKit.SCALE_TYPE_3_4, mScaleMode);
                    break;
                case R.id.click_to_1_1:
                    mVideoScale3_4.setActivated(false);
                    mVideoScale9_16.setActivated(false);
                    mVideoScale1_1.setActivated(true);
                    if (!mVideoScaleCrop.isActivated() && !mVideoScaleFit.isActivated()) {
                        mVideoScaleFit.setActivated(true);
                    }
                    resizePreview(KSYEditKit.SCALE_TYPE_1_1, mScaleMode);
                    break;
                case R.id.video_scale_fit:
                    mVideoScaleCrop.setActivated(false);
                    mVideoScaleFit.setActivated(true);
                    if (!mVideoScale3_4.isActivated() && !mVideoScale1_1.isActivated() &&
                            !mVideoScale9_16.isActivated()) {
                        mVideoScale9_16.setActivated(true);
                    }

                    resizePreview(mScaleType, KSYEditKit.SCALING_MODE_BEST_FIT);
                    break;
                case R.id.video_scale_crop:
                    mVideoScaleCrop.setActivated(true);
                    mVideoScaleFit.setActivated(false);
                    if (!mVideoScale3_4.isActivated() && !mVideoScale1_1.isActivated() &&
                            !mVideoScale9_16.isActivated()) {
                        mVideoScale9_16.setActivated(true);
                    }
                    resizePreview(mScaleType, KSYEditKit.SCALING_MODE_CROP);
                    break;
                case R.id.click_to_rotate:
                    onClickRotate();
                    break;
                default:
                    break;
            }
        }
    }

    private void onClickRotate() {
        mRotateDegrees += 90;
        if(mRotateDegrees == 360) {
            mRotateDegrees = 0;
        }
        mEditKit.setRotateDegrees(mRotateDegrees);
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
//                    }
//                    mMainHandler.postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            seekToPreview(mVideoRangeSeekBar.getRangeStart() + mHLVOffsetX);
//                        }
//                    }, 500);
                }
            });
        }

        @Override
        public void onActionUp() {
            rangeLoopPreview();
        }
    };

    /**
     * loop preview during range
     */
    private void rangeLoopPreview() {
        long startTime = (long) ((mVideoRangeSeekBar.getRangeStart() + mHLVOffsetX) * 1000);
        long endTime = (long) ((mVideoRangeSeekBar.getRangeEnd() + mHLVOffsetX) * 1000);

        //是否对播放区间的设置立即生效，true为立即生效
        mEditKit.setEditPreviewRanges(startTime, endTime, true);
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
        mEditKit.seekTo(seekTo);

        if (mVideoRangeSeekBar != null) {
            mVideoRangeSeekBar.setIndicatorOffsetSec((mEditKit.getEditPreviewCurrentPosition() * 1.0f - mHLVOffsetX * 1000) /
                    1000);
        }

        Log.d(TAG, "seek currentpostion:" + mEditKit.getEditPreviewCurrentPosition());
    }

    private void setRangeTextView(float offset) {
        mVideoRangeStart.setText(formatTimeStr(mVideoRangeSeekBar.getRangeStart() + offset));
        mVideoRangeEnd.setText(formatTimeStr(mVideoRangeSeekBar.getRangeEnd() + offset));

        mVideoRange.setText(formatTimeStr2(((int) (10 * mVideoRangeSeekBar.getRangeEnd()))
                - (int) (10 * mVideoRangeSeekBar.getRangeStart())));
        mPreviewLength = (long) (mVideoRangeSeekBar.getRangeEnd() -
                mVideoRangeSeekBar.getRangeStart()) * 1000;
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
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mHLVOffsetX = mVideoRangeSeekBar.getRange(currentX);

                    if (mEditPreviewDuration > mMaxClipSpanMs) {
                        if ((mVideoRangeSeekBar.getRangeEnd() + mHLVOffsetX) * 1000 >= mEditPreviewDuration) {
                            mHLVOffsetX = (mEditPreviewDuration / 1000 - mVideoRangeSeekBar.getRangeEnd());
                        }
                    }

                    setRangeTextView(mHLVOffsetX);

                    if (mLastX != mVideoRangeSeekBar.getRangeStart() + mHLVOffsetX) {
                        //do not need to effect
                        //rangeLoopPreview();
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
        String[] items = {"美颜", "滤镜", "滤镜特效", "水印", "变速", "时长裁剪", "画布裁剪", "音乐", "变声", "混响",
                "涂鸦", "动态贴纸", "贴纸", "字幕","倒放"};
        mTitleData = Arrays.asList(items);
        mTitleView = (RecyclerView) findViewById(R.id.edit_title_recyclerView);
        mTitleAdapter = new BottomTitleAdapter(this, mTitleData);
        BottomTitleAdapter.OnItemClickListener listener = new BottomTitleAdapter.OnItemClickListener() {
            @Override
            public void onClick(int curIndex, int preIndex) {
                mBottomViewPreIndex = curIndex;
                if (curIndex != WATER_MARK_INDEX && curIndex != REVERSE_PLAY_INDEX) {
                    mBottomViewList[curIndex].setVisibility(View.VISIBLE);
                    if (curIndex == FILTER_EFFECTS_INDEX) {
                        //暂停播放
                        pausePreview();
                        //更新进度条位置
                        mEffectsView.setProgress(mEditKit.getEditPreviewCurrentPosition());
                    } else {

                    }
                    //贴纸相关需要显示贴纸的预览view
                    if (curIndex >= ANIMATED_STICKER_LAYOUT_INDEX && curIndex <=
                            SUBTITLE_LAYOUT_INDEX && mKSYStickerView.getVisibility() != View.VISIBLE) {
                        mKSYStickerView.setVisibility(View.VISIBLE);
                    }

                    if (curIndex == PAINT_MENU_LAYOUT_INDEX) {
                        if (mPaintView.getVisibility() != View.VISIBLE) {
                            mPaintView.setVisibility(View.VISIBLE);
                        }
                        mPaintView.setPaintEnable(true);
                        mIsPainting = true;
                    }

                    if (curIndex == VIDEO_SCALE_INDEX ||
                            curIndex >= ANIMATED_STICKER_LAYOUT_INDEX && curIndex <=
                                    SUBTITLE_LAYOUT_INDEX) {
                        //裁剪和贴纸模式时需要隐藏涂鸦，否则接收不到touch时间，
                        //PaintView的bug，已经提issues：https://github.com/LiuHongtao/PaintView/issues/4
                        if (mPaintView.getVisibility() == View.VISIBLE) {
                            mPaintView.setVisibility(View.GONE);
                        }
                        Toast.makeText(EditActivity.this, "暂时隐藏涂鸦", Toast.LENGTH_SHORT).show();
                    } else {
                        if (mPaintView.getVisibility() != View.VISIBLE) {
                            mPaintView.setVisibility(View.VISIBLE);
                        }
                    }
                } else  if (curIndex == WATER_MARK_INDEX) {
                    if (curIndex != preIndex) {
                        mWaterMarkChecked = true;
                        onWaterMarkLogoClick(mWaterMarkChecked);
                    } else {
                        mWaterMarkChecked = !mWaterMarkChecked;
                        onWaterMarkLogoClick(mWaterMarkChecked);
                    }
                } else {
                    if (curIndex != preIndex) {
                        if (!mReversePlayChecked) {
                            mReversePlayChecked = true;
                            onReversePlayClick(mReversePlayChecked);
                        }
                    } else {
                        mReversePlayChecked = !mReversePlayChecked;
                        onReversePlayClick(mReversePlayChecked);
                    }
                }
                if (preIndex != WATER_MARK_INDEX && preIndex != REVERSE_PLAY_INDEX &&
                        preIndex != -1 && curIndex != preIndex) {
                    mBottomViewList[preIndex].setVisibility(View.GONE);
                    if (preIndex == PAINT_MENU_LAYOUT_INDEX) {
                        mPaintView.setPaintEnable(false);
                        mIsPainting = false;
                    }
                }
                if ((preIndex == STICKER_LAYOUT_INDEX || preIndex == SUBTITLE_LAYOUT_INDEX ||
                        preIndex == ANIMATED_STICKER_LAYOUT_INDEX) || preIndex == FILTER_EFFECTS_INDEX) {
                    if (mSectionView.isSeeking()) {
                        mSectionView.calculateRange();
                    }
                    //若暂停状态恢复到播放状态再继续进行其它功能的编辑
                    if (mPauseView.getDrawable().getLevel() == 1) {
                        onPauseClick();
                    }
                }
            }
        };
        mTitleAdapter.setOnItemClickListener(listener);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mTitleView.setLayoutManager(layoutManager);
        mTitleView.setAdapter(mTitleAdapter);
    }

    private void clearImgFilter() {
        mImgBeautyTypeIndex = BEAUTY_DISABLE;
        mEffectFilterIndex = FILTER_DISABLE;
        if (mBeautyFilters != null) {
            mBeautyFilters.clear();
        }
        if (mEffectFilters != null) {
            mEffectFilters.clear();
        }
        mBeautyAdapter.clear();
        changeOriginalBeautyState(true);

        mFilterAdapter.clear();
        changeOriginalImageState(true);

    }

    private void addBeautyFiler() {
        if (mImgBeautyTypeIndex == mLastImgBeautyTypeIndex) {
            return;
        }
        if (mBeautyFilters == null) {
            mBeautyFilters = new LinkedHashMap<>();
        }

        //disable beauty
        if (mImgBeautyTypeIndex == BEAUTY_DISABLE) {
            if (mBeautyFilters.containsKey(mLastImgBeautyTypeIndex)) {
                ImgFilterBase lastFilter = mBeautyFilters.get
                        (mLastImgBeautyTypeIndex);
                if (mEditKit.getImgTexFilterMgt().getFilter().contains(lastFilter)) {
                    mEditKit.getImgTexFilterMgt().replaceFilter(lastFilter, null, false);
                }
            }
            mLastImgBeautyTypeIndex = mImgBeautyTypeIndex;
            //暂停状态时，使得滤镜在预览区域立即生效
            mEditKit.queueLastFrame();
            return;
        }
        //enable filter
        if (mBeautyFilters.containsKey(mImgBeautyTypeIndex)) {
            ImgFilterBase filterBase = mBeautyFilters.get
                    (mImgBeautyTypeIndex);
            if (mBeautyFilters.containsKey(mLastImgBeautyTypeIndex)) {
                ImgFilterBase lastFilter = mBeautyFilters.get(mLastImgBeautyTypeIndex);
                if (mEditKit.getImgTexFilterMgt().getFilter().contains(lastFilter)) {
                    mEditKit.getImgTexFilterMgt().replaceFilter(lastFilter, filterBase, false);
                }
            } else {
                if (!mEditKit.getImgTexFilterMgt().getFilter().contains(filterBase)) {
                    mEditKit.getImgTexFilterMgt().addFilter(filterBase);
                }
            }
            mLastImgBeautyTypeIndex = mImgBeautyTypeIndex;
            //暂停状态时，使得滤镜在预览区域立即生效
            mEditKit.queueLastFrame();
            return;
        }
        ImgFilterBase filterBase = null;
        switch (mImgBeautyTypeIndex) {
            case BEAUTY_NATURE:
                ImgBeautySoftFilter softFilter = new ImgBeautySoftFilter(mEditKit.getGLRender());
                softFilter.setGrindRatio(0.5f);
                filterBase = softFilter;
                break;
            case BEAUTY_PRO:
                ImgBeautyProFilter proFilter = new ImgBeautyProFilter(mEditKit.getGLRender()
                        , getApplicationContext());
                proFilter.setGrindRatio(0.5f);
                proFilter.setWhitenRatio(0.5f);
                proFilter.setRuddyRatio(0);
                filterBase = proFilter;
                break;
            case BEAUTY_FLOWER_LIKE:
                ImgBeautyProFilter pro1Filter = new ImgBeautyProFilter(mEditKit.getGLRender()
                        , getApplicationContext(), 3);
                pro1Filter.setGrindRatio(0.5f);
                pro1Filter.setWhitenRatio(0.5f);
                pro1Filter.setRuddyRatio(0.15f);
                mBeautyFilters.put(BEAUTY_FLOWER_LIKE, pro1Filter);
                filterBase = pro1Filter;
                break;
            case BEAUTY_DELICATE:
                ImgBeautyProFilter pro2Filter = new ImgBeautyProFilter(mEditKit.getGLRender()
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

        if (filterBase != null) {
            ImgFilterBase lastFilter = null;
            if (mBeautyFilters.containsKey(mLastImgBeautyTypeIndex)) {
                lastFilter = mBeautyFilters.get(mLastImgBeautyTypeIndex);

            }
            mBeautyFilters.put(mImgBeautyTypeIndex, filterBase);
            if (lastFilter != null && mEditKit.getImgTexFilterMgt().getFilter().contains
                    (lastFilter)) {
                mEditKit.getImgTexFilterMgt().replaceFilter(lastFilter, filterBase, false);
            } else {
                mEditKit.getImgTexFilterMgt().addFilter(filterBase);
            }

        }
        mLastImgBeautyTypeIndex = mImgBeautyTypeIndex;
        //暂停状态时，使得滤镜在预览区域立即生效
        mEditKit.queueLastFrame();
    }

    private void addEffectFilter() {
        if (mLastEffectFilterIndex == mEffectFilterIndex) {
            return;
        }
        if (mEffectFilters == null) {
            mEffectFilters = new LinkedHashMap<>();
        }

        if (mEffectFilterIndex == FILTER_DISABLE) {
            if (mEffectFilters.containsKey(mLastEffectFilterIndex)) {
                ImgFilterBase lastFilter = mEffectFilters.get(mLastEffectFilterIndex);
                if (mEditKit.getImgTexFilterMgt().getFilter().contains(lastFilter)) {
                    mEditKit.getImgTexFilterMgt().replaceFilter(lastFilter, null, false);
                }
            }
            mLastEffectFilterIndex = mEffectFilterIndex;
            //暂停状态时，使得滤镜在预览区域立即生效
            mEditKit.queueLastFrame();
            return;
        }
        if (mEffectFilters.containsKey(mEffectFilterIndex)) {
            ImgFilterBase filter = mEffectFilters.get(mEffectFilterIndex);
            if (mEffectFilters.containsKey(mLastEffectFilterIndex)) {
                ImgFilterBase lastfilter = mEffectFilters.get(mLastEffectFilterIndex);
                if (mEditKit.getImgTexFilterMgt().getFilter().contains(lastfilter)) {
                    mEditKit.getImgTexFilterMgt().replaceFilter(lastfilter, filter, false);
                }
            } else {
                if (!mEditKit.getImgTexFilterMgt().getFilter().contains(filter)) {
                    mEditKit.getImgTexFilterMgt().addFilter(filter);
                }
            }
            mLastEffectFilterIndex = mEffectFilterIndex;
        } else {
            ImgFilterBase filter;
            if (mFilterTypeIndex < 13) {
                filter = new ImgBeautySpecialEffectsFilter(mEditKit.getGLRender(),
                        getApplicationContext(), mEffectFilterIndex);
            } else {
                filter = new ImgBeautyStylizeFilter(mEditKit
                        .getGLRender(), getApplicationContext(), mEffectFilterIndex);
            }
            mEffectFilters.put(mEffectFilterIndex, filter);
            ImgFilterBase lastFilter = null;
            if (mEffectFilters.containsKey(mLastEffectFilterIndex)) {
                lastFilter = mEffectFilters.get(mLastEffectFilterIndex);
            }
            if (lastFilter != null && mEditKit.getImgTexFilterMgt().getFilter().contains
                    (lastFilter)) {
                mEditKit.getImgTexFilterMgt().replaceFilter(lastFilter, filter, false);
            } else {
                mEditKit.getImgTexFilterMgt().addFilter(filter);
            }
            mLastEffectFilterIndex = mEffectFilterIndex;
        }
        //暂停状态时，使得滤镜在预览区域立即生效
        mEditKit.queueLastFrame();
    }

    private void setEffectFilter(int type) {
        mEffectFilterIndex = type;
        addEffectFilter();
    }

    private void initBeautyUI() {
        final int[] BEAUTY_TYPE = {BEAUTY_NATURE, BEAUTY_PRO, BEAUTY_FLOWER_LIKE, BEAUTY_DELICATE};
        mBeautyOriginalView = (ImageView) findViewById(R.id.iv_beauty_origin);
        mBeautyBorder = (ImageView) findViewById(R.id.iv_beauty_border);
        mBeautyOriginalText = (TextView) findViewById(R.id.tv_beauty_origin);
        mBeautyRecyclerView = (RecyclerView) findViewById(R.id.beauty_recyclerView);
        changeOriginalBeautyState(true);
        List<ImageTextAdapter.Data> beautyData = DataFactory.getBeautyTypeDate(this);
        mBeautyAdapter = new ImageTextAdapter(this, beautyData);
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
                addBeautyFiler();
            }
        };
        mBeautyOriginalView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBeautyAdapter.clear();
                changeOriginalBeautyState(true);
                mImgBeautyTypeIndex = BEAUTY_DISABLE;
                addBeautyFiler();
            }
        });
        mBeautyAdapter.setOnImageItemClick(listener);
        mBeautyRecyclerView.setAdapter(mBeautyAdapter);
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
        mFilterAdapter = new ImageTextAdapter(this, filterData);
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
                mFilterAdapter.clear();
                changeOriginalImageState(true);
            }
        });
        mFilterAdapter.setOnImageItemClick(listener);
        mFilterRecyclerView.setAdapter(mFilterAdapter);
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
        //恢复播放的状态
        if (mPauseView.getDrawable().getLevel() == 1) {
            mPauseView.getDrawable().setLevel(2);
        }

        mEditKit.resumeEditPreview();
        mFilterOriginImage.callOnClick();
        startPreviewTimerTask();
    }

    private void initBgmView() {
        List<BgmSelectAdapter.BgmData> list = DataFactory.getBgmData(getApplicationContext());
        mBgmAdapter = new BgmSelectAdapter(this, list);
        mBgmRecyclerView = (RecyclerView) findViewById(R.id.bgm_recycler);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mBgmRecyclerView.setLayoutManager(manager);
        setEnableBgmEdit(false);
        mOriginAudioVolumeSeekBar.setProgress((int) mEditKit.getOriginAudioVolume() * 100);
        mBgmVolumeSeekBar.setProgress((int) mEditKit.getBgmVolume() * 100);
        BgmSelectAdapter.OnItemClickListener listener = new BgmSelectAdapter.OnItemClickListener() {
            @Override
            public void onCancel() {
                mFirstPlay = true;
                setEnableBgmEdit(false);
                mEditKit.stopBgm();
                mAudioSeekLayout.setVisibility(View.GONE);
            }

            @Override
            public boolean onSelected(String path) {
                if (ViewUtils.isForeground(EditActivity.this, EditActivity.class.getName()) &&
                        !isComposeWindowShow()) {
                    if (mEditKit.startBgm(path, true)) {
                        mFirstPlay = true;
                        setEnableBgmEdit(true);
                        mEditKit.setBGMRanges(0, (long) mPreviewLength, false);
                        return true;
                    } else {
                        mBgmAdapter.clear();
                    }
                    return false;
                } else {
                    if (ViewUtils.isForeground(EditActivity.this, EditActivity.class.getName())) {
                        mBgmAdapter.clearTask();
                        mBgmAdapter.clear();
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void onImport() {
                mFirstPlay = true;
                importMusicFile();
            }
        };
        mBgmAdapter.setOnItemClickListener(listener);
        mBgmRecyclerView.setAdapter(mBgmAdapter);
    }

    private boolean isComposeWindowShow() {
        if (mComposeDialog != null && mComposeDialog.isShowing()) {
            return true;
        }
        return false;
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

    private class ComposeDialog extends Dialog {
        private TextView mStateTextView;
        private TextView mProgressText;
        private View mSystemState;
        private TextView mCpuRate;
        private AlertDialog mConfirmDialog;
        private Timer mTimer;

        protected ComposeDialog(Context context, int themeResId) {
            super(context, themeResId);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            View composeView = LayoutInflater.from(EditActivity.this).inflate(R.layout.compose_layout, null);
            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            setContentView(composeView, params);
            setCanceledOnTouchOutside(false);
            mStateTextView = (TextView) composeView.findViewById(R.id.state_text);
            mProgressText = (TextView) composeView.findViewById(R.id.progress_text);
            mSystemState = composeView.findViewById(R.id.system_state);
            mSystemState.setVisibility(View.VISIBLE);
            mCpuRate = (TextView) composeView.findViewById(R.id.cpu_rate);
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
            String mime_type = FileUtils.getMimeType(new File(path));
            final Intent intent = new Intent(EditActivity.this, PublishActivity.class);
            intent.putExtra(COMPOSE_PATH, path);
            intent.putExtra(MIME_TYPE, mime_type);
            intent.putExtra(PREVIEW_LEN, mPreviewLength);
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }
            if (mComposeDialog.isShowing()) {
                mComposeDialog.closeDialog();
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    startActivity(intent);
                }
            });
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    if (!mComposeFinished) {
                        mConfirmDialog = new AlertDialog.Builder(EditActivity.this).setCancelable
                                (true)
                                .setTitle("中止合成?")
                                .setNegativeButton("取消", new OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        mConfirmDialog = null;
                                    }
                                })
                                .setPositiveButton("确定", new OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface arg0, int arg1) {
                                        if (!mComposeFinished) {
                                            mEditKit.stopCompose();
                                            //合成开始后，之前的特效滤镜无效了，不能再次被使用
                                            mEditKit.removeAllTimeEffectFilter();
                                            mEffectsView.clear();
                                            mComposeFinished = false;
                                            closeDialog();
                                            resumeEditPreview();
                                        }
                                        mConfirmDialog = null;
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

        public void closeDialog() {
            mProgressText.setText(0 + "%");
            if (mTimer != null) {
                mTimer.cancel();
                mTimer = null;
            }

            if (mConfirmDialog != null && mConfirmDialog.isShowing()) {
                mConfirmDialog.dismiss();
                mConfirmDialog = null;
            }

            EditActivity.ComposeDialog.this.dismiss();
        }
    }
}
