package com.jizhi.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.jizhi.R
import com.jizhi.data.local.JiZhiDatabase
import com.jizhi.data.local.SentenceEntity
import kotlinx.coroutines.runBlocking

class SentenceWidgetViewsFactory(
    private val context: Context,
    intent: Intent
) : RemoteViewsService.RemoteViewsFactory {

    private var widgetId: Int = -1
    private var sentences: List<SentenceEntity> = emptyList()

    init {
        widgetId = intent.getIntExtra(
            android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID,
            android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID
        )
    }

    override fun onCreate() {}
    override fun onDataSetChanged() {
        // 使用同步查询方法
        sentences = runBlocking {
            val db = JiZhiDatabase.getInstance(context)
            db.sentenceDao().getRecentSentencesSync(1)
        }
    }

    override fun onDestroy() {}
    override fun getCount(): Int = sentences.size

    override fun getViewAt(position: Int): RemoteViews {
        val sentence = sentences.getOrNull(position) ?: return RemoteViews(
            context.packageName,
            R.layout.widget_sentence
        )

        val views = RemoteViews(context.packageName, R.layout.widget_sentence)
        views.setTextViewText(R.id.widget_title, "《${sentence.title}》")
        views.setTextViewText(R.id.widget_text, sentence.content)
        views.setTextViewText(R.id.widget_dynasty_author, "【${sentence.dynasty}】${sentence.author}")

        val clickIntent = Intent().apply {
            putExtra(android.appwidget.AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            putExtra("sentence_id", sentence.id)
        }
        views.setOnClickFillInIntent(R.id.widget_container, clickIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews =
        RemoteViews(context.packageName, R.layout.widget_sentence)

    override fun getViewTypeCount(): Int = 1
    override fun hasStableIds(): Boolean = true
    override fun getItemId(position: Int): Long = position.toLong()
}
