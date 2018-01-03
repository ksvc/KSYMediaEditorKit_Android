package com.ksyun.media.shortvideo.multicanvasdemo.data;

import java.io.Serializable;

/**
 * 模版ui信息
 */
public class MultiCanvasInfo implements Serializable  {
    //以下位置信息为相对于模版预览区域size
    //以1:1，以下形式的模型为例
    //--------width--------
    //          |y         |
    //-x-record1-x'-record2-|height
    //-x-record3- -record4-|
    //          |          |
    //---------------------
    //record1 的x_preview为x/width * previewWidth，w_preview为record1_x/width * previewWidth,y_preview为y/height*previewHeight;
    //record2 的x_preview为(x+record1_x+x')/width;
    //其它坐标依次类推
    public boolean showAdd;   //是否显示添加按钮
    //视频的位置信息
    public int x_preview;  //录制视频距离模版整个区域的最左边的比例
    public int y_preview;  //录制视频距离模版整个区域的最上边的比例
    public int w_preview;
    public int h_preview;
    //添加视频按钮的位置信息，此部分也可以在xml中通过相对位置直接实现
    public int x_record_icon;
    public int y_record_icon;
    public int w_record_icon;
    public int h_record_icon;

    public MultiCanvasInfo(int x_preview, int y_preview, int w_preview, int h_preview, boolean showAdd) {
        this.x_preview = x_preview;
        this.y_preview = y_preview;
        this.w_preview = w_preview;
        this.h_preview = h_preview;
        this.showAdd = showAdd;
    }

    public MultiCanvasInfo(int x_preview, int y_preview, int w_preview, int h_preview,
                           int x_record_icon, int y_record_icon, int w_record_icon, int h_record_icon,
                           boolean showAdd) {
        this.x_preview = x_preview;
        this.y_preview = y_preview;
        this.w_preview = w_preview;
        this.h_preview = h_preview;
        this.x_record_icon = x_record_icon;
        this.y_record_icon = y_record_icon;
        this.w_record_icon = w_record_icon;
        this.h_record_icon = h_record_icon;
        this.showAdd = showAdd;
    }
}
