package com.ksyun.media.shortvideo.demo.util;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.ksyun.media.shortvideo.demo.R;
import com.ksyun.media.shortvideo.demo.adapter.BgmSelectAdapter;
import com.ksyun.media.shortvideo.demo.adapter.SoundEffectAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter数据构造的工厂类.
 */

public class DataFactory {
    private static final int MODE_SOUND_CHANGE = 0;
    private static final int MODE_REVERB = 1;

    public static final String[] BGM_NAME = {"cancel", "Faded", "Hotel California", "Immortals", "import"};
    public static final int[] BGM_IMAGE_ID = {R.drawable.close, R.drawable.faded, R.drawable.hotel_california,
            R.drawable.immortals, R.drawable.add};

    public static final String[] SOUND_CHANGE_NAME = {"cancel", "大叔", "萝莉", "庄重", "机器人"};
    public static final int[] SOUND_CHANGE_IMG_ID = {R.drawable.close, R.drawable.uncle,
            R.drawable.lolita, R.drawable.solemn, R.drawable.robot};

    public static final String[] REVERB_NAME = {"cancel", "录音棚", "小舞台", "演唱会", "KTV"};
    public static final int[] REVERB_IMG_ID = {R.drawable.close, R.drawable.record_studio,
            R.drawable.woodwing, R.drawable.concert, R.drawable.ktv};

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

    public static List<SoundEffectAdapter.SoundEffectData> getSoundEffectData(Context context, int mode) {
        List<SoundEffectAdapter.SoundEffectData> dataList = new ArrayList<>();
        if (mode == MODE_SOUND_CHANGE) {
            for (int i = 0; i < SOUND_CHANGE_NAME.length; i++) {
                Drawable image = context.getResources().getDrawable(SOUND_CHANGE_IMG_ID[i]);
                String type = SOUND_CHANGE_NAME[i];
                SoundEffectAdapter.SoundEffectData data = new SoundEffectAdapter.SoundEffectData(image,type);
                dataList.add(data);
            }
        } else {
            for (int i = 0; i < REVERB_NAME.length; i++) {
                Drawable image = context.getResources().getDrawable(REVERB_IMG_ID[i]);
                String type = REVERB_NAME[i];
                SoundEffectAdapter.SoundEffectData data = new SoundEffectAdapter.SoundEffectData(image,type);
                dataList.add(data);
            }
        }
        return dataList;
    }
}
