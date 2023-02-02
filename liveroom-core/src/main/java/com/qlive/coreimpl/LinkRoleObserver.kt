package com.qlive.coreimpl

/**
 * Link role observer
 * 关心用户角色变化
 * @constructor Create empty Link role observer
 */
interface LinkRoleObserver {
    fun notifyLinkRoleSwitched(isLink: Boolean)
}