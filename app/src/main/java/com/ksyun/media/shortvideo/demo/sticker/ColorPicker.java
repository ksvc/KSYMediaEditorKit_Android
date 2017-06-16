package com.ksyun.media.shortvideo.demo.sticker;

import com.ksyun.media.shortvideo.demo.R;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * color picker
 */

public class ColorPicker extends Dialog implements SeekBar.OnSeekBarChangeListener {
    private static final String COLOR_STRING_FORMAT = "#%02x%02x%02x";

    private View mColorView; //选择颜色预览显示
    private SeekBar mRedSeekBar; //红色选择seek
    private SeekBar mGreenSeekBar;//绿色选择seek
    private SeekBar mBlueSeekBar; //蓝色选择seek
    private TextView mRedToolTip; //红色色值提示
    private TextView mGreenToolTip; //绿色色值提示
    private TextView mBlueToolTip; //蓝色色值提示
    private EditText mCodHex; //选择颜色的16进制色值
    private int mRedValue; //红色色值
    private int mGreenValue;  //绿色色值
    private int mBlueValue;  //蓝色色值
    private int mSeekBarLeft;
    private Rect mThumbRect;

    public ColorPicker(Context context) {
        super(context);
        this.mRedValue = 0;
        this.mGreenValue = 0;
        this.mBlueValue = 0;
    }

    public ColorPicker(Context context, int red, int green, int blue) {
        super(context);

        if (0 <= red && red <= 255)
            this.mRedValue = red;
        else
            this.mRedValue = 0;

        if (0 <= green && green <= 255)
            this.mGreenValue = green;
        else
            this.mGreenValue = 0;

        if (0 <= blue && blue <= 255)
            this.mBlueValue = blue;
        else
            this.mBlueValue = 0;
    }


    /**
     * Simple onCreate function. Here there is the init of the GUI.
     *
     * @param savedInstanceState As usual ...
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.color_picker);

        mColorView = findViewById(R.id.colorView);

        mRedSeekBar = (SeekBar) findViewById(R.id.redSeekBar);
        mGreenSeekBar = (SeekBar) findViewById(R.id.greenSeekBar);
        mBlueSeekBar = (SeekBar) findViewById(R.id.blueSeekBar);

        mSeekBarLeft = mRedSeekBar.getPaddingLeft();

        mRedToolTip = (TextView) findViewById(R.id.redToolTip);
        mGreenToolTip = (TextView) findViewById(R.id.greenToolTip);
        mBlueToolTip = (TextView) findViewById(R.id.blueToolTip);

        mCodHex = (EditText) findViewById(R.id.codHex);

        mRedSeekBar.setOnSeekBarChangeListener(this);
        mGreenSeekBar.setOnSeekBarChangeListener(this);
        mBlueSeekBar.setOnSeekBarChangeListener(this);

        mRedSeekBar.setProgress(mRedValue);
        mGreenSeekBar.setProgress(mGreenValue);
        mBlueSeekBar.setProgress(mBlueValue);

        mColorView.setBackgroundColor(Color.rgb(mRedValue, mGreenValue, mBlueValue));

        mCodHex.setText(String.format(COLOR_STRING_FORMAT, mRedValue, mGreenValue, mBlueValue));
        mCodHex.setEnabled(false);
    }


    /**
     * Method that syncrhonize the color between the bars, the view and the HEC code text.
     *
     * @param s HEX Code of the color.
     */
    private void updateColorView(String s) {
        if (s.matches("-?[0-9a-fA-F]+")) {
            int color = (int) Long.parseLong(s, 16);
            mRedValue = (color >> 16) & 0xFF;
            mGreenValue = (color >> 8) & 0xFF;
            mBlueValue = (color >> 0) & 0xFF;

            mColorView.setBackgroundColor(Color.rgb(mRedValue, mGreenValue, mBlueValue));
            mRedSeekBar.setProgress(mRedValue);
            mGreenSeekBar.setProgress(mGreenValue);
            mBlueSeekBar.setProgress(mBlueValue);
        } else {
            mCodHex.setError("format error");
        }
    }


    @Override
    public void onWindowFocusChanged(boolean hasFocus) {

        mThumbRect = mRedSeekBar.getThumb().getBounds();

        mRedToolTip.setX(mSeekBarLeft + mThumbRect.left);
        if (mRedValue < 10)
            mRedToolTip.setText("  " + mRedValue);
        else if (mRedValue < 100)
            mRedToolTip.setText(" " + mRedValue);
        else
            mRedToolTip.setText(mRedValue + "");

        mThumbRect = mGreenSeekBar.getThumb().getBounds();

        mGreenToolTip.setX(mSeekBarLeft + mThumbRect.left);
        if (mGreenValue < 10)
            mGreenToolTip.setText("  " + mGreenValue);
        else if (mRedValue < 100)
            mGreenToolTip.setText(" " + mGreenValue);
        else
            mGreenToolTip.setText(mGreenValue + "");

        mThumbRect = mBlueSeekBar.getThumb().getBounds();

        mBlueToolTip.setX(mSeekBarLeft + mThumbRect.left);
        if (mBlueValue < 10)
            mBlueToolTip.setText("  " + mBlueValue);
        else if (mBlueValue < 100)
            mBlueToolTip.setText(" " + mBlueValue);
        else
            mBlueToolTip.setText(mBlueValue + "");

    }

    /**
     * Method called when the user change the value of the bars. This sync the colors.
     *
     * @param seekBar  SeekBar that has changed
     * @param progress The new progress value
     * @param fromUser If it coem from User
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (seekBar.getId() == R.id.redSeekBar) {

            mRedValue = progress;
            mThumbRect = seekBar.getThumb().getBounds();

            mRedToolTip.setX(mSeekBarLeft + mThumbRect.left);

            if (progress < 10)
                mRedToolTip.setText("  " + mRedValue);
            else if (progress < 100)
                mRedToolTip.setText(" " + mRedValue);
            else
                mRedToolTip.setText(mRedValue + "");

        } else if (seekBar.getId() == R.id.greenSeekBar) {

            mGreenValue = progress;
            mThumbRect = seekBar.getThumb().getBounds();

            mGreenToolTip.setX(seekBar.getPaddingLeft() + mThumbRect.left);
            if (progress < 10)
                mGreenToolTip.setText("  " + mGreenValue);
            else if (progress < 100)
                mGreenToolTip.setText(" " + mGreenValue);
            else
                mGreenToolTip.setText(mGreenValue + "");

        } else if (seekBar.getId() == R.id.blueSeekBar) {

            mBlueValue = progress;
            mThumbRect = seekBar.getThumb().getBounds();

            mBlueToolTip.setX(mSeekBarLeft + mThumbRect.left);
            if (progress < 10)
                mBlueToolTip.setText("  " + mBlueValue);
            else if (progress < 100)
                mBlueToolTip.setText(" " + mBlueValue);
            else
                mBlueToolTip.setText(mBlueValue + "");

        }

        mColorView.setBackgroundColor(Color.rgb(mRedValue, mGreenValue, mBlueValue));

        mCodHex.setText(String.format(COLOR_STRING_FORMAT, mRedValue, mGreenValue, mBlueValue));

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }


    public int getRed() {
        return mRedValue;
    }

    public int getGreen() {
        return mGreenValue;
    }

    public int getBlue() {
        return mBlueValue;
    }

    public int getColor() {
        return Color.rgb(mRedValue, mGreenValue, mBlueValue);
    }
}
