package com.qlive.uiwidghtbeauty.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.viewpager.widget.ViewPager
import com.qlive.uiwidghtbeauty.QSenseTimeManager
import com.qlive.uiwidghtbeauty.R
import com.qlive.uiwidghtbeauty.adapter.BeautyOptionsAdapter
import com.qlive.uiwidghtbeauty.model.BeautyOptionsItem

class EffectView : FrameLayout {

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        LayoutInflater.from(context).inflate(R.layout.kir_layout_effect_view, this, true)
        init()
    }
    private val beautyOptionsList = ArrayList<BeautyOptionsItem>().apply {
        add(0, BeautyOptionsItem("基础美颜"))
        add(1, BeautyOptionsItem("美形"))
        add(2, BeautyOptionsItem("微整形"))
        add(3, BeautyOptionsItem("美妆"))
        add(4, BeautyOptionsItem("滤镜"))
        add(5, BeautyOptionsItem("调整"))
    }
    private val pages: List<View> by lazy {
        listOf<View>(
            BaseBeautyPage(context),
            ProfessionalBeautyPage(context),
            MicroBeautyPage(context),
            MakeUpBeautyPage(context),
            FilterBeautyPage(context),
            AdjustBeautyPage(context),
        )
    }

    private val mBeautyOptionsAdapter: BeautyOptionsAdapter by lazy {
        BeautyOptionsAdapter(beautyOptionsList, context);
    }

    @SuppressLint("NotifyDataSetChanged", "ClickableViewAccessibility")
    private fun init() {
        // 美颜面板上的滑动选项
        val beautyOptionsRecycleView = findViewById<RecyclerView>(R.id.rv_beauty_options)
        beautyOptionsRecycleView.layoutManager =
            StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
        beautyOptionsRecycleView.addItemDecoration(SpaceItemDecoration(0))

        val vpEffect = findViewById<ViewPager>(R.id.vpEffect)
        vpEffect.adapter = CommonViewPagerAdapter(pages)

        beautyOptionsRecycleView.adapter = mBeautyOptionsAdapter
        mBeautyOptionsAdapter.setClickBeautyListener { v ->
            val position: Int = v.tag.toString().toInt()
            vpEffect.setCurrentItem(position, true)
            mBeautyOptionsAdapter.setSelectedPosition(position)
            mBeautyOptionsAdapter.notifyDataSetChanged()
        }
        findViewById<View>(R.id.reset).setOnClickListener {
            pages.forEach {
                (it as BaseEffectPage<*>).reset()
            }
        }
        findViewById<View>(R.id.tv_show_origin).setOnTouchListener { view, event ->
            val action = event.action
            if (action == MotionEvent.ACTION_DOWN) {
                QSenseTimeManager.sSenseTimePlugin?.setEffectEnabled(false)
            } else if (action == MotionEvent.ACTION_UP) {
                QSenseTimeManager.sSenseTimePlugin?.setEffectEnabled(true)
            }
            true
        }
    }
}