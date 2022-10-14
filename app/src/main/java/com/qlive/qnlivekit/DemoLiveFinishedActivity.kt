package com.qlive.qnlivekit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveStatistics
import com.qlive.sdk.QLive
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.adapter.QRecyclerViewHolder
import com.qlive.uikitcore.smartrecycler.QSmartAdapter
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.android.synthetic.main.activity_demo_live_finished.*
import kotlinx.android.synthetic.main.item_statistics_small.view.tvKey
import kotlinx.android.synthetic.main.item_statistics_small.view.tvValues

/**
 * 自定义开播结束页面
 *
 * @constructor Create empty Demo live finished activity
 */
class DemoLiveFinishedActivity : AppCompatActivity() {

    companion object {
        fun checkStart(
            context: Context,
            room: QLiveRoomInfo,
        ) {
            val intent = Intent(context, DemoLiveFinishedActivity::class.java)
            intent.putExtra("QLiveRoomInfo", room)
            context.startActivity(intent)
        }
    }

    private val mStatisticsAdapter by lazy { StatisticsAdapter() }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_live_finished)
        ivClose.setOnClickListener {
            finish()
        }
        recyLiveData.layoutManager = GridLayoutManager(this, 3)
        recyLiveData.adapter = mStatisticsAdapter
        (intent.getSerializableExtra("QLiveRoomInfo") as QLiveRoomInfo?)?.let { roomInfo ->
            llLiveData.setOnClickListener {
                DemoStatisticsActivity.checkStart(this@DemoLiveFinishedActivity, roomInfo)
            }
            Glide.with(this)
                .load(roomInfo.coverURL)
                .transform(MultiTransformation(CenterCrop(), BlurTransformation(25, 3)))
                .into(ivRoomCover)
            Glide.with(this)
                .load(roomInfo.anchor?.avatar)
                .into(ivAnchorAvatar)
            tvAnchorName.text = roomInfo.anchor?.nick ?: ""
            tvAnchorID.text = "主播ID：${roomInfo.anchor.userId}"
            QLive.getRooms()
                .getLiveStatistics(roomInfo.liveID, object : QLiveCallBack<QLiveStatistics> {
                    override fun onError(code: Int, msg: String?) {
                    }

                    override fun onSuccess(data: QLiveStatistics) {
                        mStatisticsAdapter.setNewData(data.toQLiveStatisticsWrap())
                    }
                })
        }
    }

    fun QLiveStatistics.toQLiveStatisticsWrap(): MutableList<QLiveStatisticsWrap> {
        val wraps = ArrayList<QLiveStatisticsWrap>()
        this.info.forEach {
            if (it.type == QLiveStatistics.TYPE_LIVE_WATCHER_COUNT) {
                wraps.add(QLiveStatisticsWrap("浏览次数", it.pageView.toFormatNumber()))
                wraps.add(QLiveStatisticsWrap("观看人数", it.uniqueVisitor.toFormatNumber()))
            }
            if (it.type == QLiveStatistics.TYPE_PUBCHAT_COUNT) {
                wraps.add(QLiveStatisticsWrap("聊天互动", it.pageView.toFormatNumber()))
            }

            if (it.type == QLiveStatistics.TYPE_LIKE_COUNT) {
                wraps.add(QLiveStatisticsWrap("点赞", it.pageView.toFormatNumber()))
            }
            if (it.type == QLiveStatistics.TYPE_GIFT_COUNT) {
                wraps.add(QLiveStatisticsWrap("观众打赏", it.pageView.toFormatNumber()))
            }
        }
        return wraps
    }

    class StatisticsAdapter : QSmartAdapter<QLiveStatisticsWrap>(
        R.layout.item_statistics_small
    ) {
        override fun convert(helper: QRecyclerViewHolder, item: QLiveStatisticsWrap) {
            helper.itemView.tvKey.text = item.key
            helper.itemView.tvValues.text = item.value
        }
    }
}