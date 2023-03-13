package com.qlive.uikitshopping

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import com.qlive.core.QLiveClient
import com.qlive.roomservice.QRoomService
import com.qlive.sdk.QLive
import com.qlive.shoppingservice.QItem
import com.qlive.uikitcore.QLiveClientClone
import com.qlive.uikitcore.QLiveRoomDepdComponentManager
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitcore.activity.BaseFrameActivity

/**
 * 看讲解页面
 *
 * @constructor Create empty Watch explaining activity
 */
class WatchExplainingActivity : BaseFrameActivity() {

    companion object {
        internal var customLayoutID = -1
        internal var qClient: QLiveClient? = null
        fun start(client: QLiveClient, item: QItem, context: Context) {
            qClient = client
            val intent = Intent(context, WatchExplainingActivity::class.java)
            intent.putExtra(WatchExplainingPage.params_key_item, item)
            context.startActivity(intent)
        }
    }

    private val mQUIKitContext by lazy {
        QLiveUIKitContext(
            this@WatchExplainingActivity,
            supportFragmentManager,
            this@WatchExplainingActivity,
            this@WatchExplainingActivity,
            { _, call -> call.onError(-1, "current activity can not option") },
            { _, call -> call.onError(-1, "current activity can not option") },
            { null },
            { null }
        )
    }

    private val mComponentManager by lazy {
        QLiveRoomDepdComponentManager(
            QLiveClientClone(qClient!!),
            mQUIKitContext
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//设置透明状态栏
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);//设置透明导航栏
        super.onCreate(savedInstanceState)
        val decorView = window.decorView
        mComponentManager.scanComponent(decorView)
        initView()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        initView()
    }

    private fun initView() {
        val room = qClient?.getService(QRoomService::class.java)?.roomInfo ?: return
        mComponentManager.mComponents.forEach {
            it.onEntering(room, QLive.getLoginUser())
        }
    }

    override fun onDestroy() {
        mComponentManager.onDestroyed()
        super.onDestroy()
    }

    override fun init() {}
    override fun getLayoutId(): Int {
        if (customLayoutID > -1) {
            return customLayoutID
        }
        return R.layout.kit_activity_watch_explaining
    }

}