package com.ksyun.media.shortvideo.demo.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ksyun.media.shortvideo.demo.R;

import java.util.List;

/**
 * 变声混响适配器.
 */

public class SoundEffectAdapter extends RecyclerView.Adapter<SoundEffectAdapter.SoundEffectViewHolder> {
    private Context mContext;
    private List<SoundEffectData> mData;
    private OnItemClickListener mListener;
    private SoundEffectViewHolder mPreHolder;

    private static final int INDEX_CANCEL = 0;

    public SoundEffectAdapter(Context context, List<SoundEffectData> data) {
        this.mContext = context;
        this.mData = data;
    }

    public interface OnItemClickListener {
        void onCancel();

        void onSelected(int index);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    @Override
    public SoundEffectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.image_text_view, parent, false);
        SoundEffectViewHolder holder = new SoundEffectViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final SoundEffectViewHolder holder, final int position) {
        SoundEffectData data = mData.get(position);
        holder.image.setImageDrawable(data.drawable);
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPreHolder != null) {
                    mPreHolder.setActivated(false);
                }
                if (position == INDEX_CANCEL) {
                    if (mListener != null) {
                        mListener.onCancel();
                    }
                } else {
                    holder.setActivated(true);
                    if (mListener != null) {
                        mListener.onSelected(position);
                    }
                    mPreHolder = holder;
                }
            }
        });
        if (position != INDEX_CANCEL) {
            holder.text.setText(data.text);
        }
    }

    public void clear() {
        if (mPreHolder != null) {
            mPreHolder.setActivated(false);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public class SoundEffectViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public ImageView border;
        public TextView text;

        public SoundEffectViewHolder(View view) {
            super(view);
            image = (ImageView) view.findViewById(R.id.image_content);
            border = (ImageView) view.findViewById(R.id.image_border);
            text = (TextView) view.findViewById(R.id.title_text);
        }

        public void setActivated(boolean activated) {
            if (activated) {
                border.setVisibility(View.VISIBLE);
                text.setActivated(true);
            } else {
                border.setVisibility(View.INVISIBLE);
                text.setActivated(false);
            }
        }
    }

    public static class SoundEffectData {
        public Drawable drawable;
        public String text;

        public SoundEffectData(Drawable image, String type) {
            this.drawable = image;
            this.text = type;
        }
    }
}
