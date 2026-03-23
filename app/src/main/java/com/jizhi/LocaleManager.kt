package com.jizhi

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * Locale 管理器
 * 提供 Compose 实时语言切换支持
 */
object LocaleManager {

    private var currentLocale by mutableStateOf<Locale>(Locale.getDefault())

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
    fun setLocale(context: Context, language: String): Locale {
        saveLanguage(context, language)
        currentLocale = when (language) {
            LanguageConstants.Language.CHINESE.value -> Locale.SIMPLIFIED_CHINESE
            LanguageConstants.Language.ENGLISH.value -> Locale.ENGLISH
            else -> Locale.getDefault()
        }
        return updateContextLocale(context, currentLocale)
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
     * 保存语言设置
     */
    private fun saveLanguage(context: Context, language: String) {
        val prefs = context.getSharedPreferences("jizhi_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString(LanguageConstants.KEY_LANGUAGE, language).apply()
    }

    /**
     * 更新 Context 的 locale 配置
     */
    private fun updateContextLocale(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        return context.createConfigurationContext(config)
    }

    /**
     * 获取语言显示名称
     */
    fun getLanguageDisplayName(context: Context): String {
        val language = getSavedLanguage(context)
        return when (language) {
            LanguageConstants.Language.SYSTEM.value -> context.getString(R.string.language_system)
            LanguageConstants.Language.CHINESE.value -> context.getString(R.string.language_chinese)
            LanguageConstants.Language.ENGLISH.value -> context.getString(R.string.language_english)
            else -> context.getString(R.string.language_system)
        }
    }
}

/**
 * CompositionLocal 用于提供当前 Locale
 */
val LocalLocale = compositionLocalOf { Locale.getDefault() }
