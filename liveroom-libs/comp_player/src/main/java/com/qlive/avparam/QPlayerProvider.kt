package com.qlive.avparam

/**
 * Q player provider
 * 播放器提供者 向插件提供当前client的播放器对象
 * @constructor Create empty Q player provider
 */
interface QPlayerProvider {
    var playerGetter: (() -> QIPlayer)
}