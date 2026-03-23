package com.jizhi

import java.util.Locale

/**
 * 语言设置常量类
 */
object LanguageConstants {

    /**
     * 语言选项
     */
    enum class Language(val value: String, val locale: Locale) {
        SYSTEM("system", Locale.getDefault()),
        CHINESE("chinese", Locale.SIMPLIFIED_CHINESE),
        ENGLISH("english", Locale.ENGLISH);

        companion object {
            /**
             * 根据值获取语言枚举
             */
            fun fromValue(value: String): Language {
                return entries.find { it.value == value } ?: SYSTEM
            }
        }
    }

    /**
     * SharedPreferences key
     */
    const val KEY_LANGUAGE = "language_setting"

    /**
     * 默认语言（跟随系统）
     */
    const val DEFAULT_LANGUAGE = "system"
}
