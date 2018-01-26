package com.ksyun.media.shortvideo.multicanvasdemo;

import com.ksyun.media.streamer.framework.AVConst;
import com.ksyun.media.streamer.framework.VideoCodecFormat;
import com.ksyun.media.streamer.kit.StreamerConstants;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * 配置窗口示例：
 * 录制参数配置
 * 编辑合成参数配置
 */
public class ConfigActivity extends Activity {

    private final static String TAG = "ConfigActivity";
    private final static int PERMISSION_REQUEST_STORAGE = 1;

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
    private TextView mPortrait;

    private ImageView mStartRecord;   //由此进入多画布录制示例窗口

    private ConfigObserver mObserver;
    private static ShortVideoConfig mRecordConfig = new ShortVideoConfig();  //录制参数配置

    private static final int[] RECORD_PROFILE_ID = {R.id.record_config_low_power,
            R.id.record_config_balance, R.id.record_config_high_performance};
    private static final int[] ENCODE_PROFILE_TYPE = {VideoCodecFormat.ENCODE_PROFILE_LOW_POWER,
            VideoCodecFormat.ENCODE_PROFILE_BALANCE, VideoCodecFormat.ENCODE_PROFILE_HIGH_PERFORMANCE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_config);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        mObserver = new ConfigObserver();

        mRecRes720p = findViewById(R.id.record_config_r720p);
        mRecRes720p.setOnClickListener(mObserver);
        mRecRes1080p = findViewById(R.id.record_config_r1080p);
        mRecRes1080p.setOnClickListener(mObserver);
        mRecEncodeWithH264 = findViewById(R.id.record_config_h264);
        mRecEncodeWithH264.setOnClickListener(mObserver);
        mRecEncodeWithH265 = findViewById(R.id.record_config_h265);
        mRecEncodeWithH265.setOnClickListener(mObserver);
        mRecEncodeByHW = findViewById(R.id.record_config_hw);
        mRecEncodeByHW.setOnClickListener(mObserver);
        mRecEncodeBySW = findViewById(R.id.record_config_sw);
        mRecEncodeBySW.setOnClickListener(mObserver);
        mRecProfileGroup = new TextView[3];
        for (int i = 0; i < mRecProfileGroup.length; i++) {
            mRecProfileGroup[i] = findViewById(RECORD_PROFILE_ID[i]);
            mRecProfileGroup[i].setOnClickListener(mObserver);
        }
        mRecFrameRate = findViewById(R.id.record_config_frameRate);
        mRecVideoBitrate = findViewById(R.id.record_config_video_bitrate);
        mRecAudioBitrate = findViewById(R.id.record_config_audio_bitrate);
        mPortrait = findViewById(R.id.record_config_portrait);
        mPortrait.setOnClickListener(mObserver);
        mStartRecord = findViewById(R.id.config_record);
        mStartRecord.setOnClickListener(mObserver);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPermisson();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_STORAGE: {
                break;
            }
        }
    }

    private void initView() {
        mRecRes720p.setActivated(true);
        mRecEncodeWithH264.setActivated(true);
        mRecEncodeByHW.setActivated(true);
        mRecProfileGroup[1].setActivated(true);  //推荐平衡模式，华为mate8高性能模式硬编存在画质问题
        mPortrait.setActivated(true);
    }

    private void onRecordEncodeProfileClick(int index) {
        mRecProfileGroup[index].setActivated(true);
        for (int i = 0; i < mRecProfileGroup.length; i++) {
            if (i != index) {
                mRecProfileGroup[i].setActivated(false);
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
    }

    /**
     * 读取磁盘权限检查
     */
    private void checkPermisson() {
        int storagePer = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);
        int writePer = ActivityCompat.checkSelfPermission(this, Manifest.permission
                .WRITE_EXTERNAL_STORAGE);

        if (storagePer != PackageManager.PERMISSION_GRANTED || writePer != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Log.e(TAG, "hasPermission: API version < M");

            } else {
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest
                        .permission.WRITE_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions,
                        PERMISSION_REQUEST_STORAGE);
            }
        } else {

        }
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
                case R.id.record_config_portrait:
                    mPortrait.setActivated(true);
                    break;
                case R.id.config_record:
                    confirmConfig();
                    //启动短视频录制
                    MultiCanvasRecordActivity.startActivity(ConfigActivity.this, null);
                    break;
                default:
                    break;
            }

        }
    }


    public static ShortVideoConfig getRecordConfig() {
        return mRecordConfig;
    }
}
