package com.ksyun.media.shortvideo.demo;

import com.ksyun.media.shortvideo.utils.AuthInfoManager;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

/**
 * entry activity for shortvideo
 */

public class ShortVideoActivity extends Activity {
    private static String TAG = "ShortVideoActivity";
    public static String AUTH_SERVER_URI = "http://ksvs-demo.ks-live.com:8321/Auth";//the uri of your appServer
    //view
    private Button mAuthButton;    //SDK鉴权
    private Button mRecordButton;  //进入录制短视频功能
    private Button mImportFileButton; //导入本地视频用于进入短视频编辑

    private ButtonObserver mButtonObserver;
    private Handler mMainHandler;

    //config params
    //auth
    private HttpRequestTask mAuthTask;  //SDK鉴权异步任务
    private HttpRequestTask.HttpResponseListener mAuthResponse;
    private static int MAX_RETRY_COUNT = 3;  //若AKSK请求失败尝试3次
    private int mRetryCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.short_video_activity);

        mAuthButton = (Button) findViewById(R.id.auth);
        mRecordButton = (Button) findViewById(R.id.record);
        mImportFileButton = (Button) findViewById(R.id.import_file);

        mButtonObserver = new ButtonObserver();
        mAuthButton.setOnClickListener(mButtonObserver);
        mRecordButton.setOnClickListener(mButtonObserver);
        mImportFileButton.setOnClickListener(mButtonObserver);

        mMainHandler = new Handler();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }

        if (mAuthTask != null) {
            mAuthTask.cancel(true);
            mAuthTask.release();
            mAuthTask = null;
        }

        AuthInfoManager.getInstance().removeAuthResultListener(mCheckAuthResultListener);
    }

    private class ButtonObserver implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.auth:
                    mRetryCount = 0;
                    onAuthClick();
                    break;
                case R.id.record:
                    onRecordClick();
                    break;
                case R.id.import_file:
                    onImportFileClick();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * SDK鉴权
     */
    private void onAuthClick() {
        mAuthButton.setEnabled(false);

        if (mAuthResponse == null) {
            mAuthResponse = new HttpRequestTask.HttpResponseListener() {
                @Override
                public void onHttpResponse(int responseCode, String response) {
                    //params response
                    boolean authResult = false;
                    if (responseCode == 200) {
                        try {
                            JSONObject temp = new JSONObject(response);
                            JSONObject data = temp.getJSONObject("Data");
                            int result = data.getInt("RetCode");
                            if (result == 0) {
                                String authInfo = data.getString("Authorization");
                                String date = data.getString("x-amz-date");
                                //初始化鉴权信息
                                AuthInfoManager.getInstance().setAuthInfo(getApplicationContext(),
                                        authInfo, date);
                                //添加鉴权结果回调接口(不是必须)
                                AuthInfoManager.getInstance().addAuthResultListener(mCheckAuthResultListener);
                                //开始向KSServer申请鉴权
                                AuthInfoManager.getInstance().checkAuth();
                                authResult = true;
                            } else {
                                Log.e(TAG, "get auth failed from app server RetCode:" + result);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e(TAG, "get auth failed from app server json parse failed");
                        }
                    } else {
                        Log.e(TAG, "get auth failed from app server responseCode:" + responseCode);
                    }

                    final boolean finalAuthResult = authResult;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!finalAuthResult) {
                                Toast.makeText(ShortVideoActivity.this, "get auth failed from app " +
                                        "server", Toast.LENGTH_SHORT)
                                        .show();
                                //鉴权失败，尝试3次
                                if(mRetryCount < MAX_RETRY_COUNT) {
                                    mRetryCount++;
                                    onAuthClick();
                                } else {
                                    mAuthButton.setEnabled(true);
                                }
                            } else {
                                mAuthButton.setEnabled(true);
                            }
                        }
                    });
                }
            };
        }

        if (mAuthTask != null && mAuthTask.getStatus() != AsyncTask.Status.FINISHED) {
            mAuthTask.cancel(true);
            mAuthTask = null;
        }
        //开启异步任务，向AppServer请求鉴权信息
        mAuthTask = new HttpRequestTask(mAuthResponse);
        String url = AUTH_SERVER_URI + "?Pkg=" + getApplicationContext().getPackageName();
        Log.d(TAG, "request auth:" + url);
        mAuthTask.execute(url);
    }

    private AuthInfoManager.CheckAuthResultListener mCheckAuthResultListener = new AuthInfoManager
            .CheckAuthResultListener() {
        @Override
        public void onAuthResult(int result) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    AuthInfoManager.getInstance().removeAuthResultListener(mCheckAuthResultListener);
                    if (AuthInfoManager.getInstance().getAuthState()) {
                        Toast.makeText(ShortVideoActivity.this, "Auth Success", Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        Toast.makeText(ShortVideoActivity.this, "Auth Failed", Toast.LENGTH_SHORT)
                                .show();
                    }
                }
            });
        }
    };

    private void onRecordClick() {
        //params config
        final ShortVideoConfigDialog configDialog = new ShortVideoConfigDialog(this,
                ShortVideoConfigDialog.SHORTVIDEOCONFIG_TYPE_RECORD);
        configDialog.setCancelable(false);
        configDialog.show();
        configDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                ShortVideoConfigDialog.ShortVideoConfig config = configDialog.getShortVideoConfig();
                if (config != null) {
                    //启动短视频录制
                    RecordActivity.startActivity(getApplicationContext(),
                            config.fps, config.videoBitrate,
                            config.audioBitrate, config.resolution, config.encodeType,
                            config.encodeMethod, config.encodeProfile);
                }

            }
        });
    }

    private void onImportFileClick() {
        FileImportActivity.startActivity(getApplicationContext());
    }
}
