package com.ksyun.media.shortvideo.demo.util;

/**
 * 获取应用CPU占用工具类
 */

import android.os.Process;

import java.io.RandomAccessFile;

public class SystemStateObtainUtil {
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
        return sampleValue;
    }
}
