package com.jizhi.data.remote

/**
 * 小组件句子数据类（带标题、作者信息）
 */
data class WidgetSentence(
    val id: String = "",
    val content: String,
    val originContentList: List<String> = emptyList(),
    val translateList: List<String> = emptyList(),
    val title: String,
    val dynasty: String,
    val author: String,
    val isFavorite: Boolean = false
) {
    /**
     * 获取拼接后的全文（向后兼容）
     */
    val originContent: String
        get() = originContentList.joinToString("\n")

    /**
     * 获取拼接后的翻译（向后兼容）
     */
    val translate: String
        get() = translateList.joinToString("\n")
}
