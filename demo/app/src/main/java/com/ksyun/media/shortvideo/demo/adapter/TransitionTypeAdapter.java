package com.ksyun.media.shortvideo.demo.adapter;

import com.ksyun.media.shortvideo.demo.R;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * transition type adapter
 */

public class TransitionTypeAdapter extends RecyclerView.Adapter<TransitionTypeAdapter.MyViewHolder> {
    private Context mContext;
    private List<String> mData;
    private MyViewHolder mPreHolder;
    private MyViewHolder mFirstHolder;
    private int mPreIndex = 0;
    private OnItemClickListener mListener;

    public interface OnItemClickListener {
        void onClick(int curIndex, int preIndex);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mListener = listener;
    }

    public TransitionTypeAdapter(Context context, List<String> data) {
        super();
        mContext = context;
        mData = data;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.transtion_type_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {
        if (position == 0) {
            mFirstHolder = holder;
        }
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

                holder.setActivated(true);

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
        }

        if (mFirstHolder != null) {
            mPreHolder = mFirstHolder;
            mPreIndex = 0;
        }
    }

    public void selectView(RecyclerView.ViewHolder holder, int index) {
        if (holder instanceof MyViewHolder) {
            MyViewHolder viewHolder = (MyViewHolder) holder;
            if (mPreHolder != null) {
                mPreHolder.setActivated(false);
            }
            viewHolder.setActivated(true);

            mPreHolder = viewHolder;
            mPreIndex = index;
        }
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title;

        public MyViewHolder(View view) {
            super(view);
            title = view.findViewById(R.id.type_title);
        }

        public void setActivated(boolean active) {
            if (active) {
                title.setActivated(true);
            } else {
                title.setActivated(false);
            }
        }
    }
}
