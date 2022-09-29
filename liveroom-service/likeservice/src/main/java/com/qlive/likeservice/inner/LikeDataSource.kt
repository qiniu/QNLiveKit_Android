package com.qlive.likeservice.inner

import com.google.gson.JsonObject
import com.qlive.coreimpl.http.HttpClient
import com.qlive.likeservice.QLikeResponse

class LikeDataSource {

    suspend fun like(live_id: String, count: Int): QLikeResponse {
       return HttpClient.httpClient.put(
            "/client/live/room/${live_id}/like",
            JsonObject().apply {
                addProperty("count", count)
            }.toString(), QLikeResponse::class.java
        )
    }
}