package com.jizhi.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.tokenDataStore: DataStore<Preferences> by preferencesDataStore(name = "jizhi_token_prefs")
private val Context.widgetDataStore: DataStore<Preferences> by preferencesDataStore(name = "jizhi_widget_prefs")

object DataStoreManager {

    private val KEY_USER_TOKEN = stringPreferencesKey("user_token")

    suspend fun getToken(context: Context): String {
        return context.tokenDataStore.data.map { preferences ->
            preferences[KEY_USER_TOKEN] ?: ""
        }.first()
    }

    suspend fun saveToken(context: Context, token: String) {
        context.tokenDataStore.edit { preferences ->
            preferences[KEY_USER_TOKEN] = token
        }
    }

    suspend fun clearToken(context: Context) {
        context.tokenDataStore.edit { preferences ->
            preferences.remove(KEY_USER_TOKEN)
        }
    }

    private val KEY_WIDGET_ID = stringPreferencesKey("widget_id")
    private val KEY_WIDGET_SENTENCE = stringPreferencesKey("widget_sentence")
    private val KEY_WIDGET_ORIGIN_CONTENT = stringPreferencesKey("widget_origin_content")
    private val KEY_WIDGET_TRANSLATE = stringPreferencesKey("widget_translate")
    private val KEY_WIDGET_TITLE = stringPreferencesKey("widget_title")
    private val KEY_WIDGET_DYNASTY = stringPreferencesKey("widget_dynasty")
    private val KEY_WIDGET_AUTHOR = stringPreferencesKey("widget_author")
    private val KEY_WIDGET_IS_FAVORITE = booleanPreferencesKey("widget_is_favorite")
    private val KEY_WIDGET_UPDATE_TIME = longPreferencesKey("widget_update_time")

    // 小组件配置相关的 Keys
    private val KEY_LINE_BREAK_MODE = intPreferencesKey("line_break_mode")
    private val KEY_WIDGET_BACKGROUND_COLOR = longPreferencesKey("widget_background_color")
    private val KEY_WIDGET_TEXT_COLOR = longPreferencesKey("widget_text_color")
    private val KEY_SELECTED_FONT_ID = stringPreferencesKey("selected_font_id")
    private val KEY_CUSTOM_FONT_PATHS = stringPreferencesKey("custom_font_paths")
    private val KEY_UPDATE_INTERVAL_HOURS = stringPreferencesKey("update_interval_hours")
    private val KEY_WIDGET_CONFIG_PREFIX = "widget_config_"

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
        context.widgetDataStore.edit { preferences ->
            preferences[KEY_WIDGET_ID] = id
            preferences[KEY_WIDGET_SENTENCE] = content
            preferences[KEY_WIDGET_ORIGIN_CONTENT] = originContentList.joinToString("\n")
            preferences[KEY_WIDGET_TRANSLATE] = translateList.joinToString("\n")
            preferences[KEY_WIDGET_TITLE] = title
            preferences[KEY_WIDGET_DYNASTY] = dynasty
            preferences[KEY_WIDGET_AUTHOR] = author
            preferences[KEY_WIDGET_IS_FAVORITE] = isFavorite
            preferences[KEY_WIDGET_UPDATE_TIME] = System.currentTimeMillis()
        }
    }

    suspend fun getWidgetSentence(context: Context): String {
        return context.widgetDataStore.data.map { preferences ->
            preferences[KEY_WIDGET_SENTENCE] ?: ""
        }.first()
    }

    suspend fun getWidgetSentenceDetail(context: Context): WidgetSentenceData? {
        val preferences = context.widgetDataStore.data.first()
        val content = preferences[KEY_WIDGET_SENTENCE] ?: ""
        if (content.isEmpty()) return null

        return WidgetSentenceData(
            id = preferences[KEY_WIDGET_ID] ?: "",
            content = content,
            title = preferences[KEY_WIDGET_TITLE] ?: "诗词",
            dynasty = preferences[KEY_WIDGET_DYNASTY] ?: "未知",
            author = preferences[KEY_WIDGET_AUTHOR] ?: "未知",
            originContentList = emptyList(),
            translateList = emptyList(),
            isFavorite = preferences[KEY_WIDGET_IS_FAVORITE] ?: false
        )
    }

    suspend fun updateWidgetFavorite(context: Context, isFavorite: Boolean) {
        context.widgetDataStore.edit { preferences ->
            preferences[KEY_WIDGET_IS_FAVORITE] = isFavorite
        }
    }

    suspend fun getWidgetCacheData(context: Context): WidgetCacheData {
        val preferences = context.widgetDataStore.data.first()
        return WidgetCacheData(
            content = preferences[KEY_WIDGET_SENTENCE] ?: "",
            title = preferences[KEY_WIDGET_TITLE] ?: "",
            dynasty = preferences[KEY_WIDGET_DYNASTY] ?: "",
            author = preferences[KEY_WIDGET_AUTHOR] ?: ""
        )
    }

    // ==================== 小组件配置存储 ====================

    suspend fun getLineBreakMode(context: Context): Int {
        val preferences = context.widgetDataStore.data.first()
        return preferences[KEY_LINE_BREAK_MODE] ?: 0
    }

    suspend fun saveLineBreakMode(context: Context, mode: Int) {
        context.widgetDataStore.edit { preferences ->
            preferences[KEY_LINE_BREAK_MODE] = mode
        }
    }

    suspend fun getWidgetBackgroundColor(context: Context): Long {
        val preferences = context.widgetDataStore.data.first()
        return preferences[KEY_WIDGET_BACKGROUND_COLOR] ?: 0L
    }

    suspend fun saveWidgetBackgroundColor(context: Context, color: Long) {
        context.widgetDataStore.edit { preferences ->
            preferences[KEY_WIDGET_BACKGROUND_COLOR] = color
        }
    }

    suspend fun getWidgetTextColor(context: Context): Long {
        val preferences = context.widgetDataStore.data.first()
        return preferences[KEY_WIDGET_TEXT_COLOR] ?: 0xFFFFFFFFL
    }

    suspend fun saveWidgetTextColor(context: Context, color: Long) {
        context.widgetDataStore.edit { preferences ->
            preferences[KEY_WIDGET_TEXT_COLOR] = color
        }
    }

    suspend fun getSelectedFontId(context: Context): String {
        val preferences = context.widgetDataStore.data.first()
        return preferences[KEY_SELECTED_FONT_ID] ?: "serif"
    }

    suspend fun saveSelectedFontId(context: Context, fontId: String) {
        context.widgetDataStore.edit { preferences ->
            preferences[KEY_SELECTED_FONT_ID] = fontId
        }
    }

    suspend fun getCustomFontPaths(context: Context): List<String> {
        val preferences = context.widgetDataStore.data.first()
        val json = preferences[KEY_CUSTOM_FONT_PATHS] ?: "[]"
        return try {
            org.json.JSONArray(json).let { arr ->
                (0 until arr.length()).map { arr.getString(it) }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveCustomFontPaths(context: Context, paths: List<String>) {
        context.widgetDataStore.edit { preferences ->
            preferences[KEY_CUSTOM_FONT_PATHS] = org.json.JSONArray(paths).toString()
        }
    }

    suspend fun addCustomFontPath(context: Context, fontPath: String) {
        val currentPaths = getCustomFontPaths(context).toMutableList()
        if (!currentPaths.contains(fontPath)) {
            currentPaths.add(fontPath)
            saveCustomFontPaths(context, currentPaths)
        }
    }

    suspend fun removeCustomFontPath(context: Context, fontPath: String) {
        val currentPaths = getCustomFontPaths(context).toMutableList()
        currentPaths.remove(fontPath)
        saveCustomFontPaths(context, currentPaths)
    }

    suspend fun getUpdateIntervalHours(context: Context): Float {
        val preferences = context.widgetDataStore.data.first()
        return preferences[KEY_UPDATE_INTERVAL_HOURS]?.toFloatOrNull() ?: 1f
    }

    suspend fun saveUpdateIntervalHours(context: Context, hours: Float) {
        context.widgetDataStore.edit { preferences ->
            preferences[KEY_UPDATE_INTERVAL_HOURS] = hours.toString()
        }
    }

    // ==================== WidgetConfig JSON 存储 ====================

    suspend fun saveWidgetConfig(context: Context, widgetId: Int, json: String) {
        val key = stringPreferencesKey("${KEY_WIDGET_CONFIG_PREFIX}$widgetId")
        context.widgetDataStore.edit { preferences ->
            preferences[key] = json
        }
    }

    suspend fun getWidgetConfig(context: Context, widgetId: Int): String? {
        val key = stringPreferencesKey("${KEY_WIDGET_CONFIG_PREFIX}$widgetId")
        val preferences = context.widgetDataStore.data.first()
        return preferences[key]
    }

    suspend fun removeWidgetConfig(context: Context, widgetId: Int) {
        val key = stringPreferencesKey("${KEY_WIDGET_CONFIG_PREFIX}$widgetId")
        context.widgetDataStore.edit { preferences ->
            preferences.remove(key)
        }
    }
}
