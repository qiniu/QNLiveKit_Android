package com.qlive.uiwidghtbeauty.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.qlive.uiwidghtbeauty.R;
import com.qlive.uiwidghtbeauty.model.BeautyItem;

import java.util.ArrayList;

public class BeautyItemAdapter extends RecyclerView.Adapter {

    ArrayList<BeautyItem> mBeautyItem;
    private View.OnClickListener mOnClickBeautyItemListener;
    private int mSelectedPosition = 0;
    Context mContext;

    public BeautyItemAdapter(Context context, ArrayList<BeautyItem> list) {
        mContext = context;
        mBeautyItem = list;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.beauty_item, null);
        return new BeautyItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final BeautyItemViewHolder viewHolder = (BeautyItemViewHolder) holder;
        viewHolder.mName.setText(mBeautyItem.get(position).getText());
        viewHolder.mSubscription.setText(mBeautyItem.get(position).getProgress() + "");
        viewHolder.mName.setTextColor(Color.parseColor("#ffffff"));
        viewHolder.mSubscription.setTextColor(Color.parseColor("#ffffff"));
        viewHolder.mImage.setImageBitmap(mBeautyItem.get(position).getUnselectedtIcon());
        holder.itemView.setSelected(mSelectedPosition == position);
        if (mSelectedPosition == position) {
            viewHolder.mSubscription.setTextColor(Color.parseColor("#bc47ff"));
            viewHolder.mName.setTextColor(Color.parseColor("#bc47ff"));
            viewHolder.mImage.setImageBitmap(mBeautyItem.get(position).getSelectedIcon());
        }
        if (mOnClickBeautyItemListener != null) {
            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(mOnClickBeautyItemListener);
            holder.itemView.setSelected(mSelectedPosition == position);

        }
    }

    @Override
    public int getItemCount() {
        return mBeautyItem.size();
    }

    public void setSelectedPosition(int position) {
        mSelectedPosition = position;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    public void setClickBeautyListener(View.OnClickListener listener) {
        mOnClickBeautyItemListener = listener;
    }

    static class BeautyItemViewHolder extends RecyclerView.ViewHolder {

        View view;
        ImageView mImage;
        TextView mName;
        TextView mSubscription;

        public BeautyItemViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            mName = itemView.findViewById(R.id.beauty_item_description);
            mSubscription = itemView.findViewById(R.id.beauty_item_subscription);
            mImage = itemView.findViewById(R.id.beauty_item_iv);
        }
    }
}
