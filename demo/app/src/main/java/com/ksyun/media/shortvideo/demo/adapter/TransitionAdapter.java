package com.ksyun.media.shortvideo.demo.adapter;

import com.ksyun.media.shortvideo.demo.R;
import com.ksyun.media.shortvideo.utils.ProbeMediaInfoTools;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Transition adapter
 */

public class TransitionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private List<String> mImagePathList = new ArrayList<String>();// 文件路径列表
    private ProbeMediaInfoTools probeMediaInfoTools = new ProbeMediaInfoTools();
    private OnTransitionItemListener mTranstionItemListener;

    private ImageHolder mPreHolder;
    private int mPreIndex;

    public TransitionAdapter(Context context, List<String> path) {
        super();
        mContext = context;
        mImagePathList = path;
    }

    public void setTranstionItemListener(OnTransitionItemListener listener) {
        mTranstionItemListener = listener;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.transtion_item, parent, false);
        ImageHolder holer = new ImageHolder(v);
        return holer;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final ImageHolder imageHolder = (ImageHolder) holder;
        String path = mImagePathList.get(position);
        Bitmap bitmap = probeMediaInfoTools.getVideoThumbnailAtTime(path, 0, 0, 0, false);
        Drawable drawable = getImage(bitmap);
        imageHolder.image.setImageDrawable(drawable);
        imageHolder.image.setTag(path);

        if (mPreIndex == position) {
            mPreHolder = imageHolder;
            mPreHolder.setActivated(true);
        }

        //当前版本暂时不处理片尾转场
        if (position == (mImagePathList.size() - 1)) {
            imageHolder.mTranstionOn.setVisibility(View.GONE);
        } else {
            imageHolder.mTranstionOn.setVisibility(View.VISIBLE);
            imageHolder.mTranstionOn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPreHolder != null && position != mPreHolder.getPosition()) {
                        mPreHolder.setActivated(false);
                    }

                    imageHolder.setActivated(true);

                    if (mTranstionItemListener != null) {
                        mTranstionItemListener.transitionSelected(position);
                    }

                    mPreHolder = imageHolder;
                    mPreIndex = position;
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mImagePathList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return 1;
    }

    public class ImageHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public ImageView mTranstionOn;

        public ImageHolder(View itemView) {
            super(itemView);
            this.image = (ImageView) itemView.findViewById(R.id.video_thumbnail);
            this.mTranstionOn = (ImageView) itemView.findViewById(R.id.transtion_on_click);
        }

        public void setActivated(boolean active) {
            if (active) {
                mTranstionOn.setActivated(true);
            } else {
                mTranstionOn.setActivated(false);
            }
        }
    }

    private Drawable getImage(Bitmap bitMap) {
        if (bitMap != null) {
            int width = bitMap.getWidth();
            int height = bitMap.getHeight();
            // 设置想要的大小
            int size = 400;
            int newWidth = size;
            int newHeight = size;
            // 计算缩放比例
            float scaleWidth = ((float) newWidth) / width;
            float scaleHeight = ((float) newHeight) / height;
            // 取得想要缩放的matrix参数
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            // 得到新的图片
            bitMap = Bitmap.createBitmap(bitMap, 0, 0, width, height, matrix, true);
            return new BitmapDrawable(bitMap);
        }
        return null;
    }

    public interface OnTransitionItemListener {
        void transitionSelected(int index);
    }
}
