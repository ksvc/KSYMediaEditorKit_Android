package com.ksyun.media.shortvideo.demo.adapter;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ksyun.media.shortvideo.demo.R;

import java.util.List;

/**
 * mv展示列表适配器
 */

public class MVTypeListAdapter extends RecyclerView.Adapter<MVTypeListAdapter.MyViewHolder> {

    private List<MVData> mData;
    private OnItemClickListener mListener;
    private MyViewHolder mPreHolder;

    public MVTypeListAdapter(List<MVData> data) {
        mData = data;
    }

    public interface OnItemClickListener {
        void onClick(MVData data);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mListener = listener;
    }

    public void addMVData(MVData data) {
        if (!mData.contains(data)) {
            mData.add(data);
            notifyDataSetChanged();
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bgm_item_view, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        //invisible download in mv ver 1.0
        holder.mBorder.setVisibility(View.INVISIBLE);
        holder.mDownload.setVisibility(View.INVISIBLE);

        holder.mContent.setImageDrawable(mData.get(position).drawable);
        holder.mText.setText(mData.get(position).text);
        holder.mContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPreHolder != null) {
                    mPreHolder.setActivated(false);
                }
                holder.setActivated(true);
                if (mListener != null) {
                    mPreHolder = (MyViewHolder) v.getTag();
                    mListener.onClick(mData.get(position));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void clearState() {
        if (mPreHolder != null) {
            mPreHolder.setActivated(false);
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView mContent;
        public ImageView mBorder;
        public ImageView mDownload;
        public ProgressBar mProgress;
        public TextView mText;

        public MyViewHolder(View view) {
            super(view);
            mContent = (ImageView) view.findViewById(R.id.bgm_image_content);
            mBorder = (ImageView) view.findViewById(R.id.bgm_image_border);
            mDownload = (ImageView) view.findViewById(R.id.bgm_image_download);
            mProgress = (ProgressBar) view.findViewById(R.id.download_progress);
            mText = (TextView) view.findViewById(R.id.bgm_name);
            mContent.setTag(this);
        }

        public void setActivated(boolean active) {
            if (active) {
                mBorder.setVisibility(View.VISIBLE);
                mText.setActivated(true);
            } else {
                mBorder.setVisibility(View.GONE);
                mText.setActivated(false);
            }
        }
    }

    public static class MVData {
        public Drawable drawable;
        public String text;

        public MVData(Drawable image, String type) {
            this.drawable = image;
            this.text = type;
        }
    }
}
