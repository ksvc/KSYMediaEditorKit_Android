package com.ksyun.media.shortvideo.demo.util;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.ksyun.media.shortvideo.demo.R;
import com.ksyun.media.shortvideo.demo.adapter.BgmSelectAdapter;
import com.ksyun.media.shortvideo.demo.adapter.ImageTextAdapter;
import com.ksyun.media.shortvideo.demo.adapter.MVTypeListAdapter;
import com.ksyun.media.shortvideo.demo.adapter.SoundEffectAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter数据构造的工厂类.
 */

public class DataFactory {
    private static final int MODE_SOUND_CHANGE = 0;
    private static final int MODE_REVERB = 1;

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

    private static final String[] BGM_NAME = {"cancel", "Faded", "Hotel California", "Immortals", "import"};
    private static final int[] BGM_IMAGE_ID = {R.drawable.close, R.drawable.faded, R.drawable.hotel_california,
            R.drawable.immortals, R.drawable.add};

    private static final String[] SOUND_CHANGE_NAME = {"cancel", "大叔", "萝莉", "庄重", "机器人"};
    private static final int[] SOUND_CHANGE_IMG_ID = {R.drawable.close, R.drawable.uncle,
            R.drawable.lolita, R.drawable.solemn, R.drawable.robot};

    private static final String[] REVERB_NAME = {"cancel", "录音棚", "小舞台", "演唱会", "KTV"};
    private static final int[] REVERB_IMG_ID = {R.drawable.close, R.drawable.record_studio,
            R.drawable.woodwing, R.drawable.concert, R.drawable.ktv};

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

    public static List<BgmSelectAdapter.BgmData> getBgmData(Context context) {
        List<BgmSelectAdapter.BgmData> dataList = new ArrayList<>();
        for (int i = 0; i < BGM_NAME.length; i++) {
            Drawable image = context.getResources().getDrawable(BGM_IMAGE_ID[i]);
            String type = BGM_NAME[i];
            BgmSelectAdapter.BgmData data = new BgmSelectAdapter.BgmData(image, type);
            dataList.add(data);
        }
        return dataList;
    }

    public static List<MVTypeListAdapter.MVData> getMVData() {
        List<MVTypeListAdapter.MVData> dataList = new ArrayList<>();
        return dataList;
    }

    public static List<SoundEffectAdapter.SoundEffectData> getSoundEffectData(Context context, int mode) {
        List<SoundEffectAdapter.SoundEffectData> dataList = new ArrayList<>();
        if (mode == MODE_SOUND_CHANGE) {
            for (int i = 0; i < SOUND_CHANGE_NAME.length; i++) {
                Drawable image = context.getResources().getDrawable(SOUND_CHANGE_IMG_ID[i]);
                String type = SOUND_CHANGE_NAME[i];
                SoundEffectAdapter.SoundEffectData data = new SoundEffectAdapter.SoundEffectData(image, type);
                dataList.add(data);
            }
        } else {
            for (int i = 0; i < REVERB_NAME.length; i++) {
                Drawable image = context.getResources().getDrawable(REVERB_IMG_ID[i]);
                String type = REVERB_NAME[i];
                SoundEffectAdapter.SoundEffectData data = new SoundEffectAdapter.SoundEffectData(image, type);
                dataList.add(data);
            }
        }
        return dataList;
    }
}
