package com.ksyun.media.shortvideo.demo;

//import com.squareup.leakcanary.LeakCanary;
//import com.squareup.leakcanary.RefWatcher;

import android.app.Application;
import android.content.Context;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.tencent.bugly.crashreport.CrashReport;


public class ExampleApplication extends Application{
//    private RefWatcher mRefWatcher;
//
//
//    public static RefWatcher getRefWatcher(Context context) {
//        ExampleApplication application = (ExampleApplication) context.getApplicationContext();
//        return application.mRefWatcher;
//    }

    @Override
    public void onCreate() {
        super.onCreate();
//        mRefWatcher = LeakCanary.install(this);
        /*
         * 初始化Bugly，需要传入注册时申请的APPID，第三个参数为SDK调试模式开关；
         * 建议在测试阶段建议设置成true，发布时设置为false。
         * Bugly为应用崩溃日志收集工具，开发者可根据实际情况选择不集成或依赖其它Bug收集工具
         */
        CrashReport.initCrashReport(getApplicationContext(), "4e98881bde", true);
        // 初始化fresco库，用来支持动态贴纸功能，若不使用，可以不调用
        Fresco.initialize(this);
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration.createDefault(this);
        ImageLoader.getInstance().init(configuration);
    }
}
