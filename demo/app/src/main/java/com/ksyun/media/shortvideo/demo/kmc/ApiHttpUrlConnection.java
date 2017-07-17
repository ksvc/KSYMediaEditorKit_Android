package com.ksyun.media.shortvideo.demo.kmc;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiHttpUrlConnection {
    private static int CONNECTION_TIMEOUT = 5000;
    private static int READTIMEOUT = 10000;
    private static final String CHARSET = "UTF-8";
    private static String TAG = "ApiHttpUrlConnection";


    public static String doPost(String url, String params) {
        try {
            HttpURLConnection httpConn = (HttpURLConnection) new URL(url).openConnection();
            httpConn.setRequestMethod("POST");
            httpConn.setConnectTimeout(CONNECTION_TIMEOUT);
            httpConn.setReadTimeout(READTIMEOUT);
            httpConn.setDoOutput(true);
            httpConn.setDoInput(true);

            httpConn.setUseCaches(false);
            httpConn.connect();
            OutputStream output = httpConn.getOutputStream();
            DataOutputStream outputdata = new DataOutputStream(output);

            outputdata.writeBytes(params);
            outputdata.flush();
            outputdata.close();

            BufferedReader reader = new BufferedReader(new InputStreamReader(httpConn.getInputStream()));
            String line;
            String result = "";

            while ((line = reader.readLine()) != null) {
                result += "\n" + line;
            }

            reader.close();
            httpConn.disconnect();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap getImageBitmap(String url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setConnectTimeout(READTIMEOUT);
        conn.setRequestMethod("GET");
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            InputStream inStream = conn.getInputStream();
            Bitmap bitmap = BitmapFactory.decodeStream(inStream);
            return bitmap;
        }
        return null;
    }

}
