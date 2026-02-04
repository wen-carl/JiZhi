package com.jizhi.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.widgetSentenceDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "widget_sentence"
)

data class WidgetSentenceData(
    val id: String = "",
    val content: String = "",
    val originContentList: List<String> = emptyList(),
    val title: String = "",
    val dynasty: String = "",
    val author: String = "",
    val translateList: List<String> = emptyList(),
    val isFavorite: Boolean = false
) {
    val originContent: String
        get() = originContentList.joinToString("\n")
}

data class WidgetCacheData(
    val content: String,
    val title: String,
    val dynasty: String,
    val author: String
)

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
            isFavorite = preferences[KEY_IS_FAVORITE]?.toBoolean() ?: false
        )
    }

    suspend fun saveSentence(sentence: WidgetSentenceData) {
        dataStore.edit { preferences ->
            preferences[KEY_ID] = sentence.id
            preferences[KEY_CONTENT] = sentence.content
            preferences[KEY_ORIGIN_CONTENT] = sentence.originContent
            preferences[KEY_TITLE] = sentence.title
            preferences[KEY_DYNASTY] = sentence.dynasty
            preferences[KEY_AUTHOR] = sentence.author
            preferences[KEY_IS_FAVORITE] = sentence.isFavorite.toString()
        }
    }

    suspend fun saveFromEntity(entity: SentenceEntity) {
        saveSentence(
            WidgetSentenceData(
                id = entity.id,
                content = entity.content,
                originContentList = entity.originContentList,
                title = entity.title,
                dynasty = entity.dynasty,
                author = entity.author,
                isFavorite = entity.isFavorite
            )
        )
    }

    suspend fun getSentence(): WidgetSentenceData {
        return sentenceFlow.first()
    }

    fun hasValidData(): Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[KEY_CONTENT]?.isNotEmpty() == true
    }
}
