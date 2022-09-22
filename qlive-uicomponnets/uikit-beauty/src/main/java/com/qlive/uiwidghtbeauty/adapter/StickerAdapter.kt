package com.qlive.uiwidghtbeauty.adapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.qlive.uiwidghtbeauty.model.StickerItem
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.qlive.uiwidghtbeauty.R
import com.qlive.uiwidghtbeauty.adapter.StickerAdapter.StickerViewHolder
import com.qlive.uiwidghtbeauty.model.EffectState
import com.qlive.uiwidghtbeauty.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception
import java.util.ArrayList

class StickerAdapter(var mStickerList: ArrayList<StickerItem>, var mContext: Context) :
    RecyclerView.Adapter<StickerViewHolder>() {
    private var mOnClickStickerListener: View.OnClickListener? = null
    private var mSelectedPosition = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StickerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sticker_item, null)
        return StickerViewHolder(view)
    }

    /**
     * loading 状态绑定
     *
     * @param stickerItem
     * @param holder
     * @param position
     */
    private fun bindState(stickerItem: StickerItem?, holder: StickerViewHolder?, position: Int) {
        if (stickerItem != null) {
            when (stickerItem.state) {

                EffectState.NORMAL_STATE ->                     //设置为等待下载状态
                    if (holder!!.normalState.visibility != View.VISIBLE) {
                        holder.normalState.visibility = View.VISIBLE
                        holder.downloadingState.visibility = View.INVISIBLE
                        holder.downloadingState.isActivated = false
                        holder.loadingStateParent.visibility = View.INVISIBLE
                    }
                EffectState.LOADING_STATE ->                     //设置为loading 状态
                    if (holder!!.downloadingState.visibility != View.VISIBLE) {
                        holder.normalState.visibility = View.INVISIBLE
                        holder.downloadingState.isActivated = true
                        holder.downloadingState.visibility = View.VISIBLE
                        holder.loadingStateParent.visibility = View.VISIBLE
                    }
                EffectState.DONE_STATE ->                     //设置为下载完成状态
                    if (holder!!.normalState.visibility != View.INVISIBLE || holder.downloadingState.visibility != View.INVISIBLE) {
                        holder.normalState.visibility = View.INVISIBLE
                        holder.downloadingState.visibility = View.INVISIBLE
                        holder.downloadingState.isActivated = false
                        holder.loadingStateParent.visibility = View.INVISIBLE
                    }
                else -> {}
            }
        }
    }

    override fun onBindViewHolder(holder: StickerViewHolder, position: Int) {
        val imgUrl = mStickerList[position].icon
        if (imgUrl.scheme!!.startsWith("http")) {
            GlobalScope.launch(Dispatchers.Main) {
                val bmJob = async(Dispatchers.IO) {
                    var bitmap: Bitmap? = null
                    try {
                        bitmap = Utils.getImageSync(imgUrl.toString(), holder.view.context)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    if (bitmap == null) {
                        bitmap =
                            BitmapFactory.decodeResource(
                                holder.view.context.resources,
                                R.drawable.none
                            )
                    }
                    bitmap!!
                }
                holder.imageView.setImageBitmap(bmJob.await())
            }
        } else {
            holder.imageView.setImageURI(imgUrl)
        }
        bindState(getItem(position), holder, position)
        holder.view.isSelected = mSelectedPosition == position
        if (mOnClickStickerListener != null) {
            holder.view.tag = position
            holder.view.setOnClickListener(mOnClickStickerListener)
        }
    }

    fun setClickStickerListener(listener: View.OnClickListener?) {
        mOnClickStickerListener = listener
    }

    fun getItem(position: Int): StickerItem? {
        return if (position in 0 until itemCount) {
            mStickerList[position]
        } else null
    }

    override fun getItemCount(): Int {
        return mStickerList.size
    }

    class StickerViewHolder(var view: View) : ViewHolder(
        view
    ) {
        var imageView: ImageView = itemView.findViewById<View>(R.id.icon) as ImageView
        var normalState: ImageView = itemView.findViewById<View>(R.id.normalState) as ImageView
        var downloadingState: ImageView =
            itemView.findViewById<View>(R.id.downloadingState) as ImageView
        var loadingStateParent: ViewGroup =
            itemView.findViewById<View>(R.id.loadingStateParent) as ViewGroup
    }

    fun setSelectedPosition(position: Int) {
        mSelectedPosition = position
    }
}