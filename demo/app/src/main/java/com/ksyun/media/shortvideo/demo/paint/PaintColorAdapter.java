package com.ksyun.media.shortvideo.demo.paint;

import com.ksyun.media.shortvideo.demo.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * for paint color select
 */

public class PaintColorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context mContext;
    private List<Integer> mColorList = new ArrayList<>();
    private PaintMenu.PaintColorSelect mPaintSelect;
    private int mSelectedPos = 0;
    private ColorViewHolder mSelectHolder;
    public PaintColorAdapter(Context context) {
        mContext = context;
        mColorList = initColors();
    }
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.paint_color_item, parent, false);
        ColorViewHolder colorViewHolder = new ColorViewHolder(view);
        return colorViewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ColorViewHolder viewHolder = (ColorViewHolder) holder;
        viewHolder.colorImage.setColorFilter(mColorList.get(position));
        viewHolder.colorImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mPaintSelect != null) {
                    mPaintSelect.onColorSelected(mColorList.get(position));
                    viewHolder.colorImage.setSelected(true);
                    if(mSelectHolder != null) {
                        mSelectHolder.colorImage.setSelected(false);
                    }
                    mSelectedPos = position;
                    mSelectHolder = viewHolder;
                }
            }
        });
        if(mSelectedPos == position) {
            viewHolder.colorImage.setSelected(true);
            mSelectHolder = viewHolder;
        } else {
            viewHolder.colorImage.setSelected(false);
        }

    }

    @Override
    public int getItemCount() {
        return mColorList.size();
    }

    private static class ColorViewHolder extends  RecyclerView.ViewHolder {

        private ImageView colorImage;
        public ColorViewHolder(View itemView) {
            super(itemView);
            colorImage = (ImageView) itemView.findViewById(R.id.paint_color_image);
        }
    }

    private List<Integer> initColors() {
        List<Integer> list = new ArrayList<>();
        TypedArray colors = mContext.getResources().obtainTypedArray(R.array.paint_colors);

        int size = colors.length();
        for (int i = 0; i < size; i++) {
            int color = colors.getColor(i, Color.WHITE);
            list.add(color);
        }
        colors.recycle();
        return list;
    }

    public void setPaintSelect(PaintMenu.PaintColorSelect paintSelect) {
        this.mPaintSelect = paintSelect;
    }

    public void setSelectedPos(int color) {
        int r = mColorList.indexOf(color);
        if(r < 0) {
            return;
        }
        mSelectedPos = r;
    }

    public int getSelectedPos() {
        return this.mSelectedPos;
    }
}