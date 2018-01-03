package com.ksyun.media.shortvideo.multicanvasdemo.util;

import com.ksyun.media.shortvideo.multicanvasdemo.data.MultiCanvasInfo;

/**
 * generate model pos
 */

public class MultiCanvasFactory {
    public static MultiCanvasInfo[] getModel4Info(int previewWidth, int previewHeight) {
        MultiCanvasInfo[] multiCanvasInfos = new MultiCanvasInfo[4];
        int x = (int) ((5.0f / 375f) * previewWidth);
        int y = (int) ((5.0f / 375f) * previewHeight);
        int w = (int) ((180.0f / 375f) * previewWidth);
        int h = (int) ((180.0f / 375f) * previewHeight);
        int x_icon = (int) ((75.0f / 375f) * previewWidth);
        int y_icon = (int) ((75.0f / 375f) * previewHeight);
        int w_icon = (int) ((70.0f / 375f) * previewWidth);
        int h_icon = (int) ((70.0f / 375f) * previewHeight);

        multiCanvasInfos[0] = new MultiCanvasInfo(x, y, w, h, x_icon, y_icon, w_icon, h_icon, true);
        x = (int) ((190f / 375f) * previewWidth);
        x_icon = (int) ((260.0f / 375f) * previewWidth);
        multiCanvasInfos[1] = new MultiCanvasInfo(x, y, w, h, x_icon, y_icon, w_icon, h_icon, true);
        x = (int) ((5.0f / 375f) * previewWidth);
        y = (int) ((190.0f / 375f) * previewWidth);
        x_icon = (int) ((75.0f / 375f) * previewWidth);
        y_icon = (int) ((260.0f / 375f) * previewHeight);
        multiCanvasInfos[2] = new MultiCanvasInfo(x, y, w, h, x_icon, y_icon, w_icon, h_icon, true);
        x = (int) ((190f / 375f) * previewWidth);
        x_icon = (int) ((260.0f / 375f) * previewWidth);
        multiCanvasInfos[3] = new MultiCanvasInfo(x, y, w, h, x_icon, y_icon, w_icon, h_icon, true);
        return multiCanvasInfos;
    }
}
