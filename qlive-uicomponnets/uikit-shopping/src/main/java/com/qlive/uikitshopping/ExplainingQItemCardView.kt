package com.qlive.uikitshopping

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.qlive.core.QLiveClient
import com.qlive.core.been.QExtension
import com.qlive.shoppingservice.QItem
import com.qlive.shoppingservice.QShoppingService
import com.qlive.shoppingservice.QShoppingServiceListener
import com.qlive.uikitcore.QKitCardView
import com.qlive.uikitcore.QLiveUIKitContext
import kotlinx.android.synthetic.main.kit_view_explaining_qitem.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 主播正在讲解的商品卡片
 *
 * @constructor Create empty Explaining q item view
 */
class ExplainingQItemCardView : QKitCardView {

    companion object {
        /**
         * 点击事件
         */
        var onItemClickListener: (context: QLiveUIKitContext?, client: QLiveClient?, view: View, item: QItem) -> Unit =
            { _, _, _, _ ->

            }

        /**
         * 展示时长
         */
        var displayTime = 8000L
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        visibility = View.GONE
    }

    override fun getLayoutId(): Int {
        return R.layout.kit_view_explaining_qitem
    }

    private var job: Job? = null

    private fun startShowJob() {
        job = kitContext?.lifecycleOwner?.lifecycleScope?.launch(Dispatchers.Main) {
            try {
                delay(displayTime)
                visibility = View.GONE
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        job?.start()
    }

    private val mShoppingServiceListener = object : QShoppingServiceListener {
        override fun onExplainingUpdate(item: QItem?) {
            if (item != null) {
                Glide.with(kitContext!!.androidContext)
                    .load(item.thumbnail)
                    .into(ivCover)
                tvNowPrice.text = item.currentPrice
                tvTitle.text = item.title
                tvOrder.text = item.order.toString()
                visibility = View.VISIBLE
                job?.cancel()
                startShowJob()
            } else {
                job?.cancel()
                visibility = View.GONE
            }
        }

        override fun onExtensionUpdate(item: QItem?, extension: QExtension?) {}
        override fun onItemListUpdate() {}
    }

    override fun initView() {
        setOnClickListener {
            client?.getService(QShoppingService::class.java)?.explaining?.let {
                onItemClickListener.invoke(
                    kitContext,
                    client,
                    this,
                    client!!.getService(QShoppingService::class.java).explaining
                )
            }
        }
        ivClose.setOnClickListener {
            job?.cancel()
            visibility = View.GONE
        }
        client?.getService(QShoppingService::class.java)
            ?.addServiceListener(mShoppingServiceListener)
    }

    override fun onDestroyed() {
        client?.getService(QShoppingService::class.java)
            ?.removeServiceListener(mShoppingServiceListener)
        super.onDestroyed()
    }
}