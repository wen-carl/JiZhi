package com.jizhi.widget

import android.content.Intent
import android.widget.RemoteViewsService

/**
 * 小组件远程视图服务
 * 用于提供小组件的数据更新
 */
class SentenceWidgetService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return SentenceWidgetViewsFactory(applicationContext, intent)
    }
}
