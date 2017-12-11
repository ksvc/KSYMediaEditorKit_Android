package com.ksyun.media.shortvideo.demo.util;

import com.ksyun.ks3.model.ObjectMetadata;
import com.ksyun.ks3.model.acl.CannedAccessControlList;
import com.ksyun.ks3.services.AuthListener;
import com.ksyun.ks3.services.Ks3Client;
import com.ksyun.ks3.services.Ks3ClientConfiguration;
import com.ksyun.ks3.services.handler.PutObjectResponseHandler;
import com.ksyun.ks3.services.request.PutObjectRequest;
import com.ksyun.media.shortvideo.utils.*;

import android.content.Context;
import android.util.Log;


import java.io.File;

/**
 * KS3 client tools
 */

public class KS3ClientWrap {
    private static String TAG = "KS3ClientWrap";
    private Context mContext;
    private OnGetAuthInfoListener mGetAuthInfoListener;
    private Ks3Client mClient;

    private static KS3ClientWrap sInstance;

    public static KS3ClientWrap getInstance(Context context) {
        if(sInstance == null) {
            synchronized (KS3ClientWrap.class) {
                if(sInstance == null) {
                    sInstance = new KS3ClientWrap(context);
                }
            }
        }
        return sInstance;
    }

    public static void uninitInstance() {
        if(sInstance != null) {
            sInstance.release();
            sInstance = null;
        }
    }

    private KS3ClientWrap(Context context) {
        mContext = context.getApplicationContext();
        mClient = new Ks3Client(mAuthInfoListener, mContext);
        Ks3ClientConfiguration configuration = Ks3ClientConfiguration.getDefaultConfiguration();
        mClient.setConfiguration(configuration);
        mClient.setEndpoint("ks3-cn-beijing.ksyun.com");
    }

    /**
     * @hide
     */
    public AuthListener mAuthInfoListener = new AuthListener() {
        @Override
        public String onCalculateAuth(String httpMethod, String ContentType,
                                      String Date, String ContentMD5, String Resource, String Headers) {
            if (mGetAuthInfoListener != null) {
                KS3AuthInfo authInfo = mGetAuthInfoListener.onGetAuthInfo
                        (httpMethod,
                                ContentType,
                                Date, ContentMD5, Resource, Headers);
                mGetAuthInfoListener = null;
                if (authInfo != null) {
                    Log.d(TAG, "token:" + authInfo.token);
                    Log.d(TAG, "server date:" + authInfo.date);
                    Date = authInfo.date;
                    return authInfo.token;
                }
                return "";
            }
            return "";
        }
    };

    /**
     * release
     */
    private void release() {
    }

    /**
     * return Ks3Client
     *
     * @return
     */
    public Ks3Client getKs3Client() {
        return mClient;
    }

    /**
     * upload file to KS3 Server
     *
     * @param bucketInfo            the params using Ks3Client
     * @param file                  the file which need to upload
     * @param putResponseHandler    upload process callback
     * @param onGetAuthInfoListener get Ks3's token callback
     */
    public void putObject(KS3UploadInfo bucketInfo, File file,
                          PutObjectResponseHandler putResponseHandler, OnGetAuthInfoListener onGetAuthInfoListener) {
        mGetAuthInfoListener = onGetAuthInfoListener;
        PutObjectRequest request = new PutObjectRequest(bucketInfo.bucket, bucketInfo.objectkey, file);
        CannedAccessControlList list = CannedAccessControlList.PublicRead;
        request.setCannedAcl(list);
        ObjectMetadata metadata = new ObjectMetadata();
        String mineType = com.ksyun.media.shortvideo.utils.FileUtils.getMimeType(file);
        if(mineType.equals(com.ksyun.media.shortvideo.utils.FileUtils.MIME_TYPE_MP4)) {
            metadata.addOrEditMeta(ObjectMetadata.Meta.ContentType, "video/mp4");
        } else if (mineType.equals(com.ksyun.media.shortvideo.utils.FileUtils.MIME_TYPE_GIF)) {
            metadata.addOrEditMeta(ObjectMetadata.Meta.ContentType, "image/gif");
        } else {
            metadata.addOrEditMeta(ObjectMetadata.Meta.ContentType, "video/mp4");
        }
        request.setObjectMeta(metadata);
        mClient.putObject(request, putResponseHandler);
    }

    /**
     * upload file to KS3 Server
     *
     * @param bucketInfo               the params using Ks3Client
     * @param path                     the file path which need to upload
     * @param putObjectResponseHandler upload process callback
     * @param onGetAuthInfoListener    get Ks3's token callback
     */
    public void putObject(KS3UploadInfo bucketInfo, String path,
                          PutObjectResponseHandler putObjectResponseHandler,
                          OnGetAuthInfoListener onGetAuthInfoListener) {
        File file = new File(path);
        putObject(bucketInfo, file, putObjectResponseHandler, onGetAuthInfoListener);
    }

    /**
     * cancel upload
     */
    public void cancel() {
        mClient.cancel(mContext);
    }

    /**
     * get Ks3's token callback<br/>
     * need to fetch token for file upload
     */
    public interface OnGetAuthInfoListener {
        KS3AuthInfo onGetAuthInfo(String httpMethod, String ContentType,
                                  String Date, String ContentMD5, String Resource, String Headers);
    }

    /**
     * bucket info for upload file to ks3
     */
    public static class KS3UploadInfo {
        /**
         * bucket of ks3
         */
        public String bucket;
        /**
         * file path which need to upload
         */
        public String objectkey;
        /**
         * upload process callback
         */
        public PutObjectResponseHandler putObjectResponseHandler;

        public KS3UploadInfo(String bucket, String objectkey, PutObjectResponseHandler responseHandler) {
            this.bucket = bucket;
            this.objectkey = objectkey;
            this.putObjectResponseHandler = responseHandler;
        }
    }

    /**
     * KS3 authentication info<br/>
     * data class
     */
    public static class KS3AuthInfo {
        /**
         * the token for authentication about KS3
         */
        public String token;
        /**
         * the date for authentication
         */
        public String date;

        public KS3AuthInfo(String token, String date) {
            this.token = token;
            this.date = date;
        }
    }
}
