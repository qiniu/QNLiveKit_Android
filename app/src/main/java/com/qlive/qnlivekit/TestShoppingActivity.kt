package com.qlive.qnlivekit

import android.content.Intent
import com.bumptech.glide.Glide
import com.qlive.qnlivekit.databinding.ActivityTestShoppingActvityBinding
import com.qlive.shoppingservice.QItem
import com.qlive.uikit.component.FloatingModel
import com.qlive.uikit.component.FuncCPTPlayerFloatingHandler
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.activity.BaseBindingActivity

/**
 * 测试小窗购物页面
 *
 * @constructor Create empty Test shopping activity
 */
class TestShoppingActivity : BaseBindingActivity<ActivityTestShoppingActvityBinding>() {

    companion object {
        fun start(context: QLiveUIKitContext, item: QItem) {
            val floatCPT = context.getLiveFuncComponent(FuncCPTPlayerFloatingHandler::class.java)
            if (floatCPT == null) {
                val intent = Intent(context.currentActivity, TestShoppingActivity::class.java)
                intent.putExtra("QItem", item)
                context.currentActivity.startActivity(intent)
            } else {
                floatCPT.create(FloatingModel.GO_NEXT_PAGE) { succeed: Boolean, msg: String ->
                    val intent = Intent(context.currentActivity, TestShoppingActivity::class.java)
                    intent.putExtra("QItem", item)
                    context.currentActivity.startActivity(intent)
                }
            }
        }
    }

    override fun init() {
        binding.ivGoodsBack.setOnClickListener {
            onBackPressed()
        }
        intent.getSerializableExtra("QItem")?.let {
            (it as QItem).apply {
                Glide.with(this@TestShoppingActivity)
                    .load(thumbnail)
                    .into(binding.ivGoodsImg)
            }
        }
    }
}