package com.ksyun.media.shortvideo.demo.media;

/**
 * 媒体信息数据类
 */

public class MediaInfo {
    public String filePath;
    public String mimeType;
    public String thumbPath;
    public String title;
    public int id;
    public int duration;
    public int type;

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof MediaInfo) {
            MediaInfo info = (MediaInfo) obj;
            return id == info.id;
        }
        return false;
    }
}
