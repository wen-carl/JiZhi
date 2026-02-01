package com.jizhi.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// DataStore 实例扩展
private val Context.widgetSentenceDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "widget_sentence"
)

/**
 * 小组件句子数据
 */
data class WidgetSentenceData(
    val id: String = "",
    val content: String = "",
    val originContentList: List<String> = emptyList(),
    val title: String = "",
    val dynasty: String = "",
    val author: String = "",
    val isFavorite: Boolean = false,
    val isFromWidget: Boolean = false,
    val updatedAt: Long = 0L
) {
    fun isEmpty(): Boolean = content.isEmpty()

    /**
     * 获取拼接后的全文（向后兼容）
     */
    val originContent: String
        get() = originContentList.joinToString("\n")
}

/**
 * 小组件句子 DataStore
 * 使用 Preferences DataStore 存储小组件显示的句子数据
 */
@Singleton
class WidgetSentenceDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.widgetSentenceDataStore

    companion object {
        private val KEY_ID = stringPreferencesKey("id")
        private val KEY_CONTENT = stringPreferencesKey("content")
        private val KEY_ORIGIN_CONTENT = stringPreferencesKey("origin_content")
        private val KEY_TITLE = stringPreferencesKey("title")
        private val KEY_DYNASTY = stringPreferencesKey("dynasty")
        private val KEY_AUTHOR = stringPreferencesKey("author")
        private val KEY_IS_FAVORITE = stringPreferencesKey("is_favorite")
        private val KEY_IS_FROM_WIDGET = stringPreferencesKey("is_from_widget")
        private val KEY_UPDATED_AT = stringPreferencesKey("updated_at")
    }

    /**
     * 获取句子数据 Flow
     */
    val sentenceFlow: Flow<WidgetSentenceData> = dataStore.data.map { preferences ->
        val originContentStr = preferences[KEY_ORIGIN_CONTENT] ?: ""
        val originContentList = if (originContentStr.isNotEmpty()) {
            originContentStr.split("\n").filter { it.isNotBlank() }
        } else emptyList()

        WidgetSentenceData(
            id = preferences[KEY_ID] ?: "",
            content = preferences[KEY_CONTENT] ?: "",
            originContentList = originContentList,
            title = preferences[KEY_TITLE] ?: "",
            dynasty = preferences[KEY_DYNASTY] ?: "",
            author = preferences[KEY_AUTHOR] ?: "",
            isFavorite = preferences[KEY_IS_FAVORITE]?.toBoolean() ?: false,
            isFromWidget = preferences[KEY_IS_FROM_WIDGET]?.toBoolean() ?: false,
            updatedAt = preferences[KEY_UPDATED_AT]?.toLongOrNull() ?: 0L
        )
    }

    /**
     * 保存句子数据
     */
    suspend fun saveSentence(sentence: WidgetSentenceData) {
        dataStore.edit { preferences ->
            preferences[KEY_ID] = sentence.id
            preferences[KEY_CONTENT] = sentence.content
            preferences[KEY_ORIGIN_CONTENT] = sentence.originContent
            preferences[KEY_TITLE] = sentence.title
            preferences[KEY_DYNASTY] = sentence.dynasty
            preferences[KEY_AUTHOR] = sentence.author
            preferences[KEY_IS_FAVORITE] = sentence.isFavorite.toString()
            preferences[KEY_IS_FROM_WIDGET] = sentence.isFromWidget.toString()
            preferences[KEY_UPDATED_AT] = System.currentTimeMillis().toString()
        }
    }

    /**
     * 从 SentenceEntity 保存
     */
    suspend fun saveFromEntity(entity: SentenceEntity) {
        saveSentence(
            WidgetSentenceData(
                id = entity.id,
                content = entity.content,
                originContentList = entity.originContentList,
                title = entity.title,
                dynasty = entity.dynasty,
                author = entity.author,
                isFavorite = entity.isFavorite,
                isFromWidget = entity.isFromWidget,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    /**
     * 获取句子数据
     */
    suspend fun getSentence(): WidgetSentenceData {
        var result = WidgetSentenceData()
        dataStore.data.collect { preferences ->
            val originContentStr = preferences[KEY_ORIGIN_CONTENT] ?: ""
            val originContentList = if (originContentStr.isNotEmpty()) {
                originContentStr.split("\n").filter { it.isNotBlank() }
            } else emptyList()

            result = WidgetSentenceData(
                id = preferences[KEY_ID] ?: "",
                content = preferences[KEY_CONTENT] ?: "",
                originContentList = originContentList,
                title = preferences[KEY_TITLE] ?: "",
                dynasty = preferences[KEY_DYNASTY] ?: "",
                author = preferences[KEY_AUTHOR] ?: "",
                isFavorite = preferences[KEY_IS_FAVORITE]?.toBoolean() ?: false,
                isFromWidget = preferences[KEY_IS_FROM_WIDGET]?.toBoolean() ?: false,
                updatedAt = preferences[KEY_UPDATED_AT]?.toLongOrNull() ?: 0L
            )
        }
        return result
    }

    /**
     * 清空数据
     */
    suspend fun clear() {
        dataStore.edit { it.clear() }
    }

    /**
     * 是否存在有效数据
     */
    fun hasValidData(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_CONTENT]?.isNotEmpty() == true
    }
}
