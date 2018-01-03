package com.ksyun.media.shortvideo.multicanvasdemo.util;

/**
 * 获取应用CPU占用工具类
 */

import android.os.Process;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;

public class SystemStateObtainUtil {
    // 当前CPU频率获取路径
    private static final String CUR_FREQ_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq";
    // CPU最大频率获取路径
    private static final String MAX_FREQ_PATH = "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq";

    private volatile static SystemStateObtainUtil sInstance = null;
    private Long mLastCpuTime;
    private Long mLastAppCpuTime;
    private RandomAccessFile mSystemStatFile;
    private RandomAccessFile mAppStatFile;

    private SystemStateObtainUtil() {
    }

    public static SystemStateObtainUtil getInstance() {
        if (sInstance == null) {
            synchronized (SystemStateObtainUtil.class) {
                if (sInstance == null) {
                    sInstance = new SystemStateObtainUtil();
                }
            }
        }
        return sInstance;
    }

    public double sampleCPU() {
        long cpuTime;
        long appTime;
        double sampleValue = 0.0D;
        try {
            if (mSystemStatFile == null || mAppStatFile == null) {
                mSystemStatFile = new RandomAccessFile("/proc/stat", "r");
                mAppStatFile = new RandomAccessFile("/proc/" + Process.myPid() + "/stat", "r");
            } else {
                mSystemStatFile.seek(0L);
                mAppStatFile.seek(0L);
            }
            String procStatString = mSystemStatFile.readLine();
            String appStatString = mAppStatFile.readLine();
            String procStats[] = procStatString.split(" ");
            String appStats[] = appStatString.split(" ");
            cpuTime = Long.parseLong(procStats[2]) + Long.parseLong(procStats[3])
                    + Long.parseLong(procStats[4]) + Long.parseLong(procStats[5])
                    + Long.parseLong(procStats[6]) + Long.parseLong(procStats[7])
                    + Long.parseLong(procStats[8]);
            appTime = Long.parseLong(appStats[13]) + Long.parseLong(appStats[14]);
            if (mLastCpuTime == null && mLastAppCpuTime == null) {
                mLastCpuTime = cpuTime;
                mLastAppCpuTime = appTime;
                return sampleValue;
            }
            sampleValue = ((double) (appTime - mLastAppCpuTime) / (double) (cpuTime - mLastCpuTime)) * 100D;
            mLastCpuTime = cpuTime;
            mLastAppCpuTime = appTime;
        } catch (Exception e) {
            e.printStackTrace();
        }
        double freqRate = (double) getFreqValue(CUR_FREQ_PATH) / getFreqValue(MAX_FREQ_PATH);
        return sampleValue * freqRate;
    }

    public int getFreqValue(String type) {
        int value = 0;
        FileReader fr = null;
        BufferedReader br = null;
        try {
            fr = new FileReader(type);
            br = new BufferedReader(fr);
            String text = br.readLine();
            value = Integer.parseInt(text.trim());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fr != null) {
                try {
                    fr.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return value;
    }
}
