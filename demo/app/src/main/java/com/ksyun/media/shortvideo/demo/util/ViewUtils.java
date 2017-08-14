package com.ksyun.media.shortvideo.demo.util;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

/**
 * View utils
 */

public class ViewUtils {
    private static String TAG = "ViewUtils";

    public static boolean isForeground(Context context, String className) {
        if (context == null || TextUtils.isEmpty(className)) {
            return false;
        }

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (className.equals(cpn.getClassName())) {
                return true;
            }
        }
        Log.d(TAG, className + " is not Foreground");

        return false;
    }
}
