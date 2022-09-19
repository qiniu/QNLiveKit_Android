package com.qlive.uiwidghtbeauty.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.qlive.uiwidghtbeauty.QSenseTimeManager.sSenseTimePlugin
import com.qlive.uiwidghtbeauty.R
import com.qlive.uiwidghtbeauty.adapter.FilterAdapter
import com.qlive.uiwidghtbeauty.model.FilterItem
import com.qlive.uiwidghtbeauty.utils.Constants
import com.qlive.uiwidghtbeauty.utils.ResourcesUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class FilterBeautyPage : FrameLayout, BaseEffectPage<FilterItem> {

    override var onItemClick: (groupIndex: String, item: FilterItem, itemIndex: Int) -> Unit =
        { _, _, _ ->
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        LayoutInflater.from(context).inflate(R.layout.kit_filter_page, this, true)
        init()
    }

    private lateinit var mFilterOptionsRecycleView: RecyclerView
    private var mFilterAdapters = HashMap<String, FilterAdapter>()
    private var mFilterListMap = HashMap<String, ArrayList<FilterItem>>()
    private var mCurrentFilterGroupIndex = -1
    private var mCurrentFilterIndex = -1

    private lateinit var mFilterGroupsLinearLayout: LinearLayout
    private lateinit var mFilterIconsRelativeLayout: RelativeLayout
    private lateinit var mFilterGroupBack: ImageView
    private lateinit var mFilterGroupName: TextView
    private lateinit var mFilterStrengthLayout: View
    private lateinit var mFilterStrengthBar: SeekBar
    private lateinit var mFilterStrengthText: TextView

    private fun init() {
        GlobalScope.launch(Dispatchers.Main) {
            val ret = async(Dispatchers.IO){
                mFilterListMap=ResourcesUtil.getFilterListMap(context)
            }
            ret.await()
            // 滤镜相关
            // 滤镜相关
            mFilterAdapters = java.util.HashMap()
            mFilterAdapters[Constants.FILTER_PORTRAIT] =
                FilterAdapter(mFilterListMap[Constants.FILTER_PORTRAIT], context)
            mFilterAdapters[Constants.FILTER_SCENERY] = FilterAdapter(
                mFilterListMap[Constants.FILTER_SCENERY], context
            )
            mFilterAdapters[Constants.FILTER_STILL_LIFE] =
                FilterAdapter(mFilterListMap[Constants.FILTER_STILL_LIFE], context)
            mFilterAdapters[Constants.FILTER_FOOD] = FilterAdapter(
                mFilterListMap[Constants.FILTER_FOOD], context
            )
            mFilterOptionsRecycleView = findViewById(R.id.rv_filter_icons)
            mFilterOptionsRecycleView.layoutManager =
                StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
            mFilterOptionsRecycleView.addItemDecoration(SpaceItemDecoration(0))

            mFilterIconsRelativeLayout = findViewById(R.id.rl_filter_icons)
            mFilterGroupsLinearLayout = findViewById(R.id.ll_filter_groups)
            val filterGroupPortrait = findViewById<LinearLayout>(R.id.ll_filter_group_portrait)
            filterGroupPortrait.setOnClickListener {
                mFilterGroupsLinearLayout.visibility = INVISIBLE
                mFilterIconsRelativeLayout.visibility = VISIBLE
                if (mCurrentFilterGroupIndex == 0 && mCurrentFilterIndex != -1) {
                    mFilterStrengthLayout.visibility = VISIBLE
                }
                mFilterOptionsRecycleView.layoutManager =
                    StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
                mFilterOptionsRecycleView.adapter =
                    mFilterAdapters[Constants.FILTER_PORTRAIT]
                mFilterGroupBack.setImageDrawable(resources.getDrawable(R.drawable.icon_portrait_selected))
                mFilterGroupName.text = "人像"
            }
            val filterGroupScenery = findViewById<LinearLayout>(R.id.ll_filter_group_scenery)
            filterGroupScenery.setOnClickListener {
                mFilterGroupsLinearLayout.visibility = INVISIBLE
                mFilterIconsRelativeLayout.visibility = VISIBLE
                if (mCurrentFilterGroupIndex == 1 && mCurrentFilterIndex != -1) {
                    mFilterStrengthLayout.visibility = VISIBLE
                }
                mFilterOptionsRecycleView.layoutManager =
                    StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
                mFilterOptionsRecycleView.adapter =
                    mFilterAdapters[Constants.FILTER_SCENERY]
                mFilterGroupBack.setImageDrawable(resources.getDrawable(R.drawable.icon_scenery_selected))
                mFilterGroupName.text = "风景"
            }
            val filterGroupStillLife = findViewById<LinearLayout>(R.id.ll_filter_group_still_life)
            filterGroupStillLife.setOnClickListener {
                mFilterGroupsLinearLayout.visibility = INVISIBLE
                mFilterIconsRelativeLayout.visibility = VISIBLE
                if (mCurrentFilterGroupIndex == 2 && mCurrentFilterIndex != -1) {
                    mFilterStrengthLayout.visibility = VISIBLE
                }
                mFilterOptionsRecycleView.layoutManager =
                    StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
                mFilterOptionsRecycleView.adapter =
                    mFilterAdapters[Constants.FILTER_STILL_LIFE]
                mFilterGroupBack.setImageDrawable(resources.getDrawable(R.drawable.icon_still_life_selected))
                mFilterGroupName.text = "静物"
            }
            val filterGroupFood = findViewById<LinearLayout>(R.id.ll_filter_group_food)
            filterGroupFood.setOnClickListener {
                mFilterGroupsLinearLayout.visibility = INVISIBLE
                mFilterIconsRelativeLayout.visibility = VISIBLE
                if (mCurrentFilterGroupIndex == 3 && mCurrentFilterIndex != -1) {
                    mFilterStrengthLayout.visibility = VISIBLE
                }
                mFilterOptionsRecycleView.layoutManager =
                    StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
                mFilterOptionsRecycleView.adapter =
                    mFilterAdapters[Constants.FILTER_FOOD]
                mFilterGroupBack.setImageDrawable(resources.getDrawable(R.drawable.icon_food_selected))
                mFilterGroupName.text = "食物"
            }

            mFilterGroupBack = findViewById(R.id.iv_filter_group)
            mFilterGroupBack.setOnClickListener { v: View? ->
                mFilterGroupsLinearLayout.visibility = VISIBLE
                mFilterIconsRelativeLayout.visibility = INVISIBLE
                mFilterStrengthLayout.visibility = INVISIBLE
            }
            mFilterGroupName = findViewById(R.id.tv_filter_group)
            mFilterStrengthText = findViewById(R.id.tv_filter_strength)

            mFilterStrengthLayout = findViewById<RelativeLayout>(R.id.rv_filter_strength)
            mFilterStrengthBar = findViewById(R.id.sb_filter)

            mFilterStrengthBar.progress = 65
            mFilterStrengthText.text = "65"


            mFilterStrengthBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        // 设置滤镜强度
                        sSenseTimePlugin!!.setFilterStrength(progress.toFloat() / 100)
                        mFilterStrengthText.text = progress.toString() + ""
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar) {}
                override fun onStopTrackingTouch(seekBar: SeekBar) {}
            })

            // 为滤镜中的人像分类添加点击监听，选择第一个选项为取消滤镜，否则设置对应的滤镜并更新视图

            // 为滤镜中的人像分类添加点击监听，选择第一个选项为取消滤镜，否则设置对应的滤镜并更新视图
            mFilterAdapters[Constants.FILTER_PORTRAIT]!!.setClickFilterListener { v ->
                resetFilterView()
                val position = v.tag.toString().toInt()
                mFilterAdapters[Constants.FILTER_PORTRAIT]!!.setSelectedPosition(
                    position
                )
                mCurrentFilterGroupIndex = 0
                mCurrentFilterIndex = -1
                if (position == 0) {
                    sSenseTimePlugin!!.setFilter("")
                } else {
                    sSenseTimePlugin!!.setFilter(
                        mFilterListMap[Constants.FILTER_PORTRAIT]!![position].model
                    )
                    mCurrentFilterIndex = position
                    mFilterStrengthLayout.visibility = VISIBLE
//                    mShowOriginBtn1.setVisibility(INVISIBLE)
//                    mShowOriginBtn2.setVisibility(INVISIBLE)
//                    mShowOriginBtn3.setVisibility(VISIBLE)
                    (findViewById<View>(R.id.iv_filter_group_portrait) as ImageView).setImageDrawable(
                        resources.getDrawable(R.drawable.icon_portrait_selected)
                    )
                    (findViewById<View>(R.id.tv_filter_group_portrait) as TextView).setTextColor(
                        resources.getColor(R.color.text_selected)
                    )
                }
                mFilterAdapters[Constants.FILTER_PORTRAIT]!!.notifyDataSetChanged()
            }

            // 为滤镜中的风景分类添加点击监听，选择第一个选项为取消滤镜，否则设置对应的滤镜并更新视图

            // 为滤镜中的风景分类添加点击监听，选择第一个选项为取消滤镜，否则设置对应的滤镜并更新视图
            mFilterAdapters[Constants.FILTER_SCENERY]!!.setClickFilterListener { v ->
                resetFilterView()
                val position = v.tag.toString().toInt()
                mFilterAdapters[Constants.FILTER_SCENERY]!!.setSelectedPosition(
                    position
                )
                mCurrentFilterGroupIndex = 1
                mCurrentFilterIndex = -1
                if (position == 0) {
                    sSenseTimePlugin!!.setFilter("")
                } else {
                    sSenseTimePlugin!!.setFilter(
                        mFilterListMap[Constants.FILTER_SCENERY]!![position].model
                    )
                    mCurrentFilterIndex = position
                    mFilterStrengthLayout.visibility = VISIBLE
//                    mShowOriginBtn1.setVisibility(INVISIBLE)
//                    mShowOriginBtn2.setVisibility(INVISIBLE)
//                    mShowOriginBtn3.setVisibility(VISIBLE)
                    (findViewById<View>(R.id.iv_filter_group_scenery) as ImageView).setImageDrawable(
                        resources.getDrawable(R.drawable.icon_scenery_selected)
                    )
                    (findViewById<View>(R.id.tv_filter_group_scenery) as TextView).setTextColor(
                        resources.getColor(R.color.text_selected)
                    )
                }
                mFilterAdapters[Constants.FILTER_SCENERY]!!.notifyDataSetChanged()
            }

            // 为滤镜中的静物分类添加点击监听，选择第一个选项为取消滤镜，否则设置对应的滤镜并更新视图

            // 为滤镜中的静物分类添加点击监听，选择第一个选项为取消滤镜，否则设置对应的滤镜并更新视图
            mFilterAdapters[Constants.FILTER_STILL_LIFE]!!.setClickFilterListener { v ->
                resetFilterView()
                val position = v.tag.toString().toInt()
                mFilterAdapters[Constants.FILTER_STILL_LIFE]!!.setSelectedPosition(
                    position
                )
                mCurrentFilterGroupIndex = 2
                mCurrentFilterIndex = -1
                if (position == 0) {
                    sSenseTimePlugin!!.setFilter("")
                } else {
                    sSenseTimePlugin!!.setFilter(
                        mFilterListMap[Constants.FILTER_STILL_LIFE]!![position].model
                    )
                    mCurrentFilterIndex = position
                    mFilterStrengthLayout.visibility = VISIBLE
//                    mShowOriginBtn1.setVisibility(INVISIBLE)
//                    mShowOriginBtn2.setVisibility(INVISIBLE)
//                    mShowOriginBtn3.setVisibility(VISIBLE)
                    (findViewById<View>(R.id.iv_filter_group_still_life) as ImageView).setImageDrawable(
                        resources.getDrawable(R.drawable.icon_still_life_selected)
                    )
                    (findViewById<View>(R.id.tv_filter_group_still_life) as TextView).setTextColor(
                        resources.getColor(R.color.text_selected)
                    )
                }
                mFilterAdapters[Constants.FILTER_STILL_LIFE]!!.notifyDataSetChanged()
            }

            // 为滤镜中的食物分类添加点击监听，选择第一个选项为取消滤镜，否则设置对应的滤镜并更新视图

            // 为滤镜中的食物分类添加点击监听，选择第一个选项为取消滤镜，否则设置对应的滤镜并更新视图
            mFilterAdapters[Constants.FILTER_FOOD]!!.setClickFilterListener { v ->
                resetFilterView()
                val position = v.tag.toString().toInt()
                mFilterAdapters[Constants.FILTER_FOOD]!!.setSelectedPosition(
                    position
                )
                mCurrentFilterGroupIndex = 3
                mCurrentFilterIndex = -1
                if (position == 0) {
                    sSenseTimePlugin!!.setFilter("")
                } else {
                    sSenseTimePlugin!!.setFilter(
                        mFilterListMap[Constants.FILTER_FOOD]!![position].model
                    )
                    mCurrentFilterIndex = position
                    mFilterStrengthLayout.visibility = VISIBLE
//                    mShowOriginBtn1.setVisibility(INVISIBLE)
//                    mShowOriginBtn2.setVisibility(INVISIBLE)
//                    mShowOriginBtn3.setVisibility(VISIBLE)
                    (findViewById<View>(R.id.iv_filter_group_food) as ImageView).setImageDrawable(
                        resources.getDrawable(R.drawable.icon_food_selected)
                    )
                    (findViewById<View>(R.id.tv_filter_group_food) as TextView).setTextColor(
                        resources.getColor(R.color.text_selected)
                    )
                }
                mFilterAdapters[Constants.FILTER_FOOD]!!.notifyDataSetChanged()
            }
        }
    }

    /**
     * 重置滤镜视图
     */
    @SuppressLint("NotifyDataSetChanged")
    private fun resetFilterView() {
        (findViewById<View>(R.id.iv_filter_group_portrait) as ImageView).setImageDrawable(
            resources.getDrawable(R.drawable.icon_portrait_unselected)
        )
        (findViewById<View>(R.id.tv_filter_group_portrait) as TextView).setTextColor(
            resources.getColor(R.color.white)
        )
        (findViewById<View>(R.id.iv_filter_group_scenery) as ImageView).setImageDrawable(
            resources.getDrawable(R.drawable.icon_scenery_unselected)
        )
        (findViewById<View>(R.id.tv_filter_group_scenery) as TextView).setTextColor(
            resources.getColor(R.color.white)
        )
        (findViewById<View>(R.id.iv_filter_group_still_life) as ImageView).setImageDrawable(
            resources.getDrawable(R.drawable.icon_still_life_unselected)
        )
        (findViewById<View>(R.id.tv_filter_group_still_life) as TextView).setTextColor(
            resources.getColor(R.color.white)
        )
        (findViewById<View>(R.id.iv_filter_group_food) as ImageView).setImageDrawable(
            resources.getDrawable(R.drawable.icon_food_unselected)
        )
        (findViewById<View>(R.id.tv_filter_group_food) as TextView).setTextColor(
            resources.getColor(R.color.white)
        )
        mFilterAdapters[Constants.FILTER_PORTRAIT]!!.setSelectedPosition(-1)
        mFilterAdapters[Constants.FILTER_PORTRAIT]!!.notifyDataSetChanged()
        mFilterAdapters[Constants.FILTER_SCENERY]!!.setSelectedPosition(-1)
        mFilterAdapters[Constants.FILTER_SCENERY]!!.notifyDataSetChanged()
        mFilterAdapters[Constants.FILTER_STILL_LIFE]!!.setSelectedPosition(-1)
        mFilterAdapters[Constants.FILTER_STILL_LIFE]!!.notifyDataSetChanged()
        mFilterAdapters[Constants.FILTER_FOOD]!!.setSelectedPosition(-1)
        mFilterAdapters[Constants.FILTER_FOOD]!!.notifyDataSetChanged()
        mFilterStrengthLayout.visibility = INVISIBLE
    }

    override fun reset() {
        resetFilterView()
        if (mFilterListMap[Constants.FILTER_PORTRAIT]!!.size > 0) {
            for (i in mFilterListMap[Constants.FILTER_PORTRAIT]!!.indices) {
                if (mFilterListMap[Constants.FILTER_PORTRAIT]!![i].name == "original") {
                    mCurrentFilterIndex = i
                }
            }
          //  if (mCurrentFilterIndex > 0) {
                mCurrentFilterGroupIndex = 0
                mFilterAdapters[Constants.FILTER_PORTRAIT]!!.setSelectedPosition(mCurrentFilterIndex)
                sSenseTimePlugin!!.setFilter(
                    mFilterListMap[Constants.FILTER_PORTRAIT]!![mCurrentFilterIndex].model
                )
                (findViewById<View>(R.id.iv_filter_group_portrait) as ImageView).setImageDrawable(
                    resources.getDrawable(R.drawable.icon_portrait_selected)
                )
                (findViewById<View>(R.id.tv_filter_group_portrait) as TextView).setTextColor(
                    resources.getColor(R.color.text_selected)
                )
                mFilterAdapters[Constants.FILTER_PORTRAIT]!!.notifyDataSetChanged()
           // }
        }
        mFilterStrengthBar.progress = 65
    }

}