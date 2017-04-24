package com.ksyun.media.shortvideo.demo;

import com.ksyun.ks3.exception.Ks3Error;
import com.ksyun.ks3.services.handler.PutObjectResponseHandler;
import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.shortvideo.demo.filter.DemoFilter;
import com.ksyun.media.shortvideo.demo.filter.DemoFilter2;
import com.ksyun.media.shortvideo.demo.filter.DemoFilter3;
import com.ksyun.media.shortvideo.demo.filter.DemoFilter4;
import com.ksyun.media.shortvideo.utils.KS3ClientWrap;
import com.ksyun.media.shortvideo.kit.KSYEditKit;
import com.ksyun.media.shortvideo.utils.ShortVideoConstants;
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
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.AppCompatSeekBar;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Edit Video
 */

public class EditActivity extends Activity implements
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static String TAG = "EditActivity";
    private static String FILEURL_SERVER = "http://ksvs-demo.ks-live.com:8720/api/upload/ks3/signurl";

    private GLSurfaceView mEditPreviewView;
    private RelativeLayout mPreviewLayout;
    private RelativeLayout mBarBottomLayout;
    private ImageView mBackView;
    private ImageView mNextView;
    private ImageView mMuteView;
    private ImageView mPauseView;
    private TextView mFilterView;
    private TextView mWaterMarkView;
    private CheckBox mWaterMartLogoView;
    private View mFilterLayout;
    private View mWatermarkLayout;
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

    private ButtonObserver mButtonObserver;
    private CheckBoxObserver mCheckBoxObserver;

    public final static String SRC_URL = "srcurl";

    private String mLogoPath = "assets://KSYLogo/logo.png";//"file:///sdcard/test.png";

    private KSYEditKit mEditKit;
    private boolean mComposeFinished = false;
    private KS3TokenTask mTokenTask;
    private String mCurObjectKey;

    private boolean mPaused = false;
    private boolean mNeedResume = false;


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
        //TODO LANDSCAPE
        WindowManager windowManager = (WindowManager) getApplication().
                getSystemService(getApplication().WINDOW_SERVICE);

        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();

        mButtonObserver = new EditActivity.ButtonObserver();
        mCheckBoxObserver = new EditActivity.CheckBoxObserver();
        mEditPreviewView = (GLSurfaceView) findViewById(R.id.edit_preview);
        mPreviewLayout = (RelativeLayout) findViewById(R.id.preview_layout);
        mBarBottomLayout = (RelativeLayout) findViewById(R.id.edit_bar_bottom);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(screenWidth,
                screenWidth);
        mPreviewLayout.setLayoutParams(params);

        params = new LinearLayout.LayoutParams(screenWidth,
                screenHeight - screenWidth);
        mBarBottomLayout.setLayoutParams(params);

        mMuteView = (ImageView) findViewById(R.id.click_to_mute);
        mMuteView.setOnClickListener(mButtonObserver);
        mMuteView.getDrawable().setLevel(2);
        mPauseView = (ImageView) findViewById(R.id.click_to_pause);
        mPauseView.setOnClickListener(mButtonObserver);
        mPauseView.getDrawable().setLevel(2);

        mFilterLayout = findViewById(R.id.beauty_choose);
        mFilterView = (TextView) findViewById(R.id.click_to_filter);
        mFilterView.setOnClickListener(mButtonObserver);
        mFilterView.setActivated(true);
        mFilterLayout.setVisibility(View.VISIBLE);

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


        mBackView = (ImageView) findViewById(R.id.click_to_back);
        mBackView.setOnClickListener(mButtonObserver);
        mNextView = (ImageView) findViewById(R.id.click_to_next);
        mNextView.setOnClickListener(mButtonObserver);

        mEditKit = new KSYEditKit(this);
        mEditKit.setDisplayPreview(mEditPreviewView);
        mEditKit.setOnErrorListener(mOnErrorListener);
        mEditKit.setOnInfoListener(mOnInfoListener);

        Bundle bundle = getIntent().getExtras();
        String url = bundle.getString(SRC_URL);
        if (!TextUtils.isEmpty(url)) {
            mEditKit.setEditPreviewUrl(url);
        }
        initBeautyUI();

        startEditPreview();
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPutObjectResponseHandler = null;

        if (mComposeAlertDialog != null) {
            mComposeAlertDialog.closeDialog();
            mComposeAlertDialog = null;
        }
        mEditKit.stopEditPreview();
        mEditKit.release();
    }

    private void startEditPreview() {
        if(mNeedResume) {
            mEditKit.onResume();
            mNeedResume = false;
        }
        //设置预览的音量
        mEditKit.setVolume(0.4f);
        //设置是否循环预览
        mEditKit.setLooping(true);
        //开启预览
        mEditKit.startEditPreview();
    }

    private void stopEditPreview() {
        mNeedResume = true;
        mEditKit.stopEditPreview();
        mEditKit.onPause();
    }

    private void onFilterClick() {
        if (mFilterLayout.getVisibility() == View.INVISIBLE) {
            mFilterLayout.setVisibility(View.VISIBLE);
            mFilterView.setActivated(true);
            mWaterMarkView.setActivated(false);
            mWatermarkLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void onWatermarkClick() {
        if (mWatermarkLayout.getVisibility() == View.INVISIBLE) {
            mWatermarkLayout.setVisibility(View.VISIBLE);
            mWaterMarkView.setActivated(true);
            mFilterView.setActivated(false);
            mFilterLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void onWaterMarkLogoClick(boolean isCheck) {
        if (isCheck) {
            mEditKit.showWaterMarkLogo(mLogoPath, 0.08f, 0.04f, 0.20f, 0, 0.8f);
        } else {
            mEditKit.hideWaterMarkLogo();
        }
    }

    private void showWaterMark() {
        if (mWaterMartLogoView.isChecked()) {
            mEditKit.showWaterMarkLogo(mLogoPath, 0.08f, 0.04f, 0.20f, 0, 0.8f);
        }
    }

    private void hideWaterMark() {
        mEditKit.hideWaterMarkLogo();
    }

    private void onMuteClick() {
        if (mMuteView.getDrawable().getLevel() == 2) {
            mEditKit.setMuteAudio(true);
            mMuteView.getDrawable().setLevel(1);
        } else {
            mEditKit.setMuteAudio(false);
            mMuteView.getDrawable().setLevel(2);
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
                    mEditKit.setVideoFps(config.previewFps);
                    mEditKit.setVideoCodecId(config.encodeType);
                    mEditKit.setAudioKBitrate(config.audioBitrate);
                    mEditKit.setVideoKBitrate(config.videoBitrate);
                    //关闭上一次合成窗口
                    if (mComposeAlertDialog != null) {
                        mComposeAlertDialog.closeDialog();
                    }

                    mComposeAlertDialog = new ComposeAlertDialog(EditActivity.this, R.style.dialog);
                    //设置合成路径
                    String composeUrl = "/sdcard/" + System.currentTimeMillis() + ".mp4";
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
                        EditActivity.this.startEditPreview();
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
                    break;


            }
        }
    };

    private KSYEditKit.OnInfoListener mOnInfoListener = new KSYEditKit.OnInfoListener() {
        @Override
        public Object onInfo(int type, String... msgs) {
            if (type == ShortVideoConstants.SHORTVIDEO_COMPOSE_START) {
                Log.d(TAG, "compose start");
                stopEditPreview();
                mBeautySpinner.setSelection(0);
                if (mComposeAlertDialog != null) {
                    mComposeAlertDialog.setCancelable(false);
                    mComposeAlertDialog.show();
                    mComposeAlertDialog.composeStarted();
                }
                return null;
            } else if (type == ShortVideoConstants.SHORTVIDEO_COMPOSE_FINISHED) {
                Log.d(TAG, "compose finished");
                if (mComposeAlertDialog != null) {
                    mComposeAlertDialog.composeFinished(msgs[0]);
                    mComposeFinished = true;
                }
                //上传必要信息：bucket,objectkey，及PutObjectResponseHandler上传过程回调
                mCurObjectKey = getPackageName() + "/" + System.currentTimeMillis() + ".mp4";
                KS3ClientWrap.KS3UploadInfo bucketInfo = new KS3ClientWrap.KS3UploadInfo
                        ("ksvsdemo", mCurObjectKey, mPutObjectResponseHandler);
                return bucketInfo;
            } else if (type == ShortVideoConstants.SHORTVIDEO_GET_KS3AUTH) {
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
                case R.id.click_to_back:
                    onBackoffClick();
                    break;
                case R.id.click_to_next:
                    onNextClick();
                    break;
                case R.id.click_to_mute:
                    onMuteClick();
                    break;
                case R.id.click_to_pause:
                    onPauseClick();
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
                default:
                    break;
            }
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
        mBeautySpinner.setSelection(4);
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
                        mEditKit.abort();
                    }

                    closeDialog();
                    EditActivity.this.startEditPreview();
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
                stopPlay();
                startPlay(mFilePath);
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
