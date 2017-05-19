package com.ksyun.media.shortvideo.demo.filter;

import com.ksyun.media.streamer.filter.imgtex.ImgTexFilter;
import com.ksyun.media.streamer.util.gles.GLRender;
import com.ksyun.media.streamer.util.gles.TexTransformUtil;

import java.nio.FloatBuffer;

/**
 * for vertical rotate
 */

public class ImgYFlipFilter extends ImgTexFilter {
    private FloatBuffer mTexCoordsBuf = TexTransformUtil.getVFlipTexCoordsBuf();

    public ImgYFlipFilter(GLRender glRender) {
        super(glRender);
    }

    @Override
    protected FloatBuffer getTexCoords() {
        return mTexCoordsBuf;
    }
}
