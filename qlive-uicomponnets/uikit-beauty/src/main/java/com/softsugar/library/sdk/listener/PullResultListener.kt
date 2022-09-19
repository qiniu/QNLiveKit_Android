package com.softsugar.library.sdk.listener

import com.softsugar.library.sdk.entity.MaterialEntity

interface PullResultListener {
    fun onSuccesss(mutableList: MutableList<MaterialEntity>)
    fun onFail()
}