package com.qlive.coreimpl

import android.content.Context
import com.qlive.core.QLiveCallBack
import com.qlive.core.QLiveConfig
import com.qlive.core.QRooms
import com.qlive.core.QTokenGetter
import com.qlive.core.been.QLiveUser
import com.qlive.coreimpl.datesource.UserDataSource
import com.qlive.coreimpl.http.QLiveHttpService
import com.qlive.coreimpl.model.AppConfig
import com.qlive.coreimpl.model.InnerUser
import com.qlive.coreimpl.util.backGround
import com.qlive.coreimpl.util.getCode
import com.qlive.liblog.QLiveLogUtil
import com.qlive.playerclient.QPlayerClient
import com.qlive.pushclient.QPusherClient
import com.qlive.qnim.QNIMManager
import im.floo.floolib.BMXErrorCode

object QLiveDelegate {

    var qRooms: QRooms = QRoomImpl.instance;

    fun init(
        context: Context,
        config: QLiveConfig?,
        tokenGetter: QTokenGetter
    ) {
        AppCache.setContext(context)
        val sdkConfig = config ?: QLiveConfig()
        QLiveHttpService.baseUrl = sdkConfig.serverURL
        QLiveHttpService.tokenGetter = tokenGetter
        QLiveLogUtil.isLogAble = config?.isLogAble ?: true
    }

    fun setUser(userInfo: QLiveUser, callBack: QLiveCallBack<Void>) {
        backGround {
            doWork {
                val user = UserDataSource.loginUser
                if (user.avatar != userInfo.avatar
                    || user.nick != userInfo.nick
                    || user.extensions != userInfo.extensions
                ) {
                    UserDataSource().updateUser(
                        userInfo.avatar,
                        userInfo.nick,
                        userInfo.extensions
                    )
                    UserDataSource.loginUser.avatar = userInfo.avatar
                    UserDataSource.loginUser.nick = userInfo.nick
                    UserDataSource.loginUser.extensions = userInfo.extensions
                }
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
                UserDataSource().getToken()
                val user = QLiveHttpService.get("/client/user/profile", null, InnerUser::class.java)
                UserDataSource.loginUser = user
                val appConfig = QLiveHttpService.get("/client/app/config", null, AppConfig::class.java)
                QNIMManager.init(appConfig.im_app_id, AppCache.appContext)
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
                callBack.onSuccess(null)
            }
            catchError {
                callBack.onError(it.getCode(), it.message)
            }
        }
    }

    private var uikitObj: Any? = null

    fun <T> getUIKIT(): T {
        if (uikitObj != null) {
            return uikitObj as T
        }
        val classStr = "com.qlive.uikit.QLiveUIKitImpl"
        val classImpl = Class.forName(classStr)
        val constructor = classImpl.getConstructor(Context::class.java)
        uikitObj = constructor.newInstance(AppCache.appContext) as T
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