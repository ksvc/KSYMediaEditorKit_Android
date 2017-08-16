package com.ksyun.media.shortvideo.demo;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ksyun.media.shortvideo.demo.util.FileUtils;
import com.ksyun.media.shortvideo.kit.KSYRemuxKit;
import com.ksyun.media.shortvideo.kit.KSYTranscodeKit;
import com.ksyun.media.shortvideo.utils.MP4ParserUtil;
import com.ksyun.media.streamer.encoder.VideoEncodeFormat;
import com.ksyun.media.streamer.framework.AVConst;
import com.ksyun.media.streamer.kit.StreamerConstants;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 配置窗口示例：
 * 录制参数配置
 * 编辑合成参数配置
 */
public class ConfigActivity extends Activity {

    private final static String TAG = "ConfigActivity";
    private final static int REQUEST_CODE = 6384;
    private final static int PERMISSION_REQUEST_STORAGE = 1;
    private final static int PERMISSION_REQUEST_WRITE = 2;
    private final static String TITLE = "ksy_import_file";
    private final static String EXT_TRANSCODE = "/newTranscode";//转码生成后的文件前缀

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

    private AsyncTask mMergeFilesTask;
    private MergeFilesAlertDialog mTranscodeDialog;
    private KSYTranscodeKit mCurrentTranscodeKit;
    private List<String> mTranscodedFiles;
    private Timer mTimer;  //转码进度更新timer

    private ImageView mImport;  //从外部导入文件
    private ImageView mStartRecord;   //由此进入录制示例窗口

    private ConfigObserver mObserver;
    private static ShortVideoConfig mRecordConfig = new ShortVideoConfig();  //录制参数配置

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

    @Override
    protected void onResume() {
        super.onResume();
        checkPermisson();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mTranscodeDialog != null) {
            mTranscodeDialog.dismiss();
            mTranscodeDialog = null;
        }

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (mCurrentTranscodeKit != null) {
            mCurrentTranscodeKit.release();
        }

        if (mMergeFilesTask != null) {
            mMergeFilesTask.cancel(true);
            mMergeFilesTask = null;
        }
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
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        List<Uri> uris = new LinkedList<>();
                        ClipData clipData = data.getClipData();
                        if (clipData != null) {
                            for (int i = 0; i < clipData.getItemCount(); i++) {
                                ClipData.Item item = clipData.getItemAt(i);
                                uris.add(item.getUri());
                                Log.i(TAG, "Uri = " + item.getUri());
                            }
                            //多选后转码和拼接处理
                            startTranscode(uris);

                        } else {
                            Uri uri = data.getData();
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
                                    if (path.endsWith("m3u8")) {

                                        final MergeFilesAlertDialog dialog = new MergeFilesAlertDialog
                                                (ConfigActivity.this, R.style.dialog);
                                        dialog.setCancelable(false);
                                        dialog.show();

                                        KSYRemuxKit ksyRemuxKit = new KSYRemuxKit();
                                        ksyRemuxKit.setOnInfoListener(new KSYRemuxKit.OnInfoListener() {
                                            @Override
                                            public void onInfo(KSYRemuxKit ksyRemuxKit, int type, String msg) {
                                                if (type == KSYTranscodeKit.INFO_COMPLETED) {
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

    private void startTranscode(final List<Uri> srcFiles) {
        if (mTranscodedFiles == null) {
            mTranscodedFiles = new LinkedList<>();
        }
        mTranscodedFiles.clear();

        mTranscodeDialog = new MergeFilesAlertDialog
                (ConfigActivity.this, R.style.dialog);
        mTranscodeDialog.setCancelable(false);
        mTranscodeDialog.show();
        transcode(srcFiles, 0);
    }

    private void transcode(final List<Uri> srcFiles, final int currentTransFileIdx) {
        //取消上一次进度更新
        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }

        if (currentTransFileIdx >= srcFiles.size()) {
            //取消上一次的合成
            if (mMergeFilesTask != null) {
                mMergeFilesTask.cancel(true);
                mMergeFilesTask = null;
            }

            if (mTranscodedFiles != null && mTranscodedFiles.size() > 0) {
                final String outputFile = getTranscodeFileFolder() + "/mergedFile" + System.currentTimeMillis() + ".mp4";
                mMergeFilesTask = new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        boolean succ = MP4ParserUtil.stitchMovies(mTranscodedFiles, outputFile);
                        Log.d(TAG, "outputFile = " + outputFile + "  succ = " + succ);
                        mMergeFilesTask = null;
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object obj) {
                        if (mTranscodeDialog != null) {
                            mTranscodeDialog.dismiss();
                            mTranscodeDialog = null;
                        }
                        EditActivity.startActivity(ConfigActivity.this, outputFile);
                    }

                };
                mMergeFilesTask.execute();
            } else {
                if (mTranscodeDialog != null) {
                    mTranscodeDialog.dismiss();
                    mTranscodeDialog = null;
                }
                Toast.makeText(ConfigActivity.this, "Transcode: do not need be merged", Toast
                        .LENGTH_SHORT).show();
            }
            return;
        }

        String srcUrl = FileUtils.getPath(ConfigActivity.this, srcFiles.get
                (currentTransFileIdx));
        File file = new File(srcUrl);
        String mimeType = FileUtils.getMimeType(file);
        if (!isSupportedMimeType(mimeType)) {
            //当前文件不支持，继续对下一个文件进行转码
            Toast.makeText(ConfigActivity.this, "Transcode: this file do not support(" + srcUrl +
                    ")", Toast.LENGTH_SHORT).show();
            transcode(srcFiles, currentTransFileIdx + 1);
            return;
        }

        final KSYTranscodeKit ksyTranscodeKit = new KSYTranscodeKit();
        mCurrentTranscodeKit = ksyTranscodeKit;
        //设置转码后视频的分辨率
        ksyTranscodeKit.setTargetResolution(480, 480);
        ksyTranscodeKit.setAudioSampleRate(22050);
        ksyTranscodeKit.setAudioChannels(1);
        ksyTranscodeKit.setVideoFps(20);
        ksyTranscodeKit.setVideoKBitrate(800);
        ksyTranscodeKit.setAudioKBitrate(48);
        ksyTranscodeKit.setOnInfoListener(new KSYTranscodeKit.OnInfoListener() {
            @Override
            public void onInfo(KSYTranscodeKit ksyTranscodeKit, int type, String msg) {
                Log.d(TAG, "transcode kit info:" + type);
                if (type == KSYTranscodeKit.INFO_COMPLETED) {
                    //将转码后的文件添加到merge列表中
                    mTranscodedFiles.add(msg);
                    ksyTranscodeKit.release();
                    mCurrentTranscodeKit = null;
                    try {
                        //start trancode next file
                        transcode(srcFiles, currentTransFileIdx + 1);
                    } catch (Exception e) {
                        Log.e(TAG, "File select error:" + e);
                    }
                } else if (type == KSYTranscodeKit.INFO_ABORTED) {
                    mTranscodedFiles.clear();
                    ksyTranscodeKit.release();
                    mCurrentTranscodeKit = null;
                }
            }
        });
        ksyTranscodeKit.setOnErrorListener(new KSYTranscodeKit.OnErrorListener() {
            @Override
            public void onError(KSYTranscodeKit ksyTranscodeKit, int type, long msg) {
                mTranscodeDialog.dismiss();
                mTranscodeDialog = null;
                Toast.makeText(ConfigActivity.this, "Transcode " +
                        "failed:" + type, Toast.LENGTH_SHORT).show();
                //TODO:其中一个文件失败，剩余文件还可以继续
                ksyTranscodeKit.release();
                mCurrentTranscodeKit = null;
            }
        });
        StringBuilder filePath = new StringBuilder(getTranscodeFileFolder());
        filePath.append(EXT_TRANSCODE);
        filePath.append(String.valueOf(currentTransFileIdx));
        filePath.append(".mp4");
        ksyTranscodeKit.start(FileUtils.getPath(ConfigActivity.this, srcFiles.get(currentTransFileIdx)),
                filePath.toString());

        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                final int progress = (int) ksyTranscodeKit.getProgress();
                if (mTranscodeDialog != null) {
                    mTranscodeDialog.updateProgress(progress, (currentTransFileIdx + 1));
                }
            }

        }, 500, 500);

    }

    private String getTranscodeFileFolder() {
        String fileFolder = "/sdcard/ksy_sv_transcode_test";
        File file = new File(fileFolder);
        if (!file.exists()) {
            file.mkdir();
        }
        return fileFolder;
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
                                    if (mTranscodeDialog != null) {
                                        mTranscodeDialog.dismiss();
                                        mTranscodeDialog = null;
                                    }
                                    mConfimDialog = null;
                                    if (mCurrentTranscodeKit != null) {
                                        mCurrentTranscodeKit.stop();
                                    }
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
