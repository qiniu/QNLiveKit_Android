package com.softsugar.library.api

import android.content.Context
import com.softsugar.library.sdk.entity.DownLoadResult
import com.softsugar.library.sdk.entity.MaterialEntity
import com.softsugar.library.sdk.listener.DownloadListener
import com.softsugar.library.sdk.utils.DownloadUtils

interface MaterialApi {

    fun init(context: Context, appId: String, appKey: String)

    fun updateTokenSync():Boolean

    /**
     * 根据groupID获取素材列表(同步方法)
     */
    suspend fun getDataListSync(groupId: String): MutableList<MaterialEntity>?

    suspend fun downLoadZip(url: String): DownLoadResult

    fun downLoadZip(url: String, call: DownloadListener)
}