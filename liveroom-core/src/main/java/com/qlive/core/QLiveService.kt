package com.qlive.core

//插件 服务
interface QLiveService : QClientLifeCycleListener{
    open suspend fun checkLeave() {
    }
}