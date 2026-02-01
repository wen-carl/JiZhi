package com.jizhi.data.remote

import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers

/**
 * 今日诗词API服务接口
 * 定义所有API请求方法
 */
interface JinrishiciApiService {

    /**
     * 获取用户Token
     * Token用于标识用户身份，永久有效
     */
    @GET("token")
    suspend fun getToken(): TokenResponse

    /**
     * 获取每日推荐诗句
     * 需要在Header中携带Token
     *
     * @param token 用户令牌
     * @return 包含诗句数据的响应
     */
    @GET("sentence")
    @Headers("Accept: application/json")
    suspend fun getSentence(
        @Header("X-User-Token") token: String
    ): SentenceResponse

    /**
     * 获取API信息（用于测试）
     *
     * @param token 用户令牌（可选）
     * @return API信息响应
     */
    @GET("info")
    suspend fun getInfo(
        @Header("X-User-Token") token: String? = null
    ): retrofit2.Response<String>
}
