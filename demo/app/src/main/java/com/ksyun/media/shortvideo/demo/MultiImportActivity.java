package com.ksyun.media.shortvideo.demo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ksyun.media.shortvideo.demo.adapter.TransitionAdapter;
import com.ksyun.media.shortvideo.demo.adapter.TransitionTypeAdapter;
import com.ksyun.media.shortvideo.kit.KSYMultiEditKit;
import com.ksyun.media.shortvideo.utils.ShortVideoConstants;
import com.ksyun.media.streamer.encoder.VideoEncodeFormat;
import com.ksyun.media.streamer.filter.imgtex.ImgTexScaleFilter;
import com.ksyun.media.streamer.framework.AVConst;
import com.ksyun.media.streamer.kit.StreamerConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;


public class MultiImportActivity extends Activity {
    private static String TAG = "MultiImportActivity";

    private static final int[] OUTPUT_PROFILE_ID = {R.id.trans_output_config_low_power,
            R.id.trans_output_config_balance, R.id.trans_output_config_high_performance};
    private static final int[] ENCODE_PROFILE_TYPE = {VideoEncodeFormat.ENCODE_PROFILE_LOW_POWER,
            VideoEncodeFormat.ENCODE_PROFILE_BALANCE, VideoEncodeFormat.ENCODE_PROFILE_HIGH_PERFORMANCE};

    private RelativeLayout mPreviewLayout;
    private GLSurfaceView mTransPreviewView;

    private RelativeLayout mBottomLayout;
    private RecyclerView mTransitionList;
    private TransitionAdapter mTransitionAdapter;
    private RecyclerView mTransitionTypeList;
    private TransitionTypeAdapter mTransitionTypeAdapter;

    private ImageView mNextView;

    private ArrayList<String> mFilePathList;

    private KSYMultiEditKit mKSYTransitionsKit;
    private ButtonObserver mButtonObserver;

    private int mTransitionType = 0;
    private int mTransitionIndex = 1;

    private Dialog mConfigDialog;
    private ComposeFilesDialog mComposeProgressDialog;
    private ShortVideoConfig mConfigParams;
    private Timer mTimer;
    private ConfigObserver mConfigObserver;

    /*******转码参数配置示例******/
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
    private EditText mOutFrameRate;
    private EditText mOutVideoBitrate;
    private EditText mOutAudioBitrate;
    private EditText mOutVideoCRF;
    private TextView mOutputConfirm;
    private EditText mTargetWidth;
    private EditText mTargetHeight;

    public static void startActivity(Context context, ArrayList srcUrl) {
        Intent intent = new Intent(context, MultiImportActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        intent.putStringArrayListExtra("infoList", srcUrl);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_transition);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mButtonObserver = new MultiImportActivity.ButtonObserver();
        mConfigObserver = new MultiImportActivity.ConfigObserver();

        mFilePathList = getIntent().getStringArrayListExtra("infoList");

        mPreviewLayout = findViewById(R.id.transtion_preview_layout);
        mBottomLayout = findViewById(R.id.transtion_bottom_layout);
        mNextView = findViewById(R.id.click_to_next);
        mNextView.setOnClickListener(mButtonObserver);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int previewWidth = screenWidth;
        int previewHeight = screenWidth * 4 / 3;
        RelativeLayout.LayoutParams previewParams = (RelativeLayout.LayoutParams) mPreviewLayout
                .getLayoutParams();
        previewParams.height = previewHeight;
        previewParams.width = previewWidth;
        mPreviewLayout.setLayoutParams(previewParams);

        RelativeLayout.LayoutParams bottomParams = (RelativeLayout.LayoutParams) mBottomLayout
                .getLayoutParams();
        bottomParams.width = screenWidth;
        bottomParams.height = screenHeight - previewHeight;
        mBottomLayout.setLayoutParams(bottomParams);

        mTransPreviewView = (GLSurfaceView) findViewById(R.id.transition_preview);

        mTransitionList = (RecyclerView) findViewById(R.id.transtion_recyclerView);
        mTransitionTypeList = (RecyclerView) findViewById(R.id.transtion_type_recyclerView);

        mTransitionAdapter = new TransitionAdapter(this, mFilePathList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mTransitionList.setLayoutManager(layoutManager);
        mTransitionList.setAdapter(mTransitionAdapter);
        mTransitionAdapter.setTranstionItemListener(new TransitionAdapter.OnTransitionItemListener() {
            @Override
            public void transitionSelected(int index) {
                //当前去除了片头片尾滤镜，因此需要在index基础上加1
                mTransitionIndex = index + 1;

                final int type = mKSYTransitionsKit.getTransitionType(mTransitionIndex);
                mTransitionTypeList.scrollToPosition(type);
                mTransitionTypeList.postDelayed(new Runnable()
                {
                    @Override
                    public void run()
                    {

                        RecyclerView.ViewHolder holder = mTransitionTypeList
                                .findViewHolderForAdapterPosition(type);
                        mTransitionTypeAdapter.selectView(holder, type);
                    }
                },50);
            }
        });

        String[] items = {"无转场", "模糊", "淡入淡出", "闪白", "闪黑", "翻页上", "翻页下", "翻页左", "翻页右"};
        mTransitionTypeAdapter = new TransitionTypeAdapter(this, Arrays.asList(items));
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
        layoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        mTransitionTypeList.setLayoutManager(layoutManager1);
        mTransitionTypeList.setAdapter(mTransitionTypeAdapter);
        mTransitionTypeAdapter.setOnItemClickListener(new TransitionTypeAdapter.OnItemClickListener() {
            @Override
            public void onClick(int curIndex, int preIndex) {
                mTransitionType = curIndex;
                mKSYTransitionsKit.setTransitionType(mTransitionType, mTransitionIndex);
            }
        });

        mKSYTransitionsKit = new KSYMultiEditKit(this);
        mKSYTransitionsKit.setSrcUrls(mFilePathList);
        mKSYTransitionsKit.setOnErrorListener(mOnErrorListener);
        mKSYTransitionsKit.setOnInfoListener(mOnInfoListener);
        mKSYTransitionsKit.setDisplayPreview(mTransPreviewView);
        mKSYTransitionsKit.startPreview();

    }

    public void onResume() {
        super.onResume();
        mKSYTransitionsKit.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mKSYTransitionsKit.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mKSYTransitionsKit.stopPreview();
        mKSYTransitionsKit.release();

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mComposeProgressDialog != null) {
            mComposeProgressDialog.dismiss();
            mComposeProgressDialog = null;
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

    private void onBackoffClick() {
        mKSYTransitionsKit.stopPreview();
        MultiImportActivity.this.finish();
    }

    private void onNextClick() {
        showConfigDialog();
    }

    private class ButtonObserver implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.click_to_next:
                    onNextClick();
                    break;
                default:
                    break;
            }
        }
    }

    private void showConfigDialog() {
        if (mConfigDialog != null) {
            mConfigDialog.show();
            return;
        }
        mConfigDialog = new Dialog(this, R.style.TransCodeDialog);
        View contentView = LayoutInflater.from(this).inflate(R.layout.transcode_popup_layout, null);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        mConfigDialog.setContentView(contentView, params);
        mOutRes480p = contentView.findViewById(R.id.trans_output_config_r480p);
        mOutRes480p.setOnClickListener(mConfigObserver);
        mOutRes540p = contentView.findViewById(R.id.trans_output_config_r540p);
        mOutRes540p.setOnClickListener(mConfigObserver);
        mOutEncodeWithH264 = contentView.findViewById(R.id.trans_output_config_h264);
        mOutEncodeWithH264.setOnClickListener(mConfigObserver);
        mOutEncodeWithH265 = contentView.findViewById(R.id.trans_output_config_h265);
        mOutEncodeWithH265.setOnClickListener(mConfigObserver);
        mOutEncodeByHW = contentView.findViewById(R.id.trans_output_config_hw);
        mOutEncodeByHW.setOnClickListener(mConfigObserver);
        mOutEncodeBySW = contentView.findViewById(R.id.trans_output_config_sw);
        mOutEncodeBySW.setOnClickListener(mConfigObserver);
        mOutDecodeByHW = contentView.findViewById(R.id.trans_output_config_decode_hw);
        mOutDecodeByHW.setOnClickListener(mConfigObserver);
        mOutDecodeBySW = contentView.findViewById(R.id.trans_output_config_decode_sw);
        mOutDecodeBySW.setOnClickListener(mConfigObserver);
        mOutForMP4 = contentView.findViewById(R.id.trans_output_config_mp4);
        mOutForMP4.setOnClickListener(mConfigObserver);
        mOutForGIF = contentView.findViewById(R.id.trans_output_config_gif);
        mOutForGIF.setOnClickListener(mConfigObserver);
        mOutProfileGroup = new TextView[3];
        for (int i = 0; i < mOutProfileGroup.length; i++) {
            mOutProfileGroup[i] = contentView.findViewById(OUTPUT_PROFILE_ID[i]);
            mOutProfileGroup[i].setOnClickListener(mConfigObserver);
        }
        mOutFrameRate = contentView.findViewById(R.id.trans_output_config_frameRate);
        mOutVideoBitrate = contentView.findViewById(R.id.trans_output_config_video_bitrate);
        mOutAudioBitrate = contentView.findViewById(R.id.trans_output_config_audio_bitrate);
        mTargetWidth = contentView.findViewById(R.id.trans_output_config_video_width);
        mTargetHeight = contentView.findViewById(R.id.trans_output_config_video_height);
        mOutVideoCRF = contentView.findViewById(R.id.trans_output_config_video_crf);
        mOutputConfirm = contentView.findViewById(R.id.trans_output_confirm);
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
        mConfigDialog.show();

    }

    private void onOutputConfirmClick() {
        confirmConfigParams();
        if (mConfigDialog.isShowing()) {
            mConfigDialog.dismiss();
        }
        startCompose();
    }

    private void confirmConfigParams() {
        if (mConfigParams == null) {
            mConfigParams = new ShortVideoConfig();
        }
        if (mOutRes480p.isActivated()) {
            mConfigParams.resolution = StreamerConstants.VIDEO_RESOLUTION_480P;
        } else if (mOutRes540p.isActivated()) {
            mConfigParams.resolution = StreamerConstants.VIDEO_RESOLUTION_540P;
        }
        if (mOutEncodeWithH264.isActivated()) {
            mConfigParams.encodeType = AVConst.CODEC_ID_AVC;
        } else if (mOutEncodeWithH265.isActivated()) {
            mConfigParams.encodeType = AVConst.CODEC_ID_HEVC;
        }

        if (mOutEncodeByHW.isActivated()) {
            mConfigParams.encodeMethod = StreamerConstants.ENCODE_METHOD_HARDWARE;
        } else if (mOutEncodeBySW.isActivated()) {
            mConfigParams.encodeMethod = StreamerConstants.ENCODE_METHOD_SOFTWARE;
        }

        if (mOutDecodeByHW.isActivated()) {
            mConfigParams.decodeMethod = StreamerConstants.DECODE_METHOD_HARDWARE;
        } else if (mOutDecodeBySW.isActivated()) {
            mConfigParams.decodeMethod = StreamerConstants.DECODE_METHOD_SOFTWARE;
        }

        if (mOutForGIF.isActivated()) {
            mConfigParams.encodeType = AVConst.CODEC_ID_GIF;
        }
        for (int i = 0; i < mOutProfileGroup.length; i++) {
            if (mOutProfileGroup[i].isActivated()) {
                mConfigParams.encodeProfile = ENCODE_PROFILE_TYPE[i];
                break;
            }
        }
        mConfigParams.fps = Integer.parseInt(mOutFrameRate.getText().toString());
        mConfigParams.videoBitrate = Integer.parseInt(mOutVideoBitrate.getText().toString());
        mConfigParams.audioBitrate = Integer.parseInt(mOutAudioBitrate.getText().toString());
        mConfigParams.videoCRF = Integer.parseInt(mOutVideoCRF.getText().toString());
        mConfigParams.width = Integer.parseInt(mTargetWidth.getText().toString());
        mConfigParams.height = Integer.parseInt(mTargetHeight.getText().toString());
    }

    public class ConfigObserver implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.trans_output_config_audio_bitrate:
                    mOutRes480p.setActivated(true);
                    mOutRes540p.setActivated(false);
                    break;
                case R.id.trans_output_config_r540p:
                    mOutRes480p.setActivated(false);
                    mOutRes540p.setActivated(true);
                    break;
                case R.id.trans_output_config_h264:
                    mOutEncodeWithH264.setActivated(true);
                    mOutEncodeWithH265.setActivated(false);
                    break;
                case R.id.trans_output_config_h265:
                    mOutEncodeWithH264.setActivated(false);
                    mOutEncodeWithH265.setActivated(true);
                    break;
                case R.id.trans_output_config_hw:
                    mOutEncodeByHW.setActivated(true);
                    mOutEncodeBySW.setActivated(false);
                    mOutVideoCRF.setEnabled(false);
                    break;
                case R.id.trans_output_config_sw:
                    mOutEncodeByHW.setActivated(false);
                    mOutEncodeBySW.setActivated(true);
                    mOutVideoCRF.setEnabled(true);
                    break;
                case R.id.trans_output_config_decode_hw:
                    mOutDecodeByHW.setActivated(true);
                    mOutDecodeBySW.setActivated(false);
                    break;
                case R.id.trans_output_config_decode_sw:
                    mOutDecodeBySW.setActivated(true);
                    mOutDecodeByHW.setActivated(false);
                    break;
                case R.id.trans_output_config_mp4:
                    mOutForMP4.setActivated(true);
                    mOutForGIF.setActivated(false);
                    mOutEncodeWithH264.setEnabled(true);
                    mOutEncodeWithH265.setEnabled(true);
                    mOutEncodeByHW.setEnabled(true);
                    break;
                case R.id.trans_output_config_gif:
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

    private void startCompose() {
        mKSYTransitionsKit.setEncodeMethod(mConfigParams.encodeMethod);
        mKSYTransitionsKit.setTargetSize(mConfigParams.width, mConfigParams.height);
        mKSYTransitionsKit.setVideoKBitrate(mConfigParams.videoBitrate);
        mKSYTransitionsKit.setAudioKBitrate(mConfigParams.audioBitrate);
        mKSYTransitionsKit.setAudioChannels(mConfigParams.audioChannel);
        mKSYTransitionsKit.setAudioSampleRate(mConfigParams.audioSampleRate);
        mKSYTransitionsKit.setVideoFps(mConfigParams.fps);
        mKSYTransitionsKit.setScaleMode(ImgTexScaleFilter.SCALING_MODE_BEST_FIT);
        mKSYTransitionsKit.setVideoDecodeMethod(mConfigParams.decodeMethod);

        String outputFile = getFilesImportFolder() + "/outputFile" +
                System.currentTimeMillis() + ".mp4";
        mKSYTransitionsKit.startCompose(outputFile);
    }

    private void showProgressDialog() {
        if (mComposeProgressDialog == null) {
            mComposeProgressDialog = new ComposeFilesDialog
                    (MultiImportActivity.this, R.style.dialog);
            mComposeProgressDialog.setCancelable(false);
        }
        mComposeProgressDialog.show();

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (mKSYTransitionsKit != null && mComposeProgressDialog != null) {
                    KSYMultiEditKit.ComposeProgressInfo progressInfo = mKSYTransitionsKit
                            .getProgressInfo();
                    if (mComposeProgressDialog != null) {
                        mComposeProgressDialog.updateProgress((int) progressInfo.progress,
                                progressInfo.fileIdx);
                    }
                }
            }
        }, 500, 50);
    }

    private String getFilesImportFolder() {
        String fileFolder = Environment.getExternalStorageDirectory().
                getAbsolutePath() + "/ksy_sv_transcode_test";
        File file = new File(fileFolder);
        if (!file.exists()) {
            file.mkdir();
        }
        return fileFolder;
    }

    private class ComposeFilesDialog extends AlertDialog {
        private TextView mProgress;
        private AlertDialog mConfimDialog;

        protected ComposeFilesDialog(Context context, int themID) {
            super(context, themID);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            setContentView(R.layout.merge_record_files_layout);
            mProgress = (TextView) findViewById(R.id.progress_text);
        }

        public void updateProgress(final int progress, final int index) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (mProgress.getVisibility() != View.VISIBLE) {
                        mProgress.setVisibility(View.VISIBLE);
                    }
                    StringBuilder builder = new StringBuilder();
                    builder.append(String.valueOf(index));
                    builder.append(":");
                    builder.append(String.valueOf(progress));
                    builder.append("%");
                    mProgress.setText(String.valueOf(builder.toString()));
                }
            });
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    mConfimDialog = new Builder(MultiImportActivity.this).setCancelable
                            (true)
                            .setTitle("中止合成?")
                            .setNegativeButton("取消", new OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    mConfimDialog = null;
                                }
                            })
                            .setPositiveButton("确定", new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    if (mComposeProgressDialog != null) {
                                        mComposeProgressDialog.dismiss();
                                        mComposeProgressDialog = null;
                                    }
                                    mConfimDialog = null;
                                    mKSYTransitionsKit.stopCompose();
                                }
                            }).show();

                    break;
                default:
                    break;
            }
            return false;
        }
    }

    private KSYMultiEditKit.OnErrorListener mOnErrorListener = new KSYMultiEditKit.OnErrorListener() {
        @Override
        public void onError(int type, long msg) {
            Log.e(TAG, "error:" + type + "_" + msg);
            if (mComposeProgressDialog != null) {
                mComposeProgressDialog.dismiss();
                mComposeProgressDialog = null;
            }
        }
    };

    private KSYMultiEditKit.OnInfoListener mOnInfoListener = new KSYMultiEditKit.OnInfoListener() {
        @Override
        public void onInfo(int type, String msg) {
            switch (type) {
                case ShortVideoConstants.SHORTVIDEO_COMPOSE_START:
                    mKSYTransitionsKit.pausePreview();
                    showProgressDialog();
                    break;
                case ShortVideoConstants.SHORTVIDEO_COMPOSE_FINISHED:
                    if (mComposeProgressDialog != null) {
                        mComposeProgressDialog.dismiss();
                        mComposeProgressDialog = null;
                    }
                    MultiImportActivity.this.finish();
                    if (!TextUtils.isEmpty(msg)) {
                        EditActivity.startActivity(MultiImportActivity.this, msg);
                    }
                    break;
                case ShortVideoConstants.SHORTVIDEO_COMPOSE_ABORTED:
                    if (mComposeProgressDialog != null) {
                        mComposeProgressDialog.dismiss();
                        mComposeProgressDialog = null;
                    }
                    mKSYTransitionsKit.resumePreview();
                    break;
                default:
                    break;
            }
        }
    };
}
