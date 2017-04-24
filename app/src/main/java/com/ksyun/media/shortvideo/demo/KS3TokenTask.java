package com.ksyun.media.shortvideo.demo;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.ksyun.media.shortvideo.utils.KS3ClientWrap;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * get ks3 token on 17/4/13.
 */

public class KS3TokenTask {
    private static String TAG = "KS3TokenTask";
    public static final String TOKEN_SERVER_URL = "http://ksvs-demo.ks-live.com:8720/api/upload/ks3/sig";
    //Get ks3 Token
    private static final String TYPE = "type";
    private static final String MD5 = "md5";
    private static final String VERB = "verb";
    private static final String RES = "res";
    private static final String HEADERS = "headers";
    private static final String DATE = "date";
    private RequestQueue mRequestQueue;
    private Context mContext;

    public KS3TokenTask(Context context) {
        mContext = context;
    }

    /**
     * 向APPServer请求token
     *
     * @param httpMethod
     * @param contentType
     * @param date
     * @param contentMD5
     * @param resource
     * @param headers
     * @return
     */
    public KS3ClientWrap.KS3AuthInfo requsetTokenToAppServer(String httpMethod, String contentType, String
            date, String
            contentMD5, String resource, String headers) {
        Log.d(TAG, "requsetTokenToAppServer");
        String response = doGetToken(httpMethod, contentType, contentMD5, date, resource, headers);

        String token = null;
        String serverDate = null;
        try {
            JSONObject json = new JSONObject(response);

            token = json.getString("Authorization");
            Log.d(TAG, "token:" + token);
            serverDate = json.getString("Date");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return new KS3ClientWrap.KS3AuthInfo(token, serverDate);
    }


    //Get Image Token
    private String doGetToken(String httpMethod, String contentType, String contentMD5, String
            date, String resource, String headers) {
        Map<String, String> param = new HashMap<>();
        param.put(TYPE, contentType);
        param.put(MD5, contentMD5);
        param.put(VERB, httpMethod);
        param.put(RES, resource);
        param.put(HEADERS, headers);
        param.put(DATE, date);
        return doSyncHttpRequest(TOKEN_SERVER_URL, param);
    }

    private String doSyncHttpRequest(String url, Map<String, String> params) {

        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(mContext);
        }

        String response = new String();
        RequestFuture future = RequestFuture.newFuture();
        StringRequest request = new StringRequest(Request.Method.GET, getFinalUrlWithParams(url,
                params, false), future, future);
        mRequestQueue.add(request);
        try {
            response = (String) future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        return response;
    }

    private String getFinalUrlWithParams(String url, Map<String, String> param, boolean paramsOnly) {
        String ret = "";

        if (param != null && param.size() > 0) {
            Uri uri = Uri.parse(url);
            Uri.Builder builder = uri.buildUpon();
            for (Map.Entry<String, String> entry : param.entrySet()) {
                builder.appendQueryParameter(entry.getKey(), entry.getValue());
            }

            if (paramsOnly)
                ret = builder.build().getQuery();
            else
                ret = builder.build().toString();
        }

        return ret;
    }
}
