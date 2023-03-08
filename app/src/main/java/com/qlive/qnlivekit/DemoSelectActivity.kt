package com.qlive.qnlivekit

import com.qlive.qnlivekit.databinding.ActivityDemoSelectBinding
import com.qlive.sdk.QLive
import com.qlive.uikit.RoomPage
import com.qlive.uikitcore.activity.BaseBindingActivity

class DemoSelectActivity : BaseBindingActivity<ActivityDemoSelectBinding>() {

    override fun init() {
        binding.btnNoShopping.setOnClickListener {
            QLive.getLiveUIKit().getPage(RoomPage::class.java).playerCustomLayoutID =
                R.layout.custom_layout_player_noshopping
            QLive.getLiveUIKit().getPage(RoomPage::class.java).anchorCustomLayoutID =
                R.layout.custom_layout_pusher_noshoping

            QLive.getLiveUIKit().launch(this)
        }

        binding.btnShoppingAble.setOnClickListener {
            QLive.getLiveUIKit().getPage(RoomPage::class.java).playerCustomLayoutID =
                R.layout.kit_activity_room_player
            QLive.getLiveUIKit().getPage(RoomPage::class.java).anchorCustomLayoutID =
                R.layout.kit_activity_room_pusher

            QLive.getLiveUIKit().launch(this)
        }

        binding.btnKTVAble.setOnClickListener {

            QLive.getLiveUIKit().getPage(RoomPage::class.java).playerCustomLayoutID =
                R.layout.custom_layout_ktv_player
            QLive.getLiveUIKit().getPage(RoomPage::class.java).anchorCustomLayoutID =
                R.layout.custom_layout_ktv_pusher

            QLive.getLiveUIKit().launch(this)
        }
    }

}