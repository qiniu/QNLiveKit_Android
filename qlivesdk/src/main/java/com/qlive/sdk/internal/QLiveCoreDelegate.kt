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
import com.qlive.coreimpl.http.HttpService.Companion.httpClient
import com.qlive.jsonutil.JsonUtils
import com.qlive.liblog.QLiveLogUtil
import com.qlive.playerclient.QPlayerClient
import com.qlive.pushclient.QPusherClient
import com.qlive.qnim.QNIMManager
import com.qlive.sdk.internal.AppCache.Companion.appContext
import com.qlive.sdk.internal.AppCache.Companion.setContext
import im.floo.floolib.BMXErrorCode


internal class QLiveCoreDelegate {
    var qRooms: QRooms = QRoomImpl.instance;
    private var loginUser: QLiveUser? = null
    private var uikitObj: Any? = null
    private val dataSource = QLiveDataSource()

    fun getLiveUser(): QLiveUser? {
        if (loginUser == null) {
            val userStr = SpUtil.get("qlive").readString("loginUser") ?: return null
            loginUser = JsonUtils.parseObject(userStr, QLiveUser::class.java)
        }
        return loginUser
    }

    fun init(
        context: android.content.Context,
        config: QLiveConfig?,
        tokenGetter: QTokenGetter
    ) {
        setContext(context)
        val sdkConfig = config ?: QLiveConfig()
        httpClient.baseUrl = sdkConfig.serverURL
        httpClient.tokenGetter = tokenGetter
        QLiveLogUtil.isLogAble = config?.isLogAble ?: true
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
                SpUtil.get("qlive").saveData("loginUser", JsonUtils.toJson(loginUser!!))
                callBack.onSuccess(null)
            }
            catchError {
                callBack.onError(it.getCode(), it.message)
            }
        }
    }

    fun login(callBack: QLiveCallBack<Void>) {
        backGround {
            doWork {
                dataSource.getToken()
                val user = dataSource.profile()
                loginUser = user
                val appConfig = dataSource.appConfig()
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
                SpUtil.get("qlive").saveData("loginUser", JsonUtils.toJson(loginUser!!))
                callBack.onSuccess(null)
            }
            catchError {
                callBack.onError(it.getCode(), it.message)
            }
        }
    }

    fun <T> getUIKIT(): T {
        if (uikitObj != null) {
            return uikitObj as T
        }
        val classStr = "com.qlive.uikit.QLiveUIKitImpl"
        val classImpl = Class.forName(classStr)
        val constructor = classImpl.getConstructor(Context::class.java)
        uikitObj = constructor.newInstance(appContext) as T
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