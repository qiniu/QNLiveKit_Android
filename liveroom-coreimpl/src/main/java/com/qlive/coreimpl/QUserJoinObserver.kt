package com.qlive.coreimpl

interface QUserJoinObserver {
    fun notifyUserJoin(userId:String)
    fun notifyUserLeft(userId:String)
}