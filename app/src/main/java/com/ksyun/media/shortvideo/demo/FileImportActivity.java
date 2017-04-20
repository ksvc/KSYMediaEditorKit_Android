package com.ksyun.media.shortvideo.demo;

import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.shortvideo.demo.file.FileUtils;

import org.w3c.dom.Text;

import android.Manifest;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * import Local File
 */

public class FileImportActivity extends Activity implements
        ActivityCompat.OnRequestPermissionsResultCallback {
    private static String TAG = "FileImportActivity";

    private final static int PERMISSION_REQUEST_STORAGE = 1;
    private static final int REQUEST_CODE = 6384;

    private RelativeLayout mPreviewLayout;
    private RelativeLayout mBarBottomLayout;
    private ImageView mImportFileView;
    private TextView mFileDuration;
    private ImageView mBackView;
    private ImageView mNextView;

    private KSYMediaPlayer mMediaPlayer;
    private SurfaceView mVideoSurfaceView;
    private SurfaceHolder mSurfaceHolder;

    private FileImportActivity.ButtonObserver mObserverButton;

    private Handler mMainHandler;
    private String mFilePath;

    public final static String SRC_URL = "srcurl";

    public static void startActivity(Context context, String url) {
        Intent intent = new Intent(context, FileImportActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(SRC_URL, url);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.import_acitvity);
        mMainHandler = new Handler();

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

        mPreviewLayout = (RelativeLayout) findViewById(R.id.preview_layout);
        mBarBottomLayout = (RelativeLayout) findViewById(R.id.import_bar_bottom);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(screenWidth,
                screenWidth);
        mPreviewLayout.setLayoutParams(params);

        params = new LinearLayout.LayoutParams(screenWidth,
                screenHeight - screenWidth);
        mBarBottomLayout.setLayoutParams(params);

        mObserverButton = new FileImportActivity.ButtonObserver();
        mVideoSurfaceView = (SurfaceView) findViewById(R.id.import_preview);
        mFileDuration = (TextView) findViewById(R.id.file_duration);
        mImportFileView = (ImageView) findViewById(R.id.click_to_import);
        mImportFileView.setOnClickListener(mObserverButton);
        mBackView = (ImageView) findViewById(R.id.click_to_back);
        mBackView.setOnClickListener(mObserverButton);
        mNextView = (ImageView) findViewById(R.id.click_to_next);
        mNextView.setOnClickListener(mObserverButton);

        //play local file
        mSurfaceHolder = mVideoSurfaceView.getHolder();
        mSurfaceHolder.addCallback(mSurfaceCallback);
        getMediaPlayer();

        Bundle bundle = getIntent().getExtras();
        String url = bundle.getString(SRC_URL);
        mFilePath = url;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPermisson();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }

        releasePlay();
    }

    private void onImportClick() {
        resetPlay();
        importFile();
    }

    private void importFile() {
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, "ksy_import_file");
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void onBackoffClick() {
        FileImportActivity.this.finish();
    }

    private void onNextClick() {
        stopPlay();
        EditActivity.startActivity(getApplicationContext(), mFilePath);
    }

    private class ButtonObserver implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.click_to_import:
                    onImportClick();
                    break;
                case R.id.click_to_back:
                    onBackoffClick();
                    break;
                case R.id.click_to_next:
                    onNextClick();
                    break;
                default:
                    break;
            }
        }
    }

    private void checkPermisson() {
        int storagePer = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE);

        if (storagePer != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                Log.e(TAG, "hasPermission: API version < M");

            } else {
                String[] permissions = {Manifest.permission.READ_EXTERNAL_STORAGE};
                ActivityCompat.requestPermissions(this, permissions,
                        PERMISSION_REQUEST_STORAGE);
            }
        } else {
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

                                Toast.makeText(FileImportActivity.this,
                                        "File Selected: " + path, Toast.LENGTH_LONG).show();
                                startPlay(path);
                            } else {
                                Toast.makeText(FileImportActivity.this,
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
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //显示文件时长
                        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
                        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
                        String hms = formatter.format(mMediaPlayer.getDuration());
                        mFileDuration.setText(hms);
                    }
                });
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
            mMediaPlayer = new KSYMediaPlayer.Builder(this.getApplicationContext()).build();
        }
        return mMediaPlayer;
    }

    private void startPlay(String path) {
        mFilePath = path;

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

    private boolean isSupportedMimeType(String mimeType) {
        for (int i = 0; i < SUPPORT_FILE_MIME_TYPE.length; i++) {
            if (mimeType.equals(SUPPORT_FILE_MIME_TYPE[i])) {
                return true;
            }
        }
        return false;
    }

    private String[] SUPPORT_FILE_MIME_TYPE = new String[]{
            "video/mp4",
            "video/ext-mp4",
            "video/3gpp",
            "video/mov"
    };
}
