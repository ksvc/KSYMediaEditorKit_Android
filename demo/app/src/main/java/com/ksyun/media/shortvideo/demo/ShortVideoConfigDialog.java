package com.ksyun.media.shortvideo.demo;

import com.ksyun.media.streamer.encoder.VideoEncodeFormat;
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
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * Config recorder or compose params
 */

public class ShortVideoConfigDialog extends Dialog {

    public static int SHORTVIDEOCONFIG_TYPE_RECORD = 1;
    public static int SHORTVIDEOCONFIG_TYPE_COMPOSE = 2;
    private RadioGroup mResGroup;
    private RadioButton mRes360Button;
    private RadioButton mRes480Button;
    private RadioButton mRes540Button;
    private EditText mFrameRateEditText;
    private RadioButton mEncodeWithH265;
    private RadioButton mEncodeWithGif;
    private RadioGroup mEncodeGroup;
    private RadioButton mSWButton;
    private RadioButton mHWButton;
    private RadioGroup mProfileGroup;
    private RadioButton mProfileLowPowerButton;
    private RadioButton mProfileBalanceButton;
    private RadioButton mProfileHighPerfButton;
    private EditText mVideoBitRateEditText;
    private EditText mAudioBitRateEditText;
    private TextView mConfigTitle;

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
        mConfigTitle = (TextView) findViewById(R.id.config_title);
        mFrameRateEditText = (EditText) findViewById(R.id.frameRatePicker);
        mFrameRateEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mVideoBitRateEditText = (EditText) findViewById(R.id.videoBitratePicker);
        mVideoBitRateEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mAudioBitRateEditText = (EditText) findViewById(R.id.audioBitratePicker);
        mAudioBitRateEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

        mResGroup = (RadioGroup) findViewById(R.id.resolution_group);
        mRes360Button = (RadioButton) findViewById(R.id.r360p);
        mRes480Button = (RadioButton) findViewById(R.id.r480p);
        mRes540Button = (RadioButton) findViewById(R.id.r540p);
        mEncodeWithH265 = (RadioButton) findViewById(R.id.encode_h265);
        mEncodeWithGif = (RadioButton) findViewById(R.id.encode_gif);
        mEncodeGroup = (RadioGroup) findViewById(R.id.encode_group);
        mSWButton = (RadioButton) findViewById(R.id.encode_sw);
        mHWButton = (RadioButton) findViewById(R.id.encode_hw);
        mProfileGroup = (RadioGroup) findViewById(R.id.encode_profile);
        mProfileLowPowerButton = (RadioButton) findViewById(R.id.encode_profile_low_power);
        mProfileBalanceButton = (RadioButton) findViewById(R.id.encode_profile_balance);
        mProfileHighPerfButton = (RadioButton) findViewById(R.id.encode_profile_high_perf);

        if (mConfigType == SHORTVIDEOCONFIG_TYPE_COMPOSE) {
            mEncodeGroup.setVisibility(View.GONE);
            mEncodeWithGif.setVisibility(View.VISIBLE);
            mConfigTitle.setText("合成参数配置");
        } else if(mConfigType == SHORTVIDEOCONFIG_TYPE_RECORD) {
            mEncodeWithGif.setVisibility(View.GONE);
            mConfigTitle.setText("录制参数配置");
        }

        Button mConfimButton = (Button) findViewById(R.id.preview_params_confim);
        mConfimButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mShortVideoConfig = new ShortVideoConfig();
                if (!TextUtils.isEmpty(mFrameRateEditText.getText().toString())) {
                    mShortVideoConfig.fps = Integer.parseInt(mFrameRateEditText.getText()
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

                if (mResGroup.getVisibility() == View.VISIBLE) {
                    if (mRes360Button.isChecked()) {
                        mShortVideoConfig.resolution = StreamerConstants.VIDEO_RESOLUTION_360P;
                    } else if (mRes480Button.isChecked()) {
                        mShortVideoConfig.resolution = StreamerConstants.VIDEO_RESOLUTION_480P;
                    } else if (mRes540Button.isChecked()) {
                        mShortVideoConfig.resolution = StreamerConstants.VIDEO_RESOLUTION_540P;
                    } else {
                        mShortVideoConfig.resolution = StreamerConstants.VIDEO_RESOLUTION_720P;
                    }
                }

                if (mEncodeWithH265.isChecked()) {
                    mShortVideoConfig.encodeType = AVConst.CODEC_ID_HEVC;
                } else if(mEncodeWithGif.isChecked()) {
                    mShortVideoConfig.encodeType = AVConst.CODEC_ID_GIF;
                } else {
                    mShortVideoConfig.encodeType = AVConst.CODEC_ID_AVC;
                }

                if (mProfileLowPowerButton.isChecked()) {
                    mShortVideoConfig.encodeProfile = VideoEncodeFormat.ENCODE_PROFILE_LOW_POWER;
                } else if (mProfileBalanceButton.isChecked()) {
                    mShortVideoConfig.encodeProfile = VideoEncodeFormat.ENCODE_PROFILE_BALANCE;
                } else {
                    mShortVideoConfig.encodeProfile = VideoEncodeFormat
                            .ENCODE_PROFILE_HIGH_PERFORMANCE;
                }

                if (mEncodeGroup.getVisibility() == View.VISIBLE) {
                    if (mHWButton.isChecked()) {
                        mShortVideoConfig.encodeMethod = StreamerConstants.ENCODE_METHOD_HARDWARE;
                    } else if (mSWButton.isChecked()) {
                        mShortVideoConfig.encodeMethod = StreamerConstants.ENCODE_METHOD_SOFTWARE;
                    }
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
        public int fps;
        public int resolution;
        public int videoBitrate;
        public int audioBitrate;
        public int encodeType;
        public int encodeMethod;
        public int encodeProfile;
    }
}
