package com.ksyun.media.shortvideo.demo;

import com.ksyun.media.streamer.framework.AVConst;
import com.ksyun.media.streamer.kit.StreamerConstants;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;

/**
 * Config recorder or compose params
 */

public class ShortVideoConfigDialog extends Dialog {

    public static int SHORTVIDEOCONFIG_TYPE_RECORD = 1;
    public static int SHORTVIDEOCONFIG_TYPE_COMPOSE = 2;
    private RadioButton mRes360Button;
    private RadioButton mRes480Button;
    private RadioButton mRes540Button;
    private EditText mFrameRateEditText;
    private RadioButton mEncodeWithH265;
    private RadioButton mSWButton;
    private RadioButton mHWButton;
    private EditText mVideoBitRateEditText;
    private EditText mAudioBitRateEditText;

    private ShortVideoConfig mShortVideoConfig;
    private int mConfigType = SHORTVIDEOCONFIG_TYPE_RECORD;

    protected ShortVideoConfigDialog(Context context, int type) {
        super(context);
        mConfigType = type;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.record_params_layout);
        mFrameRateEditText = (EditText) findViewById(R.id.frameRatePicker);
        mFrameRateEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mVideoBitRateEditText = (EditText) findViewById(R.id.videoBitratePicker);
        mVideoBitRateEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mAudioBitRateEditText = (EditText) findViewById(R.id.audioBitratePicker);
        mAudioBitRateEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

        mRes360Button = (RadioButton) findViewById(R.id.r360p);
        mRes480Button = (RadioButton) findViewById(R.id.r480p);
        mRes540Button = (RadioButton) findViewById(R.id.r540p);
        mEncodeWithH265 = (RadioButton) findViewById(R.id.encode_h265);
        mSWButton = (RadioButton) findViewById(R.id.encode_sw);
        mHWButton = (RadioButton) findViewById(R.id.encode_hw);

//        if(mConfigType == SHORTVIDEOCONFIG_TYPE_COMPOSE) {
//            mFrameRateEditText.setVisibility(View.GONE);
//        }
        Button mConfimButton = (Button) findViewById(R.id.preview_params_confim);
        mConfimButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShortVideoConfig = new ShortVideoConfig();
                if (!TextUtils.isEmpty(mFrameRateEditText.getText().toString())) {
                    mShortVideoConfig.previewFps = Integer.parseInt(mFrameRateEditText.getText()
                            .toString());
                }

                if (!TextUtils.isEmpty(mVideoBitRateEditText.getText().toString())) {
                    mShortVideoConfig.videoBitrate = Integer.parseInt(mVideoBitRateEditText
                            .getText()
                            .toString());
                }

                if (!TextUtils.isEmpty(mAudioBitRateEditText.getText().toString())) {
                    mShortVideoConfig.audioBitrate = Integer.parseInt(mAudioBitRateEditText
                            .getText()
                            .toString());
                }

                if (mRes360Button.isChecked()) {
                    mShortVideoConfig.previewResolution = StreamerConstants.VIDEO_RESOLUTION_360P;
                } else if (mRes480Button.isChecked()) {
                    mShortVideoConfig.previewResolution = StreamerConstants.VIDEO_RESOLUTION_480P;
                } else if (mRes540Button.isChecked()) {
                    mShortVideoConfig.previewResolution = StreamerConstants.VIDEO_RESOLUTION_540P;
                } else {
                    mShortVideoConfig.previewResolution = StreamerConstants.VIDEO_RESOLUTION_720P;
                }

                if (mEncodeWithH265.isChecked()) {
                    mShortVideoConfig.encodeType = AVConst.CODEC_ID_HEVC;
                } else {
                    mShortVideoConfig.encodeType = AVConst.CODEC_ID_AVC;
                }

                if (mHWButton.isChecked()) {
                    mShortVideoConfig.encodeMethod = StreamerConstants.ENCODE_METHOD_HARDWARE;
                } else if (mSWButton.isChecked()) {
                    mShortVideoConfig.encodeMethod = StreamerConstants.ENCODE_METHOD_SOFTWARE;
                }

                ShortVideoConfigDialog.this.dismiss();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                ShortVideoConfigDialog.this.dismiss();
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    public ShortVideoConfig getShortVideoConfig() {
        return mShortVideoConfig;
    }

    public class ShortVideoConfig {
        public int previewFps;   //摄像头预览的采集帧率
        public int previewResolution;  //摄像头预览的分辨率
        public int videoBitrate;
        public int audioBitrate;
        public int encodeType;
        public int encodeMethod;
    }
}
