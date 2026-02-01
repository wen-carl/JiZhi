package com.jizhi.data

/**
 * 诗词内容格式化工具
 * 根据换行模式格式化诗词内容
 */
object PoemFormatter {

    /**
     * 根据换行模式格式化诗词内容
     * @param content 原始内容
     * @param lineBreakMode 换行模式
     * @return 格式化后的内容
     */
    fun format(content: String, lineBreakMode: LineBreakMode): String {
        return when (lineBreakMode) {
            LineBreakMode.DEFAULT -> content
            LineBreakMode.AUTO_PUNCTUATION -> formatWithAutoPunctuation(content)
            LineBreakMode.FORCE_PUNCTUATION -> formatWithForcePunctuation(content)
        }
    }

    /**
     * 直接格式化 List<String> 内容
     */
    fun formatList(contentList: List<String>, lineBreakMode: LineBreakMode): String {
        if (contentList.isEmpty()) return ""
        val joinedContent = contentList.joinToString("\n")
        return format(joinedContent, lineBreakMode)
    }

    /**
     * 智能标点换行
     * 一行显示不下时才在标点处换行
     */
    private fun formatWithAutoPunctuation(content: String): String {
        // 短诗（20字以内）直接按标点换行
        if (content.length <= 20) {
            return content
                .replace("。", "。\n")
                .replace("，", "，\n")
                .replace("！", "！\n")
                .replace("？", "？\n")
                .trim()
        }
        return content
    }

    /**
     * 强制标点换行
     * 强制在「。」或「，」后换行，但「。」后不换行（如果是最后一个字符）
     */
    private fun formatWithForcePunctuation(content: String): String {
        val result = StringBuilder()
        val chars = content.toCharArray()

        for (i in chars.indices) {
            val char = chars[i]
            result.append(char)

            // 在「，」后强制换行
            if (char == '，') {
                result.append('\n')
            }
            // 在「。」后换行，但如果是最后一个字符则不换行
            else if (char == '。' && i < chars.size - 1) {
                result.append('\n')
            }
        }

        return result.toString().trim()
    }

    /**
     * 检查内容是否包含换行符
     */
    fun hasNewlines(content: String): Boolean {
        return content.contains('\n')
    }

    /**
     * 将包含换行符的内容按行分割
     */
    fun splitLines(content: String): List<String> {
        return content.split('\n').filter { it.isNotBlank() }
    }
}
