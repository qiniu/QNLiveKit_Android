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
import com.qlive.uikitcore.adapter.QRecyclerViewBindHolder
import com.qlive.uikitcore.dialog.CommonTipDialog
import com.qlive.uikitcore.dialog.LoadingDialog
import com.qlive.uikitcore.dialog.ViewBindingDialogFragment
import com.qlive.uikitcore.ext.asToast
import com.qlive.uikitcore.ext.setDoubleCheckClickListener
import com.qlive.uikitcore.smartrecycler.QSmartViewBindAdapter
import com.qlive.uikitshopping.databinding.KitDialogAnchorShoppingBinding
import com.qlive.uikitshopping.databinding.KitItemAnchorGoodsBinding
import com.qlive.uikitcore.view.flowlayout.FlowLayout
import com.qlive.uikitcore.view.flowlayout.TagAdapter
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
) : ViewBindingDialogFragment<KitDialogAnchorShoppingBinding>() {

    /**
     * Companion静态配置
     * 用户自定义 UI和事件入口
     * @constructor Create empty Companion
     */
    companion object {
        /**
         * 自定义列表适配
         */
        var adapterCreate: (context: QLiveUIKitContext, client: QLiveClient) -> QSmartViewBindAdapter<QItem,*>? =
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

    private fun loadItem() {
        shoppingService.getItemList(object : QLiveCallBack<List<QItem>> {
            override fun onError(code: Int, msg: String) {
                binding.recyclerViewGoods.onFetchDataError()
            }

            override fun onSuccess(data: List<QItem>) {
                data.forEachIndexed { index, qItem ->
                    if (qItem.itemID == shoppingService.explaining?.itemID) {
                        lastExplainingIndex = index
                        binding.recyclerViewGoods.post {
                            mShoppingRecordTimer.checkStart(qItem)
                        }
                    }
                }
                binding.recyclerViewGoods.onFetchDataFinish(data, false)

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
        binding.recyclerViewGoods.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewGoods.setUp(adapter, false, true) {
            loadItem()
        }
        //adapter.onAttachedToRecyclerView(recyclerViewGoods.recyclerView)
        binding.tvManager.setOnClickListener {
            val d = ShoppingManagerDialog(kitContext, client)
            d.mDefaultListener = object : BaseDialogListener() {
                override fun onDismiss(dialog: DialogFragment) {
                    binding.recyclerViewGoods.startRefresh()
                }
            }
            d.show(childFragmentManager, "")
        }
        binding.recyclerViewGoods.startRefresh()
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

    private inner class AnchorShoppingAdapter :
        QSmartViewBindAdapter<QItem, KitItemAnchorGoodsBinding>() {

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

        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<KitItemAnchorGoodsBinding>,
            item: QItem
        ) {
            Glide.with(mContext)
                .load(item.thumbnail)
                .into(helper.binding.ivCover)
            helper.binding.mAutoVoiceWaveView.attach(kitContext.lifecycleOwner)
            if (shoppingService.explaining?.itemID == item.itemID) {
                lastExplainingIndex = data.indexOf(item)
                helper.binding.llItemShowing.visibility = View.VISIBLE
                helper.binding.mAutoVoiceWaveView.setAutoPlay(true)
                helper.binding.tvExplaining.text = getString(R.string.shopping_stop_explaining)
                helper.binding.tvExplaining.setOnClickListener {
                    LoadingDialog.showLoading(kitContext.fragmentManager)
                    shoppingService.cancelExplaining(object : QLiveCallBack<Void> {
                        override fun onError(code: Int, msg: String?) {
                            msg?.asToast(context)
                            LoadingDialog.cancelLoadingDialog()
                        }

                        override fun onSuccess(data: Void?) {
                            LoadingDialog.cancelLoadingDialog()
                            binding.recyclerViewGoods.startRefresh()
                        }
                    })
                }
            } else {
                helper.binding.llItemShowing.visibility = View.GONE
                helper.binding.mAutoVoiceWaveView.setAutoPlay(false)
                helper.binding.tvExplaining.text = getString(R.string.shopping_explaining)
                helper.binding.tvExplaining.setOnClickListener {
                    LoadingDialog.showLoading(kitContext.fragmentManager)
                    shoppingService.setExplaining(item, object : QLiveCallBack<Void> {
                        override fun onError(code: Int, msg: String?) {
                            msg?.asToast(context)
                            LoadingDialog.cancelLoadingDialog()
                        }

                        override fun onSuccess(data: Void?) {
                            LoadingDialog.cancelLoadingDialog()
                            binding.recyclerViewGoods.recyclerView.smoothScrollToPosition(0)
                            binding.recyclerViewGoods.startRefresh()
                        }
                    })
                }
            }
            helper.binding.tvOrder.text = item.order.toString()
            helper.binding.tvGoodsName.text = item.title
            helper.binding.flGoodsTag.adapter =
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
            helper.binding.tvNowPrice.text = item.currentPrice
            helper.binding.tvOriginPrice.text = item.originPrice
            helper.binding.tvOriginPrice.paint.flags = Paint.STRIKE_THRU_TEXT_FLAG;
            helper.binding.tvPull.setOnClickListener { }

            if (item.status == QItemStatus.PULLED.value) {
                helper.binding.tvExplaining.isSelected = false
                helper.binding.tvExplaining.isClickable = false
                //已经下架
                helper.binding.tvPulledCover.visibility = View.VISIBLE
                helper.binding.tvPull.text = getString(R.string.shopping_go_sale)
                helper.binding.tvPull.setOnClickListener {
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
                helper.binding.tvExplaining.isClickable = true
                helper.binding.tvExplaining.isSelected = true
                helper.binding.tvPulledCover.visibility = View.GONE
                helper.binding.tvPull.text = getString(R.string.shopping_go_pulled)

                helper.binding.tvPull.setOnClickListener {
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
                    helper.binding.tvStartRecord.visibility = View.GONE
                    helper.binding.llRecording.visibility = View.GONE
                    helper.binding.llRecorded.visibility = View.VISIBLE
                    helper.binding.ivRecordDuration.text =
                        formatTime((item.record?.end ?: 0L) - (item.record?.start ?: 0L))
                }
                RECORD_STATUS_RECORDING -> {
                    helper.binding.tvStartRecord.visibility = View.GONE
                    helper.binding.llRecording.visibility = View.VISIBLE
                    helper.binding.llRecorded.visibility = View.GONE
                    helper.binding.ivRecordPosition.text =
                        formatTime((System.currentTimeMillis() / 1000) - (item.record?.start ?: 0L))
                }
                else -> {
                    helper.binding.tvStartRecord.visibility = View.GONE
                    helper.binding.llRecording.visibility = View.GONE
                    helper.binding.llRecorded.visibility = View.GONE
                }
            }
            helper.binding.llRecorded.setDoubleCheckClickListener {
                showTip(mContext.getString(R.string.shopping_is_confirm_remove_record)) {
                    client.getService(QShoppingService::class.java)
                        .deleteRecord(listOf(item.record?.id), object : QLiveCallBack<Void> {
                            override fun onError(code: Int, msg: String?) {
                                msg?.asToast(context)
                            }

                            override fun onSuccess(data: Void?) {
                                binding.recyclerViewGoods.startRefresh()
                            }
                        })
                }
            }
            if (shoppingService.explaining?.itemID == item.itemID
                && item.record == null
            ) {
                helper.binding.tvStartRecord.visibility = View.VISIBLE
                helper.binding.tvStartRecord.setOnClickListener {
                    client.getService(QShoppingService::class.java)
                        .startRecord(object : QLiveCallBack<Void> {
                            override fun onError(code: Int, msg: String?) {
                                msg?.asToast(context)
                            }

                            override fun onSuccess(data: Void?) {
                                binding.recyclerViewGoods.startRefresh()
                            }
                        })
                }
            } else {
                helper.binding.tvStartRecord.visibility = View.GONE
                helper.binding.tvStartRecord.setOnClickListener(null)
            }
        }
    }
}