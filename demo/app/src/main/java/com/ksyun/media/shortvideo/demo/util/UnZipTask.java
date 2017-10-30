package com.ksyun.media.shortvideo.demo.util;

import com.ksyun.media.shortvideo.demo.RecordActivity;

import android.os.AsyncTask;

import java.io.File;

/**
 *
 */

public class UnZipTask extends AsyncTask<File, Void, Void> {
    private OnProcessListener mListener;
    private String mFilePath;
    private String mFileName;

    public UnZipTask(String targetZip) {
        mFileName = targetZip.substring(0, targetZip.length() - RecordActivity.ZIP_INFO.length());
    }

    @Override
    protected Void doInBackground(File... params) {
        try {
            File file = params[0];
            int len = file.getAbsolutePath().length();
            mFilePath = file.getAbsolutePath().substring(0, len - RecordActivity.ZIP_INFO.length());

            if (!new File(mFilePath).exists()) {
                FileUtils.unZipFolder(file.getAbsolutePath(), file.getParent());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mListener != null) {
            mListener.onFinish(mFilePath, mFileName);
        }
        super.onPostExecute(aVoid);
    }

    public interface OnProcessListener {
        void onFinish(String filePath, String fileName);
    }

    public void setOnProcessListener(OnProcessListener listener) {
        mListener = listener;
    }
}
