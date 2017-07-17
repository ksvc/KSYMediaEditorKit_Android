package com.ksyun.media.shortvideo.demo;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ksyun.media.streamer.encoder.VideoEncodeFormat;
import com.ksyun.media.streamer.framework.AVConst;
import com.ksyun.media.streamer.kit.StreamerConstants;

/**
 * 配置窗口示例：
 * 录制参数配置
 * 编辑合成参数配置
 */
public class ConfigActivity extends Activity {

    /*******录制参数配置示例******/
    private TextView mRecRes720p;
    private TextView mRecRes1080p;
    private TextView mRecEncodeWithH264;
    private TextView mRecEncodeWithH265;
    private TextView mRecEncodeByHW;
    private TextView mRecEncodeBySW;
    private TextView[] mRecProfileGroup;
    private EditText mRecFrameRate;
    private EditText mRecVideoBitrate;
    private EditText mRecAudioBitrate;

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

    private ImageView mImport;  //从外部导入文件
    private ImageView mStartRecord;   //由此进入录制示例窗口

    private ConfigObserver mObserver;
    private static ShortVideoConfig mRecordConfig;  //录制参数配置
    private static ShortVideoConfig mComposeConfig;  //编辑合成配置参数

    private static final int[] RECORD_PROFILE_ID = {R.id.record_config_low_power,
            R.id.record_config_balance, R.id.record_config_high_performance};
    private static final int[] OUTPUT_PROFILE_ID = {R.id.output_config_low_power,
            R.id.output_config_balance, R.id.output_config_high_performance};
    private static final int[] ENCODE_PROFILE_TYPE = {VideoEncodeFormat.ENCODE_PROFILE_LOW_POWER,
            VideoEncodeFormat.ENCODE_PROFILE_BALANCE, VideoEncodeFormat.ENCODE_PROFILE_HIGH_PERFORMANCE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_config);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mObserver = new ConfigObserver();
        mRecordConfig = new ShortVideoConfig();
        mComposeConfig = new ShortVideoConfig();

        mRecRes720p = (TextView) findViewById(R.id.record_config_r720p);
        mRecRes720p.setOnClickListener(mObserver);
        mRecRes1080p = (TextView) findViewById(R.id.record_config_r1080p);
        mRecRes1080p.setOnClickListener(mObserver);
        mRecEncodeWithH264 = (TextView) findViewById(R.id.record_config_h264);
        mRecEncodeWithH264.setOnClickListener(mObserver);
        mRecEncodeWithH265 = (TextView) findViewById(R.id.record_config_h265);
        mRecEncodeWithH265.setOnClickListener(mObserver);
        mRecEncodeByHW = (TextView) findViewById(R.id.record_config_hw);
        mRecEncodeByHW.setOnClickListener(mObserver);
        mRecEncodeBySW = (TextView) findViewById(R.id.record_config_sw);
        mRecEncodeBySW.setOnClickListener(mObserver);
        mRecProfileGroup = new TextView[3];
        for (int i = 0; i < mRecProfileGroup.length; i++) {
            mRecProfileGroup[i] = (TextView) findViewById(RECORD_PROFILE_ID[i]);
            mRecProfileGroup[i].setOnClickListener(mObserver);
        }
        mRecFrameRate = (EditText) findViewById(R.id.record_config_frameRate);
        mRecVideoBitrate = (EditText) findViewById(R.id.record_config_video_bitrate);
        mRecAudioBitrate = (EditText) findViewById(R.id.record_config_audio_bitrate);

        mOutRes480p = (TextView) findViewById(R.id.output_config_r480p);
        mOutRes480p.setOnClickListener(mObserver);
        mOutRes540p = (TextView) findViewById(R.id.output_config_r540p);
        mOutRes540p.setOnClickListener(mObserver);
        mOutEncodeWithH264 = (TextView) findViewById(R.id.output_config_h264);
        mOutEncodeWithH264.setOnClickListener(mObserver);
        mOutEncodeWithH265 = (TextView) findViewById(R.id.output_config_h265);
        mOutEncodeWithH265.setOnClickListener(mObserver);
        mOutForMP4 = (TextView) findViewById(R.id.output_config_mp4);
        mOutForMP4.setOnClickListener(mObserver);
        mOutForGIF = (TextView) findViewById(R.id.output_config_gif);
        mOutForGIF.setOnClickListener(mObserver);
        mOutProfileGroup = new TextView[3];
        for (int i = 0; i < mOutProfileGroup.length; i++) {
            mOutProfileGroup[i] = (TextView) findViewById(OUTPUT_PROFILE_ID[i]);
            mOutProfileGroup[i].setOnClickListener(mObserver);
        }
        mOutFrameRate = (EditText) findViewById(R.id.output_config_frameRate);
        mOutVideoBitrate = (EditText) findViewById(R.id.output_config_video_bitrate);
        mOutAudioBitrate = (EditText) findViewById(R.id.output_config_audio_bitrate);

        mImport = (ImageView) findViewById(R.id.config_import);
        mImport.setOnClickListener(mObserver);
        mStartRecord = (ImageView) findViewById(R.id.config_record);
        mStartRecord.setOnClickListener(mObserver);
        initView();
    }

    private void initView() {
        mRecRes720p.setActivated(true);
        mRecEncodeWithH264.setActivated(true);
        mRecEncodeByHW.setActivated(true);
        mRecProfileGroup[1].setActivated(true);
        mOutRes480p.setActivated(true);
        mOutEncodeWithH264.setActivated(true);
        mOutForMP4.setActivated(true);
        mOutProfileGroup[1].setActivated(true);
    }

    private void onRecordEncodeProfileClick(int index) {
        mRecProfileGroup[index].setActivated(true);
        for (int i = 0; i < mRecProfileGroup.length; i++) {
            if (i != index) {
                mRecProfileGroup[i].setActivated(false);
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

    private void confirmConfig() {
        if (mRecRes720p.isActivated()) {
            mRecordConfig.resolution = StreamerConstants.VIDEO_RESOLUTION_720P;
        } else if (mRecRes1080p.isActivated()) {
            mRecordConfig.resolution = StreamerConstants.VIDEO_RESOLUTION_1080P;
        }
        if (mRecEncodeWithH264.isActivated()) {
            mRecordConfig.encodeType = AVConst.CODEC_ID_AVC;
        } else if (mRecEncodeWithH265.isActivated()) {
            mRecordConfig.encodeType = AVConst.CODEC_ID_HEVC;
        }
        if (mRecEncodeByHW.isActivated()) {
            mRecordConfig.encodeMethod = StreamerConstants.ENCODE_METHOD_HARDWARE;
        } else if (mRecEncodeBySW.isActivated()) {
            mRecordConfig.encodeMethod = StreamerConstants.ENCODE_METHOD_SOFTWARE;
        }
        for (int i = 0; i < mRecProfileGroup.length; i++) {
            if (mRecProfileGroup[i].isActivated()) {
                mRecordConfig.encodeProfile = ENCODE_PROFILE_TYPE[i];
                break;
            }
        }
        mRecordConfig.fps = Integer.parseInt(mRecFrameRate.getText().toString());
        mRecordConfig.videoBitrate = Integer.parseInt(mRecVideoBitrate.getText().toString());
        mRecordConfig.audioBitrate = Integer.parseInt(mRecAudioBitrate.getText().toString());

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

    public class ConfigObserver implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.record_config_r720p:
                    mRecRes720p.setActivated(true);
                    mRecRes1080p.setActivated(false);
                    break;
                case R.id.record_config_r1080p:
                    mRecRes720p.setActivated(false);
                    mRecRes1080p.setActivated(true);
                    break;
                case R.id.record_config_h264:
                    mRecEncodeWithH264.setActivated(true);
                    mRecEncodeWithH265.setActivated(false);
                    break;
                case R.id.record_config_h265:
                    mRecEncodeWithH264.setActivated(false);
                    mRecEncodeWithH265.setActivated(true);
                    break;
                case R.id.record_config_hw:
                    mRecEncodeByHW.setActivated(true);
                    mRecEncodeBySW.setActivated(false);
                    break;
                case R.id.record_config_sw:
                    mRecEncodeByHW.setActivated(false);
                    mRecEncodeBySW.setActivated(true);
                    break;
                case R.id.record_config_low_power:
                    onRecordEncodeProfileClick(0);
                    break;
                case R.id.record_config_balance:
                    onRecordEncodeProfileClick(1);
                    break;
                case R.id.record_config_high_performance:
                    onRecordEncodeProfileClick(2);
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
                case R.id.config_import:
                    confirmConfig();
                    //启动本地导入页面
                    FileImportActivity.startActivity(getApplicationContext());
                    break;
                case R.id.config_record:
                    confirmConfig();
                    //启动短视频录制
                    RecordActivity.startActivity(getApplicationContext());
                    break;
                default:
                    break;
            }

        }
    }

    public static ShortVideoConfig getRecordConfig() {
        return mRecordConfig;
    }

    public static ShortVideoConfig getComposeConfig() {
        return mComposeConfig;
    }

    public class ShortVideoConfig {
        public int fps;
        public int resolution;
        public int videoBitrate;
        public int audioBitrate;
        public int encodeType;
        public int encodeMethod;
        public int encodeProfile;
    }
}
