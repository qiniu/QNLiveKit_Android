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
import com.qlive.uiwidghtbeauty.adapter.MakeupAdapter
import com.qlive.uiwidghtbeauty.model.MakeupItem
import com.qlive.uiwidghtbeauty.utils.Constants
import com.qlive.uiwidghtbeauty.utils.Constants.*
import com.qlive.uiwidghtbeauty.utils.ResourcesUtil
import com.sensetime.stmobile.params.STEffectBeautyGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

class MakeUpBeautyPage : FrameLayout, BaseEffectPage<MakeupItem> {
    override var onItemClick: (groupIndex: String, item: MakeupItem, itemIndex: Int) -> Unit =
        { _, _, _ ->
        }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        LayoutInflater.from(context).inflate(R.layout.kit_layout_make_up, this, true)
        init()
    }

    private val mMakeupGroupIds: HashMap<String, String> = HashMap<String, String>()
    val mMakeupAdapters = HashMap<String, MakeupAdapter>()
    var mMakeupLists = HashMap<String, ArrayList<MakeupItem>>()
    var mMakeupOptionIndex = HashMap<String, Int>()
    val mMakeupOptionSelectedIndex = HashMap<Int, Int>()
    val mMakeupStrength = HashMap<Int, Int>()

    lateinit var mMakeupOptionsRecycleView: RecyclerView
    private lateinit var mMakeupGroupBack: ImageView
    private lateinit var mMakeupGroupName: TextView
    private lateinit var mMakeupIconsRelativeLayout: View
    private lateinit var mMakeupGroupRelativeLayout: View

    lateinit var mFilterStrengthLayout: View
    lateinit var mFilterStrengthBar: SeekBar
    private lateinit var mFilterStrengthText: TextView
    var mCurrentStylePath: String? = null
    var mCurrentMakeupGroupIndex = -1

    @SuppressLint("NotifyDataSetChanged")
    fun init() {
        GlobalScope.launch(Dispatchers.Main) {
            val ret = async(Dispatchers.IO){
                mMakeupLists = ResourcesUtil.getMakeupListMap(context)
                mMakeupOptionIndex = ResourcesUtil.getMakeupOptionIndexMap()
                mMakeupGroupIds.clear()
                mMakeupGroupIds[MAKEUP_LIP] = GROUP_LIP
                mMakeupGroupIds[MAKEUP_EYEBALL] = GROUP_EYEBALL
                mMakeupGroupIds[MAKEUP_BLUSH] = GROUP_BLUSH
                mMakeupGroupIds[MAKEUP_BROW] = GROUP_BROW
                mMakeupGroupIds[MAKEUP_HIGHLIGHT] = GROUP_HIGHLIGHT
                mMakeupGroupIds[MAKEUP_EYE] = GROUP_EYE
                mMakeupGroupIds[MAKEUP_EYELINER] = GROUP_EYELINER
                mMakeupGroupIds[MAKEUP_EYELASH] = GROUP_EYELASH
                mMakeupGroupIds[MAKEUP_STYLE] = GROUP_STYLE

                mMakeupAdapters[Constants.MAKEUP_LIP] = MakeupAdapter(
                    mMakeupLists[Constants.MAKEUP_LIP], context
                )
                mMakeupAdapters[Constants.MAKEUP_HIGHLIGHT] =
                    MakeupAdapter(mMakeupLists[Constants.MAKEUP_HIGHLIGHT], context)
                mMakeupAdapters[Constants.MAKEUP_BLUSH] =
                    MakeupAdapter(mMakeupLists[Constants.MAKEUP_BLUSH], context)
                mMakeupAdapters[Constants.MAKEUP_BROW] =
                    MakeupAdapter(mMakeupLists[Constants.MAKEUP_BROW], context)
                mMakeupAdapters[Constants.MAKEUP_EYE] = MakeupAdapter(
                    mMakeupLists[Constants.MAKEUP_EYE], context
                )
                mMakeupAdapters[Constants.MAKEUP_EYELINER] =
                    MakeupAdapter(mMakeupLists[Constants.MAKEUP_EYELINER], context)
                mMakeupAdapters[Constants.MAKEUP_EYELASH] =
                    MakeupAdapter(mMakeupLists[Constants.MAKEUP_EYELASH], context)
                mMakeupAdapters.put(
                    Constants.MAKEUP_EYEBALL, MakeupAdapter(
                        mMakeupLists[Constants.MAKEUP_EYEBALL], context
                    )
                )
                mMakeupAdapters[MAKEUP_STYLE] =
                    MakeupAdapter(mMakeupLists[MAKEUP_STYLE], context)

                for (i in 402 until MAKEUP_TYPE_COUNT + 402) {
                    mMakeupOptionSelectedIndex[i] = 0
                    mMakeupStrength[i] = 80
                }

            }
            ret.await()

            // 美妆相关视图，美妆包含口红、腮红、修容等8个部位的特效，都为二级列表，其下有各自的样式
            // 美妆相关视图，美妆包含口红、腮红、修容等8个部位的特效，都为二级列表，其下有各自的样式

            mMakeupOptionsRecycleView = findViewById<RecyclerView>(R.id.rv_makeup_icons)
            mMakeupOptionsRecycleView.layoutManager = StaggeredGridLayoutManager(
                1,
                StaggeredGridLayoutManager.HORIZONTAL
            )
            mMakeupOptionsRecycleView.addItemDecoration(SpaceItemDecoration(0))
            mMakeupIconsRelativeLayout = findViewById(R.id.rl_makeup_icons)
            mMakeupGroupRelativeLayout = findViewById(R.id.rl_makeup_groups)
            mFilterStrengthLayout = findViewById(R.id.rv_filter_strength)
            mFilterStrengthBar = findViewById(R.id.sb_filter)
            mFilterStrengthText = findViewById(R.id.tv_filter_strength)
            mFilterStrengthBar.progress = 65
            mFilterStrengthText.setText("65")

            // 口红
            // 口红
            val makeupGroupLip = findViewById<LinearLayout>(R.id.ll_makeup_group_lip)
            makeupGroupLip.setOnClickListener {
                mMakeupGroupRelativeLayout.visibility = INVISIBLE
                mMakeupIconsRelativeLayout.visibility = VISIBLE
                mCurrentMakeupGroupIndex = Constants.ST_MAKEUP_LIP
                if (mMakeupOptionSelectedIndex[Constants.ST_MAKEUP_LIP] != 0) {
                    mFilterStrengthLayout.visibility = VISIBLE
                    mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_lip_selected))
                } else {
                    mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_lip_unselected))
                }
                mFilterStrengthBar.progress = mMakeupStrength[mCurrentMakeupGroupIndex]!!
                mMakeupOptionsRecycleView.layoutManager =
                    StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
                mMakeupOptionsRecycleView.adapter =
                    mMakeupAdapters[Constants.MAKEUP_LIP]
                mMakeupGroupName.text = "口红"
            }
            // 腮红
            // 腮红
            val makeupGroupCheeks = findViewById<LinearLayout>(R.id.ll_makeup_group_cheeks)
            makeupGroupCheeks.setOnClickListener {
                mMakeupGroupRelativeLayout.visibility = INVISIBLE
                mMakeupIconsRelativeLayout.visibility = VISIBLE
                mCurrentMakeupGroupIndex = Constants.ST_MAKEUP_BLUSH
                if (mMakeupOptionSelectedIndex[Constants.ST_MAKEUP_BLUSH] != 0) {
                    mFilterStrengthLayout?.visibility = VISIBLE
                    mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_cheeks_selected))
                } else {
                    mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_cheeks_unselected))
                }
                mFilterStrengthBar.progress = mMakeupStrength[mCurrentMakeupGroupIndex]!!
                mMakeupOptionsRecycleView.layoutManager =
                    StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
                mMakeupOptionsRecycleView.adapter =
                    mMakeupAdapters[Constants.MAKEUP_BLUSH]
                mMakeupGroupName.text = "腮红"
            }

            // 修容

            // 修容
            val makeupGroupFace = findViewById<LinearLayout>(R.id.ll_makeup_group_face)
            makeupGroupFace.setOnClickListener {
                mMakeupGroupRelativeLayout.visibility = INVISIBLE
                mMakeupIconsRelativeLayout.visibility = VISIBLE
                mCurrentMakeupGroupIndex =
                    Constants.ST_MAKEUP_HIGHLIGHT
                if (mMakeupOptionSelectedIndex[Constants.ST_MAKEUP_HIGHLIGHT] != 0) {
                    mFilterStrengthLayout?.visibility = VISIBLE
                    mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_face_selected))
                } else {
                    mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_face_unselected))
                }
                mFilterStrengthBar.progress = mMakeupStrength[mCurrentMakeupGroupIndex]!!
                mMakeupOptionsRecycleView.layoutManager =
                    StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
                mMakeupOptionsRecycleView.adapter =
                    mMakeupAdapters[Constants.MAKEUP_HIGHLIGHT]
                mMakeupGroupName.text = "修容"
            }

            // 眉毛

            // 眉毛
            val makeupGroupBrow = findViewById<LinearLayout>(R.id.ll_makeup_group_brow)
            makeupGroupBrow.setOnClickListener {
                mMakeupGroupRelativeLayout.visibility = INVISIBLE
                mMakeupIconsRelativeLayout.visibility = VISIBLE
                mCurrentMakeupGroupIndex = Constants.ST_MAKEUP_BROW
                if (mMakeupOptionSelectedIndex[Constants.ST_MAKEUP_BROW] != 0) {
                    mFilterStrengthLayout?.visibility = VISIBLE
                    mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_brow_selected))
                } else {
                    mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_brow_unselected))
                }
                mFilterStrengthBar.progress = mMakeupStrength[mCurrentMakeupGroupIndex]!!
                mMakeupOptionsRecycleView.layoutManager =
                    StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
                mMakeupOptionsRecycleView.adapter =
                    mMakeupAdapters[Constants.MAKEUP_BROW]
                mMakeupGroupName.text = "眉毛"
            }

            // 眼影

            // 眼影
            val makeupGroupEye = findViewById<LinearLayout>(R.id.ll_makeup_group_eye)
            makeupGroupEye.setOnClickListener {
                mMakeupGroupRelativeLayout.visibility = INVISIBLE
                mMakeupIconsRelativeLayout.visibility = VISIBLE
                mCurrentMakeupGroupIndex = Constants.ST_MAKEUP_EYE
                if (mMakeupOptionSelectedIndex[Constants.ST_MAKEUP_EYE] != 0) {
                    mFilterStrengthLayout?.visibility = VISIBLE
                    mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_eye_selected))
                } else {
                    mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_eye_unselected))
                }
                mFilterStrengthBar.progress = mMakeupStrength[mCurrentMakeupGroupIndex]!!
                mMakeupOptionsRecycleView.layoutManager =
                    StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
                mMakeupOptionsRecycleView.adapter =
                    mMakeupAdapters[Constants.MAKEUP_EYE]
                mMakeupGroupName.text = "眼影"
            }

            // 眼线
            // 眼线
            val makeupGroupEyeLiner = findViewById<LinearLayout>(R.id.ll_makeup_group_eyeliner)
            makeupGroupEyeLiner.setOnClickListener {
                mMakeupGroupRelativeLayout.visibility = INVISIBLE
                mMakeupIconsRelativeLayout.visibility = VISIBLE
                mCurrentMakeupGroupIndex =
                    Constants.ST_MAKEUP_EYELINER
                if (mMakeupOptionSelectedIndex[Constants.ST_MAKEUP_EYELINER] != 0) {
                    mFilterStrengthLayout?.visibility = VISIBLE
                    mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_eyeliner_selected))
                } else {
                    mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_eyeline_unselected))
                }
                mFilterStrengthBar.progress = mMakeupStrength[mCurrentMakeupGroupIndex]!!
                mMakeupOptionsRecycleView.layoutManager =
                    StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
                mMakeupOptionsRecycleView.adapter =
                    mMakeupAdapters[Constants.MAKEUP_EYELINER]
                mMakeupGroupName.text = "眼线"
            }

            // 眼睫毛

            // 眼睫毛
            val makeupGroupEyeLash = findViewById<LinearLayout>(R.id.ll_makeup_group_eyelash)
            makeupGroupEyeLash.setOnClickListener {
                mMakeupGroupRelativeLayout.visibility = INVISIBLE
                mMakeupIconsRelativeLayout.visibility = VISIBLE
                mCurrentMakeupGroupIndex =
                    Constants.ST_MAKEUP_EYELASH
                if (mMakeupOptionSelectedIndex[Constants.ST_MAKEUP_EYELASH] != 0) {
                    mFilterStrengthLayout?.visibility = VISIBLE
                    mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_eyelash_selected))
                } else {
                    mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_eyelash_unselected))
                }
                mFilterStrengthBar.progress = mMakeupStrength[mCurrentMakeupGroupIndex]!!
                mMakeupOptionsRecycleView.layoutManager =
                    StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
                mMakeupOptionsRecycleView.adapter =
                    mMakeupAdapters[Constants.MAKEUP_EYELASH]
                mMakeupGroupName.text = "眼睫毛"
            }

            // 美瞳

            // 美瞳
            val makeupGroupEyeBall = findViewById<LinearLayout>(R.id.ll_makeup_group_eyeball)
            makeupGroupEyeBall.setOnClickListener {
                mMakeupGroupRelativeLayout.visibility = INVISIBLE
                mMakeupIconsRelativeLayout.visibility = VISIBLE
                mCurrentMakeupGroupIndex =
                    Constants.ST_MAKEUP_EYEBALL
                if (mMakeupOptionSelectedIndex[Constants.ST_MAKEUP_EYEBALL] != 0) {
                    mFilterStrengthLayout?.visibility = VISIBLE
                    mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_eyeball_selected))
                } else {
                    mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_eyeball_unselected))
                }
                mFilterStrengthBar.progress = mMakeupStrength[mCurrentMakeupGroupIndex]!!
                mMakeupOptionsRecycleView.layoutManager =
                    StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
                mMakeupOptionsRecycleView.adapter =
                    mMakeupAdapters[Constants.MAKEUP_EYEBALL]
                mMakeupGroupName.text = "美瞳"
            }

            // 整妆/风格妆

            // 整妆/风格妆
            val makeupGroupStyle = findViewById<LinearLayout>(R.id.ll_makeup_group_style)
            makeupGroupStyle.setOnClickListener {
                mMakeupGroupRelativeLayout.visibility = INVISIBLE
                mMakeupIconsRelativeLayout.visibility = VISIBLE
                mCurrentMakeupGroupIndex = ST_MAKEUP_STYLE
                if (mMakeupOptionSelectedIndex[ST_MAKEUP_STYLE] != 0) {
                    mFilterStrengthLayout.visibility = VISIBLE
                    mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_eyeball_selected))
                } else {
                    mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_eyeball_unselected))
                }
                mFilterStrengthBar.progress = mMakeupStrength[mCurrentMakeupGroupIndex]!!
                mMakeupOptionsRecycleView.layoutManager =
                    StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.HORIZONTAL)
                mMakeupOptionsRecycleView.adapter = mMakeupAdapters[MAKEUP_STYLE]
                mMakeupGroupName.text = "整妆"
            }

            // 返回按钮，检查所选特效的情况，如果选中则将其图标和文字变色，否则复原图标和文字颜色

            // 返回按钮，检查所选特效的情况，如果选中则将其图标和文字变色，否则复原图标和文字颜色
            mMakeupGroupBack = findViewById(R.id.iv_makeup_group)
            mMakeupGroupBack.setOnClickListener {
                mMakeupGroupRelativeLayout.visibility = VISIBLE
                mMakeupIconsRelativeLayout.visibility = INVISIBLE
                mFilterStrengthLayout?.visibility = INVISIBLE
                if (mMakeupOptionSelectedIndex[Constants.ST_MAKEUP_LIP] != 0) {
                    (findViewById<View>(R.id.iv_makeup_group_lip) as ImageView).setImageDrawable(
                        resources.getDrawable(R.drawable.makeup_lip_selected)
                    )
                    (findViewById<View>(R.id.tv_makeup_group_lip) as TextView).setTextColor(
                        resources.getColor(R.color.text_selected)
                    )
                } else {
                    (findViewById<View>(R.id.iv_makeup_group_lip) as ImageView).setImageDrawable(
                        resources.getDrawable(R.drawable.makeup_lip_unselected)
                    )
                    (findViewById<View>(R.id.tv_makeup_group_lip) as TextView).setTextColor(
                        resources.getColor(R.color.white)
                    )
                }
                if (mMakeupOptionSelectedIndex[Constants.ST_MAKEUP_BLUSH] != 0) {
                    (findViewById<View>(R.id.iv_makeup_group_cheeks) as ImageView).setImageDrawable(
                        resources.getDrawable(R.drawable.makeup_cheeks_selected)
                    )
                    (findViewById<View>(R.id.tv_makeup_group_cheeks) as TextView).setTextColor(
                        resources.getColor(R.color.text_selected)
                    )
                } else {
                    (findViewById<View>(R.id.iv_makeup_group_cheeks) as ImageView).setImageDrawable(
                        resources.getDrawable(R.drawable.makeup_cheeks_unselected)
                    )
                    (findViewById<View>(R.id.tv_makeup_group_cheeks) as TextView).setTextColor(
                        resources.getColor(R.color.white)
                    )
                }
                if (mMakeupOptionSelectedIndex[Constants.ST_MAKEUP_HIGHLIGHT] != 0) {
                    (findViewById<View>(R.id.iv_makeup_group_face) as ImageView).setImageDrawable(
                        resources.getDrawable(R.drawable.makeup_face_selected)
                    )
                    (findViewById<View>(R.id.tv_makeup_group_face) as TextView).setTextColor(
                        resources.getColor(R.color.text_selected)
                    )
                } else {
                    (findViewById<View>(R.id.iv_makeup_group_face) as ImageView).setImageDrawable(
                        resources.getDrawable(R.drawable.makeup_face_unselected)
                    )
                    (findViewById<View>(R.id.tv_makeup_group_face) as TextView).setTextColor(
                        resources.getColor(R.color.white)
                    )
                }
                if (mMakeupOptionSelectedIndex[Constants.ST_MAKEUP_BROW] != 0) {
                    (findViewById<View>(R.id.iv_makeup_group_brow) as ImageView).setImageDrawable(
                        resources.getDrawable(R.drawable.makeup_brow_selected)
                    )
                    (findViewById<View>(R.id.tv_makeup_group_brow) as TextView).setTextColor(
                        resources.getColor(R.color.text_selected)
                    )
                } else {
                    (findViewById<View>(R.id.iv_makeup_group_brow) as ImageView).setImageDrawable(
                        resources.getDrawable(R.drawable.makeup_brow_unselected)
                    )
                    (findViewById<View>(R.id.tv_makeup_group_brow) as TextView).setTextColor(
                        resources.getColor(R.color.white)
                    )
                }
                if (mMakeupOptionSelectedIndex[Constants.ST_MAKEUP_EYE] != 0) {
                    (findViewById<View>(R.id.iv_makeup_group_eye) as ImageView).setImageDrawable(
                        resources.getDrawable(R.drawable.makeup_eye_selected)
                    )
                    (findViewById<View>(R.id.tv_makeup_group_eye) as TextView).setTextColor(
                        resources.getColor(R.color.text_selected)
                    )
                } else {
                    (findViewById<View>(R.id.iv_makeup_group_eye) as ImageView).setImageDrawable(
                        resources.getDrawable(R.drawable.makeup_eye_unselected)
                    )
                    (findViewById<View>(R.id.tv_makeup_group_eye) as TextView).setTextColor(
                        resources.getColor(R.color.white)
                    )
                }
                if (mMakeupOptionSelectedIndex[Constants.ST_MAKEUP_EYELINER] != 0) {
                    (findViewById<View>(R.id.iv_makeup_group_eyeliner) as ImageView).setImageDrawable(
                        resources.getDrawable(R.drawable.makeup_eyeliner_selected)
                    )
                    (findViewById<View>(R.id.tv_makeup_group_eyeliner) as TextView).setTextColor(
                        resources.getColor(R.color.text_selected)
                    )
                } else {
                    (findViewById<View>(R.id.iv_makeup_group_eyeliner) as ImageView).setImageDrawable(
                        resources.getDrawable(R.drawable.makeup_eyeline_unselected)
                    )
                    (findViewById<View>(R.id.tv_makeup_group_eyeliner) as TextView).setTextColor(
                        resources.getColor(R.color.white)
                    )
                }
                if (mMakeupOptionSelectedIndex[Constants.ST_MAKEUP_EYELASH] != 0) {
                    (findViewById<View>(R.id.iv_makeup_group_eyelash) as ImageView).setImageDrawable(
                        resources.getDrawable(R.drawable.makeup_eyelash_selected)
                    )
                    (findViewById<View>(R.id.tv_makeup_group_eyelash) as TextView).setTextColor(
                        resources.getColor(R.color.text_selected)
                    )
                } else {
                    (findViewById<View>(R.id.iv_makeup_group_eyelash) as ImageView).setImageDrawable(
                        resources.getDrawable(R.drawable.makeup_eyelash_unselected)
                    )
                    (findViewById<View>(R.id.tv_makeup_group_eyelash) as TextView).setTextColor(
                        resources.getColor(R.color.white)
                    )
                }
                if (mMakeupOptionSelectedIndex[Constants.ST_MAKEUP_EYEBALL] != 0) {
                    (findViewById<View>(R.id.iv_makeup_group_eyeball) as ImageView).setImageDrawable(
                        resources.getDrawable(R.drawable.makeup_eyeball_selected)
                    )
                    (findViewById<View>(R.id.tv_makeup_group_eyeball) as TextView).setTextColor(
                        resources.getColor(R.color.text_selected)
                    )
                } else {
                    (findViewById<View>(R.id.iv_makeup_group_eyeball) as ImageView).setImageDrawable(
                        resources.getDrawable(R.drawable.makeup_eyeball_unselected)
                    )
                    (findViewById<View>(R.id.tv_makeup_group_eyeball) as TextView).setTextColor(
                        resources.getColor(R.color.white)
                    )
                }
            }
            mMakeupGroupName = findViewById(R.id.tv_makeup_group)


            // 为美妆列表的每一个部位的 adapter 都设置一个监听，当点击该视图时更新 seekbar 和下面的指示数字
            // 为美妆列表的每一个部位的 adapter 都设置一个监听，当点击该视图时更新 seekbar 和下面的指示数字

            mMakeupAdapters.entries.forEach { entry ->
                entry.value.setClickMakeupListener { v ->
                    val position = v.tag.toString().toInt()
                    if (position == 0) {
                        entry.value.setSelectedPosition(position)
                        mMakeupOptionSelectedIndex[mMakeupOptionIndex[entry.key]!!] =
                            position
                        mFilterStrengthLayout.visibility =
                            INVISIBLE
                        sSenseTimePlugin!!.setMakeup(
                            mCurrentMakeupGroupIndex,
                            ""
                        )
                        updateMakeupOptions(mCurrentMakeupGroupIndex, false)
                    } else {
                        onItemClick.invoke(entry.key, entry.value.getItem(position), position)
                        entry.value.setSelectedPosition(position)
                        mMakeupOptionSelectedIndex[mMakeupOptionIndex[entry.key]!!] =
                            position
                        sSenseTimePlugin!!.setMakeup(
                            mCurrentMakeupGroupIndex,
                            mMakeupLists[ResourcesUtil.getMakeupNameOfType(
                                mCurrentMakeupGroupIndex
                            )]!![position].path
                        )
                        sSenseTimePlugin!!.setMakeupStrength(
                            mCurrentMakeupGroupIndex,
                            mMakeupStrength[mCurrentMakeupGroupIndex]!!.toFloat() / 100f
                        )
                        mFilterStrengthLayout.visibility =
                            VISIBLE
                        mFilterStrengthBar.progress =
                            mMakeupStrength[mCurrentMakeupGroupIndex]!!
                        updateMakeupOptions(mCurrentMakeupGroupIndex, true)
                    }
                    entry.value.notifyDataSetChanged()
                }
            }

            mFilterStrengthBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                @SuppressLint("SetTextI18n")
                override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
//                        sSenseTimePlugin!!.setMakeupStrength(
//                            mCurrentMakeupGroupIndex,
//                            progress.toFloat() / 100
//                        )
                        if (mCurrentMakeupGroupIndex == ST_MAKEUP_STYLE) {
                            sSenseTimePlugin!!.setBeautyGroupStrength(
                                STEffectBeautyGroup.EFFECT_BEAUTY_GROUP_MAKEUP,
                                mCurrentStylePath,
                                progress.toFloat() / 100
                            )
                            sSenseTimePlugin!!.setBeautyGroupStrength(
                                STEffectBeautyGroup.EFFECT_BEAUTY_GROUP_FILTER,
                                mCurrentStylePath,
                                progress.toFloat() / 100
                            )
                        } else {
                            sSenseTimePlugin!!.setMakeupStrength(
                                mCurrentMakeupGroupIndex,
                                progress.toFloat() / 100
                            )
                        }
                        mMakeupStrength[mCurrentMakeupGroupIndex] = progress
                        mFilterStrengthText.setText(progress.toString() + "")
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            })
            GlobalScope.launch(Dispatchers.IO) {
                fetchMakeupGroupMaterialList(mMakeupGroupIds)
            }
        }
    }

    fun updateMakeupOptions(type: Int, value: Boolean) {
        if (value) {
            if (type == ST_MAKEUP_LIP) {
                mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_lip_selected))
                (findViewById<View>(R.id.iv_makeup_group_lip) as ImageView).setImageDrawable(
                    resources.getDrawable(R.drawable.makeup_lip_selected)
                )
                (findViewById<View>(R.id.tv_makeup_group_lip) as TextView).setTextColor(
                    resources.getColor(R.color.text_selected)
                )
            }
            if (type == ST_MAKEUP_BLUSH) {
                mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_cheeks_selected))
                (findViewById<View>(R.id.iv_makeup_group_cheeks) as ImageView).setImageDrawable(
                    resources.getDrawable(R.drawable.makeup_cheeks_selected)
                )
                (findViewById<View>(R.id.tv_makeup_group_cheeks) as TextView).setTextColor(
                    resources.getColor(R.color.text_selected)
                )
            }
            if (type == ST_MAKEUP_HIGHLIGHT) {
                mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_face_selected))
                (findViewById<View>(R.id.iv_makeup_group_face) as ImageView).setImageDrawable(
                    resources.getDrawable(R.drawable.makeup_face_selected)
                )
                (findViewById<View>(R.id.tv_makeup_group_face) as TextView).setTextColor(
                    resources.getColor(R.color.text_selected)
                )
            }
            if (type == ST_MAKEUP_BROW) {
                mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_brow_selected))
                (findViewById<View>(R.id.iv_makeup_group_brow) as ImageView).setImageDrawable(
                    resources.getDrawable(R.drawable.makeup_brow_selected)
                )
                (findViewById<View>(R.id.tv_makeup_group_brow) as TextView).setTextColor(
                    resources.getColor(R.color.text_selected)
                )
            }
            if (type == ST_MAKEUP_EYE) {
                mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_eye_selected))
                (findViewById<View>(R.id.iv_makeup_group_eye) as ImageView).setImageDrawable(
                    resources.getDrawable(R.drawable.makeup_eye_selected)
                )
                (findViewById<View>(R.id.tv_makeup_group_eye) as TextView).setTextColor(
                    resources.getColor(R.color.text_selected)
                )
            }
            if (type == ST_MAKEUP_EYELINER) {
                mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_eyeliner_selected))
                (findViewById<View>(R.id.iv_makeup_group_eyeliner) as ImageView).setImageDrawable(
                    resources.getDrawable(R.drawable.makeup_eyeliner_selected)
                )
                (findViewById<View>(R.id.tv_makeup_group_eyeliner) as TextView).setTextColor(
                    resources.getColor(R.color.text_selected)
                )
            }
            if (type == ST_MAKEUP_EYELASH) {
                mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_eyelash_selected))
                (findViewById<View>(R.id.iv_makeup_group_eyelash) as ImageView).setImageDrawable(
                    resources.getDrawable(R.drawable.makeup_eyelash_selected)
                )
                (findViewById<View>(R.id.tv_makeup_group_eyelash) as TextView).setTextColor(
                    resources.getColor(R.color.text_selected)
                )
            }
            if (type == ST_MAKEUP_EYEBALL) {
                mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_eyeball_selected))
                (findViewById<View>(R.id.iv_makeup_group_eyeball) as ImageView).setImageDrawable(
                    resources.getDrawable(R.drawable.makeup_eyeball_selected)
                )
                (findViewById<View>(R.id.tv_makeup_group_eyeball) as TextView).setTextColor(
                    resources.getColor(R.color.text_selected)
                )
            }
            if (type == ST_MAKEUP_STYLE) {
                mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_all_selected))
                (findViewById<View>(R.id.iv_makeup_group_style) as ImageView).setImageDrawable(
                    resources.getDrawable(R.drawable.makeup_all_selected)
                )
                (findViewById<View>(R.id.tv_makeup_group_style) as TextView).setTextColor(
                    resources.getColor(R.color.text_selected)
                )
            }
        } else {
            if (type == ST_MAKEUP_LIP) {
                mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_lip_unselected))
                (findViewById<View>(R.id.iv_makeup_group_lip) as ImageView).setImageDrawable(
                    resources.getDrawable(R.drawable.makeup_lip_unselected)
                )
                (findViewById<View>(R.id.tv_makeup_group_lip) as TextView).setTextColor(
                    resources.getColor(R.color.white)
                )
            }
            if (type == ST_MAKEUP_BLUSH) {
                mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_cheeks_unselected))
                (findViewById<View>(R.id.iv_makeup_group_cheeks) as ImageView).setImageDrawable(
                    resources.getDrawable(R.drawable.makeup_cheeks_unselected)
                )
                (findViewById<View>(R.id.tv_makeup_group_cheeks) as TextView).setTextColor(
                    resources.getColor(R.color.white)
                )
            }
            if (type == ST_MAKEUP_HIGHLIGHT) {
                mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_face_unselected))
                (findViewById<View>(R.id.iv_makeup_group_face) as ImageView).setImageDrawable(
                    resources.getDrawable(R.drawable.makeup_face_unselected)
                )
                (findViewById<View>(R.id.tv_makeup_group_face) as TextView).setTextColor(
                    resources.getColor(R.color.white)
                )
            }
            if (type == ST_MAKEUP_BROW) {
                mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_brow_unselected))
                (findViewById<View>(R.id.iv_makeup_group_brow) as ImageView).setImageDrawable(
                    resources.getDrawable(R.drawable.makeup_brow_unselected)
                )
                (findViewById<View>(R.id.tv_makeup_group_brow) as TextView).setTextColor(
                    resources.getColor(R.color.white)
                )
            }
            if (type == ST_MAKEUP_EYE) {
                mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_eye_unselected))
                (findViewById<View>(R.id.iv_makeup_group_eye) as ImageView).setImageDrawable(
                    resources.getDrawable(R.drawable.makeup_eye_unselected)
                )
                (findViewById<View>(R.id.tv_makeup_group_eye) as TextView).setTextColor(
                    resources.getColor(R.color.white)
                )
            }
            if (type == ST_MAKEUP_EYELINER) {
                mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_eyeline_unselected))
                (findViewById<View>(R.id.iv_makeup_group_eyeliner) as ImageView).setImageDrawable(
                    resources.getDrawable(R.drawable.makeup_eyeline_unselected)
                )
                (findViewById<View>(R.id.tv_makeup_group_eyeliner) as TextView).setTextColor(
                    resources.getColor(R.color.white)
                )
            }
            if (type == ST_MAKEUP_EYELASH) {
                mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_eyelash_unselected))
                (findViewById<View>(R.id.iv_makeup_group_eyelash) as ImageView).setImageDrawable(
                    resources.getDrawable(R.drawable.makeup_eyelash_unselected)
                )
                (findViewById<View>(R.id.tv_makeup_group_eyelash) as TextView).setTextColor(
                    resources.getColor(R.color.white)
                )
            }
            if (type == ST_MAKEUP_EYEBALL) {
                mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_eyeball_unselected))
                (findViewById<View>(R.id.iv_makeup_group_eyeball) as ImageView).setImageDrawable(
                    resources.getDrawable(R.drawable.makeup_eyeball_unselected)
                )
                (findViewById<View>(R.id.tv_makeup_group_eyeball) as TextView).setTextColor(
                    resources.getColor(R.color.white)
                )
            }
            if (type == ST_MAKEUP_STYLE) {
                mMakeupGroupBack.setImageDrawable(resources.getDrawable(R.drawable.makeup_all_unselected))
                (findViewById<View>(R.id.iv_makeup_group_style) as ImageView).setImageDrawable(
                    resources.getDrawable(R.drawable.makeup_all_unselected)
                )
                (findViewById<View>(R.id.tv_makeup_group_style) as TextView).setTextColor(
                    resources.getColor(R.color.white)
                )
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun reset() {
        for (i in 402 until MAKEUP_TYPE_COUNT + 402) {
            sSenseTimePlugin?.setMakeup(i, "")
            sSenseTimePlugin?.setMakeupStrength(i, 0f)
            mMakeupOptionSelectedIndex[i] = 0
            mMakeupStrength[i] = 80
        }

        sSenseTimePlugin!!.setBeautyGroupStrength(
            STEffectBeautyGroup.EFFECT_BEAUTY_GROUP_MAKEUP,
            "",
            0f
        )
        sSenseTimePlugin!!.setBeautyGroupStrength(
            STEffectBeautyGroup.EFFECT_BEAUTY_GROUP_FILTER,
            "",
            0f
        )
        mMakeupOptionSelectedIndex[ST_MAKEUP_STYLE] = 0



        mFilterStrengthLayout.visibility = INVISIBLE

        (findViewById<View>(R.id.iv_makeup_group_lip) as ImageView).setImageDrawable(
            resources.getDrawable(R.drawable.makeup_lip_unselected)
        )
        (findViewById<View>(R.id.tv_makeup_group_lip) as TextView).setTextColor(
            resources.getColor(R.color.white)
        )
        (findViewById<View>(R.id.iv_makeup_group_cheeks) as ImageView).setImageDrawable(
            resources.getDrawable(R.drawable.makeup_cheeks_unselected)
        )
        (findViewById<View>(R.id.tv_makeup_group_cheeks) as TextView).setTextColor(
            resources.getColor(R.color.white)
        )
        (findViewById<View>(R.id.iv_makeup_group_face) as ImageView).setImageDrawable(
            resources.getDrawable(R.drawable.makeup_face_unselected)
        )
        (findViewById<View>(R.id.tv_makeup_group_face) as TextView).setTextColor(
            resources.getColor(R.color.white)
        )
        (findViewById<View>(R.id.iv_makeup_group_brow) as ImageView).setImageDrawable(
            resources.getDrawable(R.drawable.makeup_brow_unselected)
        )
        (findViewById<View>(R.id.tv_makeup_group_brow) as TextView).setTextColor(
            resources.getColor(R.color.white)
        )
        (findViewById<View>(R.id.iv_makeup_group_eye) as ImageView).setImageDrawable(
            resources.getDrawable(R.drawable.makeup_eye_unselected)
        )
        (findViewById<View>(R.id.tv_makeup_group_eye) as TextView).setTextColor(
            resources.getColor(R.color.white)
        )
        (findViewById<View>(R.id.iv_makeup_group_eyeliner) as ImageView).setImageDrawable(
            resources.getDrawable(R.drawable.makeup_eyeline_unselected)
        )
        (findViewById<View>(R.id.tv_makeup_group_eyeliner) as TextView).setTextColor(
            resources.getColor(R.color.white)
        )
        (findViewById<View>(R.id.iv_makeup_group_eyelash) as ImageView).setImageDrawable(
            resources.getDrawable(R.drawable.makeup_eyelash_unselected)
        )
        (findViewById<View>(R.id.tv_makeup_group_eyelash) as TextView).setTextColor(
            resources.getColor(R.color.white)
        )

        (findViewById<View>(R.id.iv_makeup_group_eyeball) as ImageView).setImageDrawable(
            resources.getDrawable(R.drawable.makeup_eyeball_unselected)
        )
        (findViewById<View>(R.id.tv_makeup_group_eyeball) as TextView).setTextColor(
            resources.getColor(R.color.white)
        )

        (findViewById<View>(R.id.iv_makeup_group_style) as ImageView).setImageDrawable(
            resources.getDrawable(R.drawable.makeup_all_unselected)
        )
        (findViewById<View>(R.id.tv_makeup_group_style) as TextView).setTextColor(
            resources.getColor(R.color.white)
        )

        mMakeupAdapters[MAKEUP_LIP]!!.setSelectedPosition(0)
        mMakeupAdapters[MAKEUP_LIP]!!.notifyDataSetChanged()
        mMakeupAdapters[MAKEUP_HIGHLIGHT]!!.setSelectedPosition(0)
        mMakeupAdapters[MAKEUP_HIGHLIGHT]!!.notifyDataSetChanged()
        mMakeupAdapters[MAKEUP_BLUSH]!!.setSelectedPosition(0)
        mMakeupAdapters[MAKEUP_BLUSH]!!.notifyDataSetChanged()
        mMakeupAdapters[MAKEUP_BROW]!!.setSelectedPosition(0)
        mMakeupAdapters[MAKEUP_BROW]!!.notifyDataSetChanged()
        mMakeupAdapters[MAKEUP_EYE]!!.setSelectedPosition(0)
        mMakeupAdapters[MAKEUP_EYE]!!.notifyDataSetChanged()
        mMakeupAdapters[MAKEUP_EYELINER]!!.setSelectedPosition(0)
        mMakeupAdapters[MAKEUP_EYELINER]!!.notifyDataSetChanged()
        mMakeupAdapters[MAKEUP_EYELASH]!!.setSelectedPosition(0)
        mMakeupAdapters[MAKEUP_EYELASH]!!.notifyDataSetChanged()
        mMakeupAdapters[MAKEUP_EYEBALL]!!.setSelectedPosition(0)
        mMakeupAdapters[MAKEUP_EYEBALL]!!.notifyDataSetChanged()

        mMakeupAdapters[MAKEUP_STYLE]!!.setSelectedPosition(0)
        mMakeupAdapters[MAKEUP_STYLE]!!.notifyDataSetChanged()
    }
}

