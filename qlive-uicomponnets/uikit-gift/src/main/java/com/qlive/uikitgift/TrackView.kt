package com.qlive.uikitgift

import android.view.View
import com.qlive.giftservice.QGiftMsg


interface TrackView {

    var finishedCall :(()->Unit) ?
    /**
     * 是不是同一个轨道上的
     */
    fun showInSameTrack(gift: QGiftMsg):Boolean
    /**
     * 显示礼物
     */
    fun onNewModel(mode: QGiftMsg)
    /**
     * 是不是忙碌
     */
    fun isShow():Boolean
    /**
     * 退出直播间或者切换房间 清空
     */
    fun clear(isRoomChange:Boolean=false)

    fun asView():View
}