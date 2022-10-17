package com.qlive.uikitgift

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.giftservice.QGift
import com.qlive.giftservice.QGiftService
import com.qlive.sdk.QLive
import com.qlive.uikitcore.adapter.QRecyclerViewBindHolder
import com.qlive.uikitcore.dialog.ViewBindingDialogFragment
import com.qlive.uikitcore.ext.asToast
import com.qlive.uikitcore.smartrecycler.QSmartViewBindAdapter
import com.qlive.uikitgift.databinding.KitDialogGiftBinding
import com.qlive.uikitgift.databinding.KitItemGiftBinding

class GiftDialog(private val client: QLiveClient) :
    ViewBindingDialogFragment<KitDialogGiftBinding>() {

    init {
        applyDimAmount(0f)
        applyGravityStyle(Gravity.BOTTOM)
    }

    companion object {
        /**
         *
         * 发送礼物拦截
         * 返回是否允许发送
         */
        var payInterceptor: (gift: QGift) -> Boolean = {
            true
        }

        /**
         * 发送礼物失败结果回调
         * 业务方可以根据错误码处理 比如跳转余额不足充值
         * @param gift 礼物
         * @param code
         * @param msg
         */
        var payErrorCallBack: (context: Context, gift: QGift, code: Int, msg: String) -> Unit =
            { c, _, _, m ->
                m.asToast(c)
            }
    }

    private var adapter = GiftAdapter()

    private val payToast by lazy {
        Toast(context).apply {
            setGravity(Gravity.CENTER, 0, 0)
            duration = Toast.LENGTH_SHORT
            view = LayoutInflater.from(requireContext())
                .inflate(R.layout.kit_toast_pay_succes, null, false)
        }
    }

    private fun showPaySuccessToast() {
        payToast.show()
    }

    override fun init() {

        binding.giftRecycler.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.giftRecycler.adapter = adapter

        binding.ivClose.setOnClickListener {
            dismiss()
        }
        QLive.getRooms().getGiftConfig(-1, object : QLiveCallBack<List<QGift>> {
            override fun onError(code: Int, msg: String?) {
                msg?.asToast(requireContext())
            }

            override fun onSuccess(data: List<QGift>) {
                adapter.setNewData(data)
            }
        })
        adapter.payCall = { gift ->
            val payCall = {
                if (payInterceptor.invoke(gift)) {
                    val service = client.getService(QGiftService::class.java)
                    service.sendGift(gift.giftID, gift.amount, object : QLiveCallBack<Void> {
                        override fun onError(code: Int, msg: String) {
                            payErrorCallBack.invoke(requireContext(), gift, code, msg)
                        }

                        override fun onSuccess(data: Void?) {
                            showPaySuccessToast()
                        }
                    })
                }
            }
            if (gift.amount == 0) {
                SenderRedPackageDialog()
                    .apply {
                        mDefaultListener = object : BaseDialogListener() {
                            override fun onDialogPositiveClick(dialog: DialogFragment, any: Any) {
                                super.onDialogPositiveClick(dialog, any)
                                payCall.invoke()
                            }
                        }
                    }
                    .show(childFragmentManager, "")
            } else {
                payCall.invoke()
            }
        }
    }

    class GiftAdapter : QSmartViewBindAdapter<QGift, KitItemGiftBinding>() {
        private var mSelectIndex = -1
        var payCall: (item: QGift) -> Unit = {

        }

        override fun convertViewBindHolder(
            helper: QRecyclerViewBindHolder<KitItemGiftBinding>,
            item: QGift
        ) {
            if (data.indexOf(item) == mSelectIndex) {
                helper.binding.tvFLGiftContent.isSelected = true
                helper.binding.tvPriceBig.visibility = View.VISIBLE
                helper.binding.tvPay.visibility = View.VISIBLE
                helper.binding.tvPriceSmall.visibility = View.INVISIBLE
                helper.binding.tvGiftName.visibility = View.INVISIBLE
            } else {
                helper.binding.tvFLGiftContent.isSelected = false
                helper.binding.tvPriceBig.visibility = View.INVISIBLE
                helper.binding.tvPay.visibility = View.GONE
                helper.binding.tvPriceSmall.visibility = View.VISIBLE
                helper.binding.tvGiftName.visibility = View.VISIBLE
            }

            helper.binding.tvPriceBig.text = item.amount.toString()
            helper.binding.tvPriceSmall.text = item.amount.toString()
            helper.binding.tvGiftName.text = item.name.toString()
            Glide.with(mContext)
                .load(item.img)
                .into(helper.binding.ivGiftIcon)
            if (item.amount == 0) {
                helper.binding.tvPriceBig.visibility = View.INVISIBLE
                helper.binding.tvPriceSmall.visibility = View.INVISIBLE
            }
//            helper.binding.tvPay.setOnClickListener {
//                payCall.invoke(item)
//            }
            helper.binding.tvFLGiftContent.setOnClickListener {

                val oldIndex = mSelectIndex
                val newIndex = data.indexOf(item)
                if (oldIndex == newIndex) {
                    payCall.invoke(item)
                    return@setOnClickListener
                }
                mSelectIndex = newIndex
                notifyItemChanged(oldIndex)
                notifyItemChanged(newIndex)
            }
        }
    }
}