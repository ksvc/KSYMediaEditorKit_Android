package com.ksyun.media.shortvideo.demo.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ksyun.media.shortvideo.demo.R;

import java.util.List;

/**
 * EditActivity底部功能导航栏的RecyclerView的Adapter
 */

public class BottomTitleAdapter extends RecyclerView.Adapter<BottomTitleAdapter.MyViewHolder> {
    private Context mContext;
    private List<String> mData;
    private MyViewHolder mPreHolder;
    private int mPreIndex = -1;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onClick(int curIndex, int preIndex);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public BottomTitleAdapter(Context context, List<String> data) {
        mContext = context;
        mData = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.bottom_title_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        holder.setIsRecyclable(false);
        holder.title.setText(mData.get(position));
        if (mPreIndex == position) {
            mPreHolder = holder;
            holder.setActivated(true);
        }
        holder.title.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPreHolder != null && position != mPreHolder.getPosition()) {
                    mPreHolder.setActivated(false);
                }
                if (position == 2 && holder.title.isActivated()) {
                    holder.setActivated(false);
                } else {
                    holder.setActivated(true);
                }
                if (mListener != null) {
                    mListener.onClick(position, mPreIndex);
                }
                mPreHolder = holder;
                mPreIndex = position;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void clear() {
        if (mPreHolder != null) {
            mPreHolder.setActivated(false);
            mPreIndex = -1;
            mPreHolder = null;
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        View indicator;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.item_edit_title);
            indicator = view.findViewById(R.id.item_title_indicator);
        }

        public void setActivated(boolean active) {
            if (active) {
                title.setActivated(true);
                indicator.setActivated(true);
            } else {
                title.setActivated(false);
                indicator.setActivated(false);
            }
        }
    }
}
