package com.ksyun.media.shortvideo.multicanvasdemo.util;

import com.ksyun.media.shortvideo.multicanvasdemo.R;
import com.ksyun.media.shortvideo.multicanvasdemo.adapter.ImageTextAdapter;

import android.content.Context;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter数据构造的工厂类.
 */

public class DataFactory {
    private static final String[] BEAUTY_TYPE_NAME = {"自然", "唯美", "花颜", "粉嫩"};
    private static final int[] BEAUTY_TYPE_ID = {R.drawable.beauty_nature, R.drawable.beauty_pro,
            R.drawable.beauty_flower_like, R.drawable.beauty_delicate};

    private static final String[] IMG_FILTER_NAME = {"小清新", "靓丽", "甜美可人", "怀旧", "蓝调", "老照片",
            "樱花", "樱花(夜)", "红润(夜)", "阳光(夜)", "红润", "阳光", "自然", "优格", "流年", "柔光", "初夏",
            "纽约", "碧波", "日系", "梦幻", "恬淡", "候鸟", "淡雅"};
    private static final int[] FILTER_IMAGE_ID = {R.drawable.filter_fresh, R.drawable.filter_beautiful,
            R.drawable.filter_sweet, R.drawable.filter_sepia, R.drawable.filter_blue, R.drawable.filter_nostalgia,
            R.drawable.filter_sakura, R.drawable.filter_sakura_night, R.drawable.filter_ruddy_night, R.drawable.filter_sunshine_night,
            R.drawable.filter_ruddy, R.drawable.filter_sunshine, R.drawable.filter_nature, R.drawable.yogurt, R.drawable.fleeting_time,
            R.drawable.soft_ligth, R.drawable.early_summer, R.drawable.newyork, R.drawable.greenwaves,
            R.drawable.japanese, R.drawable.illusion, R.drawable.tranquil, R.drawable.migrant_bird, R.drawable.elegant};


    public static List<ImageTextAdapter.Data> getBeautyTypeDate(Context context) {
        List<ImageTextAdapter.Data> beautyDate = new ArrayList<>();
        for (int i = 0; i < BEAUTY_TYPE_NAME.length; i++) {
            Drawable image = context.getResources().getDrawable(BEAUTY_TYPE_ID[i]);
            String type = BEAUTY_TYPE_NAME[i];
            ImageTextAdapter.Data data = new ImageTextAdapter.Data(image, type);
            beautyDate.add(data);
        }
        return beautyDate;
    }

    public static List<ImageTextAdapter.Data> getImgFilterData(Context context) {
        List<ImageTextAdapter.Data> filterData = new ArrayList<>();
        for (int i = 0; i < IMG_FILTER_NAME.length; i++) {
            Drawable image = context.getResources().getDrawable(FILTER_IMAGE_ID[i]);
            String type = IMG_FILTER_NAME[i];
            ImageTextAdapter.Data data = new ImageTextAdapter.Data(image, type);
            filterData.add(data);
        }
        return filterData;
    }
}
