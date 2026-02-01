package com.jizhi.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 句子数据实体类
 * 用于存储从今日诗词API获取的诗句数据
 */
@Entity(tableName = "sentences")
data class SentenceEntity(
    @PrimaryKey
    val id: String,                    // 诗句唯一标识符
    val content: String,               // 诗句短句内容
    val originContentList: List<String> = emptyList(),  // 诗词全文（从origin获取，List格式）
    val translateList: List<String> = emptyList(),      // 翻译内容（List格式）
    val popularity: Int,               // 流行度评分
    val title: String,                 // 诗词标题
    val dynasty: String,               // 朝代
    val author: String,                // 作者
    val matchTags: String = "",        // 匹配标签（逗号分隔）
    val recommendedReason: String = "",// 推荐原因
    val cacheAt: String = "",          // 缓存时间
    val createdAt: Long = System.currentTimeMillis(),  // 创建时间
    val isFavorite: Boolean = false,   // 是否被喜欢/收藏
    val isFromWidget: Boolean = false  // 是否从小组件恢复（不显示喜欢按钮）
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

    /**
     * 判断是否有完整内容
     */
    val hasOriginContent: Boolean
        get() = originContentList.isNotEmpty()

    /**
     * 判断是否有翻译
     */
    val hasTranslate: Boolean
        get() = translateList.isNotEmpty()
}
