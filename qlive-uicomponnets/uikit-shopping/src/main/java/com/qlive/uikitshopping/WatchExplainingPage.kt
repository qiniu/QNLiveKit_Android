package com.qlive.uikitshopping

import android.content.Context
import com.qlive.core.QLiveClient
import com.qlive.sdk.QPage
import com.qlive.shoppingservice.QItem

/**
 * 观看讲解页面
 *
 * @constructor Create empty Watch explaining page
 */
class WatchExplainingPage : QPage {

    companion object {
        const val params_key_item = "qitem"
    }

    /**
     *自定义布局
     */
    fun setCustomLayoutID(layoutID: Int) {
        WatchExplainingActivity.customLayoutID = layoutID
    }

    /**
     * Start
     *
     * @param client
     * @param item
     * @param context
     */
    fun start(client: QLiveClient, item: QItem, context: Context) {
        WatchExplainingActivity.start(client, item, context)
    }
}