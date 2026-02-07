package com.jizhi.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.jizhi.Constants
import com.jizhi.R
import com.jizhi.data.WidgetPreferences
import com.jizhi.data.getDefaultSentencesWithInfo
import com.jizhi.data.local.DataStoreManager
import com.jizhi.data.remote.JinrishiciClient
import com.jizhi.ui.main.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 小组件数据类
 */
private data class WidgetData(
    val content: String,
    val title: String,
    val dynastyAuthor: String
)

/**
 * 句子小组件提供者
 * 处理小组件的创建、更新、删除等生命周期事件
 */
class SentenceWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // 遍历所有小组件实例进行更新
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            Constants.ACTION_UPDATE_ALL -> {
                // 更新所有小组件
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName =
                    android.content.ComponentName(context, SentenceWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            }

            Constants.ACTION_OPEN_CONFIG -> {
                // 打开主页
                val configIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(Constants.EXTRA_FROM_WIDGET, true)
                }
                context.startActivity(configIntent)
            }

            Constants.ACTION_REFRESH -> {
                // 刷新单个小组件
                val appWidgetId = intent.getIntExtra(Constants.EXTRA_WIDGET_ID, -1)
                if (appWidgetId != -1) {
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        // 当小组件被删除时，清理对应的配置
        val preferences = WidgetPreferences(context)
        for (appWidgetId in appWidgetIds) {
            CoroutineScope(Dispatchers.IO).launch {
                preferences.removeWidgetConfig(appWidgetId)
            }
        }
    }

    override fun onEnabled(context: Context) {
        // 小组件首次添加到桌面时调用
    }

    override fun onDisabled(context: Context) {
        // 最后一个小组件从桌面移除时调用
    }
}

/**
 * 更新单个小组件
 * 根据配置设置远程视图的样式和内容
 */
fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val preferences = WidgetPreferences(context)
    val scope = CoroutineScope(Dispatchers.Main)

    scope.launch {
        val config = withContext(Dispatchers.IO) {
            preferences.getWidgetConfig(appWidgetId)
        }

        val textColor = withContext(Dispatchers.IO) {
            preferences.getWidgetTextColor()
        }
        val backgroundColor = withContext(Dispatchers.IO) {
            preferences.getWidgetBackgroundColor()
        }
        val lineBreakMode = withContext(Dispatchers.IO) {
            preferences.getLineBreakMode()
        }

        val titleColor = adjustAlpha(textColor, 0.7f)
        val authorColor = adjustAlpha(textColor, 0.5f)
        config?.textSize ?: 16
        val fontFamily = config?.fontFamily ?: "default"

        // 创建远程视图
        val views = RemoteViews(context.packageName, R.layout.widget_sentence)

        // 应用样式 - 背景色
        views.setInt(R.id.widget_container, "setBackgroundColor", backgroundColor)

        // 应用样式 - 文本颜色
        views.setTextColor(R.id.widget_text, textColor)
        views.setTextColor(R.id.widget_title, titleColor)
        views.setTextColor(R.id.widget_dynasty_author, authorColor)

        // 应用样式 - 字体大小（放大）
        views.setTextViewTextSize(R.id.widget_text, android.util.TypedValue.COMPLEX_UNIT_SP, 20f)
        views.setTextViewTextSize(R.id.widget_title, android.util.TypedValue.COMPLEX_UNIT_SP, 16f)
        views.setTextViewTextSize(
            R.id.widget_dynasty_author,
            android.util.TypedValue.COMPLEX_UNIT_SP,
            12f
        )

        // 设置字体样式
        try {
            val typeface = when (fontFamily) {
                "serif" -> android.graphics.Typeface.SERIF
                "monospace" -> android.graphics.Typeface.MONOSPACE
                else -> android.graphics.Typeface.DEFAULT
            }
            val method = RemoteViews::class.java.getMethod(
                "setTypeface",
                Int::class.javaPrimitiveType,
                android.graphics.Typeface::class.java
            )
            method.invoke(views, R.id.widget_text, typeface)
            method.invoke(views, R.id.widget_title, typeface)
        } catch (e: Exception) {
            // 字体设置失败时使用默认字体，忽略错误
        }

        // 异步获取诗句数据（优先使用缓存）
        val cachedData = DataStoreManager.getWidgetCacheData(context)

        val widgetData = if (cachedData.content.isNotEmpty()) {
            // 使用缓存数据
            val formattedContent = formatWidgetContent(cachedData.content, lineBreakMode)
            WidgetData(
                content = formattedContent,
                title = cachedData.title.ifEmpty { "诗词" },
                dynastyAuthor = "【${cachedData.dynasty.ifEmpty { "未知" }}】${cachedData.author.ifEmpty { "未知" }}"
            )
        } else {
            // 没有缓存，从 API 获取
            try {
                val token = JinrishiciClient.getOrCreateToken(context)
                val response = JinrishiciClient.apiService.getSentence(token)

                if (response.status == "success" && response.data != null) {
                    val data = response.data
                    val origin = data.origin

                    // 保存到缓存（清理朝代中的"代"字）
                    val cleanDynasty = (origin?.dynasty ?: "-").removeSuffix("代")
                    JinrishiciClient.saveTodaySentenceForWidget(
                        context,
                        id = data.id,
                        content = data.content,
                        originContentList = origin?.content ?: emptyList(),
                        title = origin?.title ?: "诗词",
                        dynasty = cleanDynasty,
                        author = origin?.author ?: "未知"
                    )

                    val formattedContent = formatWidgetContent(data.content, lineBreakMode)
                    val dynastyAuthor =
                        if (origin != null && origin.dynasty.isNotEmpty() && origin.author.isNotEmpty()) {
                            "【${origin.dynasty}】${origin.author}"
                        } else {
                            "【未知】未知"
                        }

                    WidgetData(
                        content = formattedContent,
                        title = origin?.title ?: "诗词",
                        dynastyAuthor = dynastyAuthor
                    )
                } else {
                    throw Exception("API 返回失败")
                }
            } catch (e: Exception) {
                // 使用默认句子
                val default = getDefaultSentencesWithInfo().random()
                WidgetData(
                    content = default.content,
                    title = default.title,
                    dynastyAuthor = "【${default.dynasty}】${default.author}"
                )
            }
        }

        withContext(Dispatchers.Main) {
            views.setTextViewText(R.id.widget_text, widgetData.content)
            views.setTextViewText(R.id.widget_title, "《${widgetData.title}》")
            views.setTextViewText(R.id.widget_dynasty_author, widgetData.dynastyAuthor)

            // 配置点击事件 - 打开主页
            val configIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra(Constants.EXTRA_FROM_WIDGET, true)
            }
            val configPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                configIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, configPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}

/**
 * 调整颜色透明度
 */
private fun adjustAlpha(color: Int, factor: Float): Int {
    val alpha = (255 * factor).toInt()
    val red = android.graphics.Color.red(color)
    val green = android.graphics.Color.green(color)
    val blue = android.graphics.Color.blue(color)
    return android.graphics.Color.argb(alpha, red, green, blue)
}

/**
 * 小组件内容折行格式化
 * 参照主页 PoemFormatter 的逻辑
 * 定时更新由 WorkManager 统一管理
 */
private fun formatWidgetContent(
    content: String,
    lineBreakMode: com.jizhi.data.LineBreakMode,
    maxCharsPerLine: Int = 10  // 小组件每行最大字符数（18sp 字体估算）
): String {
    return when (lineBreakMode) {
        com.jizhi.data.LineBreakMode.DEFAULT -> content
        com.jizhi.data.LineBreakMode.AUTO_PUNCTUATION -> {
            // 智能标点换行：一行能显示下就不换行，否则按标点换行
            if (content.length <= maxCharsPerLine) {
                content
            } else {
                content
                    .replace("。", "。\n")
                    .replace("，", "，\n")
                    .replace("！", "！\n")
                    .replace("？", "？\n")
                    .trim()
            }
        }

        com.jizhi.data.LineBreakMode.FORCE_PUNCTUATION -> {
            // 强制标点换行
            val result = StringBuilder()
            val chars = content.toCharArray()
            for (i in chars.indices) {
                val char = chars[i]
                result.append(char)
                if (char == '，') {
                    result.append('\n')
                } else if (char == '。' && i < chars.size - 1) {
                    result.append('\n')
                }
            }
            result.toString().trim()
        }
    }
}
