package com.qlive.uiwidghtbeauty.ui

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.SeekBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.qlive.uiwidghtbeauty.QSenseTimeManager.sSenseTimePlugin
import com.qlive.uiwidghtbeauty.R
import com.qlive.uiwidghtbeauty.adapter.BeautyItemAdapter
import com.qlive.uiwidghtbeauty.model.BeautyItem
import com.qlive.uiwidghtbeauty.utils.Constants
import com.qlive.uiwidghtbeauty.utils.Constants.*
import com.qlive.uiwidghtbeauty.utils.ResourcesUtil
import com.qlive.uiwidghtbeauty.utils.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

/**
 * 基础美颜
 */
abstract class CommonBeautyPage : FrameLayout, BaseEffectPage<BeautyItem> {

    private lateinit var mIndicatorSeekbar: IndicatorSeekBar
    private lateinit var mRecyclerView: RecyclerView

    protected val beautyBaseAdapter by lazy { BeautyItemAdapter(context, beautyBaseItemList) }
    val beautyBaseItemList: ArrayList<BeautyItem> = ArrayList<BeautyItem>()
    override var onItemClick: (groupIndex: String, item: BeautyItem, itemIndex: Int) -> Unit =
        { _, _, _ ->
        }
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        LayoutInflater.from(context).inflate(R.layout.kit_common_beauty_page, this, true)
        init()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun init() {
        mRecyclerView = findViewById(R.id.rv_beauty_base)
        mIndicatorSeekbar = findViewById(R.id.beauty_item_seekbar)
        GlobalScope.launch(Dispatchers.Main) {
            val ret = async(Dispatchers.IO) {
                beautyBaseItemList.clear()
                beautyBaseItemList.addAll(loadResource())
            }
            ret.await()
            val ms = LinearLayoutManager(context)
            ms.orientation = LinearLayoutManager.HORIZONTAL
            mRecyclerView.layoutManager = ms
            mRecyclerView.addItemDecoration(
                BeautyItemDecoration(
                    Utils.dip2px(
                        context,
                        15f
                    )
                )
            )
            mRecyclerView.adapter = beautyBaseAdapter
            beautyBaseAdapter.setClickBeautyListener { v ->
                mIndicatorSeekbar.visibility = VISIBLE
                val position = v.tag.toString().toInt()
                beautyBaseAdapter.selectedPosition = position
                if (checkMicroType(position)) {
                    mIndicatorSeekbar.seekBar.progress =
                        Utils.convertToData(beautyBaseItemList[position].progress)
                } else {
                    mIndicatorSeekbar.seekBar.progress = beautyBaseItemList[position].progress
                }
                mIndicatorSeekbar.updateTextView(
                    beautyBaseItemList[position].progress
                )
                onItemClick.invoke(
                    getGroupIndex(),
                    beautyBaseItemList[position],
                    position
                )
                beautyBaseAdapter.notifyDataSetChanged()
            }
            mIndicatorSeekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                    val selectedPosition = beautyBaseAdapter.selectedPosition

                    if (selectedPosition < 0 || !fromUser) {
                        return
                    }
                    val mCurrentBeautyIndex = ResourcesUtil.calculateBeautyIndex(
                        getBeautyOptionsPosition(),
                        selectedPosition
                    )

                    if (checkMicroType(selectedPosition)) {
                        mIndicatorSeekbar.updateTextView(Utils.convertToDisplay(progress))
                        // 设置美颜强度，强度范围是 [-1,1]
                        sSenseTimePlugin!!.setBeauty(
                            Constants.BEAUTY_TYPES[mCurrentBeautyIndex],
                            Utils.convertToDisplay(progress).toFloat() / 100f
                        )
                        beautyBaseItemList[selectedPosition].progress =
                            Utils.convertToDisplay(progress)
                    } else {
                        mIndicatorSeekbar.updateTextView(progress)
                        // 设置美颜强度，强范围度是 [0,1]
                        sSenseTimePlugin!!.setBeauty(
                            Constants.BEAUTY_TYPES[mCurrentBeautyIndex],
                            progress.toFloat() / 100f
                        )
                        beautyBaseItemList[selectedPosition].progress = progress
                    }
                    beautyBaseAdapter
                        .notifyItemChanged(selectedPosition)
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                }

                override fun onStopTrackingTouch(p0: SeekBar?) {
                }
            })
        }
    }

    open fun checkMicroType(itemIndex: Int): Boolean {
        val type = ResourcesUtil.calculateBeautyIndex(getBeautyOptionsPosition(), itemIndex)
        val ans = (type != BEAUTY_BASE_REDDEN && type != BEAUTY_RESHAPE_SHRINK_FACE
                && type != BEAUTY_PLASTIC_THIN_FACE && type != BEAUTY_PLASTIC_HAIRLINE_HEIGHT
                && type != BEAUTY_PLASTIC_APPLE_MUSLE && type != BEAUTY_PLASTIC_NARROW_NOSE
                && type != BEAUTY_PLASTIC_NOSE_LENGTH && type != BEAUTY_PLASTIC_PROFILE_RHINOPLASTY
                && type != BEAUTY_BASE_FACE_SMOOTH && type != BEAUTY_PLASTIC_MOUTH_SIZE)
        return ans && getBeautyOptionsPosition()==2
    }

    override fun reset() {
        beautyBaseAdapter.selectedPosition = -1
        mIndicatorSeekbar.visibility = INVISIBLE
    }

    abstract fun getGroupIndex(): String

    abstract fun loadResource(): List<BeautyItem>
    abstract fun getBeautyOptionsPosition(): Int
}

/**
 * 基础美颜
 */
class BaseBeautyPage : CommonBeautyPage {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun getGroupIndex(): String {
        return Constants.BASE_BEAUTY
    }

    override fun loadResource(): List<BeautyItem> {
        return (ResourcesUtil.getBeautyBaseItemList(context))
    }

    override fun getBeautyOptionsPosition(): Int {
        return 0
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun reset() {
        super.reset()
        // 基础美颜
        // 基础美颜
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_BASE_WHITTEN],
            ResourcesUtil.sBeautifyParams[0]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_BASE_REDDEN],
            ResourcesUtil.sBeautifyParams[1]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_BASE_FACE_SMOOTH],
            ResourcesUtil.sBeautifyParams[2]
        )
        // 基础美颜
        // 基础美颜
        beautyBaseItemList[0].progress = (ResourcesUtil.sBeautifyParams[0] * 100).toInt()
        beautyBaseItemList[1].progress = (ResourcesUtil.sBeautifyParams[1] * 100).toInt()
        beautyBaseItemList[2].progress = (ResourcesUtil.sBeautifyParams[2] * 100).toInt()
        beautyBaseAdapter.notifyDataSetChanged()
    }
}

/**
 * 美型
 */
class ProfessionalBeautyPage : CommonBeautyPage {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun getGroupIndex(): String {
        return PROFESSIONAL_BEAUTY
    }

    override fun loadResource(): List<BeautyItem> {
        return (ResourcesUtil.getProfessionalBeautyItemList(context))
    }

    override fun getBeautyOptionsPosition(): Int {
        return 1
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun reset() {
        super.reset()
        // 美形
        // 美形
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_RESHAPE_SHRINK_FACE],
            ResourcesUtil.sBeautifyParams[3]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_RESHAPE_ENLARGE_EYE],
            ResourcesUtil.sBeautifyParams[4]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_RESHAPE_SHRINK_JAW],
            ResourcesUtil.sBeautifyParams[5]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_RESHAPE_NARROW_FACE],
            ResourcesUtil.sBeautifyParams[6]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_RESHAPE_ROUND_EYE],
            ResourcesUtil.sBeautifyParams[7]
        )
        // 美形
        beautyBaseItemList[0].progress = (ResourcesUtil.sBeautifyParams[3] * 100).toInt()
        beautyBaseItemList[1].progress = (ResourcesUtil.sBeautifyParams[4] * 100).toInt()
        beautyBaseItemList[2].progress = (ResourcesUtil.sBeautifyParams[5] * 100).toInt()
        beautyBaseItemList[3].progress = (ResourcesUtil.sBeautifyParams[6] * 100).toInt()
        beautyBaseItemList[4].progress = (ResourcesUtil.sBeautifyParams[7] * 100).toInt()
        beautyBaseAdapter.notifyDataSetChanged()
    }
}

/**
 * 微调整
 */
class MicroBeautyPage : CommonBeautyPage {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun getGroupIndex(): String {
        return Constants.MICRO_BEAUTY
    }

    override fun loadResource(): List<BeautyItem> {
        return (ResourcesUtil.getMicroBeautyItemList(context))
    }

    override fun getBeautyOptionsPosition(): Int {
        return 2
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun reset() {
        super.reset()
        // 微整形
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_PLASTIC_THIN_FACE],
            ResourcesUtil.sBeautifyParams[8]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_PLASTIC_CHIN_LENGTH],
            ResourcesUtil.sBeautifyParams[9]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_PLASTIC_HAIRLINE_HEIGHT],
            ResourcesUtil.sBeautifyParams[10]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_PLASTIC_APPLE_MUSLE],
            ResourcesUtil.sBeautifyParams[11]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_PLASTIC_NARROW_NOSE],
            ResourcesUtil.sBeautifyParams[12]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_PLASTIC_NOSE_LENGTH],
            ResourcesUtil.sBeautifyParams[13]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_PLASTIC_PROFILE_RHINOPLASTY],
            ResourcesUtil.sBeautifyParams[14]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_PLASTIC_MOUTH_SIZE],
            ResourcesUtil.sBeautifyParams[15]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_PLASTIC_PHILTRUM_LENGTH],
            ResourcesUtil.sBeautifyParams[16]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_PLASTIC_EYE_DISTANCE],
            ResourcesUtil.sBeautifyParams[17]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_PLASTIC_EYE_ANGLE],
            ResourcesUtil.sBeautifyParams[18]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_PLASTIC_OPEN_CANTHUS],
            ResourcesUtil.sBeautifyParams[19]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_PLASTIC_BRIGHT_EYE],
            ResourcesUtil.sBeautifyParams[20]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_PLASTIC_REMOVE_DARK_CIRCLES],
            ResourcesUtil.sBeautifyParams[21]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_PLASTIC_REMOVE_NASOLABIAL_FOLDS],
            ResourcesUtil.sBeautifyParams[22]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_PLASTIC_WHITE_TEETH],
            ResourcesUtil.sBeautifyParams[23]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_PLASTIC_SHRINK_CHEEKBONE],
            ResourcesUtil.sBeautifyParams[24]
        )
        // 微整形
        beautyBaseItemList[0].progress = (ResourcesUtil.sBeautifyParams[8] * 100).toInt()
        beautyBaseItemList[1].progress = (ResourcesUtil.sBeautifyParams[9] * 100).toInt()
        beautyBaseItemList[2].progress = (ResourcesUtil.sBeautifyParams[10] * 100).toInt()
        beautyBaseItemList[3].progress = (ResourcesUtil.sBeautifyParams[11] * 100).toInt()
        beautyBaseItemList[4].progress = (ResourcesUtil.sBeautifyParams[12] * 100).toInt()
        beautyBaseItemList[5].progress = (ResourcesUtil.sBeautifyParams[13] * 100).toInt()
        beautyBaseItemList[6].progress = (ResourcesUtil.sBeautifyParams[14] * 100).toInt()
        beautyBaseItemList[7].progress = (ResourcesUtil.sBeautifyParams[15] * 100).toInt()
        beautyBaseItemList[8].progress = (ResourcesUtil.sBeautifyParams[16] * 100).toInt()
        beautyBaseItemList[9].progress = (ResourcesUtil.sBeautifyParams[17] * 100).toInt()
        beautyBaseItemList[10].progress = (ResourcesUtil.sBeautifyParams[18] * 100).toInt()
        beautyBaseItemList[11].progress = (ResourcesUtil.sBeautifyParams[19] * 100).toInt()
        beautyBaseItemList[12].progress = (ResourcesUtil.sBeautifyParams[20] * 100).toInt()
        beautyBaseItemList[13].progress = (ResourcesUtil.sBeautifyParams[21] * 100).toInt()
        beautyBaseItemList[14].progress = (ResourcesUtil.sBeautifyParams[22] * 100).toInt()
        beautyBaseItemList[15].progress = (ResourcesUtil.sBeautifyParams[23] * 100).toInt()
        beautyBaseItemList[16].progress = (ResourcesUtil.sBeautifyParams[24] * 100).toInt()

        beautyBaseAdapter.notifyDataSetChanged()
    }
}

/**
 * 调整
 */
class AdjustBeautyPage : CommonBeautyPage {
    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    override fun getGroupIndex(): String {
        return Constants.ADJUST_BEAUTY
    }

    override fun loadResource(): List<BeautyItem> {
        return (ResourcesUtil.getAdjustBeautyItemList(context))
    }

    override fun getBeautyOptionsPosition(): Int {
        return 5
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun reset() {
        super.reset()
        // 调整
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_TONE_CONTRAST],
            ResourcesUtil.sBeautifyParams[25]
        )
        sSenseTimePlugin!!.setBeauty(
            Constants.BEAUTY_TYPES[Constants.BEAUTY_TONE_SATURATION],
            ResourcesUtil.sBeautifyParams[26]
        )
        // 调整
        beautyBaseItemList[0].progress = (ResourcesUtil.sBeautifyParams[25] * 100).toInt()
        beautyBaseItemList[1].progress = (ResourcesUtil.sBeautifyParams[26] * 100).toInt()
        beautyBaseAdapter.notifyDataSetChanged()
    }
}

