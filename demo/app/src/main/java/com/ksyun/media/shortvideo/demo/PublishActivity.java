package com.ksyun.media.shortvideo.demo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.v7.widget.AppCompatSeekBar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ksyun.ks3.exception.Ks3Error;
import com.ksyun.ks3.services.handler.PutObjectResponseHandler;
import com.ksyun.media.player.IMediaPlayer;
import com.ksyun.media.player.KSYMediaPlayer;
import com.ksyun.media.shortvideo.demo.util.HttpRequestTask;
import com.ksyun.media.shortvideo.demo.util.KS3TokenTask;
import com.ksyun.media.shortvideo.utils.FileUtils;
import com.ksyun.media.shortvideo.utils.KS3ClientWrap;
import com.ksyun.media.shortvideo.utils.ProbeMediaInfoTools;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicInteger;

public class PublishActivity extends Activity {
    //获取ks3播放地址，仅供demo使用，不提供上线服务
    private static String FILE_URL_SERVER = "http://ksvs-demo.ks-live.com:8720/api/upload/ks3/signurl";
    public static final String TAG = "PublishActivity";
    public static final String COMPOSE_PATH = "compose_path";
    public static final String MIME_TYPE = "mime_type";
    public static final String PREVIEW_LEN = "preview_length";

    //标记上传状态
    private static final int UPLOAD_STATE_NONE = 0;
    private static final int UPLOAD_STATE_STARTING = 1;
    private static final int UPLOAD_STATE_STARTED = 2;

    private View mPreviewLayout; //上传后的预览布局
    private View mCoverSeekLayout; //封面选择布局
    private ImageView mCoverBack;
    private TextView mCoverComplete;
    private ImageView mCoverImage;
    private AppCompatSeekBar mCoverSeekBar;
    private ImageView mPreviewBack;
    private ImageView mSaveToDCIM;
    private TextView mSaveToast;
    private WebView mGifView;  //用于预览gif
    private SurfaceView mVideoSurfaceView;  //用于预览视频
    private SurfaceHolder mSurfaceHolder;

    private String mLocalPath;  //合成视频的本地存储地址
    private String mFilePath;  //视频实际预览地址
    private String mFileMineType;
    private volatile Bitmap mBitmap;  //视频封面
    private long mSeekTime;
    private Timer mSeekTimer;
    private long mPreviewLength;  //视频预览时长
    private ButtonObserver mButtonObserver;
    private ProbeMediaInfoTools mImageSeekTools; //根据时间获取视频帧的工具类
    private Handler mMainHandler;

    private KS3TokenTask mTokenTask;
    private KS3ClientWrap mKS3Wrap;
    private String mCurObjectKey;
    private HttpRequestTask mPlayUrlGetTask;  //获取网络播放地址的http请求任务
    private KSYMediaPlayer mMediaPlayer;

    private HandlerThread mSeekThumbnailThread;
    private Handler mSeekThumbnailHandler;
    private Runnable mSeekThumbnailRunable;
    private volatile boolean mStopSeekThumbnail = true;

    /*****合成窗口View*****/
    private TextView mStateTextView;
    private TextView mProgressText;   //显示上传进度
    private PopupWindow mUploadWindow;  //上传状态的显示窗口
    private View mParentView;

    private AtomicInteger mUploadState;  //上传ks3状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_publish);
        mParentView = LayoutInflater.from(this).inflate(R.layout.activity_publish, null);
        //must set
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mLocalPath = getIntent().getExtras().getString(COMPOSE_PATH);
        mFilePath = mLocalPath;
        mMainHandler = new Handler();
        mFileMineType = getIntent().getExtras().getString(MIME_TYPE);
        mPreviewLength = getIntent().getExtras().getLong(PREVIEW_LEN);
        mPreviewLayout = findViewById(R.id.compose_preview_layout);
        mCoverSeekLayout = findViewById(R.id.cover_layout);
        mButtonObserver = new ButtonObserver();
        mPreviewBack = (ImageView) findViewById(R.id.preview_back);
        mPreviewBack.setOnClickListener(mButtonObserver);
        mSaveToDCIM = (ImageView) findViewById(R.id.save_to_album);
        mSaveToDCIM.setOnClickListener(mButtonObserver);
        mSaveToast = (TextView) findViewById(R.id.save_toast);
        getMediaPlayer();
        mVideoSurfaceView = (SurfaceView) findViewById(R.id.compose_preview);
        mGifView = (WebView) findViewById(R.id.gif_view);
        WebSettings webSettings = mGifView.getSettings();
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        mSurfaceHolder = mVideoSurfaceView.getHolder();
        mSurfaceHolder.addCallback(mSurfaceCallback);
        mUploadState = new AtomicInteger(UPLOAD_STATE_NONE);
        if (!mFileMineType.equals(FileUtils.MIME_TYPE_GIF)) {
            startCoverSeek();
        } else {
            mCoverSeekLayout.setVisibility(View.GONE);
            startUpload();  //若为gif则不选择封面直接上传
        }
    }

    private void initSeekThread() {
        if (mSeekThumbnailThread == null) {
            mSeekThumbnailThread = new HandlerThread("screen_setup_thread", Thread.NORM_PRIORITY);
            mSeekThumbnailThread.start();
            mSeekThumbnailHandler = new Handler(mSeekThumbnailThread.getLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    return;
                }
            };

            mSeekThumbnailRunable = new Runnable() {
                @Override
                public void run() {
                    if (mSeekTime < 0) {
                        mSeekTime = 0;
                    }

                    if (mSeekTime > mPreviewLength) {
                        mSeekTime = mPreviewLength;
                    }

                    mBitmap = mImageSeekTools.getVideoThumbnailAtTime(mLocalPath, mSeekTime,
                            0, 0, true);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mBitmap != null) {
                                mCoverImage.setImageBitmap(mBitmap);
                            }
                        }
                    });
                    if (!mStopSeekThumbnail) {
                        mSeekThumbnailHandler.postDelayed(mSeekThumbnailRunable, 100);
                    }
                }
            };
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                onBackClick();  //拦截返回事件直接退回到录制参数配置界面
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        releasePlay();

        if (mPlayUrlGetTask != null) {
            mPlayUrlGetTask.cancel(true);
            mPlayUrlGetTask.release();
            mPlayUrlGetTask = null;
        }

        mStopSeekThumbnail = true;
        if (mSeekThumbnailHandler != null) {
            mSeekThumbnailHandler.removeCallbacksAndMessages(null);
            mSeekThumbnailHandler = null;
        }

        if (mSeekThumbnailThread != null) {
            mSeekThumbnailThread.getLooper().quit();
            try {
                mSeekThumbnailThread.join();
            } catch (InterruptedException e) {
                Log.d(TAG, "ScreenSetUpThread Interrupted!");
            } finally {
                mSeekThumbnailThread = null;
            }
        }

        super.onDestroy();
    }

    private void startCoverSeek() {
        mCoverBack = (ImageView) findViewById(R.id.cover_back);
        mCoverBack.setOnClickListener(mButtonObserver);
        mCoverComplete = (TextView) findViewById(R.id.cover_complete);
        mCoverComplete.setOnClickListener(mButtonObserver);
        mCoverImage = (ImageView) findViewById(R.id.cover_image);
        mCoverSeekBar = (AppCompatSeekBar) findViewById(R.id.cover_seekBar);
        mImageSeekTools = new ProbeMediaInfoTools();
        mBitmap = mImageSeekTools.getVideoThumbnailAtTime(mLocalPath, mSeekTime, 0, 0, true);

        mImageSeekTools.probeMediaInfo(mLocalPath, new ProbeMediaInfoTools.ProbeMediaInfoListener() {
            @Override
            public void probeMediaInfoFinished(ProbeMediaInfoTools.MediaInfo info) {
                //使用合成视频时长更新视频的时长
                mPreviewLength = info.duration;
            }
        });

        mCoverImage.setImageBitmap(mBitmap);
        mCoverSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float rate = progress / 100.f;
                mSeekTime = (long) (mPreviewLength * rate);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                initSeekThread();
                mStopSeekThumbnail = false;
                mSeekThumbnailHandler.postDelayed(mSeekThumbnailRunable, 100);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mStopSeekThumbnail = true;
                if (mSeekThumbnailHandler != null) {
                    mSeekThumbnailHandler.removeCallbacksAndMessages(null);
                }
                mSeekThumbnailHandler.post(mSeekThumbnailRunable);
            }
        });
    }

    private class ButtonObserver implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.cover_complete:
                    onPublishClick();
                    break;
                case R.id.cover_back:
                case R.id.preview_back:
                    onBackClick();
                    break;
                case R.id.save_to_album:
                    saveFileToDCIM();
                    break;
                default:
                    break;
            }
        }
    }

    private void onBackClick() {
        if (mUploadState.get() >= UPLOAD_STATE_STARTING && mUploadState.get() <=
                UPLOAD_STATE_STARTED) {
            //取消上传，直接预览播放本地视频
            mKS3Wrap.cancel();
            onUploadFinished(false);
            return;
        }

        Intent intent = new Intent(PublishActivity.this, ConfigActivity.class);
        startActivity(intent);
    }

    private void onPublishClick() {
        startUpload();
    }

    private void startUpload() {
        mKS3Wrap = KS3ClientWrap.getInstance(getApplicationContext());
        if (!TextUtils.isEmpty(mLocalPath)) {
            mUploadState.set(UPLOAD_STATE_STARTING);
            String mineType = FileUtils.getMimeType(new File(mLocalPath));
            StringBuilder objectKey = new StringBuilder(getPackageName() +
                    "/" + System.currentTimeMillis());
            if (mineType.equals(FileUtils.MIME_TYPE_MP4)) {
                objectKey.append(".mp4");
            } else if (mineType.equals(FileUtils.MIME_TYPE_GIF)) {
                objectKey.append(".gif");
            }
            mCurObjectKey = objectKey.toString();
            //上传必要信息：bucket,objectkey，及PutObjectResponseHandler上传过程回调
            KS3ClientWrap.KS3UploadInfo bucketInfo = new KS3ClientWrap.KS3UploadInfo
                    ("ksvsdemo", mCurObjectKey, mPutObjectResponseHandler);
            //调用SDK内部接口触发上传
            mKS3Wrap.putObject(bucketInfo, mLocalPath,
                    mPutObjectResponseHandler, new KS3ClientWrap.OnGetAuthInfoListener() {
                        @Override
                        public KS3ClientWrap.KS3AuthInfo onGetAuthInfo(String s, String s1, String s2, String s3, String s4, String s5) {

                            if (mTokenTask == null) {
                                mTokenTask = new KS3TokenTask(getApplicationContext());
                            }

                            KS3ClientWrap.KS3AuthInfo authInfo = mTokenTask.requsetTokenToAppServer(s, s1,
                                    s2, s3, s4, s5);

                            return authInfo;
                        }
                    });

        }
    }

    private PutObjectResponseHandler mPutObjectResponseHandler = new PutObjectResponseHandler() {
        @Override
        public void onTaskFailure(int statesCode, Ks3Error error, Header[] responceHeaders, String response, Throwable paramThrowable) {
            Log.e(TAG, "onTaskFailure:" + statesCode);
            onUploadFinished(false);
        }

        @Override
        public void onTaskSuccess(int statesCode, Header[] responceHeaders) {
            Log.d(TAG, "onTaskSuccess:" + statesCode);
            onUploadFinished(true);
        }

        @Override
        public void onTaskStart() {
            Log.d(TAG, "onTaskStart");
            onUploadStart();
        }

        @Override
        public void onTaskFinish() {
            Log.d(TAG, "onTaskFinish");
            mUploadState.set(UPLOAD_STATE_NONE);
        }

        @Override
        public void onTaskCancel() {
            Log.d(TAG, "onTaskCancel");
            mUploadState.set(UPLOAD_STATE_NONE);
        }

        @Override
        public void onTaskProgress(double progress) {
            onUploadProgress(progress);
        }
    };

    private void onUploadStart() {
        mUploadState.set(UPLOAD_STATE_STARTED);
        resetPlay();
        showUploadProgressDialog();
    }

    private void onUploadProgress(final double progress) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mProgressText.getVisibility() == View.VISIBLE) {
                    mProgressText.setText(String.valueOf((int) progress) + "%");
                }
            }
        });
    }

    private void onUploadFinished(final boolean success) {
        mUploadState.set(UPLOAD_STATE_NONE);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCoverSeekLayout.setVisibility(View.GONE);
                if (success) {
                    mProgressText.setVisibility(View.INVISIBLE);
                    mStateTextView.setVisibility(View.VISIBLE);
                    mStateTextView.setText(R.string.upload_file_success);

                    if (mPlayUrlGetTask != null) {
                        mPlayUrlGetTask.cancel(true);
                        mPlayUrlGetTask.release();
                        mPlayUrlGetTask = null;
                    }

                    mPlayUrlGetTask = new HttpRequestTask(new HttpRequestTask.HttpResponseListener() {
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
                                            Log.d(TAG, "play url:" + mFilePath);
                                            startPreview();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                        //播放合成后的视频
                                        startPreview();
                                    }

                                }
                            }
                        }
                    });
                    mStateTextView.setText(R.string.get_file_url);
                    mPlayUrlGetTask.execute(FILE_URL_SERVER + "?objkey=" + mCurObjectKey);
                } else {
                    if (mStateTextView != null) {
                        mStateTextView.setVisibility(View.VISIBLE);
                        mStateTextView.setText(R.string.upload_file_fail);
                    }
                    startPreview();
                }
            }
        });
    }

    private void showUploadProgressDialog() {
        if (mUploadWindow == null) {
            View contentView = LayoutInflater.from(this).inflate(R.layout.compose_layout, null);
            mStateTextView = (TextView) contentView.findViewById(R.id.state_text);
            mProgressText = (TextView) contentView.findViewById(R.id.progress_text);
            mStateTextView.setText(R.string.upload_file);
            mUploadWindow = new PopupWindow(contentView, WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.WRAP_CONTENT);
        }

        if (!mUploadWindow.isShowing()) {
            mUploadWindow.showAtLocation(mParentView, Gravity.CENTER, 0, 0);
        }
    }

    public KSYMediaPlayer getMediaPlayer() {
        if (mMediaPlayer == null) {
            mMediaPlayer = new KSYMediaPlayer.Builder(getApplicationContext()).build();
        }
        return mMediaPlayer;
    }

    /**
     * 保存视频到相册并发广播进行通知
     */
    private void saveFileToDCIM() {
        String srcPath = mLocalPath;
        String desDir = Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)
                ? Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath()
                + "/Camera/" : Environment.getExternalStorageDirectory().getAbsolutePath() + "/KSYShortVideo";
        String name = srcPath.substring(srcPath.lastIndexOf('/'));
        String desPath = desDir + name;
        File desFile = new File(desPath);
        try {
            File srcFile = new File(srcPath);
            if (srcFile.exists() && !desFile.exists()) {
                InputStream is = new FileInputStream(srcPath);
                FileOutputStream fos = new FileOutputStream(desFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, length);
                }
                mSaveToast.setText("文件保存相册成功");
                mSaveToast.setVisibility(View.VISIBLE);
                mMainHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mSaveToast.setVisibility(View.INVISIBLE);
                    }
                }, 1000);
                is.close();
                fos.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 发送系统广播通知有图片更新
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(desFile);
        intent.setData(uri);
        sendBroadcast(intent);
    }

    private void startPreview() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "start compose file Preview:");
                if (mUploadWindow != null && mUploadWindow.isShowing()) {
                    mUploadWindow.dismiss();
                }

                if (mFileMineType.equals(FileUtils.MIME_TYPE_GIF)) {
                    mPreviewLayout.setVisibility(View.VISIBLE);
                    mGifView.setVisibility(View.VISIBLE);
                    mGifView.loadUrl("file://" + mLocalPath);
//                       演示播放gif的网络资源，可根据需求使用
//                        if (mFilePath.startsWith("http")) {
//                            mGifView.loadUrl(mFilePath);
//                        } else {
//                            mGifView.loadUrl("file://" + mFilePath);
//                        }
                } else {
                    mPreviewLayout.setVisibility(View.VISIBLE);
                    mVideoSurfaceView.setVisibility(View.VISIBLE);
                    startPlay(mFilePath);
                }
            }
        });
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

    private IMediaPlayer.OnErrorListener mOnMediaErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            Log.d(TAG, "mediaplayer error:" + i);
            return false;
        }
    };

    private IMediaPlayer.OnInfoListener mOnMediaInfoListener = new IMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(IMediaPlayer iMediaPlayer, int what, int extra) {
            switch (what) {
                case KSYMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mUploadWindow.isShowing()) {
                                mUploadWindow.dismiss();
                            }
                        }
                    });
                    break;
                default:
                    break;
            }
            return false;
        }
    };


    private void startPlay(String path) {
        mMediaPlayer.setLooping(true);
        mMediaPlayer.shouldAutoPlay(false);
        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mMediaPlayer.setOnInfoListener(mOnMediaInfoListener);
        mMediaPlayer.setOnErrorListener(mOnMediaErrorListener);
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
