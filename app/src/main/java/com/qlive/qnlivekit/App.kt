package com.qlive.qnlivekit

import android.app.Application
import android.util.Log
import android.view.View
import com.qlive.chatservice.QChatRoomService
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveClient
import com.qlive.core.QLiveConfig
import com.qlive.core.been.QLiveRoomInfo
import com.qlive.core.been.QLiveUser
import com.qlive.pubchatservice.QPublicChat
import com.qlive.qnlivekit.uitil.*
import com.qlive.sdk.QLive
import com.qlive.shoppingservice.QItem
import com.qlive.uikit.component.CloseRoomView
import com.qlive.uikit.component.LiveRecordListView
import com.qlive.uikitcore.CoroutineExtSetting.canUseLifecycleScope
import com.qlive.uikitcore.QLiveUIKitContext
import com.qlive.uikitpublicchat.PublicChatView
import com.qlive.uikitshopping.PlayerShoppingDialog
import com.qlive.uikituser.OnlineUserView
import okhttp3.Request

class App : Application() {
    companion object {
        //模拟接入方的登录获取token地址 - （不是低代码的服务地址）
        val demo_url = "https://niucube-api.qiniu.com"
        //  val demo_url="http://10.200.20.28:5080"
    }

    override fun onCreate() {
        super.onCreate()
        AppCache.setContext(this)
        UserManager.init()
        Log.d("App", "onCreate")
        QLive.init(this, QLiveConfig()) { callback ->
            //业务方获取token
            Log.d("QTokenGetter", "QTokenGetter ${UserManager.user?.data?.loginToken}")
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
            if (isAnchorActionCloseRoom) {
                DemoLiveFinishedActivity.checkStart(context.androidContext, room)
            }
        }
        LiveRecordListView.onClickFinishedRoomCall = { context, roomInfo ->
            DemoLiveFinishedActivity.checkStart(context, roomInfo)
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        Log.d("App", "onTrimMemory $level")
        //Process.killProcess(Process.myPid())
    }

    //demo获取token
    private fun getLoginToken(callBack: QLiveCallBack<String>) {
        Thread {
            try {
                val requestToken = Request.Builder()
                    .url("${demo_url}/v1/live/auth_token?userID=${UserManager.user?.data?.accountId}&deviceID=adjajdasod")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + UserManager.user?.data?.loginToken)
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