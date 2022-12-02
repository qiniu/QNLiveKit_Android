package com.qlive.rtm

/**
 * im操作回调
 */
interface RtmCallBack {
    fun onSuccess()
    fun onFailure(code: Int, msg: String)
}

interface RtmDadaCallBack<T> {
    fun onSuccess(data: T)
    fun onFailure(code: Int, msg: String)
}