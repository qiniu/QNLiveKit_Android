package com.qlive.qnlivekit

import android.app.Application
import android.view.View
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.core.QLiveConfig
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.qnlivekit.uitil.*

import com.qlive.sdk.QLive
import com.qlive.shoppingservice.QItem
import com.qlive.uikit.component.CloseRoomView
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitshopping.PlayerShoppingDialog

import okhttp3.Request

class App : Application() {
    companion object {

        val demo_url = "https://niucube-api.qiniu.com"
        //val demo_url="http://10.200.20.28:5080"
        var user: BZUser? = null
    }

    override fun onCreate() {
        super.onCreate()
        AppCache.setContext(this)
        QLive.init(this, QLiveConfig()) { callback ->
            //业务方获取token
            getLoginToken(callback)
        }

        //自定义事件
        PlayerShoppingDialog.onItemClickCall =
            { context: QLiveUIKitContext, client: QLiveClient, view: View, item: QItem ->
                TestShoppingActivity.start(context, item)
            }
        CloseRoomView.beforeFinishCall = { context: QLiveUIKitContext,
                                           client: QLiveClient,
                                           room: QLiveRoomInfo,
                                           isAnchorActionCloseRoom: Boolean ->
            DemoLiveFinishedActivity.checkStart(context, client, room, isAnchorActionCloseRoom)
        }
    }

    //demo获取token
    private fun getLoginToken(callBack: QLiveCallBack<String>) {
        Thread {
            try {
                val requestToken = Request.Builder()
                    .url("${demo_url}/v1/live/auth_token?userID=${user!!.data.accountId}&deviceID=adjajdasod")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + user!!.data.loginToken)
                    .get()
                    .build();
                val callToken = OKHttpManger.okHttp.newCall(requestToken);
                val repToken = callToken.execute()
                val tkjson = repToken.body?.string()
                val tkobj = JsonUtils.parseObject(tkjson, BZkIToken::class.java)
                callBack.onSuccess(tkobj?.data?.accessToken ?: "")
            } catch (e: Exception) {
                e.printStackTrace()
                callBack.onError(-1, "")
            }
        }.start()
    }
}