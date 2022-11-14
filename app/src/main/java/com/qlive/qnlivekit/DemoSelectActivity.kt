package com.qlive.qnlivekit

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.qlive.sdk.QLive
import com.qlive.uikit.RoomPage
import kotlinx.android.synthetic.main.activity_demo_select.*

class DemoSelectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demo_select)
        btnNoShopping.setOnClickListener {
            QLive.getLiveUIKit().getPage(RoomPage::class.java).playerCustomLayoutID =
                R.layout.custom_layout_player_noshopping
            QLive.getLiveUIKit().getPage(RoomPage::class.java).anchorCustomLayoutID =
                R.layout.custom_layout_pusher_no_shoping

            QLive.getLiveUIKit().launch(this)
        }

        btnShoppingAble.setOnClickListener {
            QLive.getLiveUIKit().getPage(RoomPage::class.java).playerCustomLayoutID =
                R.layout.kit_activity_room_player
            QLive.getLiveUIKit().getPage(RoomPage::class.java).anchorCustomLayoutID =
                R.layout.kit_activity_room_pusher

            QLive.getLiveUIKit().launch(this)
        }

        btnKTVAble.setOnClickListener {

            QLive.getLiveUIKit().getPage(RoomPage::class.java).playerCustomLayoutID =
                R.layout.custom_layout_ktv_player
            QLive.getLiveUIKit().getPage(RoomPage::class.java).anchorCustomLayoutID =
                R.layout.custom_layout_ktv_pusher

            QLive.getLiveUIKit().launch(this)
        }
    }
}