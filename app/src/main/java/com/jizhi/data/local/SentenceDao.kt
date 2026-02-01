package com.jizhi.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * 句子数据访问对象接口
 * 提供对句子数据库表的各种操作
 */
@Dao
interface SentenceDao {

    /**
     * 插入单条句子数据
     * 如果主键冲突则替换
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sentence: SentenceEntity)

    /**
     * 批量插入句子数据
     * 如果主键冲突则替换
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sentences: List<SentenceEntity>)

    /**
     * 获取所有句子（按创建时间倒序排列）
     */
    @Query("SELECT * FROM sentences ORDER BY createdAt DESC")
    fun getAllSentences(): Flow<List<SentenceEntity>>

    /**
     * 获取所有句子列表（非Flow版本）
     */
    @Query("SELECT * FROM sentences ORDER BY createdAt DESC")
    suspend fun getAllSentencesList(): List<SentenceEntity>

    /**
     * 获取最近N条句子
     */
    @Query("SELECT * FROM sentences ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentSentences(limit: Int): Flow<List<SentenceEntity>>

    /**
     * 获取最近N条句子（同步版本，用于 widget）
     */
    @Query("SELECT * FROM sentences ORDER BY createdAt DESC LIMIT :limit")
    suspend fun getRecentSentencesSync(limit: Int): List<SentenceEntity>

    /**
     * 根据ID获取句子（返回 Flow，数据库更新时自动通知）
     */
    @Query("SELECT * FROM sentences WHERE id = :id")
    fun getSentenceByIdFlow(id: String): Flow<SentenceEntity?>

    /**
     * 根据ID获取句子（挂起函数，一次性查询）
     */
    @Query("SELECT * FROM sentences WHERE id = :id")
    suspend fun getSentenceById(id: String): SentenceEntity?

    /**
     * 获取句子总数
     */
    @Query("SELECT COUNT(*) FROM sentences")
    suspend fun getCount(): Int

    /**
     * 删除指定句子
     */
    @Query("DELETE FROM sentences WHERE id = :id")
    suspend fun deleteById(id: String)

    /**
     * 清空所有句子
     */
    @Query("DELETE FROM sentences")
    suspend fun deleteAll()

    /**
     * 删除旧数据（保留最近的N条）
     */
    @Query("DELETE FROM sentences WHERE id NOT IN (SELECT id FROM sentences ORDER BY createdAt DESC LIMIT :keepCount)")
    suspend fun deleteOldData(keepCount: Int)

    /**
     * 检查句子是否已存在
     */
    @Query("SELECT EXISTS(SELECT 1 FROM sentences WHERE id = :id)")
    suspend fun exists(id: String): Boolean

    /**
     * 检查句子是否已喜欢
     */
    @Query("SELECT isFavorite FROM sentences WHERE id = :id")
    suspend fun isFavorite(id: String): Boolean

    /**
     * 更新喜欢状态
     */
    @Query("UPDATE sentences SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    /**
     * 获取所有喜欢的句子
     */
    @Query("SELECT * FROM sentences WHERE isFavorite = 1 ORDER BY createdAt DESC")
    fun getFavoriteSentences(): Flow<List<SentenceEntity>>

    /**
     * 随机获取一条句子
     */
    @Query("SELECT * FROM sentences ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomSentence(): SentenceEntity?
}
