package com.softsugar.library.sdk.net

import com.softsugar.library.sdk.entity.AuthEntity
import com.softsugar.library.sdk.entity.MaterialDataEntity
import com.softsugar.library.sdk.entity.ResultResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface APIInterface {

    companion object {
        const val BASE_URL = "http://sensemarsplatform.softsugar.com"
    }

    @FormUrlEncoded
    @POST("/access/studio/v1/sdkAuth")
    fun sdkAuth(
        @Field("appId") appId: String?,
        @Field("timestamp") timestamp: String?,
        @Field("sdkVersion") sdkVersion: String?,
        @Field("appVersion") appVersion: String?,
        @Field("uuid") uuid: String?,
        @Field("sign") sign: String?
    ): Call<ResultResponse<AuthEntity?>?>?

    @FormUrlEncoded
    @POST("/access/studio/v1/sdkAuth")
    fun sdkAuthNew(@Field("appId") appId: String?): Call<ResultResponse<AuthEntity?>?>?

    @FormUrlEncoded
    @POST("/api/studio/v1/materials/list")
    fun materialList(
        @Field("appId") appId: String?,
        @Field("timestamp") timestamp: String?,
        @Field("sdkVersion") sdkVersion: String?,
        @Field("appVersion") appVersion: String?,
        @Field("uuid") uuid: String?,
        @Field("data") data: String?,
        @Field("sign") sign: String?
    ): Call<ResultResponse<MaterialDataEntity?>?>?

    @FormUrlEncoded
    @POST("/api/studio/v1/materials/list")
    fun materialListNew(
        @Field("appId") appId: String,
        @Field("data") data: String
    ): Call<ResultResponse<MaterialDataEntity?>?>?

    @Streaming
    @GET
    fun download(@Url url: String): Call<ResponseBody>
}