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
 * 滤镜RecyclerView的Adapter，添加单个item的事件监听
 */

public class ImageTextAdapter extends RecyclerView.Adapter<ImageTextAdapter.FilterViewHolder> {

    private Context mContext;
    private List<Data> mData;
    private OnImageItemClickListener mListener;
    private FilterViewHolder mPreHolder;

    public interface OnImageItemClickListener {
        void onClick(int index);
    }

    public void setOnImageItemClick(OnImageItemClickListener listener) {
        this.mListener = listener;
    }

    public ImageTextAdapter(Context context, List<Data> data) {
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public FilterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.filter_item_view, parent, false);
        FilterViewHolder holder = new FilterViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final FilterViewHolder holder, final int position) {
        holder.image.setImageDrawable(mData.get(position).drawable);
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPreHolder != null) {
                    mPreHolder.title.setActivated(false);
                    mPreHolder.border.setVisibility(View.INVISIBLE);
                }
                holder.title.setActivated(true);
                holder.border.setVisibility(View.VISIBLE);
                mListener.onClick(position);
                mPreHolder = holder;
            }
        });
        holder.title.setText(mData.get(position).text);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void clear() {
        if (mPreHolder != null) {
            mPreHolder.title.setActivated(false);
            mPreHolder.border.setVisibility(View.INVISIBLE);
        }
    }

    public class FilterViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageView border;
        TextView title;

        public FilterViewHolder(View view) {
            super(view);
            this.image = (ImageView) view.findViewById(R.id.filter_image);
            this.border = (ImageView) view.findViewById(R.id.filter_image_border);
            this.title = (TextView) view.findViewById(R.id.filter_text);
        }
    }

    public static class Data {
        public Drawable drawable;
        public String text;

        public Data(Drawable image, String type) {
            this.drawable = image;
            this.text = type;
        }
    }
}
