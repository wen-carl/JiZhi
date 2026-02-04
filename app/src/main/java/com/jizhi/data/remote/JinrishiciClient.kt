package com.jizhi.data.remote

import android.content.Context
import com.jizhi.data.local.DataStoreManager
import com.jizhi.data.local.WidgetSentenceData
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object JinrishiciClient {

    private const val BASE_URL = "https://v2.jinrishici.com/"

    val apiService: JinrishiciApiService by lazy {
        createApiService()
    }

    private fun createApiService(): JinrishiciApiService {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        return retrofit.create(JinrishiciApiService::class.java)
    }

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

    suspend fun getOrCreateToken(context: Context): String {
        var token = DataStoreManager.getToken(context)

        if (token.isEmpty()) {
            try {
                val response = apiService.getToken()
                if (response.status == "success") {
                    token = response.data
                    DataStoreManager.saveToken(context, token)
                } else {
                    throw Exception("获取Token失败")
                }
            } catch (e: Exception) {
                token = "default_token"
            }
        }

        return token.ifEmpty { "default_token" }
    }

    suspend fun getSavedToken(context: Context): String {
        return DataStoreManager.getToken(context)
    }

    suspend fun saveToken(context: Context, token: String) {
        DataStoreManager.saveToken(context, token)
    }

    suspend fun clearToken(context: Context) {
        DataStoreManager.clearToken(context)
    }

    suspend fun saveTodaySentenceForWidget(
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
        DataStoreManager.saveTodaySentenceForWidget(
            context = context,
            id = id,
            content = content,
            originContentList = originContentList,
            translateList = translateList,
            title = title,
            dynasty = dynasty,
            author = author,
            isFavorite = isFavorite
        )
    }

    suspend fun getWidgetSentence(context: Context): String {
        return DataStoreManager.getWidgetSentence(context)
    }

    suspend fun getWidgetSentenceDetail(context: Context): WidgetSentenceData? {
        return DataStoreManager.getWidgetSentenceDetail(context)
    }

    suspend fun updateWidgetFavorite(context: Context, isFavorite: Boolean) {
        DataStoreManager.updateWidgetFavorite(context, isFavorite)
    }
}
