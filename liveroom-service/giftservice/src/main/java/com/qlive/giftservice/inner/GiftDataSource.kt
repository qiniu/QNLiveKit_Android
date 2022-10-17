package com.qlive.giftservice.inner

import com.google.gson.JsonObject
import com.qlive.coreimpl.http.HttpClient
import com.qlive.coreimpl.http.PageData
import com.qlive.coreimpl.http.header_cache_name
import com.qlive.giftservice.QGift
import com.qlive.giftservice.QGiftStatistics
import com.qlive.jsonutil.ParameterizedTypeImpl

class GiftDataSource {

    suspend fun giftList(type: Int, useCache: Boolean = true): List<QGift> {
        val p = ParameterizedTypeImpl(
            arrayOf(QGift::class.java),
            List::class.java,
            List::class.java
        )
        return HttpClient.httpClient.get("/client/gift/config/${type}", null, if (useCache) {
            HashMap<String, String>().apply {
                put(header_cache_name, "60")
            }
        } else {
            null
        }, null, p)
    }

    suspend fun giftByID(giftID: Int): QGift {
        val gift: QGift = giftList(-1).first {
            it.giftID == giftID
        }
        return gift
    }

    suspend fun roomGiftStatistics(
        liveID: String,
        page_num: Int,
        page_size: Int
    ): List<QGiftStatistics> {
        val p = ParameterizedTypeImpl(
            arrayOf(QGiftStatistics::class.java),
            PageData::class.java,
            PageData::class.java
        )
        return HttpClient.httpClient.get<PageData<QGiftStatistics>>(
            "/client/gift/list/live/${liveID}",
            HashMap<String, String>().apply {
                put("page_num", page_num.toString())
                put("page_size", page_size.toString())
            },
            null,
            p
        ).list
    }

    suspend fun sendGift(live_id: String, gift_id: Int, amount: Int) {
        HttpClient.httpClient.post("/client/gift/send", JsonObject().apply {
            addProperty("live_id", live_id)
            addProperty("gift_id", gift_id)
            addProperty("amount", amount)
        }.toString(), Any::class.java)
    }

    suspend fun anchorGiftStatistics(page_num: Int, page_size: Int): List<QGiftStatistics> {
        val p = ParameterizedTypeImpl(
            arrayOf(QGiftStatistics::class.java),
            PageData::class.java,
            PageData::class.java
        )
        return HttpClient.httpClient.get<PageData<QGiftStatistics>>(
            "/client/gift/list/anchor",
            HashMap<String, String>().apply {
                put("page_num", page_num.toString())
                put("page_size", page_size.toString())
            },
            null,
            p
        ).list
    }

    suspend fun userGiftStatistics(page_num: Int, page_size: Int): List<QGiftStatistics> {
        val p = ParameterizedTypeImpl(
            arrayOf(QGiftStatistics::class.java),
            PageData::class.java,
            PageData::class.java
        )
        return HttpClient.httpClient.get<PageData<QGiftStatistics>>(
            "/client/gift/list/user",
            HashMap<String, String>().apply {
                put("page_num", page_num.toString())
                put("page_size", page_size.toString())
            },
            null,
            p
        ).list
    }
}