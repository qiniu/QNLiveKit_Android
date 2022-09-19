package com.qlive.linkmicservice

import com.qlive.liblog.QLiveLogUtil
import com.qlive.rtclive.DefaultExtQNClientEventListener
import com.qlive.rtclive.QRtcLiveRoom
import com.qlive.jsonutil.JsonUtils
import com.qlive.coreimpl.model.UidMsgMode
import java.util.*

internal class MicLinkContext {

    val allLinker = LinkedList<QMicLinker>()
    var hostLeftCall = {

    }
    var onKickCall: (linker: QMicLinker, model: UidMsgMode) -> Unit = { l, m ->
    }
    var needSynchro = false

    val mQLinkMicServiceListeners = LinkedList<QLinkMicServiceListener>()
    fun removeLinker(uid: String): QMicLinker? {
        getMicLinker(uid)?.let {
            allLinker.remove(it)
            return it
        }
        return null
    }

    fun addLinker(linker: QMicLinker): Boolean {
        val it = getMicLinker(linker.user.userId)
        if (it == null) {
            allLinker.add(linker)
            return true
        } else {
            it.user = linker.user
            it.userRoomID = linker.userRoomID
            it.isOpenMicrophone = linker.isOpenMicrophone
            it.isOpenCamera = linker.isOpenCamera
            it.extension = linker.extension
            return false
        }
    }

    fun getMicLinker(uid: String): QMicLinker? {
        allLinker.forEach {
            if (it.user?.userId == uid) {
                return it
            }
        }
        return null
    }

    val mExtQNClientEventListener = object : DefaultExtQNClientEventListener {
        //
        override fun onUserJoined(p0: String, p1: String?) {
            QLiveLogUtil.d("MicLinkContext onUserJoined ${p0}  $p1")
            val micLinker = JsonUtils.parseObject(p1, QMicLinker::class.java) ?: return
            val it = getMicLinker(p0)
            addLinker(micLinker)
            if (it == null) {
                mQLinkMicServiceListeners.forEach {
                    it.onLinkerJoin(micLinker)
                }
            }
        }

        override fun onUserLeft(p0: String) {
            QLiveLogUtil.d("MicLinkContext  onUserLeft${p0} ")
            val mic = getMicLinker(p0)
            if (mic != null) {
                if (mic.user.userId == allLinker[0].user.userId) {
                    //房主下麦
//                    val remove = ArrayList<QMicLinker>()
//                    allLinker.forEachIndexed { index, mic ->
//                        if(index!=0){
//                            remove.a
//                        }
//                    }
                    hostLeftCall.invoke()
                    return
                }
                removeLinker(p0)
                mQLinkMicServiceListeners.forEach {
                    it.onLinkerLeft(mic)
                }
            }
        }
    }
    lateinit var mQRtcLiveRoom: QRtcLiveRoom

}