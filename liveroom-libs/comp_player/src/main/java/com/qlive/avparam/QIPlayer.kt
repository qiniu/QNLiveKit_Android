package com.qlive.avparam

/**
 * 播放器-
 * 监听连麦状态的播放器
 */
interface QIPlayer {
    fun setUp(uir: String, headers: Map<String, String>? = null)
    fun start()

    /**
     * 暂停
     */
    fun pause()

    /**
     * 恢复
     */
    fun resume()
    fun stop()
    fun release()

    /**
     * 连麦状态变化
     * @param isLink
     */
    fun switchLinkRole(isLink: Boolean)

    fun addEventListener(listener: QPlayerEventListener)

    fun removeEventListener(listener: QPlayerEventListener)

    fun addSEIListener(listener: QPlayerSEIListener)

    fun removeSEIListener(listener: QPlayerSEIListener)
}