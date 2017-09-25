package com.ksyun.media.shortvideo.demo.util;

import android.content.Context;

public class DensityUtil {

    public DensityUtil() {
    }

    public static int dip2px(Context paramContext, float paramFloat) {
        return (int)(0.5F + paramFloat * paramContext.getResources().getDisplayMetrics().density);
    }

    public static int px2dip(Context paramContext, float paramFloat) {
        return (int)(0.5F + paramFloat / paramContext.getResources().getDisplayMetrics().density);
    }

    public static int sp2px(Context context, float spValue) {
        float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int)(spValue * fontScale + 0.5F);
    }
}
