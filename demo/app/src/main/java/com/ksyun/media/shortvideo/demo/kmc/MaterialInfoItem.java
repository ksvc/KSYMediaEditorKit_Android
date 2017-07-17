package com.ksyun.media.shortvideo.demo.kmc;

import android.graphics.Bitmap;

import com.ksyun.media.kmcfilter.KMCArMaterial;

public class MaterialInfoItem {
    public Bitmap thumbnail;
    public KMCArMaterial material;
    public boolean hasDownload = false;

    public MaterialInfoItem(KMCArMaterial material, Bitmap thumbnail) {
        this.material = material;
        this.thumbnail = thumbnail;
    }

    public void setHasDownload(boolean isDownloaded) {
        this.hasDownload = isDownloaded;
    }
}
