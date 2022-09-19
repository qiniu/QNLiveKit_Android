package com.softsugar.library.sdk

import android.content.Context
import android.webkit.URLUtil
import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.SPUtils
import com.softsugar.library.sdk.entity.*
import com.softsugar.library.sdk.listener.DownloadListener
import com.softsugar.library.sdk.listener.PullResultListener
import com.softsugar.library.sdk.net.ApiService
import com.softsugar.library.sdk.utils.ContextHolder
import com.softsugar.library.sdk.utils.DownloadUtils
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.nio.charset.StandardCharsets

class MaterialModule : MaterialModuleApi {
    companion object {
        // private const val TAG = "MaterialModule"
    }

    override fun init(context: Context, appId: String, appKey: String) {
        ContextHolder.setAppId(appId)
        ContextHolder.setAppKey(appKey)
        ContextHolder.initial(context)
    }

    override fun updateTokenSync(): Boolean {

        val response = ApiService.createApiService().sdkAuthNew(ContextHolder.getAppId())
            ?.execute()

        if (response != null) {
            if (response.body() == null || response.code() != 200
                || response.body()?.code != 0 || !response.body()?.message.equals("success")
            ) {
                return false
            }
            val entity: AuthEntity = response.body()?.data!!
            SPUtils.getInstance().put(Constants.KEY_TOKEN, entity.token)
            // 对 data 字段进行解密 取出 sdkKey
            entity.data?.apply {
                val bbj = EncryptUtils.decryptBase64AES(
                    this.toByteArray(StandardCharsets.UTF_8),
                    ContextHolder.getAppKey().toByteArray(
                        StandardCharsets.UTF_8
                    ),
                    Constants.transformation,
                    Constants.iv.toByteArray(
                        StandardCharsets.UTF_8
                    )
                )
                // {"sdkKey":"MnlvVXHCLque9YgdVLBqC9tbku+EdGsrW3AD2ckKLMY="}
                val json = String(bbj)
                val jsonObject = JSONObject(json)
                val sdkKey = jsonObject.get("sdkKey") as String
                SPUtils.getInstance().put(Constants.KEY_SDK_KEY, sdkKey)
            }
            return true;
        }
        return false
    }

    override fun getDataListSync(groupId: String): MutableList<MaterialEntity> {
        var groupIdInt = 0
        try {
            groupIdInt = groupId.toInt()
        } catch (e: NumberFormatException) {

        }
        val data = getDataValue(groupIdInt)
        val response = ApiService.createApiService().materialListNew(
            ContextHolder.getAppId(),
            data
        )?.execute()
        if (response != null) {
            val list: MutableList<MaterialEntity>? = response.body()?.data?.effects
            if (list != null) {
                for (item in list) {
                    val path =
                        generateSDPath(URLUtil.guessFileName(item.pkgUrl, null, null))
                    if (File(path).exists()) {
                        item.zipSdPath = path
                    }
                }
            }
            if (null == list) {
                return mutableListOf()
            } else {
                return list
            }
        }
        return mutableListOf()
    }

    override fun getDataListAsync(groupId: String, listener: PullResultListener) {
        var groupIdInt = 0
        try {
            groupIdInt = groupId.toInt()
        } catch (e: NumberFormatException) {

        }
        val data = getDataValue(groupIdInt)
        ApiService.createApiService().materialListNew(
            ContextHolder.getAppId(),
            data
        )?.enqueue(object : Callback<ResultResponse<MaterialDataEntity?>?> {
            override fun onResponse(
                call: Call<ResultResponse<MaterialDataEntity?>?>,
                response: Response<ResultResponse<MaterialDataEntity?>?>
            ) {
                val list: MutableList<MaterialEntity>? = response.body()?.data?.effects
                if (list != null) {
                    for (item in list) {
                        val path =
                            generateSDPath(URLUtil.guessFileName(item.pkgUrl, null, null))
                        if (File(path).exists()) {
                            item.zipSdPath = path
                        }
                    }
                }
                if (null == list) {
                    listener.onSuccesss(mutableListOf())
                } else {
                    listener.onSuccesss(list)
                }
            }

            override fun onFailure(
                call: Call<ResultResponse<MaterialDataEntity?>?>,
                t: Throwable
            ) {
                listener.onFail()
            }

        })
    }

    override suspend fun downLoadZip(url: String): DownLoadResult {
        val fileName = URLUtil.guessFileName(url, null, null)
        val path = generateSDPath(fileName)
        return DownloadUtils.downloadSuspend(url, path)
    }

    fun generateSDPath(fileName: String): String {
        return ContextHolder.getContext().externalCacheDir?.absolutePath + File.separator + Constants.cacheFolderName + File.separator + fileName
    }

    override fun downLoadZip(url: String, call: DownloadListener) {
        val fileName = URLUtil.guessFileName(url, null, null)
        val path =
            ContextHolder.getContext().externalCacheDir?.absolutePath + File.separator + Constants.cacheFolderName + File.separator + fileName
        DownloadUtils.downloadSuspend(url, path, call)
    }

    private fun getDataValue(groupId: Int): String {
        var data = ""
        try {
            val json = JSONObject()
            json.put("effectsListId", groupId)
            val bytes = EncryptUtils.encryptAES2Base64(
                json.toString().toByteArray(),
                ContextHolder.getAppKey().toByteArray(),
                Constants.transformation,
                Constants.iv.toByteArray()
            )
            data = String(bytes)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return data
    }

}