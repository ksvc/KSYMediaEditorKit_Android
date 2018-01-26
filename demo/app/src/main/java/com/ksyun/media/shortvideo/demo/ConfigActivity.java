package com.ksyun.media.shortvideo.demo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ksyun.media.shortvideo.kit.KSYRemuxKit;
import com.ksyun.media.streamer.encoder.VideoEncodeFormat;
import com.ksyun.media.streamer.framework.AVConst;
import com.ksyun.media.streamer.framework.VideoCodecFormat;
import com.ksyun.media.streamer.kit.StreamerConstants;

import java.util.ArrayList;

/**
 * 配置窗口示例：
 * 录制参数配置
 * 编辑合成参数配置
 */
public class ConfigActivity extends Activity {

    private final static String TAG = "ConfigActivity";
    private final static int REQUEST_CODE = 6384;
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
    private TextView mLandscape;
    private TextView mPortrait;

    private ImageView mImport;  //从外部导入文件
    private ImageView mStartRecord;   //由此进入录制示例窗口

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
        mLandscape = findViewById(R.id.record_config_landscape);
        mLandscape.setOnClickListener(mObserver);
        mPortrait = findViewById(R.id.record_config_portrait);
        mPortrait.setOnClickListener(mObserver);
        mImport = findViewById(R.id.config_import);
        mImport.setOnClickListener(mObserver);
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
        mRecProfileGroup[2].setActivated(true);
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
        if (mLandscape.isActivated()) {
            mRecordConfig.isLandscape = true;
        } else {
            mRecordConfig.isLandscape = false;
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
                case R.id.record_config_landscape:
                    mLandscape.setActivated(true);
                    mPortrait.setActivated(false);
                    break;
                case R.id.record_config_portrait:
                    mLandscape.setActivated(false);
                    mPortrait.setActivated(true);
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
        Intent intent = new Intent(this, MediaImportActivity.class);
        startActivityForResult(intent, REQUEST_CODE);
    }

    /**
     * 从本地导入视频文件结果处理
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        ArrayList<String> pathList = data.getStringArrayListExtra("filePath");
                        if (pathList.size() > 1) {
                            //多选后转码和拼接处理
                            MultiImportActivity.startActivity(getApplicationContext(), pathList);
                        } else {
                            String uri = pathList.get(0);
                            Log.i(TAG, "Uri = " + uri.toString());
                            try {
                                // Get the file path from the URI
                                final String path = uri;

                                if (com.ksyun.media.shortvideo.utils.FileUtils.isSupportedFile
                                        (path)) {
                                    Toast.makeText(ConfigActivity.this,
                                            "File Selected: " + path, Toast.LENGTH_LONG).show();
                                    EditActivity.startActivity(getApplicationContext(), path);
                                } else {
                                    if (path.endsWith("m3u8")) {

                                        final MergeFilesAlertDialog dialog = new MergeFilesAlertDialog
                                                (ConfigActivity.this, R.style.dialog);
                                        dialog.setCancelable(false);
                                        dialog.show();

                                        KSYRemuxKit ksyRemuxKit = new KSYRemuxKit();
                                        ksyRemuxKit.setOnInfoListener(new KSYRemuxKit.OnInfoListener() {
                                            @Override
                                            public void onInfo(KSYRemuxKit ksyRemuxKit, int type, String msg) {
                                                if (type == KSYRemuxKit.INFO_PUBLISHER_STOPPED) {
                                                    ksyRemuxKit.release();
                                                    dialog.dismiss();
                                                    EditActivity.startActivity(ConfigActivity.this, Environment
                                                            .getExternalStorageDirectory() + "/newRemux" +
                                                            ".mp4");
                                                }
                                            }
                                        });
                                        ksyRemuxKit.setOnErrorListener(new KSYRemuxKit.OnErrorListener() {
                                            @Override
                                            public void onError(KSYRemuxKit ksyRemuxKit, int type, long msg) {
                                                ksyRemuxKit.release();
                                                dialog.dismiss();
                                                Toast.makeText(ConfigActivity.this, "Remux m3u8 " +
                                                        "failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                        ksyRemuxKit.start(path, Environment
                                                .getExternalStorageDirectory() + "/newRemux" +
                                                ".mp4");
                                    } else {
                                        Toast.makeText(ConfigActivity.this,
                                                "Do not support this file, please select other File ", Toast
                                                        .LENGTH_LONG).show();
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "File select error:" + e);
                            }
                        }

                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static ShortVideoConfig getRecordConfig() {
        return mRecordConfig;
    }

    private class MergeFilesAlertDialog extends AlertDialog {
        private TextView mProgress;
        private AlertDialog mConfimDialog;

        protected MergeFilesAlertDialog(Context context, int themID) {
            super(context, themID);
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            setContentView(R.layout.merge_record_files_layout);
            mProgress = (TextView) findViewById(R.id.progress_text);
        }

        @Override
        public boolean onKeyDown(int keyCode, KeyEvent event) {
            switch (keyCode) {
                case KeyEvent.KEYCODE_BACK:
                    mConfimDialog = new Builder(ConfigActivity.this).setCancelable
                            (true)
                            .setTitle("中止导入?")
                            .setNegativeButton("取消", new OnClickListener() {

                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    mConfimDialog = null;
                                }
                            })
                            .setPositiveButton("确定", new OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    mConfimDialog = null;
                                }
                            }).show();

                    break;
                default:
                    break;
            }
            return false;
        }
    }
}
