package com.ksyun.media.shortvideo.demo;

import com.ksyun.media.shortvideo.kit.KSYEditKit;
import com.ksyun.media.shortvideo.utils.AuthInfoManager;

import org.apache.http.util.EncodingUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;

/**
 * 引导窗口页
 */

public class ShortVideoActivity extends Activity {
    private static String TAG = "ShortVideoActivity";
    public static final String COLOPHON_URL = "https://ks3-cn-beijing.ksyun.com/ksplayer/svod_change_log/dist/Android.html";
    //view
    private TextView mStart;
    private TextView mVersion;
    private View mColophon;
    private WebView mWebView;
    private View mDefaultView;
    private Handler mMainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.short_video_activity);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mStart = (TextView) findViewById(R.id.tv_start_short_video);
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ShortVideoActivity.this, ConfigActivity.class);
                startActivity(intent);
            }
        });
        mVersion = (TextView) findViewById(R.id.sdk_version);
        mVersion.setText("SDK V" + KSYEditKit.getVersion() + " 版本");
        mColophon = findViewById(R.id.colophon);
        mDefaultView = findViewById(R.id.default_launch);
        mWebView = (WebView) findViewById(R.id.webView);
        mColophon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showWebView();
            }
        });
        mMainHandler = new Handler();
        checkAuth();
    }

    private void showWebView() {
        mDefaultView.setVisibility(View.GONE);
        mWebView.setVisibility(View.VISIBLE);
        mWebView.loadUrl(COLOPHON_URL);
        mWebView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mWebView.getVisibility() == View.VISIBLE) {
                    mWebView.setVisibility(View.GONE);
                    mDefaultView.setVisibility(View.VISIBLE);
                } else {
                    ShortVideoActivity.this.finish();
                }
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mMainHandler != null) {
            mMainHandler.removeCallbacksAndMessages(null);
            mMainHandler = null;
        }
    }

    /**
     * SDK鉴权
     */
    private void checkAuth() {
        String token = null;
        try {
            InputStream in = getResources().getAssets().open("AuthForTest.pkg");

            int length = in.available();

            byte[] buffer = new byte[length];

            in.read(buffer);

            token = EncodingUtils.getString(buffer, "UTF-8");

        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!TextUtils.isEmpty(token)) {
            AuthInfoManager.getInstance().setAuthInfo(token);
            AuthInfoManager.getInstance().checkAuth();
            if (AuthInfoManager.getInstance().getAuthState()) {
                Toast.makeText(ShortVideoActivity.this, "Auth Success", Toast.LENGTH_SHORT)
                        .show();
            } else {
                Toast.makeText(ShortVideoActivity.this, "Auth Failed", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }
}
