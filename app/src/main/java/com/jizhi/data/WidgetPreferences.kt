package com.jizhi.data

import android.content.Context
import android.graphics.Typeface
import com.jizhi.data.local.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
    val fontFamily: String,
    val isCustom: Boolean = false,
    val fontPath: String? = null
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

    suspend fun getOptionById(context: Context, id: String): FontOption? {
        val customFonts = loadCustomFonts(context)
        return (systemFonts + customFonts).find { it.id == id }
    }

    private suspend fun loadCustomFonts(context: Context): List<FontOption> {
        val fontPaths = DataStoreManager.getCustomFontPaths(context)
        return fontPaths.mapIndexed { index, path ->
            val fileName = path.substringAfterLast("/").substringBeforeLast(".")
            FontOption(
                id = "custom_$index",
                name = fileName,
                fontFamily = path,
                isCustom = true,
                fontPath = path
            )
        }
    }

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
        "今天也要加油哦！"
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
        WidgetSentence("今天也要加油哦！", "励志", "现代", "匿名")
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
 * 常用背景色预设
 */
object WidgetColors {
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

/**
 * 小组件配置存储器
 * 使用 DataStore 存储所有小组件的配置
 */
class WidgetPreferences(private val context: Context) {

    companion object {
        val BACKGROUND_COLORS = WidgetColors.BACKGROUND_COLORS
        val TEXT_COLORS = WidgetColors.TEXT_COLORS
    }

    suspend fun saveWidgetConfig(config: WidgetConfig) {
        withContext(Dispatchers.IO) {
            val json = config.toJson()
            DataStoreManager.saveWidgetConfig(context, config.widgetId, json)
        }
    }

    suspend fun getWidgetConfig(widgetId: Int): WidgetConfig? {
        return withContext(Dispatchers.IO) {
            val json =
                DataStoreManager.getWidgetConfig(context, widgetId) ?: return@withContext null
            try {
                parseWidgetConfigFromJson(json).copy(widgetId = widgetId)
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun removeWidgetConfig(widgetId: Int) {
        withContext(Dispatchers.IO) {
            DataStoreManager.removeWidgetConfig(context, widgetId)
        }
    }

    suspend fun getUpdateInterval(widgetId: Int): Float {
        return getWidgetConfig(widgetId)?.updateIntervalHours ?: 1f
    }

    suspend fun saveUpdateInterval(widgetId: Int, intervalHours: Float) {
        val config = getWidgetConfig(widgetId) ?: WidgetConfig(widgetId)
        saveWidgetConfig(config.copy(updateIntervalHours = intervalHours))
    }

    suspend fun getLineBreakMode(): LineBreakMode {
        return withContext(Dispatchers.IO) {
            val value = DataStoreManager.getLineBreakMode(context)
            LineBreakMode.fromValue(value)
        }
    }

    suspend fun saveLineBreakMode(mode: LineBreakMode) {
        withContext(Dispatchers.IO) {
            DataStoreManager.saveLineBreakMode(context, mode.value)
        }
    }

    suspend fun getWidgetBackgroundColor(): Int {
        return withContext(Dispatchers.IO) {
            DataStoreManager.getWidgetBackgroundColor(context).toInt()
        }
    }

    suspend fun saveWidgetBackgroundColor(color: Int) {
        withContext(Dispatchers.IO) {
            DataStoreManager.saveWidgetBackgroundColor(context, color.toLong())
        }
    }

    suspend fun getWidgetTextColor(): Int {
        return withContext(Dispatchers.IO) {
            DataStoreManager.getWidgetTextColor(context).toInt()
        }
    }

    suspend fun saveWidgetTextColor(color: Int) {
        withContext(Dispatchers.IO) {
            DataStoreManager.saveWidgetTextColor(context, color.toLong())
        }
    }

    suspend fun getSelectedFontId(): String {
        return withContext(Dispatchers.IO) {
            DataStoreManager.getSelectedFontId(context)
        }
    }

    suspend fun saveSelectedFontId(fontId: String) {
        withContext(Dispatchers.IO) {
            DataStoreManager.saveSelectedFontId(context, fontId)
        }
    }

    suspend fun getSelectedFontOption(): FontOption {
        val fontId = getSelectedFontId()
        return FontOptions.getOptionById(context, fontId)
            ?: FontOption("serif", "衬线体", "serif")
    }

    suspend fun getCustomFontPaths(): List<String> {
        return withContext(Dispatchers.IO) {
            DataStoreManager.getCustomFontPaths(context)
        }
    }

    suspend fun addCustomFontPath(fontPath: String) {
        withContext(Dispatchers.IO) {
            DataStoreManager.addCustomFontPath(context, fontPath)
        }
    }

    suspend fun removeCustomFontPath(fontPath: String) {
        withContext(Dispatchers.IO) {
            DataStoreManager.removeCustomFontPath(context, fontPath)
        }
    }

    suspend fun getAllOptions(): List<FontOption> {
        return withContext(Dispatchers.IO) {
            val customFonts =
                DataStoreManager.getCustomFontPaths(context).mapIndexed { index, path ->
                    val fileName = path.substringAfterLast("/").substringBeforeLast(".")
                    FontOption(
                        id = "custom_$index",
                        name = fileName,
                        fontFamily = path,
                        isCustom = true,
                        fontPath = path
                    )
                }
            FontOptions.systemFonts + customFonts
        }
    }

    suspend fun getOptionById(id: String): FontOption? {
        return getAllOptions().find { it.id == id }
    }

    fun getTypeface(fontOption: FontOption?): Typeface {
        return FontOptions.getTypeface(context, fontOption)
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
