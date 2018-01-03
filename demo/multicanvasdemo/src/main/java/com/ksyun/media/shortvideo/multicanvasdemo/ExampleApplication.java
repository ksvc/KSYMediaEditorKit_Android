package com.ksyun.media.shortvideo.multicanvasdemo;

//import com.squareup.leakcanary.LeakCanary;
//import com.squareup.leakcanary.RefWatcher;

import com.tencent.bugly.crashreport.CrashReport;

import android.app.Application;


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
    }
}
