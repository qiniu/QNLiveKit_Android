package com.qlive.shoppingservice

import com.qlive.core.been.QExtension
import com.qlive.coreimpl.http.HttpService.Companion.httpClient
import com.qlive.jsonutil.JsonUtils
import com.qlive.jsonutil.ParameterizedTypeImpl
import org.json.JSONArray
import org.json.JSONObject

internal class ShoppingDataSource {

    suspend fun addItems(liveId: String, items: List<QItem>) {
        val jsonObj = JSONObject()
        jsonObj.put("live_id", liveId)
        val jsonArray = JSONArray()
        items.forEach {
            jsonArray.put(it)
        }
        jsonObj.put("items", jsonArray)
        httpClient.post("/client/item/add", jsonObj.toString(), Any::class.java)
    }

    suspend fun deleteItems(liveId: String, itemIDList: List<String>) {
        val jsonObj = JSONObject()
        val jsonArray = JSONArray()
        itemIDList.forEach {
            jsonArray.put(it)
        }
        jsonObj.put("live_id", liveId)
        jsonObj.put("items", jsonArray)
        httpClient.post("/client/item/delete", jsonObj.toString(), Any::class.java)
    }

    suspend fun updateStatus(liveId: String, items: HashMap<String, Int>) {
        val jsonObj = JSONObject()
        jsonObj.put("live_id", liveId)
        val jsonArray = JSONArray()
        items.forEach {
            val jsonObjItem = JSONObject()
            jsonObjItem.put("item_id", it.key)
            jsonObjItem.put("status", it.value)
            jsonArray.put(jsonObjItem)
        }
        jsonObj.put("items", jsonArray)
        httpClient.post("/client/item/status", jsonObj.toString(), Any::class.java)
    }

    //获取直播间所有商品
    suspend fun getItemList(liveId: String): List<QItem> {
        val p = ParameterizedTypeImpl(
            arrayOf(QItem::class.java),
            List::class.java,
            List::class.java
        )
        return httpClient.get("/client/item/$liveId", null, null, p)
    }

    suspend fun updateItemExtension(liveId: String, ItemID: String, extension: QExtension) {
        httpClient.post(
            "/client/item/${liveId}/${ItemID}/extends",
            JsonUtils.toJson(extension),
            Any::class.java
        )
    }

    suspend fun setExplaining(liveId: String, ItemID: String) {
        httpClient.post("/client/item/demonstrate/${liveId}/${ItemID}", "{}", Any::class.java)
    }

    suspend fun cancelExplaining(liveId: String) {
        httpClient.delete("/client/item/demonstrate/${liveId}", "{}", Any::class.java)
    }

    suspend fun getExplaining(liveId: String): QItem {
        return httpClient.get("/client/item/demonstrate/${liveId}", null, QItem::class.java)
    }

    //跟新单个商品顺序
    suspend fun changeSingleOrder(live_id: String, item_id: String, from: Int, to: Int) {
        httpClient.post("/client/item/order/single", JSONObject().apply {
            put("live_id", live_id)
            put("item_id", item_id)
            put("from", from)
            put("to", to)
        }.toString(), Any::class.java)
    }

    suspend fun changeOrder(live_id: String, orders: HashMap<String, Int>) {
        httpClient.post("/client/item/order", JSONObject().apply {
            put("live_id", live_id)
            put("items", orders)
        }.toString(), Any::class.java)
    }

    //开始录制讲解
    suspend fun startRecord(liveId: String, ItemID: String) {
        httpClient.post(
            "/client/item/demonstrate/start/${liveId}/${ItemID}",
            "{}",
            Any::class.java
        )
    }

    suspend fun deleteRecord(liveId: String, recordIds: List<Int>) {
        httpClient.post("/client/item/demonstrate/record/delete", JSONObject().apply {
            put("live_id", liveId)
            put("demonstrate_item", JSONArray().apply {
                recordIds.forEach {
                    put(it)
                }
            })
        }.toString(), Any::class.java)
    }
}