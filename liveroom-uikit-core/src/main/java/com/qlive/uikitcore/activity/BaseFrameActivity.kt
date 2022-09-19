package com.qlive.uikitcore.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.qlive.liblog.QLiveLogUtil
import com.qlive.uikitcore.dialog.LoadingDialog


/**
 * activity 基础类  封装toolbar
 *
 */
abstract class BaseFrameActivity : AppCompatActivity(){

    open fun isCustomCreate():Boolean{
        return false
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(isCustomCreate()){
            return
        }
        val startTime = System.currentTimeMillis()
        setContentView(getLayoutId())
        QLiveLogUtil.d(
            "ActivityonCreate",
            "onCreate cost ${System.currentTimeMillis() - startTime}"
        )
        init()
    }

    abstract fun init()
    abstract fun getLayoutId(): Int
    fun showLoading(toShow: Boolean) {
        if (toShow) {
            LoadingDialog.showLoading(supportFragmentManager)
        } else {
            LoadingDialog.cancelLoadingDialog()
        }
    }
}