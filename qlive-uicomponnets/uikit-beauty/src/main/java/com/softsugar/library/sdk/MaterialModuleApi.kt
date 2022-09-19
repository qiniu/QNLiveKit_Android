package com.softsugar.library.sdk

import android.content.Context
import com.softsugar.library.sdk.entity.DownLoadResult
import com.softsugar.library.sdk.entity.MaterialEntity
import com.softsugar.library.sdk.listener.DownloadListener
import com.softsugar.library.sdk.listener.PullResultListener

interface MaterialModuleApi {

    fun init(context: Context, appId: String, appKey: String)

    fun updateTokenSync(): Boolean

    fun getDataListSync(groupId: String): MutableList<MaterialEntity>

    fun getDataListAsync(groupId: String,call: PullResultListener)

    suspend fun downLoadZip(url: String): DownLoadResult

    fun downLoadZip(url: String, call: DownloadListener)
}