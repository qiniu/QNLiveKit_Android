package com.qlive.coreimpl

import com.qlive.core.QLiveCallBack
import com.qlive.core.been.*
import com.qlive.coreimpl.http.HttpClient
import com.qlive.coreimpl.http.NetBzException
import com.qlive.coreimpl.http.PageData
import com.qlive.coreimpl.http.header_cache_name
import com.qlive.coreimpl.model.*
import com.qlive.coreimpl.model.LiveStatisticsReq
import com.qlive.jsonutil.JsonUtils
import com.qlive.jsonutil.ParameterizedTypeImpl
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class QLiveDataSource {

    /**
     * 刷新房间信息
     */
    suspend fun refreshRoomInfo(liveId: String): QLiveRoomInfo {
        return HttpClient.httpClient.get(
            "/client/live/room/info/${liveId}",
            null,
            QLiveRoomInfo::class.java
        )
    }

    suspend fun profile(): InnerUser {
        return HttpClient.httpClient.get("/client/user/profile", null, InnerUser::class.java)
    }

    suspend fun appConfig(): AppConfig {
        return HttpClient.httpClient.get("/client/app/config", null, AppConfig::class.java)
    }

    suspend fun listRoom(pageNumber: Int, pageSize: Int): PageData<QLiveRoomInfo> {
        val p = ParameterizedTypeImpl(
            arrayOf(QLiveRoomInfo::class.java),
            PageData::class.java,
            PageData::class.java
        )
        val date: PageData<QLiveRoomInfo> =
            HttpClient.httpClient.get("/client/live/room/list", HashMap<String, String>().apply {
                put("page_num", pageNumber.toString())
                put("page_size", pageSize.toString())
            }, null, p)
        return date
    }

    suspend fun liveRecord(pageNumber: Int, pageSize: Int): PageData<QLiveRoomInfo> {
        val p = ParameterizedTypeImpl(
            arrayOf(QLiveRoomInfo::class.java),
            PageData::class.java,
            PageData::class.java
        )
        val date: PageData<QLiveRoomInfo> =
            HttpClient.httpClient.get("/client/live/room/list/anchor", HashMap<String, String>().apply {
                put("page_num", pageNumber.toString())
                put("page_size", pageSize.toString())
            }, null, p)
        return date
    }

    suspend fun createRoom(param: QCreateRoomParam): QLiveRoomInfo {
        return HttpClient.httpClient.post(
            "/client/live/room/instance",
            JsonUtils.toJson(param),
            QLiveRoomInfo::class.java
        )
    }

    suspend fun deleteRoom(liveId: String) {
        HttpClient.httpClient.delete(
            "/live/room/instance/${liveId}",
            "{}",
            Any::class.java
        )
    }

    suspend fun pubRoom(liveId: String): QLiveRoomInfo {
        return HttpClient.httpClient.put(
            "/client/live/room/${liveId}",
            "{}",
            QLiveRoomInfo::class.java
        )
    }

    suspend fun unPubRoom(liveId: String): QLiveRoomInfo {
        return HttpClient.httpClient.delete(
            "/client/live/room/${liveId}",
            "{}",
            QLiveRoomInfo::class.java
        )
    }

    suspend fun joinRoom(liveId: String): QLiveRoomInfo {
        return HttpClient.httpClient.post(
            "/client/live/room/user/${liveId}",
            "{}",
            QLiveRoomInfo::class.java
        )
    }

    suspend fun leaveRoom(liveId: String) {
        HttpClient.httpClient.delete("/client/live/room/user/${liveId}", "{}", Any::class.java)
    }

    /**
     * 跟新直播扩展信息
     * @param extension
     */
    suspend fun updateRoomExtension(liveId: String, extension: QExtension) {
        val json = JSONObject()
        json.put("live_id", liveId)
        json.put("extends", extension)

        HttpClient.httpClient.put(
            "/client/live/room/extends",
            json.toString(),
            Any::class.java
        )
    }

    suspend fun heartbeat(liveId: String): HearBeatResp {
        return HttpClient.httpClient.get(
            "/client/live/room/heartbeat/${liveId}",
            null,
            HearBeatResp::class.java
        )
    }

    suspend fun liveStatisticsReq(statistics: List<LiveStatistics>) {
        val liveStatisticsReq = LiveStatisticsReq().apply {
            Data = statistics
        }
        HttpClient.httpClient.post(
            "/client/stats/singleLive",
            JsonUtils.toJson(liveStatisticsReq),
            Any::class.java
        )
    }

    suspend fun liveStatisticsGet(liveID: String): QLiveStatistics {
        return HttpClient.httpClient.get(
            "/client/stats/singleLive/${liveID}",
            null,
            QLiveStatistics::class.java
        )
    }

    /**
     * 当前房间在线用户
     */
    suspend fun getOnlineUser(
        liveId: String,
        page_num: Int,
        page_size: Int
    ): PageData<QLiveUser> {
        val p = ParameterizedTypeImpl(
            arrayOf(QLiveUser::class.java),
            PageData::class.java,
            PageData::class.java
        )

        val data: PageData<QLiveUser> = HttpClient.httpClient.get(
            "/client/live/room/user_list",
            HashMap<String, String>().apply {
                put("live_id", liveId)
                put("page_num", page_num.toString())
                put("page_size", page_size.toString())
            },
            null,
            p
        )
        return data
    }

    /**
     * 使用用户ID搜索房间用户
     *
     * @param uid
     */
    suspend fun searchUserByUserId(uid: String, useCache: Boolean = true): QLiveUser {
        val p = ParameterizedTypeImpl(
            arrayOf(QLiveUser::class.java),
            List::class.java,
            List::class.java
        )
        val list = HttpClient.httpClient.get<List<QLiveUser>>(
            "/client/user/users",
            HashMap<String, String>().apply {
                put("user_ids", uid)
            },
            if (useCache) {
                HashMap<String, String>().apply {
                    put(header_cache_name, "60")
                }
            } else {
                null
            },
            null,
            p
        )
        return if (list.isEmpty()) {
            throw NetBzException(-1, "targetUser is null")
        } else {
            list[0]
        }
    }

    /**
     * 使用用户im uid 搜索用户
     *
     * @param imUid
     * @param callBack
     */
    suspend fun searchUserByIMUid(imUid: String): QLiveUser {
        val p = ParameterizedTypeImpl(
            arrayOf(QLiveUser::class.java),
            List::class.java,
            List::class.java
        )
        val list = HttpClient.httpClient.get<List<QLiveUser>>(
            "/client/user/imusers",
            HashMap<String, String>().apply {
                put("im_user_ids", imUid)
            },
            null,
            p
        )
        return if (list.isEmpty()) {
            throw NetBzException(-1, "targetUser is null")
        } else {
            list[0]
        }
    }


    suspend fun updateUser(
        avatar: String,
        nickName: String,
        extensions: Map<String, String>?
    ) {
        val user = QLiveUser()
        user.avatar = avatar
        user.nick = nickName
        user.extensions = extensions
        HttpClient.httpClient.put("/client/user/user", JsonUtils.toJson(user), Any::class.java)
    }


}