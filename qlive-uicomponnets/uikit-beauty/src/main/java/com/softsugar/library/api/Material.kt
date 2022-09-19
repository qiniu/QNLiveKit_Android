package com.softsugar.library.api

import android.content.Context
import com.softsugar.library.sdk.MaterialModule
import com.softsugar.library.sdk.MaterialModuleApi
import com.softsugar.library.sdk.entity.DownLoadResult
import com.softsugar.library.sdk.entity.MaterialEntity
import com.softsugar.library.sdk.listener.DownloadListener
import com.softsugar.library.sdk.listener.PullResultListener

object Material : MaterialModuleApi {

    private val mMaterialModuleApi: MaterialModuleApi = MaterialModule()

    override fun init(context: Context, appId: String, appKey: String) {
        mMaterialModuleApi.init(context, appId, appKey)
    }

    override fun updateTokenSync(): Boolean {
        return mMaterialModuleApi.updateTokenSync()
    }

    override fun getDataListSync(groupId: String): MutableList<MaterialEntity> {
        return mMaterialModuleApi.getDataListSync(groupId)
    }

    override fun getDataListAsync(groupId: String,call:PullResultListener) {
        return mMaterialModuleApi.getDataListAsync(groupId,call)
    }

    override suspend fun downLoadZip(url: String): DownLoadResult {
        return mMaterialModuleApi.downLoadZip(url)
    }

    override fun downLoadZip(url: String, call: DownloadListener) {
        mMaterialModuleApi.downLoadZip(url, call)
    }
}