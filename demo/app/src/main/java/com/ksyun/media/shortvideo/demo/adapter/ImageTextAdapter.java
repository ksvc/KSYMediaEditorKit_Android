package com.ksyun.media.shortvideo.demo.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
    public static final int LONG_CLICK_STATE_START = 1;
    public static final int LONG_CLICK_STATE_CLICKING = 2;
    public static final int LONG_CLICK_STATE_END = 3;
    public static final int LONG_CLICK_IDEL = 0;
    private static final int UN_INIT = -1;
    private Context mContext;
    private List<Data> mData;
    private OnImageItemClickListener mListener;
    private FilterViewHolder mPreHolder;
    private int mPreIndex = UN_INIT;
    private OnImageLongItemClickListener mOnLongClick;
    private Handler mHandler;
    private boolean mIsLongClick;
    private int mLongClickState = LONG_CLICK_IDEL;

    public interface OnImageItemClickListener {
        void onClick(int index);
    }

    public interface OnImageLongItemClickListener {
        boolean onLongClick(View view, int index, int state);
    }


    public void setOnImageItemClick(OnImageItemClickListener listener) {
        this.mListener = listener;
    }

    public void setOnImageLongItemClick(OnImageLongItemClickListener longItemClick) {
        this.mOnLongClick = longItemClick;
    }

    public ImageTextAdapter(Context context, List<Data> data) {
        this.mContext = context;
        this.mData = data;
    }

    @Override
    public FilterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.image_text_view, parent, false);
        FilterViewHolder holder = new FilterViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(final FilterViewHolder holder, final int position) {
        final Data data = this.mData.get(position);
        if (data.isSelected) {
            holder.setActivated(true);
        } else {
            holder.setActivated(false);
        }
        holder.image.setImageDrawable(mData.get(position).drawable);
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener == null) return;
                if (mPreHolder != null) {
                    mPreHolder.setActivated(false);
                    ImageTextAdapter.this.mData.get(mPreIndex).isSelected = false;
                }
                holder.setActivated(true);
                mListener.onClick(position);
                ImageTextAdapter.this.mData.get(position).isSelected = true;
                mPreHolder = holder;
                mPreIndex = position;
            }
        });

        holder.image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (mOnLongClick == null) return false;

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler == null) mHandler = new Handler();
                        mHandler.postDelayed(new LongTimerRunnable(holder, view, position), 200);

                        break;
                    case MotionEvent.ACTION_MOVE:
                        if (mIsLongClick) {
                            holder.setActivated(true);
                        }
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        if (mHandler != null) {
                            mHandler.removeCallbacksAndMessages(null);
                        }
                        if (mIsLongClick) {
                            holder.setActivated(false);
                            mIsLongClick = false;
                            if (mLongClickState == LONG_CLICK_STATE_CLICKING) {
                                mLongClickState = LONG_CLICK_STATE_END;
                                if (mOnLongClick != null) {
                                    mOnLongClick.onLongClick(view, position, mLongClickState);
                                }
                                mLongClickState = LONG_CLICK_IDEL;
                            }
                            return true;
                        }
                        break;

                }
                return mIsLongClick;
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
            mPreHolder.setActivated(false);
        }
    }

    public void endLongClick() {
        //正在长按
        if (mLongClickState == LONG_CLICK_STATE_CLICKING) {
            // 取消长按状态
            cancelLongClick();
        }
    }

    private void cancelLongClick() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        if (mIsLongClick) {
            if (mPreHolder != null) {
                mPreHolder.setActivated(false);
            }
            mIsLongClick = false;
            mLongClickState = LONG_CLICK_STATE_END;
            if (mOnLongClick != null) {
                mOnLongClick.onLongClick(null, -1, mLongClickState);
            }
            mLongClickState = LONG_CLICK_IDEL;
        }
    }

    public class FilterViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        ImageView border;
        TextView title;

        public FilterViewHolder(View view) {
            super(view);
            this.image = (ImageView) view.findViewById(R.id.image_content);
            this.border = (ImageView) view.findViewById(R.id.image_border);
            this.title = (TextView) view.findViewById(R.id.title_text);
        }

        public void setActivated(boolean activated) {
            if (activated) {
                this.title.setActivated(true);
                this.border.setVisibility(View.VISIBLE);
            } else {
                this.title.setActivated(false);
                this.border.setVisibility(View.INVISIBLE);
            }
        }
    }

    public static class Data {
        public Drawable drawable;
        public String text;
        public boolean isSelected = false;

        public Data(Drawable image, String type) {
            this.drawable = image;
            this.text = type;
        }
    }

    class LongTimerRunnable implements Runnable {
        private View mView;
        private int mIndex;
        private FilterViewHolder mHolder;

        LongTimerRunnable(FilterViewHolder holder, View view, int index) {
            this.mView = view;
            this.mIndex = index;
            mHolder = holder;
        }

        @Override
        public void run() {
            if (mLongClickState == LONG_CLICK_IDEL) {
                mLongClickState = LONG_CLICK_STATE_START;
            } else if (mLongClickState == LONG_CLICK_STATE_START) {
                mLongClickState = LONG_CLICK_STATE_CLICKING;
            }
            boolean result = true;
            if (mOnLongClick != null) {
                result = mOnLongClick.onLongClick(mView, mIndex, mLongClickState);
            }
            if(result) {
                mIsLongClick = true;
                if (mIsLongClick) {
                    mHolder.setActivated(true);
                }

                if (mHandler != null) {
                    mHandler.postDelayed(new LongTimerRunnable(mHolder, mView, mIndex), 100);
                }
            } else {
                mLongClickState = LONG_CLICK_IDEL;
            }
        }
    }
}
