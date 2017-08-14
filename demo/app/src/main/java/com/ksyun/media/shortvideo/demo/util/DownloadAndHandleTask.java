package com.ksyun.media.shortvideo.demo.util;


import android.os.AsyncTask;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadAndHandleTask extends AsyncTask<String, Integer, String> {
    private String mFilePath;
    private DownloadListener mDownloadListener;

    public interface DownloadListener {
        void onCompleted(String filePath);
    }

    public DownloadAndHandleTask(String path, DownloadListener listener) {
        mFilePath = path;
        mDownloadListener = listener;
    }


    @Override
    protected String doInBackground(String... params) {
        try {
            URL url = new URL(params[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }
            InputStream in = new BufferedInputStream(connection.getInputStream());
            OutputStream out = new FileOutputStream(mFilePath);
            byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data)) != -1) {
                if (isCancelled()) {
                    in.close();
                    out.close();
                    return null;
                }
                out.write(data, 0, count);
            }
            in.close();
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        mDownloadListener.onCompleted(mFilePath);
    }

}
