package com.qlive.pkservice

import com.qlive.core.been.QExtension
import com.qlive.coreimpl.http.HttpClient.Companion.httpClient
import org.json.JSONObject

internal class PKDataSource {

    suspend fun startPk(
        init_room_id: String,
        recv_room_id: String,
        recv_user_id: String,
        ext: HashMap<String,String>?
    ): PKOutline {
        val jsonObj = JSONObject()
        jsonObj.put("init_room_id", init_room_id)
        jsonObj.put("recv_room_id", recv_room_id)
        jsonObj.put("recv_user_id", recv_user_id)
        ext?.let {
            val jsonObjectExt = JSONObject()
            it.entries.forEach {
                jsonObjectExt.put(it.key,it.value)
            }
            jsonObj.put("extends", jsonObjectExt)
        }
        return httpClient.post("/client/relay/start", jsonObj.toString(), PKOutline::class.java)
    }

    suspend fun recevPk(relay_id: String): PKOutline {
        return httpClient.get("/client/relay/${relay_id}/token", null, PKOutline::class.java)
    }

    suspend fun stopPk(relay_id: String) {
        httpClient.post("/client/relay/${relay_id}/stop", "{}", Any::class.java)
    }

    suspend fun ackACKPk(relay_id: String) {
        httpClient.post("/client/relay/${relay_id}/started", "{}", Any::class.java)
    }

    suspend fun getPkInfo(relay_id: String): PKInfo {
        return httpClient.get("/client/relay/${relay_id}", null, PKInfo::class.java)
    }

    suspend fun updatePKExt(relay_id: String, ext: QExtension) {
        val json = JSONObject()
        val jsonObjectExt = JSONObject()
        jsonObjectExt.put(ext.key,ext.value)
        json.put("extends", jsonObjectExt)
        httpClient.post("/client/relay/${relay_id}/extends", json.toString(), Any::class.java)
    }
}