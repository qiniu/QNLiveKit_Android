package com.qlive.uiwidghtbeauty.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;


import com.bumptech.glide.Glide;
import com.qlive.uiwidghtbeauty.R;
import com.qlive.uiwidghtbeauty.model.MakeupItem;

import java.util.List;

public class MakeupAdapter extends RecyclerView.Adapter {

    List<MakeupItem> mMakeupList;
    private View.OnClickListener mOnClickMakeupListener;
    private int mSelectedPosition = 0;
    Context mContext;

    public MakeupAdapter(List<MakeupItem> list, Context context) {
        mMakeupList = list;
        mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.makeup_item, null);
        return new MakeupViewHolder(view);
    }

    private void bindState(MakeupItem makeupItem, MakeupViewHolder holder) {
        if (makeupItem != null) {
            switch (makeupItem.state) {
                case NORMAL_STATE:
                    // 设置为等待下载状态
                    if (holder.normalState.getVisibility() != View.VISIBLE) {
                        holder.normalState.setVisibility(View.VISIBLE);
                        holder.downloadingState.setVisibility((View.INVISIBLE));
                        holder.downloadingState.setActivated(false);
                        holder.loadingStateParent.setVisibility((View.INVISIBLE));
                    }
                    break;
                case LOADING_STATE:
                    // 设置为loading 状态
                    if (holder.downloadingState.getVisibility() != View.VISIBLE) {
                        holder.normalState.setVisibility(View.INVISIBLE);
                        holder.downloadingState.setActivated(true);
                        holder.downloadingState.setVisibility((View.VISIBLE));
                        holder.loadingStateParent.setVisibility((View.VISIBLE));
                    }

                    break;
                case DONE_STATE:
                    // 设置为下载完成状态
                    if (holder.normalState.getVisibility() != View.INVISIBLE || holder.downloadingState.getVisibility() != View.INVISIBLE) {
                        holder.normalState.setVisibility(View.INVISIBLE);
                        holder.downloadingState.setVisibility((View.INVISIBLE));
                        holder.downloadingState.setActivated(false);
                        holder.loadingStateParent.setVisibility((View.INVISIBLE));
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final MakeupViewHolder viewHolder = (MakeupViewHolder) holder;
        bindState(getItem(position), viewHolder);
      //  viewHolder.imageView.setNeedBorder(false);
        Glide.with(mContext)
                .load(mMakeupList.get(position).icon)
                .into( viewHolder.imageView);
        viewHolder.textView.setText(mMakeupList.get(position).name);
        viewHolder.textView.setTextColor(Color.parseColor("#ffffff"));

        holder.itemView.setSelected(mSelectedPosition == position);

//        if(mSelectedPosition == position){
//            viewHolder.imageView.setNeedBorder(true);
//        }

        if(mOnClickMakeupListener != null) {
            holder.itemView.setTag(position);
            holder.itemView.setOnClickListener(mOnClickMakeupListener);
        }
    }

    public void setClickMakeupListener(View.OnClickListener listener) {
        mOnClickMakeupListener = listener;
    }

    public MakeupItem getItem(int position) {
        if (position >= 0 && position < getItemCount()) {
            return mMakeupList.get(position);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return mMakeupList.size();
    }

    static class MakeupViewHolder extends RecyclerView.ViewHolder {
        View view;
        ImageView imageView;
        ImageView normalState;
        ImageView downloadingState;
        ViewGroup loadingStateParent;
        TextView textView;

        public MakeupViewHolder(View itemView) {
            super(itemView);
            view = itemView;
            imageView = itemView.findViewById(R.id.iv_makeup_image);
            normalState = itemView.findViewById(R.id.normalState);
            downloadingState = itemView.findViewById(R.id.downloadingState);
            loadingStateParent = itemView.findViewById(R.id.loadingStateParent);
            textView = itemView.findViewById(R.id.makeup_text);
        }
    }

    public void setSelectedPosition(int position){
        mSelectedPosition = position;
    }
}
