package com.jizhi.data

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
import org.json.JSONArray
import org.json.JSONObject

/**
 * 背景色选项
 */
data class BackgroundColorOption(
    val name: String,
    val color: Int
)

/**
 * 字体选项
 */
data class FontOption(
    val id: String,
    val name: String,
    val fontFamily: String,  // Typeface 家族名称
    val isCustom: Boolean = false,  // 是否是自定义字体
    val fontPath: String? = null    // 自定义字体文件路径
)

/**
 * 系统字体选项
 */
object FontOptions {
    val systemFonts = listOf(
        FontOption("default", "系统默认", "default"),
        FontOption("sans_serif", "无衬线体", "sans-serif"),
        FontOption("serif", "衬线体", "serif"),
        FontOption("monospace", "等宽体", "monospace")
    )

    /**
     * 获取所有可用字体选项（包括系统字体和自定义字体）
     */
    fun getAllOptions(context: Context): List<FontOption> {
        val customFonts = loadCustomFonts(context)
        return systemFonts + customFonts
    }

    /**
     * 加载自定义字体
     */
    private fun loadCustomFonts(context: Context): List<FontOption> {
        val prefs = context.getSharedPreferences("jizhi_widget_prefs", Context.MODE_PRIVATE)
        val fontPathsJson = prefs.getString("custom_font_paths", "[]") ?: "[]"
        return try {
            val fontPaths = JSONArray(fontPathsJson)
            (0 until fontPaths.length()).map { index ->
                val path = fontPaths.getString(index)
                val fileName = path.substringAfterLast("/").substringBeforeLast(".")
                FontOption(
                    id = "custom_$index",
                    name = fileName,
                    fontFamily = path,
                    isCustom = true,
                    fontPath = path
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 根据 ID 获取字体选项
     */
    fun getOptionById(context: Context, id: String): FontOption? {
        return getAllOptions(context).find { it.id == id }
    }

    /**
     * 根据字体家族名称获取 Typeface
     */
    fun getTypeface(context: Context, fontOption: FontOption?): Typeface {
        return if (fontOption?.isCustom == true && fontOption.fontPath != null) {
            try {
                Typeface.createFromFile(fontOption.fontPath)
            } catch (e: Exception) {
                Typeface.DEFAULT
            }
        } else {
            when (fontOption?.fontFamily) {
                "sans-serif" -> Typeface.SANS_SERIF
                "serif" -> Typeface.SERIF
                "monospace" -> Typeface.MONOSPACE
                else -> Typeface.DEFAULT
            }
        }
    }
}

/**
 * 默认句子列表
 */
fun getDefaultSentences(): List<String> {
    return listOf(
        "今天也要加油哦！",
        "保持微笑，好运自然来",
        "你是最棒的！",
        "每天进步一点点",
        "相信自己的选择",
        "生活明朗，万物可爱",
        "一切皆有可能",
        "勇敢追求梦想",
        "活出精彩的自己",
        "今天也是美好的一天"
    )
}

/**
 * 小组件句子数据类
 */
data class WidgetSentence(
    val content: String,
    val title: String = "诗词",
    val dynasty: String = "未知",
    val author: String = "未知"
)

/**
 * 默认句子列表（带完整信息）
 */
fun getDefaultSentencesWithInfo(): List<WidgetSentence> {
    return listOf(
        WidgetSentence("今天也要加油哦！", "励志", "现代", "匿名"),
        WidgetSentence("保持微笑，好运自然来", "励志", "现代", "匿名"),
        WidgetSentence("你是最棒的！", "励志", "现代", "匿名"),
        WidgetSentence("每天进步一点点", "励志", "现代", "匿名"),
        WidgetSentence("相信自己的选择", "励志", "现代", "匿名"),
        WidgetSentence("生活明朗，万物可爱", "励志", "现代", "匿名"),
        WidgetSentence("一切皆有可能", "励志", "现代", "匿名"),
        WidgetSentence("勇敢追求梦想", "励志", "现代", "匿名"),
        WidgetSentence("活出精彩的自己", "励志", "现代", "匿名"),
        WidgetSentence("今天也是美好的一天", "励志", "现代", "匿名")
    )
}

/**
 * 小组件配置数据类
 */
data class WidgetConfig(
    val widgetId: Int,
    val sentences: List<String> = getDefaultSentences().map { it },
    val titles: List<String> = emptyList(),
    val dynasties: List<String> = emptyList(),
    val authors: List<String> = emptyList(),
    val updateIntervalHours: Float = 1f,
    val textColor: Int = 0xFFFFFFFF.toInt(),
    val textSize: Int = 16,
    val fontFamily: String = "default",
    val backgroundColor: Int = 0x80000000.toInt(),
    val backgroundImage: String? = null,
    val widgetSize: WidgetSize = WidgetSize.MEDIUM
) {
    /**
     * 随机获取一个句子
     */
    fun getRandomSentence(): String {
        return sentences.randomOrNull() ?: "今天也要加油哦！"
    }
}

/**
 * 小组件大小枚举
 */
enum class WidgetSize(val cells: Int, val description: String) {
    SMALL(1, "小 - 1x1"),
    MEDIUM(2, "中 - 2x2"),
    LARGE(4, "大 - 2x4"),
    EXTRA_LARGE(6, "超大 - 3x4")
}

/**
 * 更新周期选项（小时）
 */
object UpdateIntervalOptions {
    val options = listOf(
        0.25f to "15分钟",
        0.5f to "30分钟",
        1f to "1小时",
        2f to "2小时",
        3f to "3小时",
        6f to "6小时",
        8f to "8小时",
        24f to "1天",
        -1f to "永不自动更新"
    )
}

/**
 * 诗词内容换行模式枚举
 */
enum class LineBreakMode(val value: Int, val displayName: String, val description: String) {
    DEFAULT(0, "默认", "按系统默认规则显示"),
    AUTO_PUNCTUATION(1, "智能标点换行", "一行显示不下时，根据「。」或「，」换行"),
    FORCE_PUNCTUATION(2, "强制标点换行", "强制在「。」或「，」后换行（「。」后不换行）");

    companion object {
        fun fromValue(value: Int): LineBreakMode {
            return entries.find { it.value == value } ?: DEFAULT
        }
    }
}

/**
 * 换行模式选项
 */
object LineBreakModeOptions {
    val options = LineBreakMode.entries.map { it.value to it.displayName }
    val descriptions = LineBreakMode.entries.associate { it.value to it.description }
}

/**
 * 小组件配置存储器
 * 使用SharedPreferences存储所有小组件的配置
 */
class WidgetPreferences(context: Context) {

    companion object {
        private const val PREFS_NAME = "jizhi_widget_prefs"

        /**
         * 常用背景色预设
         */
        val BACKGROUND_COLORS = listOf(
            BackgroundColorOption("透明", 0x00000000.toInt()),
            BackgroundColorOption("纯白", 0xFFFFFFFF.toInt()),
            BackgroundColorOption("纯黑", 0xFF000000.toInt()),
            BackgroundColorOption("浅灰", 0xFFF5F5F5.toInt()),
            BackgroundColorOption("深灰", 0xFF333333.toInt()),
            BackgroundColorOption("淡蓝", 0xFFE3F2FD.toInt()),
            BackgroundColorOption("淡绿", 0xFFE8F5E9.toInt()),
            BackgroundColorOption("淡黄", 0xFFFFFDE7.toInt()),
            BackgroundColorOption("淡粉", 0xFFFCE4EC.toInt()),
            BackgroundColorOption("淡紫", 0xFFF3E5F5.toInt()),
            BackgroundColorOption("半透明黑", 0x80000000.toInt()),
            BackgroundColorOption("半透明白", 0x80FFFFFF.toInt())
        )

        /**
         * 常用文本颜色预设
         */
        val TEXT_COLORS = listOf(
            BackgroundColorOption("纯白", 0xFFFFFFFF.toInt()),
            BackgroundColorOption("纯黑", 0xFF000000.toInt()),
            BackgroundColorOption("浅灰", 0xFFE0E0E0.toInt()),
            BackgroundColorOption("深灰", 0xFF333333.toInt()),
            BackgroundColorOption("淡红", 0xFFFFCDD2.toInt()),
            BackgroundColorOption("淡蓝", 0xFFBBDEFB.toInt()),
            BackgroundColorOption("淡绿", 0xFFC8E6C9.toInt()),
            BackgroundColorOption("金色", 0xFFFFD54F.toInt())
        )
    }

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * 保存小组件配置
     */
    fun saveWidgetConfig(config: WidgetConfig) {
        val json = config.toJson()
        prefs.edit().putString("widget_${config.widgetId}", json).apply()
    }

    /**
     * 获取小组件配置
     */
    fun getWidgetConfig(widgetId: Int): WidgetConfig? {
        val json = prefs.getString("widget_$widgetId", null) ?: return null
        return try {
            parseWidgetConfigFromJson(json).copy(widgetId = widgetId)
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 删除小组件配置
     */
    fun removeWidgetConfig(widgetId: Int) {
        prefs.edit().remove("widget_$widgetId").apply()
    }

    /**
     * 获取更新间隔（小时）
     */
    fun getUpdateInterval(widgetId: Int): Float {
        return getWidgetConfig(widgetId)?.updateIntervalHours ?: 1f
    }

    /**
     * 保存更新间隔
     */
    fun saveUpdateInterval(widgetId: Int, intervalHours: Float) {
        val config = getWidgetConfig(widgetId) ?: WidgetConfig(widgetId)
        saveWidgetConfig(config.copy(updateIntervalHours = intervalHours))
    }

    /**
     * 获取换行模式
     */
    fun getLineBreakMode(): LineBreakMode {
        val value = prefs.getInt("line_break_mode", LineBreakMode.DEFAULT.value)
        return LineBreakMode.fromValue(value)
    }

    /**
     * 保存换行模式
     */
    fun saveLineBreakMode(mode: LineBreakMode) {
        prefs.edit().putInt("line_break_mode", mode.value).apply()
    }

    /**
     * 获取小组件背景色（ARGB格式，默认透明）
     */
    fun getWidgetBackgroundColor(): Int {
        return prefs.getInt("widget_background_color", Color.TRANSPARENT)
    }

    /**
     * 保存小组件背景色（ARGB格式）
     */
    fun saveWidgetBackgroundColor(color: Int) {
        prefs.edit().putInt("widget_background_color", color).apply()
    }

    /**
     * 获取小组件文本颜色（ARGB格式，默认白色）
     */
    fun getWidgetTextColor(): Int {
        return prefs.getInt("widget_text_color", 0xFFFFFFFF.toInt())
    }

    /**
     * 保存小组件文本颜色（ARGB格式）
     */
    fun saveWidgetTextColor(color: Int) {
        prefs.edit().putInt("widget_text_color", color).apply()
    }

    /**
     * 获取当前选中的字体 ID
     */
    fun getSelectedFontId(): String {
        return prefs.getString("selected_font_id", "serif") ?: "serif"
    }

    /**
     * 保存选中的字体 ID
     */
    fun saveSelectedFontId(fontId: String) {
        prefs.edit().putString("selected_font_id", fontId).apply()
    }

    /**
     * 获取当前选中的字体选项
     */
    fun getSelectedFontOption(context: Context): FontOption {
        val fontId = getSelectedFontId()
        return FontOptions.getOptionById(context, fontId)
            ?: FontOption("serif", "衬线体", "serif")
    }

    /**
     * 获取自定义字体文件列表
     */
    fun getCustomFontPaths(): List<String> {
        val fontPathsJson = prefs.getString("custom_font_paths", "[]") ?: "[]"
        return try {
            val fontPaths = JSONArray(fontPathsJson)
            (0 until fontPaths.length()).map { fontPaths.getString(it) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * 添加自定义字体文件路径
     */
    fun addCustomFontPath(fontPath: String) {
        val currentPaths = getCustomFontPaths().toMutableList()
        if (!currentPaths.contains(fontPath)) {
            currentPaths.add(fontPath)
            val json = JSONArray(currentPaths).toString()
            prefs.edit().putString("custom_font_paths", json).apply()
        }
    }

    /**
     * 移除自定义字体文件路径
     */
    fun removeCustomFontPath(fontPath: String) {
        val currentPaths = getCustomFontPaths().toMutableList()
        currentPaths.remove(fontPath)
        val json = JSONArray(currentPaths).toString()
        prefs.edit().putString("custom_font_paths", json).apply()
    }
}

/**
 * 将WidgetConfig转换为JSON
 */
fun WidgetConfig.toJson(): String {
    return JSONObject().apply {
        put("widgetId", widgetId)
        put("sentences", JSONArray(sentences).toString())
        put("titles", JSONArray(titles).toString())
        put("dynasties", JSONArray(dynasties).toString())
        put("authors", JSONArray(authors).toString())
        put("updateIntervalHours", updateIntervalHours.toDouble())
        put("textColor", textColor)
        put("textSize", textSize)
        put("fontFamily", fontFamily)
        put("backgroundColor", backgroundColor)
        put("backgroundImage", backgroundImage ?: JSONObject.NULL)
        put("widgetSize", widgetSize.name)
    }.toString()
}

/**
 * 从JSON解析WidgetConfig
 */
fun parseWidgetConfigFromJson(json: String): WidgetConfig {
    val obj = JSONObject(json)
    val sentencesArray = JSONArray(obj.optString("sentences", "[]"))
    val sentences = (0 until sentencesArray.length()).map {
        sentencesArray.getString(it)
    }

    val titlesArray = JSONArray(obj.optString("titles", "[]"))
    val titles = (0 until titlesArray.length()).map {
        titlesArray.getString(it)
    }

    val dynastiesArray = JSONArray(obj.optString("dynasties", "[]"))
    val dynasties = (0 until dynastiesArray.length()).map {
        dynastiesArray.getString(it)
    }

    val authorsArray = JSONArray(obj.optString("authors", "[]"))
    val authors = (0 until authorsArray.length()).map {
        authorsArray.getString(it)
    }

    val widgetSizeName = obj.optString("widgetSize", "MEDIUM")
    val widgetSize = try {
        WidgetSize.valueOf(widgetSizeName)
    } catch (e: Exception) {
        WidgetSize.MEDIUM
    }

    return WidgetConfig(
        widgetId = obj.optInt("widgetId", -1),
        sentences = if (sentences.isNotEmpty()) sentences else getDefaultSentences(),
        titles = titles,
        dynasties = dynasties,
        authors = authors,
        updateIntervalHours = obj.optDouble("updateIntervalHours", 1.0).toFloat(),
        textColor = obj.optInt("textColor", 0xFFFFFFFF.toInt()),
        textSize = obj.optInt("textSize", 16),
        fontFamily = obj.optString("fontFamily", "default"),
        backgroundColor = obj.optInt("backgroundColor", 0x80000000.toInt()),
        backgroundImage = if (obj.isNull("backgroundImage")) null else obj.optString("backgroundImage"),
        widgetSize = widgetSize
    )
}
