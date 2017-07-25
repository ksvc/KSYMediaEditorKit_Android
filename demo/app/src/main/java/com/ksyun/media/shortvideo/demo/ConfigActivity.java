package com.ksyun.media.shortvideo.demo;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ksyun.media.shortvideo.demo.util.FileUtils;
import com.ksyun.media.streamer.encoder.VideoEncodeFormat;
import com.ksyun.media.streamer.framework.AVConst;
import com.ksyun.media.streamer.kit.StreamerConstants;

/**
 * 配置窗口示例：
 * 录制参数配置
 * 编辑合成参数配置
 */
public class ConfigActivity extends Activity {

    private static final String TAG = "ConfigActivity";
    private static final int REQUEST_CODE = 6384;
    private static final String TITLE = "ksy_import_file";

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

    private ImageView mImport;  //从外部导入文件
    private ImageView mStartRecord;   //由此进入录制示例窗口

    private ConfigObserver mObserver;
    private static ShortVideoConfig mRecordConfig;  //录制参数配置

    private static final int[] RECORD_PROFILE_ID = {R.id.record_config_low_power,
            R.id.record_config_balance, R.id.record_config_high_performance};
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
        mRecProfileGroup[0].setActivated(true);
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
                case R.id.config_import:
                    confirmConfig();
                    //启动本地导入页面
                    onImportClick();
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

    private void onImportClick() {
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, TITLE);
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 从本地导入视频文件结果处理
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
                            String mimeType = FileUtils.getMimeType(this, uri);

                            if (!TextUtils.isEmpty(mimeType) && isSupportedMimeType(mimeType)) {

                                Toast.makeText(ConfigActivity.this,
                                        "File Selected: " + path, Toast.LENGTH_LONG).show();
                                EditActivity.startActivity(getApplicationContext(), path);
                            } else {
                                Toast.makeText(ConfigActivity.this,
                                        "Do not support this file, please select other File ", Toast
                                                .LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "File select error:" + e);
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 判断是否是所支持的MIME类型
     */
    private boolean isSupportedMimeType(String mimeType) {
        for (int i = 0; i < SUPPORT_FILE_MIME_TYPE.length; i++) {
            if (mimeType.equals(SUPPORT_FILE_MIME_TYPE[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * 支持的MIME类型数组
     */
    private String[] SUPPORT_FILE_MIME_TYPE = new String[]{
            "video/mp4",
            "video/ext-mp4",
            "video/3gpp",
            "video/mov"
    };

    public static ShortVideoConfig getRecordConfig() {
        return mRecordConfig;
    }
}
