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
     * @param maxCharsPerLine 每行最大字符数（用于智能标点换行判断）
     * @return 格式化后的内容
     */
    fun format(
        content: String,
        lineBreakMode: LineBreakMode,
        maxCharsPerLine: Int = Int.MAX_VALUE
    ): String {
        return when (lineBreakMode) {
            LineBreakMode.DEFAULT -> content
            LineBreakMode.AUTO_PUNCTUATION -> formatWithAutoPunctuation(content, maxCharsPerLine)
            LineBreakMode.FORCE_PUNCTUATION -> formatWithForcePunctuation(content)
        }
    }

    /**
     * 智能标点换行
     * 一行能显示下就不换行，一行显示不下则在标点处换行
     * @param content 原始内容
     * @param maxCharsPerLine 每行最大字符数
     */
    private fun formatWithAutoPunctuation(content: String, maxCharsPerLine: Int): String {
        // 一行能显示下，不换行
        if (content.length <= maxCharsPerLine) {
            return content
        }
        // 一行显示不下，在标点处换行
        return content
            .replace("。", "。\n")
            .replace("，", "，\n")
            .replace("！", "！\n")
            .replace("？", "？\n")
            .trim()
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
     * 将包含换行符的内容按行分割
     */
    fun splitLines(content: String): List<String> {
        return content.split('\n').filter { it.isNotBlank() }
    }
}
