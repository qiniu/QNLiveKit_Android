package com.qlive.qnim

import android.content.Context
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
    /**
     * 自定义初始化
     * 如果需要自定义初始化参数
     */
    var imSDKConfigGetter: (appId: String, context: Context) -> BMXSDKConfig =
        { appId: String, context: Context ->
            val appPath = context.filesDir.path
            val dataPath = File("$appPath/data_dir")
            val cachePath = File("$appPath/cache_dir")
            dataPath.mkdirs()
            cachePath.mkdirs()
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