package com.qlive.rtclive.rtc

import android.content.Context
import com.qiniu.droid.rtc.*
import com.qlive.liblog.QLiveLogUtil
import com.qlive.rtclive.MixStreamManager
import com.qlive.rtclive.QRTCUserStore

open class RtcClientWrap(
    val context: Context,
    private val setting: QNRTCSetting,
    private val mQNRTCClientConfig: QNRTCClientConfig,
    private val isAutoSubscribe: Boolean = false
) {

    /**
     * 额外的引擎监听包装为了把让各个模块都能监听rtc事件处理自己的逻辑
     */
    open val mQNRTCEngineEventWrap = QNRTCEngineEventWrap()
    internal val mQRTCUserStore = QRTCUserStore()
    val mMixStreamManager by lazy { MixStreamManager(this) }

    /**
     *  添加你需要的引擎状回调
     */
    fun addExtraQNRTCEngineEventListener(extraQNRTCEngineEventListener: ExtQNClientEventListener) {
        mQNRTCEngineEventWrap.addExtraQNRTCEngineEventListener(extraQNRTCEngineEventListener)
    }

    internal fun addExtraQNRTCEngineEventListenerToHead(extraQNRTCEngineEventListener: ExtQNClientEventListener) {
        mQNRTCEngineEventWrap.addExtraQNRTCEngineEventListener(extraQNRTCEngineEventListener, true)
    }

    /**
     * 移除额外的监听
     */
    fun removeExtraQNRTCEngineEventListener(extraQNRTCEngineEventListener: ExtQNClientEventListener) {
        mQNRTCEngineEventWrap.removeExtraQNRTCEngineEventListener(extraQNRTCEngineEventListener)
    }

    private val mQNRTCEventListener = QNRTCEventListener { }

    init {
        if (!QLiveLogUtil.isLogAble) {
            setting.logLevel = QNLogLevel.NONE
        } else {
            setting.logLevel = QNLogLevel.INFO
        }
        QNRTC.init(context, setting, mQNRTCEventListener) // 初始化
        addExtraQNRTCEngineEventListenerToHead(object : SimpleQNRTCListener {
            override fun onUserJoined(p0: String, p1: String?) {
                super.onUserJoined(p0, p1)
                mQRTCUserStore.addUser(QRTCUserStore.QRTCUser().apply {
                    uid = p0
                    userData = p1 ?: ""
                })
            }

            override fun onLocalUnpublished(var1: String, var2: List<QNLocalTrack>) {
                super.onLocalUnpublished(var1, var2)
                var2.forEach {
                    mQRTCUserStore.removeUserTrack(var1, it)
                }
            }

            override fun onLocalPublished(var1: String, var2: List<QNLocalTrack>) {
                super.onLocalPublished(var1, var2)
                var2.forEach {
                    mQRTCUserStore.setUserTrack(var1, it)
                }
            }

            override fun onUserPublished(p0: String, p1: MutableList<QNRemoteTrack>) {
                super.onUserPublished(p0, p1)
                p1.forEach {
                    mQRTCUserStore.setUserTrack(p0, it)
                }
            }

            override fun onUserUnpublished(p0: String, p1: MutableList<QNRemoteTrack>) {
                super.onUserUnpublished(p0, p1)
                p1.forEach {
                    mQRTCUserStore.removeUserTrack(p0, it)
                }
            }

            override fun onUserLeft(p0: String) {
                super.onUserLeft(p0)
                mQRTCUserStore.clearUser(p0)
            }
        })
    }

    /**
     *  rtc
     */
    val mClient by lazy {
        QNRTC.createClient(mQNRTCClientConfig, mQNRTCEngineEventWrap).apply {
            setAutoSubscribe(isAutoSubscribe)
        }
    }

}