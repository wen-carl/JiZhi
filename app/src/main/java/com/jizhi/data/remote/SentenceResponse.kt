package com.jizhi.data.remote

import com.google.gson.annotations.SerializedName

/**
 * 今日诗词API响应类
 * 解析从API获取的JSON数据
 */
data class SentenceResponse(
    @SerializedName("status")
    val status: String,                    // 响应状态

    @SerializedName("data")
    val data: SentenceData?,               // 诗句数据

    @SerializedName("token")
    val token: String,                     // 用户令牌

    @SerializedName("ipAddress")
    val ipAddress: String,                 // IP地址

    @SerializedName("errCode")
    val errCode: Int? = null,              // 错误码

    @SerializedName("errMessage")
    val errMessage: String? = null         // 错误信息
)

/**
 * 诗句数据类
 */
data class SentenceData(
    @SerializedName("id")
    val id: String,                        // 诗句ID

    @SerializedName("content")
    val content: String,                   // 诗句内容

    @SerializedName("popularity")
    val popularity: Int,                   // 流行度

    @SerializedName("origin")
    val origin: SentenceOrigin?,           // 原始诗词信息

    @SerializedName("matchTags")
    val matchTags: List<String>?,          // 匹配标签

    @SerializedName("recommendedReason")
    val recommendedReason: String,         // 推荐原因

    @SerializedName("cacheAt")
    val cacheAt: String                    // 缓存时间
)

/**
 * 原始诗词信息类
 */
data class SentenceOrigin(
    @SerializedName("title")
    val title: String,                     // 诗词标题

    @SerializedName("dynasty")
    val dynasty: String,                   // 朝代

    @SerializedName("author")
    val author: String,                    // 作者

    @SerializedName("content")
    val content: List<String>?,            // 诗词全文

    @SerializedName("translate")
    val translate: List<String>?           // 翻译
)

/**
 * Token响应类
 */
data class TokenResponse(
    @SerializedName("status")
    val status: String,                    // 响应状态

    @SerializedName("data")
    val data: String                       // Token字符串
)
