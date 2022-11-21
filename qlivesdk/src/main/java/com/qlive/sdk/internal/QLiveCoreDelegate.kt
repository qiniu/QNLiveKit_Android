package com.qlive.sdk.internal

import android.content.Context
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveConfig
import com.qlive.sdk.QRooms
import com.qlive.core.QTokenGetter
import com.qlive.core.been.QLiveUser
import com.qlive.coreimpl.QLiveDataSource
import com.qlive.coreimpl.backGround
import com.qlive.coreimpl.getCode
import com.qlive.coreimpl.http.HttpClient.Companion.httpClient
import com.qlive.coreimpl.http.NetBzException
import com.qlive.coreimpl.http.OKConnectionHttpClient
import com.qlive.coreimpl.model.AppConfig
import com.qlive.jsonutil.JsonUtils
import com.qlive.liblog.QLiveLogUtil
import com.qlive.playerclient.QPlayerClient
import com.qlive.pushclient.QPusherClient
import com.qlive.qnim.QNIMManager
import com.qlive.sdk.internal.AppCache.Companion.appContext
import com.qlive.sdk.internal.AppCache.Companion.setContext
import im.floo.floolib.BMXErrorCode
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


internal class QLiveCoreDelegate {
    var qRooms: QRooms = QRoomImpl.instance;
    private var loginUser: QLiveUser? = null
    private var uikitObj: Any? = null
    private val dataSource = QLiveDataSource()
    private var tokenGetter: QTokenGetter? = null
    fun getLiveUser(): QLiveUser? {
        return loginUser
    }

    fun init(
        context: android.content.Context,
        config: QLiveConfig?,
        tokenGetter: QTokenGetter
    ) {
        httpClient = OKConnectionHttpClient(context)
        setContext(context)
        val sdkConfig = config ?: QLiveConfig()
        httpClient.baseUrl = sdkConfig.serverURL
        this.tokenGetter = tokenGetter
        httpClient.onTokenExpiredCall = {
            reGetToken()
        }
        QLiveLogUtil.isLogAble = config?.isLogAble ?: true

        val appConfigStr = SpUtil.get("qlive").readString("appConfig", "")
        val appConfig = JsonUtils.parseObject(appConfigStr, AppConfig::class.java) ?: return
        QNIMManager.init(appConfig.im_app_id, appContext)
    }

    fun setUser(userInfo: QLiveUser, callBack: QLiveCallBack<Void>) {
        backGround {
            doWork {
                val user = loginUser!!
                if (user.avatar != userInfo.avatar
                    || user.nick != userInfo.nick
                    || user.extensions != userInfo.extensions
                ) {
                    dataSource.updateUser(
                        userInfo.avatar,
                        userInfo.nick,
                        userInfo.extensions
                    )
                    loginUser!!.avatar = userInfo.avatar
                    loginUser!!.nick = userInfo.nick
                    loginUser!!.extensions = userInfo.extensions
                }
                callBack.onSuccess(null)
            }
            catchError {
                callBack.onError(it.getCode(), it.message)
            }
        }
    }

    private suspend fun getToken() = suspendCoroutine<String> { coroutine ->
        tokenGetter!!.getTokenInfo(object : QLiveCallBack<String> {
            override fun onError(code: Int, msg: String?) {
                coroutine.resumeWithException(NetBzException(code, msg))
            }

            override fun onSuccess(data: String) {
                httpClient.token = data
                coroutine.resume(data)
            }
        })
    }

    //后台长时间运行 登陆信息和im信息都销毁了
    //Application重建 恢复activity
    private fun reGetToken(): Boolean {
        val latch = CountDownLatch(1)
        var isReLogin = false
        loginInner(true, object : QLiveCallBack<Void> {
            override fun onError(code: Int, msg: String?) {
                isReLogin = false
                latch.countDown()
            }

            override fun onSuccess(data: Void?) {
                isReLogin = true
                latch.countDown()
            }
        })
        latch.await()
        return isReLogin
    }

    private fun loginInner(reGetToken: Boolean, callBack: QLiveCallBack<Void>) {
        backGround {
            doWork {
                getToken()
                if (reGetToken) {
                    if (loginUser != null) {
                        callBack.onSuccess(null)
                        return@doWork
                    }
                }
                val user = dataSource.profile()
                val appConfig = dataSource.appConfig()
                SpUtil.get("qlive").saveData("appConfig", JsonUtils.toJson(appConfig))
                QNIMManager.init(appConfig.im_app_id, appContext)
                val code = QNIMManager.mRtmAdapter.loginSuspend(
                    user.userId,
                    user.imUid,
                    user.im_username,
                    user.im_password
                )
                if (code != BMXErrorCode.NoError) {
                    callBack.onError(code.swigValue(), code.name)
                    return@doWork
                }
                loginUser = user
                callBack.onSuccess(null)
            }
            catchError {
                callBack.onError(it.getCode(), it.message)
            }
        }
    }

    fun login(callBack: QLiveCallBack<Void>) {
        loginInner(false, callBack)
    }

    fun <T> getUIKIT(): T? {
        if (uikitObj != null) {
            return uikitObj as T
        }
        try {
            val classStr = "com.qlive.uikit.QLiveUIKitImpl"
            val classImpl = Class.forName(classStr)
            val constructor = classImpl.getConstructor(Context::class.java)
            uikitObj = constructor.newInstance(appContext) as T
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
        return uikitObj as T
    }

    fun createPusherClient(): QPusherClient {
        return QPusherClientImpl.create()
    }

    /**
     * 创建拉流客户端
     *
     * @return QPlayerClient
     */
    fun createPlayerClient(): QPlayerClient {
        return QPlayerClientImpl.create()
    }

}