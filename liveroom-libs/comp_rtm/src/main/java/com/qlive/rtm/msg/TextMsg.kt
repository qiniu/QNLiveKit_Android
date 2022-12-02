package com.qlive.rtm.msg

import android.text.TextUtils
import org.json.JSONObject

class TextMsg(
    var text: String,
    var fromID: String,
    var toID: String,
    var msgID: String
){
    fun optAction():String{
        var action= ""
        try {
            val jsonObj = JSONObject(this.text)
            action = jsonObj.optString("action")
        }catch (e:Exception){
            e.printStackTrace()
        }
        return action?:""
    }
    fun optData():String{
        var data=""
        try {
            val jsonObj = JSONObject(this.text)
            data = jsonObj.optString("data")?:""
            if(TextUtils.isEmpty(data)){
                data =  jsonObj.optString("msgStr")?:""
            }
        }catch (e:java.lang.Exception){
            e.printStackTrace()
        }
        return data?:""
    }


}