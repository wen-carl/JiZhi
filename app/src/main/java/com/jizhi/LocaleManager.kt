package com.jizhi

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.compositionLocalOf
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Locale 管理器
 * 提供 Compose 实时语言切换支持
 */
object LocaleManager {

    private val _currentLocale = MutableStateFlow<Locale>(Locale.getDefault())

    /**
     * 当前 Locale 状态流
     */
    val currentLocale: StateFlow<Locale> = _currentLocale.asStateFlow()

    /**
     * 获取当前语言
     */
    fun getCurrentLocale(context: Context): Locale {
        val savedLanguage = getSavedLanguage(context)
        return when (savedLanguage) {
            LanguageConstants.Language.CHINESE.value -> Locale.SIMPLIFIED_CHINESE
            LanguageConstants.Language.ENGLISH.value -> Locale.ENGLISH
            else -> Locale.getDefault()
        }
    }

    /**
     * 设置当前语言并触发重组
     */
    fun setLocale(context: Context, language: String) {
        saveLanguage(context, language)
        val newLocale = when (language) {
            LanguageConstants.Language.CHINESE.value -> Locale.SIMPLIFIED_CHINESE
            LanguageConstants.Language.ENGLISH.value -> Locale.ENGLISH
            else -> Locale.getDefault()
        }
        _currentLocale.value = newLocale
    }

    /**
     * 初始化语言（应用启动时调用）
     */
    fun initLocale(context: Context) {
        _currentLocale.value = getCurrentLocale(context)
    }

    /**
     * 获取保存的语言设置
     */
    private fun getSavedLanguage(context: Context): String {
        val prefs = context.getSharedPreferences("jizhi_prefs", Context.MODE_PRIVATE)
        return prefs.getString(LanguageConstants.KEY_LANGUAGE, LanguageConstants.DEFAULT_LANGUAGE)
            ?: LanguageConstants.DEFAULT_LANGUAGE
    }

    /**
     * 获取保存的 Locale（用于 attachBaseContext）
     */
    fun getSavedLocale(context: Context): Locale {
        val savedLanguage = getSavedLanguage(context)
        return when (savedLanguage) {
            LanguageConstants.Language.CHINESE.value -> Locale.SIMPLIFIED_CHINESE
            LanguageConstants.Language.ENGLISH.value -> Locale.ENGLISH
            else -> Locale.getDefault()
        }
    }

    /**
     * 保存语言设置
     */
    private fun saveLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences("jizhi_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(LanguageConstants.KEY_LANGUAGE, language).apply()
    }
}

/**
 * CompositionLocal 用于提供当前 Locale
 */
val LocalLocale = compositionLocalOf { Locale.getDefault() }
