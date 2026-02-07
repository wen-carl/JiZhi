package com.jizhi

/**
 * 应用常量类
 * 存放 API 地址、Action 名称、数据库名称等常量
 */
object Constants {

    /**
     * 今日诗词 API 基础地址
     */
    const val API_BASE_URL = "https://v2.jinrishici.com/"

    /**
     * 数据库名称
     */
    const val DATABASE_NAME = "jizhi_database.db"

    // ==================== 小组件 Action ====================

    /**
     * 更新所有小组件的 Action
     */
    const val ACTION_UPDATE_ALL = "com.jizhi.ACTION_UPDATE_ALL"

    /**
     * 打开小组件配置的 Action
     */
    const val ACTION_OPEN_CONFIG = "com.jizhi.ACTION_OPEN_CONFIG"

    /**
     * 刷新小组件的 Action
     */
    const val ACTION_REFRESH = "com.jizhi.ACTION_REFRESH"

    // ==================== 小组件 Intent Extra ====================

    /**
     * 小组件 ID 的 Extra 键
     */
    const val EXTRA_WIDGET_ID = "widget_id"

    /**
     * 是否来自小组件的 Extra 键
     */
    const val EXTRA_FROM_WIDGET = "from_widget"

    // ==================== WorkManager ====================

    /**
     * 句子更新 Work 的名称
     */
    const val WORK_NAME = "sentence_update_work"

    /**
     * 更新间隔小时数的 Key
     */
    const val KEY_INTERVAL_HOURS = "update_interval_hours"

    // ==================== Intent Extra (详情页) ====================

    /**
     * 诗句 ID
     */
    const val EXTRA_ID = "sentence_id"

    /**
     * 诗句内容
     */
    const val EXTRA_CONTENT = "content"

    /**
     * 原文内容
     */
    const val EXTRA_ORIGIN_CONTENT = "origin_content"

    /**
     * 翻译内容
     */
    const val EXTRA_TRANSLATE = "translate"

    /**
     * 标题
     */
    const val EXTRA_TITLE = "title"

    /**
     * 朝代
     */
    const val EXTRA_DYNASTY = "dynasty"

    /**
     * 作者
     */
    const val EXTRA_AUTHOR = "author"

    /**
     * 是否喜欢
     */
    const val EXTRA_IS_FAVORITE = "is_favorite"

    // ==================== 换行分隔符 ====================

    /**
     * 原文内容列表的分隔符（用于数据库存储）
     */
    const val CONTENT_SEPARATOR = "|||"

    /**
     * 标签分隔符
     */
    const val TAG_SEPARATOR = ","

    /**
     * 换行符
     */
    const val NEWLINE = "\n"
}
