package com.qlive.coreimpl.util


import com.qlive.rtm.RtmException
import com.qlive.avparam.RtcException
import com.qlive.coreimpl.http.NetBzException

fun Throwable.getCode(): Int {
    if (this is RtmException) {
        return code
    }
    if (this is RtcException) {
        return code
    }
    if (this is NetBzException) {
        return code
    }
    return -1
}