package com.jizhi.data.remote

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * 今日诗词API网络客户端
 * 配置Retrofit和OkHttpClient
 */
object JinrishiciClient {

    private const val BASE_URL = "https://v2.jinrishici.com/"
    private const val PREFS_NAME = "jizhi_token_prefs"
    private const val KEY_TOKEN = "user_token"

    /**
     * API服务实例（懒加载）
     */
    val apiService: JinrishiciApiService by lazy {
        createApiService()
    }

    /**
     * 创建API服务实例
     */
    private fun createApiService(): JinrishiciApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(JinrishiciApiService::class.java)
    }

    /**
     * 创建OkHttpClient
     * 配置超时时间和日志拦截器
     */
    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    /**
     * 获取用户Token
     * 如果没有则请求新的Token
     */
    suspend fun getOrCreateToken(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var token = prefs.getString(KEY_TOKEN, null)

        if (token.isNullOrEmpty()) {
            try {
                val response = apiService.getToken()
                if (response.status == "success") {
                    token = response.data
                    prefs.edit().putString(KEY_TOKEN, token).apply()
                } else {
                    throw Exception("获取Token失败")
                }
            } catch (e: Exception) {
                // 如果网络请求失败，使用备用Token
                token = "default_token"
            }
        }

        return token ?: "default_token"
    }

    /**
     * 获取保存的Token
     */
    fun getSavedToken(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_TOKEN, "") ?: ""
    }

    /**
     * 保存Token
     */
    fun saveToken(context: Context, token: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    /**
     * 清除Token
     */
    fun clearToken(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    private const val WIDGET_PREFS_NAME = "jizhi_widget_prefs"
    private const val KEY_WIDGET_ID = "widget_id"
    private const val KEY_WIDGET_SENTENCE = "widget_sentence"
    private const val KEY_WIDGET_ORIGIN_CONTENT = "widget_origin_content"
    private const val KEY_WIDGET_TRANSLATE = "widget_translate"
    private const val KEY_WIDGET_TITLE = "widget_title"
    private const val KEY_WIDGET_DYNASTY = "widget_dynasty"
    private const val KEY_WIDGET_AUTHOR = "widget_author"
    private const val KEY_WIDGET_IS_FAVORITE = "widget_is_favorite"
    private const val KEY_WIDGET_UPDATE_TIME = "widget_update_time"

    /**
     * 保存今日诗句供小组件使用
     * @param isFavorite 喜欢状态
     */
    fun saveTodaySentenceForWidget(
        context: Context,
        id: String = "",
        content: String,
        originContentList: List<String> = emptyList(),
        translateList: List<String> = emptyList(),
        title: String = "",
        dynasty: String = "",
        author: String = "",
        isFavorite: Boolean = false
    ) {
        val prefs = context.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
        // 清理朝代：删除"代"字（"宋代" -> "宋"）
        val cleanDynasty = dynasty.removeSuffix("代")
        prefs.edit().apply {
            putString(KEY_WIDGET_ID, id)
            putString(KEY_WIDGET_SENTENCE, content)
            putString(KEY_WIDGET_ORIGIN_CONTENT, originContentList.joinToString("\n"))
            putString(KEY_WIDGET_TRANSLATE, translateList.joinToString("\n"))
            putString(KEY_WIDGET_TITLE, title)
            putString(KEY_WIDGET_DYNASTY, cleanDynasty)
            putString(KEY_WIDGET_AUTHOR, author)
            putBoolean(KEY_WIDGET_IS_FAVORITE, isFavorite)
            putLong(KEY_WIDGET_UPDATE_TIME, System.currentTimeMillis())
            apply()
        }
    }

    /**
     * 获取小组件保存的句子内容
     */
    fun getWidgetSentence(context: Context): String {
        val prefs = context.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_WIDGET_SENTENCE, "") ?: ""
    }

    /**
     * 获取小组件保存的句子详情（带标题、作者）
     */
    fun getWidgetSentenceDetail(context: Context): WidgetSentence? {
        val prefs = context.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
        val content = prefs.getString(KEY_WIDGET_SENTENCE, "") ?: ""
        if (content.isEmpty()) return null

        return WidgetSentence(
            id = prefs.getString(KEY_WIDGET_ID, "") ?: "",
            content = content,
            title = prefs.getString(KEY_WIDGET_TITLE, "") ?: "诗词",
            dynasty = prefs.getString(KEY_WIDGET_DYNASTY, "") ?: "未知",
            author = prefs.getString(KEY_WIDGET_AUTHOR, "") ?: "未知",
            originContentList = emptyList(),
            translateList = emptyList(),
            isFavorite = prefs.getBoolean(KEY_WIDGET_IS_FAVORITE, false)
        )
    }

    /**
     * 更新小组件喜欢状态
     */
    fun updateWidgetFavorite(context: Context, isFavorite: Boolean) {
        val prefs = context.getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_WIDGET_IS_FAVORITE, isFavorite).apply()
    }
}
