package com.jizhi

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.preference.PreferenceManager
import java.util.Locale

/**
 * 语言帮助类
 * 提供语言设置、保存、获取功能
 */
object LocaleHelper {

    /**
     * 设置应用语言
     * @param context 上下文
     * @param language 语言代码
     */
    fun setLocale(context: Context, language: String): Context {
        saveLanguage(context, language)
        return updateResources(context, language)
    }

    /**
     * 保存语言设置
     */
    private fun saveLanguage(context: Context, language: String) {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        prefs.edit().putString(LanguageConstants.KEY_LANGUAGE, language).apply()
    }

    /**
     * 获取保存的语言设置
     */
    fun getSavedLanguage(context: Context): String {
        val prefs = PreferenceManager.getDefaultSharedPreferences(context)
        return prefs.getString(LanguageConstants.KEY_LANGUAGE, LanguageConstants.DEFAULT_LANGUAGE)
            ?: LanguageConstants.DEFAULT_LANGUAGE
    }

    /**
     * 根据保存的设置初始化应用语言
     */
    fun initLocale(context: Context): Context {
        val language = getSavedLanguage(context)
        return updateResources(context, language)
    }

    /**
     * 更新资源配置
     */
    private fun updateResources(context: Context, language: String): Context {
        val locale = LanguageConstants.Language.fromValue(language).locale
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        return context.createConfigurationContext(config)
    }

    /**
     * 获取当前语言对应的 Locale
     */
    fun getCurrentLocale(context: Context): Locale {
        val language = getSavedLanguage(context)
        return LanguageConstants.Language.fromValue(language).locale
    }

    /**
     * 检查是否为中文环境
     */
    fun isChinese(context: Context): Boolean {
        val locale = getCurrentLocale(context)
        return locale.language == Locale.CHINESE.language || locale.language == "zh"
    }
}
