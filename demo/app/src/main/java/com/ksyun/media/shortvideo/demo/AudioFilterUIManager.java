package com.ksyun.media.shortvideo.demo;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.SparseArray;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ksyun.media.shortvideo.demo.util.DownloadAndHandleTask;
import com.ksyun.media.shortvideo.demo.util.FileUtils;
import com.ksyun.media.shortvideo.kit.KSYEditKit;
import com.ksyun.media.shortvideo.kit.KSYRecordKit;
import com.ksyun.media.streamer.filter.audio.AudioFilterBase;
import com.ksyun.media.streamer.filter.audio.AudioReverbFilter;
import com.ksyun.media.streamer.filter.audio.KSYAudioEffectFilter;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * 音频相关处理
 */
public class AudioFilterUIManager {

    public static final int RECORD_MODE = 0;
    public static final int EDIT_MODE = 1;
    private static final int REQUEST_CODE = 10010;
    private static final int INDEX_BGM_ITEM_BASE = 0;    //背景音乐选项在内容集合中的索引
    private static final int INDEX_SOUND_EFFECT_BASE = 10;  //音效选项在内容集合中的索引
    private static final int AUDIO_FILTER_DISABLE = 0;  //不使用音频滤镜的类型标志
    private int mAudioEffectType = AUDIO_FILTER_DISABLE;  //变声类型缓存变量
    private int mAudioReverbType = AUDIO_FILTER_DISABLE;  //混响类型缓存变量
    //背景音乐下载地址
    private String[] mBgmLoadPath = {"https://ks3-cn-beijing.ksyun.com/ksy.vcloud.sdk/ShortVideo/faded.mp3",
            "https://ks3-cn-beijing.ksyun.com/ksy.vcloud.sdk/ShortVideo/Hotel_California.mp3",
            "https://ks3-cn-beijing.ksyun.com/ksy.vcloud.sdk/ShortVideo/Immortals.mp3"};
    //变声和混响类型数组常量
    private static final int[] SOUND_EFFECT_CONST = {KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_MALE, KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_FEMALE,
            KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_HEROIC, KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_ROBOT,
            AudioReverbFilter.AUDIO_REVERB_LEVEL_1, AudioReverbFilter.AUDIO_REVERB_LEVEL_3,
            AudioReverbFilter.AUDIO_REVERB_LEVEL_4, AudioReverbFilter.AUDIO_REVERB_LEVEL_2};

    private Context mContext;
    private View mBgmLayout;
    private View mSoundChangeLayout;
    private View mReverbLayout;
    private ImageView mCancelBgm;
    private ImageView mImportBgm;
    private ImageView mCancelSoundChange;
    private ImageView mCancelReverberation;
    private ImageView mPitchMinus;
    private ImageView mPitchPlus;
    private TextView mPitchText;
    private AppCompatSeekBar mMicAudioVolumeSeekBar;
    private AppCompatSeekBar mBgmVolumeSeekBar;
    private int mInitMode;
    private SeekBarChangedObserver mSeekBarChangedObsesrver;
    private BgmButtonObserver mObserver;
    private OnBgmHandleListener mListener;

    private KSYRecordKit mRecordKit;
    private KSYEditKit mEditKit;

    private SparseArray<BgmItemViewHolder> mBgmEffectArray = new SparseArray<>();
    private DownloadAndHandleTask mBgmLoadTask;
    private int mPitchValue = 0;  //音调值缓存变量
    private int mPreBgmItemIndex = 0;   //记录上次选择的背景音乐内容索引
    private int mPreBgmEffectIndex = 0;  //记录上次选择的变声类型索引
    private int mPreBgmReverbIndex = 0;  //记录上次选择的混响类型索引

    public interface OnBgmHandleListener {
        void onCancel();
    }

    public void setOnBgmHandleListener(OnBgmHandleListener listener) {
        this.mListener = listener;
    }

    public AudioFilterUIManager(Context context, View bgmLayout, View soundChangeLayout,
                                View reverbLayout, Object kit) {
        if (kit instanceof KSYEditKit) {
            mInitMode = EDIT_MODE;
            mEditKit = (KSYEditKit) kit;
        } else if (kit instanceof KSYRecordKit) {
            mInitMode = RECORD_MODE;
            mRecordKit = (KSYRecordKit) kit;
        }
        mContext = context;
        mBgmLayout = bgmLayout;
        mSoundChangeLayout = soundChangeLayout;
        mReverbLayout = reverbLayout;
        initBgmUI();
    }

    /**
     * 初始化背景音乐相关的UI界面
     */
    public void initBgmUI() {
        mSeekBarChangedObsesrver = new SeekBarChangedObserver();
        mObserver = new BgmButtonObserver();
        mMicAudioVolumeSeekBar = (AppCompatSeekBar) mBgmLayout.findViewById(R.id.record_mic_audio_volume);
        mMicAudioVolumeSeekBar.setOnSeekBarChangeListener(mSeekBarChangedObsesrver);
        mBgmVolumeSeekBar = (AppCompatSeekBar) mBgmLayout.findViewById(R.id.record_music_audio_volume);
        mBgmVolumeSeekBar.setOnSeekBarChangeListener(mSeekBarChangedObsesrver);
        if (mInitMode == RECORD_MODE) {
            mPitchMinus = (ImageView) mBgmLayout.findViewById(R.id.pitch_minus);
            mPitchMinus.setOnClickListener(mObserver);
            mPitchPlus = (ImageView) mBgmLayout.findViewById(R.id.pitch_plus);
            mPitchPlus.setOnClickListener(mObserver);
            mPitchText = (TextView) mBgmLayout.findViewById(R.id.pitch_text);
            mMicAudioVolumeSeekBar.setProgress((int) (mRecordKit.getVoiceVolume() * 100));
            mBgmVolumeSeekBar.setProgress((int) (mRecordKit.getVoiceVolume() * 100));
        } else {
            mMicAudioVolumeSeekBar.setProgress((int) (mEditKit.getOriginAudioVolume() * 100));
            mBgmVolumeSeekBar.setProgress((int) (mEditKit.getBgmVolume() * 100));
        }
        setEnableBgmEdit(false);
        mCancelBgm = (ImageView) mBgmLayout.findViewById(R.id.bgm_music_close);
        mCancelBgm.setOnClickListener(mObserver);
        mImportBgm = (ImageView) mBgmLayout.findViewById(R.id.bgm_music_import);
        mImportBgm.setOnClickListener(mObserver);
        int[] mBgmItemImageId = {R.id.bgm_music_iv_faded, R.id.bgm_music_iv_hotel,
                R.id.bgm_music_iv_immortals};
        int[] mBgmItemNameId = {R.id.bgm_music_tv_faded, R.id.bgm_music_tv_hotel,
                R.id.bgm_music_tv_immortals};
        for (int i = 0; i < mBgmItemImageId.length; i++) {
            BgmItemViewHolder holder = new BgmItemViewHolder((ImageView) mBgmLayout.findViewById(mBgmItemImageId[i]),
                    (TextView) mBgmLayout.findViewById(mBgmItemNameId[i]), mObserver);
            mBgmEffectArray.put(INDEX_BGM_ITEM_BASE + i, holder);
        }
        mCancelSoundChange = (ImageView) mSoundChangeLayout.findViewById(R.id.effect_iv_close);
        mCancelSoundChange.setOnClickListener(mObserver);
        mCancelReverberation = (ImageView) mReverbLayout.findViewById(R.id.reverberation_iv_close);
        mCancelReverberation.setOnClickListener(mObserver);
        int[] effectImageId = {R.id.effect_iv_uncle, R.id.effect_iv_lolita,
                R.id.effect_iv_solemn, R.id.effect_iv_robot, R.id.effect_iv_studio,
                R.id.effect_iv_woodWing, R.id.effect_iv_concert, R.id.effect_iv_ktv};
        int[] effectNameId = {R.id.effect_tv_uncle, R.id.effect_tv_lolita,
                R.id.effect_tv_solemn, R.id.effect_tv_robot, R.id.effect_tv_studio,
                R.id.effect_tv_woodWing, R.id.effect_tv_concert, R.id.effect_tv_ktv};
        for (int j = 0; j < effectImageId.length; j++) {
            if (j < 4) {
                BgmItemViewHolder holder = new BgmItemViewHolder((ImageView) mSoundChangeLayout.findViewById(effectImageId[j]),
                        (TextView) mSoundChangeLayout.findViewById(effectNameId[j]), mObserver);
                mBgmEffectArray.put(INDEX_SOUND_EFFECT_BASE + j, holder);
            } else {
                BgmItemViewHolder holder = new BgmItemViewHolder((ImageView) mReverbLayout.findViewById(effectImageId[j]),
                        (TextView) mReverbLayout.findViewById(effectNameId[j]), mObserver);
                mBgmEffectArray.put(INDEX_SOUND_EFFECT_BASE + j, holder);
            }

        }
    }

    private class SeekBarChangedObserver implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            if (!fromUser) {
                return;

            }
            float val = progress / 100.f;
            switch (seekBar.getId()) {
                case R.id.record_mic_audio_volume:
                    if (mInitMode == RECORD_MODE) {
                        mRecordKit.setVoiceVolume(val);
                    } else {
                        mEditKit.setOriginAudioVolume(val);
                    }
                    break;
                case R.id.record_music_audio_volume:
                    if (mInitMode == RECORD_MODE) {
                        mRecordKit.getAudioPlayerCapture().getMediaPlayer().setVolume(val, val);
                    } else {
                        mEditKit.setBgmVolume(val);
                    }
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }

    }

    /**
     * 根据是否有背景音乐选中来设置相应的编辑控件是否可用
     */
    public void setEnableBgmEdit(boolean enable) {
        if (mInitMode == RECORD_MODE) {
            if (mPitchMinus != null) {
                mPitchMinus.setEnabled(enable);
            }
            if (mPitchPlus != null) {
                mPitchPlus.setEnabled(enable);
            }
        }
        if (mBgmVolumeSeekBar != null) {
            mBgmVolumeSeekBar.setEnabled(enable);
        }
    }

    private class BgmButtonObserver implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.bgm_music_close:
                    onBgmItemClick(-1);
                    break;
                case R.id.bgm_music_iv_faded:
                    onBgmItemClick(0);
                    break;
                case R.id.bgm_music_iv_hotel:
                    onBgmItemClick(1);
                    break;
                case R.id.bgm_music_iv_immortals:
                    onBgmItemClick(2);
                    break;
                case R.id.bgm_music_import:
                    onBgmItemClick(-1);
                    importMusicFile();
                    break;
                case R.id.effect_iv_close:
                    onSoundEffectItemClick(-1);
                    break;
                case R.id.effect_iv_uncle:
                    onSoundEffectItemClick(0);
                    break;
                case R.id.effect_iv_lolita:
                    onSoundEffectItemClick(1);
                    break;
                case R.id.effect_iv_solemn:
                    onSoundEffectItemClick(2);
                    break;
                case R.id.effect_iv_robot:
                    onSoundEffectItemClick(3);
                    break;
                case R.id.reverberation_iv_close:
                    onSoundEffectItemClick(-2);
                    break;
                case R.id.effect_iv_studio:
                    onSoundEffectItemClick(4);
                    break;
                case R.id.effect_iv_woodWing:
                    onSoundEffectItemClick(5);
                    break;
                case R.id.effect_iv_concert:
                    onSoundEffectItemClick(6);
                    break;
                case R.id.effect_iv_ktv:
                    onSoundEffectItemClick(7);
                    break;
            }
        }
    }

    /**
     * 打开系统文件夹，导入音频文件作为背景音乐
     */
    private void importMusicFile() {
        Intent target = FileUtils.createGetContentIntent();
        Intent intent = Intent.createChooser(target, "ksy_import_music_file");
        try {
            if (mInitMode == RECORD_MODE) {
                ((RecordActivity) mContext).startActivityForResult(intent, REQUEST_CODE);
            } else {
                ((EditActivity) mContext).startActivityForResult(intent, REQUEST_CODE);
            }

        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 背景音乐点击事件处理，若本地存在从本地读取，若本地不存在开启异步任务从网络下载
     */
    private void onBgmItemClick(int index) {
        if (mInitMode == RECORD_MODE) {
            clearPitchState();
        }
        BgmItemViewHolder curHolder = mBgmEffectArray.get(INDEX_BGM_ITEM_BASE + index);
        BgmItemViewHolder preHolder = mBgmEffectArray.get(INDEX_BGM_ITEM_BASE + mPreBgmItemIndex);
        if (index == -1) {
            if (mInitMode == RECORD_MODE) {
                mRecordKit.stopBgm();
            } else {
                mEditKit.stopBgm();
            }
            if (mListener != null) {
                mListener.onCancel();
            }
            preHolder.setBottomTextActivated(false);
            setEnableBgmEdit(false);
        } else {
            if (index < 3) {
                if (mInitMode == RECORD_MODE) {
                    mRecordKit.stopBgm();
                } else {
                    mEditKit.stopBgm();
                }
                String fileName = mBgmLoadPath[index].substring(mBgmLoadPath[index].lastIndexOf('/'));
                final String filePath = FileUtils.getCacheDirectory(mContext.getApplicationContext()) + fileName;
                File file = new File(filePath);
                if (!file.exists()) {
                    if (mBgmLoadTask != null && mBgmLoadTask.getStatus() == AsyncTask.Status.RUNNING) {
                        mBgmLoadTask.cancel(true);
                    }
                    DownloadAndHandleTask.DownloadListener listener = new DownloadAndHandleTask.DownloadListener() {
                        @Override
                        public void onCompleted() {
                            if (mInitMode == RECORD_MODE) {
                                mRecordKit.startBgm(filePath, true);
                            } else {
                                mEditKit.startBgm(filePath, true);
                            }
                        }
                    };
                    mBgmLoadTask = new DownloadAndHandleTask(filePath, listener);
                    mBgmLoadTask.execute(mBgmLoadPath[index]);
                } else {
                    if (mInitMode == RECORD_MODE) {
                        mRecordKit.startBgm(filePath, true);
                    } else {
                        mEditKit.startBgm(filePath, true);
                    }
                }
            }
            preHolder.setBottomTextActivated(false);
            curHolder.setBottomTextActivated(true);
            mPreBgmItemIndex = index;
            setEnableBgmEdit(true);
        }
    }

    /**
     * 清除音调状态，重置为'0'
     */
    private void clearPitchState() {
        mPitchValue = 0;
        mPitchText.setText("0");
        KSYAudioEffectFilter audioFilter = new KSYAudioEffectFilter(KSYAudioEffectFilter.AUDIO_EFFECT_TYPE_PITCH);
        audioFilter.setPitchLevel(mPitchValue);
        mRecordKit.getBGMAudioFilterMgt().setFilter(audioFilter);
    }

    /**
     * 音效点击事件处理
     */
    private void onSoundEffectItemClick(int index) {
        BgmItemViewHolder curHolder = mBgmEffectArray.get(INDEX_SOUND_EFFECT_BASE + index);
        BgmItemViewHolder preHolder1 = mBgmEffectArray.get(INDEX_SOUND_EFFECT_BASE + mPreBgmEffectIndex);
        BgmItemViewHolder preHolder2 = mBgmEffectArray.get(INDEX_SOUND_EFFECT_BASE + mPreBgmReverbIndex);
        if (index == -1) {
            preHolder1.setBottomTextActivated(false);
            mAudioEffectType = AUDIO_FILTER_DISABLE;  //重置变声类型缓存变量
        } else if (index == -2) {
            preHolder2.setBottomTextActivated(false);
            mAudioReverbType = AUDIO_FILTER_DISABLE;  //重置混响类型缓存变量
        } else {
            if (index < 4) {
                preHolder1.setBottomTextActivated(false);
                mPreBgmEffectIndex = index;
                mAudioEffectType = SOUND_EFFECT_CONST[index];
            } else {
                preHolder2.setBottomTextActivated(false);
                mPreBgmReverbIndex = index;
                mAudioReverbType = SOUND_EFFECT_CONST[index];
            }
            curHolder.setBottomTextActivated(true);
        }
        addAudioFilter();
    }

    /**
     * 添加音频滤镜，支持变声和混响同时生效
     */
    private void addAudioFilter() {
        KSYAudioEffectFilter effectFilter;
        AudioReverbFilter reverbFilter;
        List<AudioFilterBase> filters = new LinkedList<>();
        if (mAudioEffectType != AUDIO_FILTER_DISABLE) {
            effectFilter = new KSYAudioEffectFilter
                    (mAudioEffectType);
            filters.add(effectFilter);
        }
        if (mAudioReverbType != AUDIO_FILTER_DISABLE) {
            reverbFilter = new AudioReverbFilter();
            reverbFilter.setReverbLevel(mAudioReverbType);
            filters.add(reverbFilter);
        }
        if (filters.size() > 0) {
            if (mInitMode == RECORD_MODE) {
                mRecordKit.getAudioFilterMgt().setFilter(filters);
            } else {
                mEditKit.getAudioFilterMgt().setFilter(filters);
            }
        } else {
            if (mInitMode == RECORD_MODE) {
                mRecordKit.getAudioFilterMgt().setFilter((AudioFilterBase) null);
            } else {
                mEditKit.getAudioFilterMgt().setFilter((AudioFilterBase) null);
            }
        }
    }

    /**
     * 背景音乐和音效Item的封装类，ImageView用于可视化类型说明，TextView是图片下的文字说明
     */
    public class BgmItemViewHolder {
        public ImageView mBgmItemImage;
        public TextView mBgmItemName;

        public BgmItemViewHolder(ImageView iv, TextView tv,
                                 View.OnClickListener onClickListener) {
            this.mBgmItemImage = iv;
            this.mBgmItemName = tv;
            if (mBgmItemImage != null) {
                mBgmItemImage.setOnClickListener(onClickListener);
            }
        }

        public void setBottomTextActivated(boolean isSelected) {
            if (mBgmItemName != null) {
                mBgmItemName.setActivated(isSelected);
            }
        }
    }

}
