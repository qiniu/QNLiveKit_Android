package com.qlive.uiwidghtbeauty.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.BitmapFactory
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.qlive.uiwidghtbeauty.QSenseTimeManager.sSenseTimePlugin
import com.qlive.uiwidghtbeauty.model.StickerOptionsItem
import com.qlive.uiwidghtbeauty.adapter.StickerOptionsAdapter
import androidx.viewpager.widget.ViewPager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.qlive.uiwidghtbeauty.model.StickerItem
import com.qlive.uiwidghtbeauty.R
import com.qlive.uiwidghtbeauty.utils.Constants
import java.util.*
import kotlin.collections.ArrayList

class StickerView : FrameLayout {

    private val mStickerOptionsList by lazy {
        ArrayList<StickerOptionsItem>().apply {
            add(
                0,
                StickerOptionsItem(
                    Constants.STICKER_NEW_ENGINE,
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.sticker_local_unselected
                    ),
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.sticker_local_selected
                    )
                )
            )
            // 2d
            add(
                1,
                StickerOptionsItem(
                    Constants.GROUP_2D,
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.sticker_2d_unselected
                    ),
                    BitmapFactory.decodeResource(context.resources, R.drawable.sticker_2d_selected)
                )
            )
            // 3d
            add(
                2,
                StickerOptionsItem(
                    Constants.GROUP_3D,
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.sticker_3d_unselected
                    ),
                    BitmapFactory.decodeResource(context.resources, R.drawable.sticker_3d_selected)
                )
            )
            // 手势贴纸
            add(
                3,
                StickerOptionsItem(
                    Constants.GROUP_HAND,
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.sticker_hand_action_unselected
                    ),
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.sticker_hand_action_selected
                    )
                )
            )
            // 背景贴纸
            add(
                4,
                StickerOptionsItem(
                    Constants.GROUP_BG,
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.sticker_bg_segment_unselected
                    ),
                    BitmapFactory.decodeResource(
                        context.resources,
                        R.drawable.sticker_bg_segment_selected
                    )
                )
            )
        }
    }
    private val mStickerOptionsAdapter by lazy {
        StickerOptionsAdapter(mStickerOptionsList, context)
    }

    private val pageItemViewList by lazy {
        ArrayList<StickerPageItemView>().apply {
            add(StickerPageItemView(context, Constants.NEW_ENGINE, Constants.STICKER_NEW_ENGINE))
            add(StickerPageItemView(context, "2D", Constants.GROUP_2D))
            add(StickerPageItemView(context, "3D", Constants.GROUP_3D))
            add(StickerPageItemView(context, "hand_action", Constants.GROUP_HAND))
            add(StickerPageItemView(context, "segment", Constants.GROUP_BG))
        }
    }

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init(context)
    }

    private lateinit var vpStickerPage: ViewPager

    @SuppressLint("NotifyDataSetChanged")
    private fun init(context: Context) {
        LayoutInflater.from(context).inflate(R.layout.kit_sticker_view, this, true)
        vpStickerPage = findViewById(R.id.vp_sticker_page)
        // 贴纸相关视图
        val mStickerOptionsRecycleView = findViewById<RecyclerView>(R.id.rv_sticker_options)
        mStickerOptionsRecycleView.layoutManager =
            StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
        mStickerOptionsRecycleView.addItemDecoration(SpaceItemDecoration(0))

        vpStickerPage.adapter = CommonViewPagerAdapter(pageItemViewList)
        for (stickerPageItemView in pageItemViewList) {
            stickerPageItemView.attach(stickerPageItemView.assetsIndex == Constants.NEW_ENGINE)
            stickerPageItemView.onStickerItemClick =
                { s: String?, groupIndex: String, stickerItem: StickerItem, integer: Int ->
                    sSenseTimePlugin?.setSticker(stickerItem.path)
                    setSelect(integer, groupIndex)
                }
        }
        // 当点击关闭贴纸时移除贴纸，并将视图和记录状态复原
        findViewById<View>(R.id.rv_close_sticker).setOnClickListener { v: View? ->
            // 重置所有状态为未选中状态
            setSelect(-1, "xxx")
            sSenseTimePlugin?.setSticker("")
        }

        mStickerOptionsAdapter.setSelectedPosition(0)
        mStickerOptionsAdapter.notifyDataSetChanged()
        mStickerOptionsRecycleView.adapter = mStickerOptionsAdapter
        initStickerTabListener()
        if (mStickerOptionsRecycleView.adapter == null) {
            mStickerOptionsRecycleView.adapter = mStickerOptionsAdapter
        }
    }

    private fun setSelect(index: Int, groupIndex: String) {
        for (stickerPageItemView in pageItemViewList) {
            stickerPageItemView.setSelect(index, groupIndex)
        }
    }

    /**
     * 初始化 tab 点击事件
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun initStickerTabListener() {
        // tab 切换事件订阅
        mStickerOptionsAdapter.setClickStickerListener(OnClickListener { v ->
            val position = v.tag.toString().toInt()
            mStickerOptionsAdapter.setSelectedPosition(position)
            mStickerOptionsAdapter.notifyDataSetChanged()
            vpStickerPage.setCurrentItem(position, true)
        })
    }

    companion object {
        private const val TAG = "QSenseBeautyView"
    }
}