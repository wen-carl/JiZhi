package com.jizhi.repository

import android.content.Context
import com.jizhi.data.local.DataStoreManager
import com.jizhi.data.local.SentenceDao
import com.jizhi.data.local.SentenceEntity
import com.jizhi.data.remote.JinrishiciApiService
import com.jizhi.data.remote.SentenceResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 诗句数据仓库
 * 整合网络请求和本地数据库操作
 * 使用 Hilt 进行依赖注入
 */
@Singleton
class SentenceRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sentenceDao: SentenceDao,
    private val apiService: JinrishiciApiService
) {

    /**
     * 获取今日推荐诗句
     * 从API获取并保存到数据库
     *
     * @return Result包装的响应结果
     */
    suspend fun getTodaySentence(): Result<SentenceResponse> = withContext(Dispatchers.IO) {
        try {
            val token = getOrCreateToken()
            val response = apiService.getSentence(token)

            if (response.status == "success" && response.data != null) {
                // 保存到数据库
                val entity = response.toEntity()
                sentenceDao.insert(entity)

                Result.success(response)
            } else {
                // 如果API返回错误，返回错误信息
                Result.failure(Exception(response.errMessage ?: "获取诗句失败"))
            }
        } catch (e: Exception) {
            // 网络请求失败，返回错误
            Result.failure(e)
        }
    }

    /**
     * 获取用户Token
     * 如果没有则请求新的Token
     */
    private suspend fun getOrCreateToken(): String = withContext(Dispatchers.IO) {
        var token = DataStoreManager.getToken(context)

        token.ifEmpty {
            try {
                val response = apiService.getToken()
                if (response.status == "success") {
                    token = response.data
                    DataStoreManager.saveToken(context, token)
                    token
                } else {
                    ""
                }
            } catch (e: Exception) {
                ""
            }
        }
    }

    /**
     * 获取所有历史句子
     */
    fun getAllSentences(): Flow<List<SentenceEntity>> {
        return sentenceDao.getAllSentences()
    }

    /**
     * 获取最近N条句子
     */
    fun getRecentSentences(limit: Int = 50): Flow<List<SentenceEntity>> {
        return sentenceDao.getRecentSentences(limit)
    }

    /**
     * 根据ID获取句子详情
     */
    suspend fun getSentenceById(id: String): SentenceEntity? = withContext(Dispatchers.IO) {
        sentenceDao.getSentenceById(id)
    }

    /**
     * 根据ID获取句子（Flow 监听版本）
     */
    fun getSentenceByIdFlow(id: String): Flow<SentenceEntity?> {
        return sentenceDao.getSentenceByIdFlow(id)
    }

    /**
     * 获取句子总数
     */
    suspend fun getSentenceCount(): Int = withContext(Dispatchers.IO) {
        sentenceDao.getCount()
    }

    /**
     * 删除指定句子
     */
    suspend fun deleteSentence(id: String) = withContext(Dispatchers.IO) {
        sentenceDao.deleteById(id)
    }

    /**
     * 清空所有历史记录
     */
    suspend fun clearAllHistory() = withContext(Dispatchers.IO) {
        sentenceDao.deleteAll()
    }

    /**
     * 检查句子是否已存在
     */
    suspend fun exists(id: String): Boolean = withContext(Dispatchers.IO) {
        sentenceDao.exists(id)
    }

    /**
     * 更新喜欢状态
     */
    suspend fun updateFavorite(id: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        sentenceDao.updateFavorite(id, isFavorite)
    }

    /**
     * 将API响应转换为数据库实体
     */
    private fun SentenceResponse.toEntity(): SentenceEntity {
        val data = this.data!!
        val origin = data.origin

        return SentenceEntity(
            id = data.id,
            content = data.content,
            originContentList = origin?.content ?: emptyList(),
            translateList = origin?.translate ?: emptyList(),
            popularity = data.popularity,
            title = origin?.title ?: "未知",
            dynasty = (origin?.dynasty ?: "未知").removeSuffix("代"),
            author = origin?.author ?: "未知",
            matchTags = data.matchTags?.joinToString(",") ?: "",
            recommendedReason = data.recommendedReason,
            cacheAt = data.cacheAt
        )
    }
}
