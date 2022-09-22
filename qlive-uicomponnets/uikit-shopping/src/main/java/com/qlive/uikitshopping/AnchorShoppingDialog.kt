package com.qlive.uikitshopping

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Paint
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.core.been.QExtension
import com.qlive.shoppingservice.QItem
import com.qlive.shoppingservice.QItem.RecordInfo.RECORD_STATUS_FINISHED
import com.qlive.shoppingservice.QItem.RecordInfo.RECORD_STATUS_RECORDING
import com.qlive.shoppingservice.QItemStatus
import com.qlive.shoppingservice.QShoppingService
import com.qlive.shoppingservice.QShoppingServiceListener
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.adapter.QRecyclerViewHolder
import com.qlive.uikitcore.dialog.CommonTipDialog
import com.qlive.uikitcore.dialog.FinalDialogFragment
import com.qlive.uikitcore.dialog.LoadingDialog
import com.qlive.uikitcore.ext.asToast
import com.qlive.uikitcore.ext.setDoubleCheckClickListener
import com.qlive.uikitcore.smartrecycler.QSmartAdapter
import com.qlive.uikitshopping.ui.flowlayout.FlowLayout
import com.qlive.uikitshopping.ui.flowlayout.TagAdapter
import kotlinx.android.synthetic.main.kit_dialog_abchor_shopping.*
import kotlinx.android.synthetic.main.kit_item_anchor_goods.view.*
import java.util.*

/**
 * 主播看到的商品弹窗
 *
 * @property kitContext
 * @property client
 * @constructor Create empty Anchor shopping dialog
 */
class AnchorShoppingDialog(
    private val kitContext: QLiveUIKitContext,
    private val client: QLiveClient
) : FinalDialogFragment() {

    /**
     * Companion静态配置
     * 用户自定义 UI和事件入口
     * @constructor Create empty Companion
     */
    companion object {
        /**
         * 自定义布局
         */
        var layoutId = R.layout.kit_dialog_abchor_shopping

        /**
         * 自定义列表适配
         */
        var adapterCreate: (context: QLiveUIKitContext, client: QLiveClient) -> QSmartAdapter<QItem>? =
            { _, _ ->
                null
            }
    }

    init {
        applyGravityStyle(Gravity.BOTTOM)
        applyDimAmount(0f)
    }

    private val shoppingService get() = client.getService(QShoppingService::class.java)!!
    private val adapter = adapterCreate.invoke(kitContext, client) ?: AnchorShoppingAdapter()
    var lastExplainingIndex = -1
    private val mShoppingRecordTimer = ShoppingRecordTimer(1000, action = { position, itemID ->
        (adapter.getViewByPosition(lastExplainingIndex, R.id.ivRecordPosition) as TextView?)?.let {
            it.text = formatTime(position)
        }
    })

    private val mShoppingServiceListener = object : QShoppingServiceListener {
        @SuppressLint("NotifyDataSetChanged")
        override fun onExplainingUpdate(item: QItem?) {
            if (item == null) {
                if (lastExplainingIndex >= 0) {
                    adapter.notifyItemChanged(lastExplainingIndex)
                    lastExplainingIndex = 0
                }
            } else {
                if (lastExplainingIndex >= 0) {
                    adapter.notifyItemChanged(lastExplainingIndex)
                }
                lastExplainingIndex = checkExpIndex();
                if (lastExplainingIndex < 0) {
                    return
                }
                adapter.notifyItemChanged(lastExplainingIndex)
            }
            mShoppingRecordTimer.checkStart(item)
        }

        override fun onExtensionUpdate(item: QItem, extension: QExtension) {
        }

        override fun onItemListUpdate() {

        }

        private fun checkExpIndex(): Int {
            var lastIndex = -1
            adapter.data.forEachIndexed { index, qItem ->
                if (shoppingService.explaining.itemID == qItem.itemID) {
                    lastIndex = index
                }
            }
            return lastIndex;
        }
    }

    override fun getViewLayoutId(): Int {
        return layoutId
    }

    private fun loadItem() {
        shoppingService.getItemList(object : QLiveCallBack<List<QItem>> {
            override fun onError(code: Int, msg: String) {
                recyclerViewGoods?.onFetchDataError()
            }

            override fun onSuccess(data: List<QItem>) {
                data.forEachIndexed { index, qItem ->
                    if (qItem.itemID == shoppingService.explaining?.itemID) {
                        lastExplainingIndex = index
                        recyclerViewGoods?.post {
                            mShoppingRecordTimer.checkStart(qItem)
                        }
                    }
                }
                recyclerViewGoods?.onFetchDataFinish(data, false)

            }
        })
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        mShoppingRecordTimer.cancel()
        shoppingService.removeServiceListener(mShoppingServiceListener)
    }

    override fun init() {
        shoppingService.addServiceListener(mShoppingServiceListener)
        recyclerViewGoods.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewGoods.setUp(adapter,  false, true) {
            loadItem()
        }
        //adapter.onAttachedToRecyclerView(recyclerViewGoods.recyclerView)
        tvManager.setOnClickListener {
            val d = ShoppingManagerDialog(kitContext, client)
            d.mDefaultListener = object : BaseDialogListener() {
                override fun onDismiss(dialog: DialogFragment) {
                    recyclerViewGoods.startRefresh()
                }
            }
            d.show(childFragmentManager, "")
        }
        recyclerViewGoods.startRefresh()
    }

    private fun formatTime(duration: Long): String {
        val milliseconds = duration * 1000
        if (milliseconds <= 0 || milliseconds >= 24 * 60 * 60 * 1000) {
            return "00:00"
        }
        val totalSeconds = milliseconds / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        val stringBuilder = StringBuilder()
        val mFormatter = Formatter(stringBuilder, Locale.getDefault())
        return if (hours > 0) {
            mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        } else {
            mFormatter.format("%02d:%02d", minutes, seconds).toString()
        }
    }

    private inner class AnchorShoppingAdapter : QSmartAdapter<QItem>(
        R.layout.kit_item_anchor_goods,
        ArrayList<QItem>()
    ) {

        override fun convert(helper: QRecyclerViewHolder, item: QItem) {
            Glide.with(mContext)
                .load(item.thumbnail)
                .into(helper.itemView.ivCover)
            helper.itemView.mAutoVoiceWaveView.attach(kitContext.lifecycleOwner)
            if (shoppingService.explaining?.itemID == item.itemID) {
                lastExplainingIndex = data.indexOf(item)
                helper.itemView.llItemShowing.visibility = View.VISIBLE
                helper.itemView.mAutoVoiceWaveView.setAutoPlay(true)
                helper.itemView.tvExplaining.text = getString(R.string.shopping_stop_explaining)
                helper.itemView.tvExplaining.setOnClickListener {
                    LoadingDialog.showLoading(kitContext.fragmentManager)
                    shoppingService.cancelExplaining(object : QLiveCallBack<Void> {
                        override fun onError(code: Int, msg: String?) {
                            msg?.asToast(context)
                            LoadingDialog.cancelLoadingDialog()
                        }

                        override fun onSuccess(data: Void?) {
                            LoadingDialog.cancelLoadingDialog()
                            recyclerViewGoods.startRefresh()
                        }
                    })
                }

            } else {
                helper.itemView.llItemShowing.visibility = View.GONE
                helper.itemView.mAutoVoiceWaveView.setAutoPlay(false)
                helper.itemView.tvExplaining.text = getString(R.string.shopping_explaining)
                helper.itemView.tvExplaining.setOnClickListener {
                    LoadingDialog.showLoading(kitContext.fragmentManager)
                    shoppingService.setExplaining(item, object : QLiveCallBack<Void> {
                        override fun onError(code: Int, msg: String?) {
                            msg?.asToast(context)
                            LoadingDialog.cancelLoadingDialog()
                        }

                        override fun onSuccess(data: Void?) {
                            LoadingDialog.cancelLoadingDialog()
                            recyclerViewGoods.recyclerView.smoothScrollToPosition(0)
                            recyclerViewGoods.startRefresh()
                        }
                    })
                }
            }
            helper.itemView.tvOrder.text = item.order.toString()
            helper.itemView.tvGoodsName.text = item.title
            helper.itemView.flGoodsTag.adapter =
                object : TagAdapter<TagItem>(
                    TagItem.strToTagItem(
                        item.tags
                    )
                ) {
                    override fun getView(parent: FlowLayout, position: Int, t: TagItem): View {
                        val v = LayoutInflater.from(context)
                            .inflate(R.layout.kit_item_goods_tag, parent, false)
                        (v as TextView).text = t.tagStr
                        v.setBackgroundResource(t.color)
                        return v
                    }
                }
            helper.itemView.tvNowPrice.text = item.currentPrice
            helper.itemView.tvOriginPrice.text = item.originPrice
            helper.itemView.tvOriginPrice.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG;
            helper.itemView.tvPull.setOnClickListener { }

            if (item.status == QItemStatus.PULLED.value) {
                helper.itemView.tvExplaining.isSelected = false
                helper.itemView.tvExplaining.isClickable = false
                //已经下架
                helper.itemView.tvPulledCover.visibility = View.VISIBLE
                helper.itemView.tvPull.text = getString(R.string.shopping_go_sale)
                helper.itemView.tvPull.setOnClickListener {
                    showTip(getString(R.string.shopping_is_confirm_sale)) {
                        LoadingDialog.showLoading(kitContext.fragmentManager)
                        shoppingService.updateItemStatus(item.itemID, QItemStatus.ON_SALE,
                            object : QLiveCallBack<Void> {
                                override fun onError(code: Int, msg: String?) {
                                    msg?.asToast(context)
                                    LoadingDialog.cancelLoadingDialog()
                                }

                                override fun onSuccess(v: Void?) {
                                    LoadingDialog.cancelLoadingDialog()
                                    item.status = QItemStatus.ON_SALE.value
                                    notifyItemChanged(data.indexOf(item))
                                }
                            })
                    }
                }
            }

            if (item.status == QItemStatus.ON_SALE.value || item.status == QItemStatus.ONLY_DISPLAY.value) {
                //已经上架
                helper.itemView.tvExplaining.isClickable = true
                helper.itemView.tvExplaining.isSelected = true
                helper.itemView.tvPulledCover.visibility = View.GONE
                helper.itemView.tvPull.text = getString(R.string.shopping_go_pulled)

                helper.itemView.tvPull.setOnClickListener {
                    if (shoppingService.explaining?.itemID == item.itemID) {
                        getString(R.string.shopping_tip_explaining).asToast(context)
                        return@setOnClickListener
                    }
                    showTip(getString(R.string.shopping_is_confirm_pulled)) {
                        LoadingDialog.showLoading(kitContext.fragmentManager)
                        shoppingService.updateItemStatus(item.itemID, QItemStatus.PULLED,
                            object : QLiveCallBack<Void> {
                                override fun onError(code: Int, msg: String?) {
                                    msg?.asToast(context)
                                    LoadingDialog.cancelLoadingDialog()
                                }

                                override fun onSuccess(v: Void?) {
                                    LoadingDialog.cancelLoadingDialog()
                                    item.status = QItemStatus.PULLED.value
                                    notifyItemChanged(data.indexOf(item))
                                }
                            })
                    }
                }
            }

            when (item.record?.status) {
                RECORD_STATUS_FINISHED -> {
                    helper.itemView.tvStartRecord.visibility = View.GONE
                    helper.itemView.llRecording.visibility = View.GONE
                    helper.itemView.llRecorded.visibility = View.VISIBLE
                    helper.itemView.ivRecordDuration.text =
                        formatTime((item.record?.end ?: 0L) - (item.record?.start ?: 0L))
                }
                RECORD_STATUS_RECORDING -> {
                    helper.itemView.tvStartRecord.visibility = View.GONE
                    helper.itemView.llRecording.visibility = View.VISIBLE
                    helper.itemView.llRecorded.visibility = View.GONE
                    helper.itemView.ivRecordPosition.text =
                        formatTime((System.currentTimeMillis()/1000) - (item.record?.start ?: 0L))
                }
                else -> {
                    helper.itemView.tvStartRecord.visibility = View.GONE
                    helper.itemView.llRecording.visibility = View.GONE
                    helper.itemView.llRecorded.visibility = View.GONE
                }
            }
            helper.itemView.llRecorded.setDoubleCheckClickListener {
                showTip(mContext.getString(R.string.shopping_is_confirm_remove_record)){
                    client.getService(QShoppingService::class.java)
                        .deleteRecord(listOf(item.record?.id), object : QLiveCallBack<Void> {
                            override fun onError(code: Int, msg: String?) {
                                msg?.asToast(context)
                            }

                            override fun onSuccess(data: Void?) {
                                recyclerViewGoods.startRefresh()
                            }
                        })
                }
            }
            if (shoppingService.explaining?.itemID == item.itemID
                && item.record == null
            ) {
                helper.itemView.tvStartRecord.visibility = View.VISIBLE
                helper.itemView.tvStartRecord.setOnClickListener {
                    client.getService(QShoppingService::class.java)
                        .startRecord(object : QLiveCallBack<Void> {
                            override fun onError(code: Int, msg: String?) {
                               msg?.asToast(context)
                            }

                            override fun onSuccess(data: Void?) {
                                recyclerViewGoods.startRefresh()
                            }
                        })
                }
            } else {
                helper.itemView.tvStartRecord.visibility = View.GONE
                helper.itemView.tvStartRecord.setOnClickListener(null)
            }
        }

        private fun showTip(tip: String, call: () -> Unit) {
            CommonTipDialog.TipBuild()
                .setTittle(tip)
                .setListener(object : BaseDialogListener() {
                    override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                        super.onDialogPositiveClick(dialog, any)
                        call.invoke()
                    }
                }).build("AnchorShoppingDialog_manager").show(childFragmentManager, "")
        }
    }
}