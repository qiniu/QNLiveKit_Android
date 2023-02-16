package com.qlive.qnim

import android.content.Context
import android.os.Build
import android.os.Environment
import com.qlive.liblog.QLiveLogUtil
import im.floo.floolib.BMXClientType
import im.floo.floolib.BMXLogLevel
import im.floo.floolib.BMXPushEnvironmentType
import im.floo.floolib.BMXSDKConfig
import java.io.File

/**
 *用户自定义im初始化
 */
object QNIMConfig {
    private fun getFilesPath(context: Context): String {
        val filePath: String =
            if ((Environment.MEDIA_MOUNTED == Environment.getExternalStorageState() || !Environment.isExternalStorageRemovable())
            ) {
                QLiveLogUtil.d("data_dir", "外部存储可用")
                //外部存储可用
                context.getExternalFilesDir(null)!!.path
            } else {
                QLiveLogUtil.d("data_dir", "外部存储不可用")
                //外部存储不可用
                context.filesDir.path

            }
        return filePath
    }

    /**
     * 自定义初始化
     * 如果需要自定义初始化参数
     */
    var imSDKConfigGetter: (appId: String, context: Context) -> BMXSDKConfig =
        { appId: String, context: Context ->
            val appPath = getFilesPath(context)
            val dataPath = File("$appPath/data_dir")
            val cachePath = File("$appPath/cache_dir")
            dataPath.mkdirs()
            cachePath.mkdirs()
            QLiveLogUtil.d("data_dir", dataPath.absolutePath)
            // 配置sdk config
            BMXSDKConfig(
                BMXClientType.Android, "1", dataPath.absolutePath,
                cachePath.absolutePath, "MaxIM"
            ).apply {
                consoleOutput = true
                logLevel = if (QLiveLogUtil.isLogAble) {
                    BMXLogLevel.Debug
                } else {
                    BMXLogLevel.Error
                }
                appID = appId
                setEnvironmentType(BMXPushEnvironmentType.Production)
            }
        }
}