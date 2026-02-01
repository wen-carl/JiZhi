package com.jizhi.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.jizhi.R
import com.jizhi.data.WidgetPreferences
import com.jizhi.data.getDefaultSentencesWithInfo
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

    companion object {
        const val ACTION_UPDATE_ALL = "com.jizhi.ACTION_UPDATE_ALL"
        const val ACTION_OPEN_CONFIG = "com.jizhi.ACTION_OPEN_CONFIG"
        const val ACTION_REFRESH = "com.jizhi.ACTION_REFRESH"
        const val EXTRA_WIDGET_ID = "widget_id"
        const val EXTRA_FROM_WIDGET = "from_widget"
    }

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
            ACTION_UPDATE_ALL -> {
                // 更新所有小组件
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName =
                    android.content.ComponentName(context, SentenceWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            }

            ACTION_OPEN_CONFIG -> {
                // 打开主页
                val configIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra(EXTRA_FROM_WIDGET, true)
                }
                context.startActivity(configIntent)
            }

            ACTION_REFRESH -> {
                // 刷新单个小组件
                val appWidgetId = intent.getIntExtra(EXTRA_WIDGET_ID, -1)
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
            preferences.removeWidgetConfig(appWidgetId)
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
    val config = preferences.getWidgetConfig(appWidgetId)

    val textColor = preferences.getWidgetTextColor()
    val backgroundColor = preferences.getWidgetBackgroundColor()
    val titleColor = adjustAlpha(textColor, 0.7f)  // 标题颜色略淡
    val authorColor = adjustAlpha(textColor, 0.5f) // 作者颜色更淡
    config?.textSize ?: 16
    val fontFamily = config?.fontFamily ?: "default"
    val lineBreakMode = preferences.getLineBreakMode()

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
    CoroutineScope(Dispatchers.IO).launch {
        // 直接读取 SharedPreferences 获取缓存数据
        val widgetPrefs = context.getSharedPreferences("jizhi_widget_prefs", Context.MODE_PRIVATE)
        val cachedContent = widgetPrefs.getString("widget_sentence", "") ?: ""
        val cachedTitle = widgetPrefs.getString("widget_title", "") ?: ""
        val cachedDynasty = widgetPrefs.getString("widget_dynasty", "") ?: ""
        val cachedAuthor = widgetPrefs.getString("widget_author", "") ?: ""

        val widgetData = if (cachedContent.isNotEmpty()) {
            // 使用缓存数据
            val formattedContent = formatWidgetContent(cachedContent, lineBreakMode)
            WidgetData(
                content = formattedContent,
                title = cachedTitle.ifEmpty { "诗词" },
                dynastyAuthor = "【${cachedDynasty.ifEmpty { "未知" }}】${cachedAuthor.ifEmpty { "未知" }}"
            )
        } else {
            // 没有缓存，从 API 获取
            try {
                val token = JinrishiciClient.getOrCreateToken(context)
                val response = JinrishiciClient.apiService.getSentence(token)

                if (response.status == "success" && response.data != null) {
                    val data = response.data
                    val origin = data.origin

                    // 保存到缓存
                    JinrishiciClient.saveTodaySentenceForWidget(
                        context,
                        id = data.id,
                        content = data.content,
                        originContentList = origin?.content ?: emptyList(),
                        title = origin?.title ?: "诗词",
                        dynasty = origin?.dynasty ?: "未知",
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

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    // 配置点击事件 - 打开主页
    val configIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        putExtra(SentenceWidgetProvider.EXTRA_FROM_WIDGET, true)
    }
    val configPendingIntent = PendingIntent.getActivity(
        context,
        appWidgetId,
        configIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widget_container, configPendingIntent)

    // 刷新按钮点击事件
    Intent(context, SentenceWidgetProvider::class.java).apply {
        action = SentenceWidgetProvider.ACTION_REFRESH
        putExtra(SentenceWidgetProvider.EXTRA_WIDGET_ID, appWidgetId)
    }

    // 更新小组件
    appWidgetManager.updateAppWidget(appWidgetId, views)

    // 安排下一次更新
    scheduleNextUpdate(context, appWidgetId, config?.updateIntervalHours ?: 1f)
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
 */
private fun formatWidgetContent(
    content: String,
    lineBreakMode: com.jizhi.data.LineBreakMode
): String {
    return when (lineBreakMode) {
        com.jizhi.data.LineBreakMode.DEFAULT -> content
        com.jizhi.data.LineBreakMode.AUTO_PUNCTUATION -> {
            // 智能标点换行：短诗按标点换行
            if (content.length <= 20) {
                content
                    .replace("。", "。\n")
                    .replace("，", "，\n")
                    .replace("！", "！\n")
                    .replace("？", "？\n")
                    .trim()
            } else {
                content
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

/**
 * 安排下一次自动更新
 */
private fun scheduleNextUpdate(
    context: Context,
    appWidgetId: Int,
    intervalHours: Float
) {
    if (intervalHours <= 0) return  // 如果设置为永不更新，则不安排

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
    val intent = Intent(context, SentenceWidgetProvider::class.java).apply {
        action = SentenceWidgetProvider.ACTION_REFRESH
        putExtra(SentenceWidgetProvider.EXTRA_WIDGET_ID, appWidgetId)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        appWidgetId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val intervalMillis = (intervalHours * 60 * 60 * 1000).toLong()
    val triggerTime = System.currentTimeMillis() + intervalMillis

    try {
        alarmManager.setExactAndAllowWhileIdle(
            android.app.AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    } catch (e: SecurityException) {
        // 处理无法设置精确闹钟的情况
        alarmManager.set(
            android.app.AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}

/**
 * 取消指定小组件的更新计划
 */
fun cancelUpdate(context: Context, appWidgetId: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as android.app.AlarmManager
    val intent = Intent(context, SentenceWidgetProvider::class.java).apply {
        action = SentenceWidgetProvider.ACTION_REFRESH
        putExtra(SentenceWidgetProvider.EXTRA_WIDGET_ID, appWidgetId)
    }
    val pendingIntent = PendingIntent.getBroadcast(
        context,
        appWidgetId,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pendingIntent)
}
