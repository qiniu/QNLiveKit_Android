package com.qlive.uiwidghtbeauty

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.qiniu.sensetimeplugin.QNSenseTimePlugin
import com.qlive.beautyhook.BeautyHookerImpl
import com.qlive.uiwidghtbeauty.utils.Constants.LICENSE_FILE
import com.qlive.uiwidghtbeauty.utils.LoadResourcesTask
import com.qlive.uiwidghtbeauty.utils.LoadResourcesTask.ILoadResourcesCallback
import com.qlive.uiwidghtbeauty.utils.SharedPreferencesUtils
import com.softsugar.library.api.Material
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object QSenseTimeManager {

    var sSenseTimePlugin: QNSenseTimePlugin? = null
    private const val DST_FOLDER = "resource"
    var sAppContext: Context? = null
    var isAuthorized = false

    //自动初始化 如果要修改请保留这个 BeautyHookerImpl.senseTimePlugin = sSenseTimePlugin
    fun initEffectFromLocalLicense(appContext: Context, isFromQLive: Boolean) {
        // 此处远端拉取素材的逻辑暂不开放，如需此功能可自行实现
        sAppContext = appContext
        sSenseTimePlugin = QNSenseTimePlugin.Builder(appContext)
            .setLicenseAssetPath(LICENSE_FILE)
            .setModelActionAssetPath("M_SenseME_Face_Video_5.3.3.model")
            .setCatFaceModelAssetPath("M_SenseME_CatFace_3.0.0.model")
            .setDogFaceModelAssetPath("M_SenseME_DogFace_2.0.0.model") // 关闭在线拉取授权文件，使用离线授权文件
           // .setOnlineLicense(false) // 关闭在线激活授权，使用离线激活授权
            //.setOnlineActivate(false)
            .build()
        isAuthorized = sSenseTimePlugin?.checkLicense() ?: false
        if (!isAuthorized) {
            Toast.makeText(appContext, "鉴权失败，请检查授权文件", Toast.LENGTH_SHORT).show()
        } else {
            if (isFromQLive) {
                //内置美颜插件初始化
                BeautyHookerImpl.addSubModelFromAssetsFiles.add("M_SenseME_Face_Extra_5.23.0.model")
                BeautyHookerImpl.addSubModelFromAssetsFiles.add("M_SenseME_Iris_2.0.0.model")
                BeautyHookerImpl.addSubModelFromAssetsFiles.add("M_SenseME_Hand_5.4.0.model")
                BeautyHookerImpl.addSubModelFromAssetsFiles.add("M_SenseME_Segment_4.10.8.model")
                BeautyHookerImpl.addSubModelFromAssetsFiles.add("M_SenseAR_Segment_MouthOcclusion_FastV1_1.1.1.model")
                BeautyHookerImpl.senseTimePlugin = sSenseTimePlugin
            }
        }
        checkLoadResourcesTask(appContext,
            object : LoadResourcesTask.ILoadResourcesCallback {
                override fun getContext(): Context {
                    return sAppContext!!
                }

                override fun onStartTask() {
                    Log.d("QSenseTimeManager", "onStartTask" + " ");
                }

                override fun onEndTask(result: Boolean) {
                    Log.d("QSenseTimeManager", "onEndTask" + " ");
                }
            });
        Log.d("QSenseTimeManager", "authorizeWithAppId" + "鉴权onSuccess ")
        Material.init(appContext, APP_ID, APP_KEY)
        GlobalScope.launch(Dispatchers.IO) {
            try {
                Material.updateTokenSync()
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }

    private fun checkLoadResourcesTask(
        context: Context,
        callback: ILoadResourcesCallback
    ) {
        SharedPreferencesUtils.resVersion = resourceVersion
        if (SharedPreferencesUtils.resourceReady(context)) {
            callback.onStartTask()
            callback.onEndTask(true)
        } else {
            val mTask =
                LoadResourcesTask(callback)
            mTask.execute(DST_FOLDER)
        }
    }

}