package com.qlive.uikit

import android.content.Context
import com.qlive.rtclive.QInnerVideoFrameHook
import com.qlive.uikit.RoomListActivity.Companion.start
import com.qlive.sdk.QLiveUIKit
import com.qlive.sdk.QPage
import com.qlive.uikitcore.UIJsonConfigurator
import com.qlive.uikitshopping.WatchExplainingPage

/**
 * ui库实现类 反射创建勿动
 * @property appContext
 * @constructor Create empty Q live u i kit impl
 */
class QLiveUIKitImpl(val appContext: Context) : QLiveUIKit {

    companion object {
        private val mRoomListPage = RoomListPage()
        private val mRoomPage = RoomPage()
        private val mWatchExplainingPage = WatchExplainingPage()
        private val mLiveRecordPage = LiveRecordPage()
    }

    init {
        QInnerVideoFrameHook.checkHasHooker()
        if (QInnerVideoFrameHook.isEnable) {
            //如果依赖的内置美颜 初始化美颜插件
            QInnerVideoFrameHook.mBeautyHooker?.init(appContext)
        }
        UIJsonConfigurator.init(appContext)
    }

    override fun <T : QPage> getPage(pageClass: Class<T>): T? {
        val page: T? = when (pageClass.simpleName) {
            RoomListPage::class.simpleName -> mRoomListPage as T?
            RoomPage::class.simpleName -> mRoomPage as T?
            WatchExplainingPage::class.simpleName -> mWatchExplainingPage as T?
            LiveRecordPage::class.java.simpleName -> mLiveRecordPage as T?
            else -> null
        }
        return page
    }

    override fun launch(context: Context) {
        start(context)
    }
}