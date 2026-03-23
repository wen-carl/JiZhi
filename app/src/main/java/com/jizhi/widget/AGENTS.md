# JiZhi 桌面小组件目录

**目录**: `app/src/main/java/com/jizhi/widget/`

---

## OVERVIEW

桌面小组件模块，负责在 Android 桌面展示诗词内容、处理用户点击交互，包含 AppWidgetProvider、RemoteViewsService、RemoteViewsFactory 三类组件。

---

## WHERE TO LOOK

| 任务 | 文件 | 说明 |
|------|------|------|
| 小组件生命周期 | `SentenceWidgetProvider.kt` | onUpdate/onReceive/onDeleted 等回调处理 |
| 小组件 UI 渲染 | `SentenceWidgetProvider.kt` | `updateAppWidget()` 函数构建 RemoteViews |
| 列表型小组件数据 | `SentenceWidgetViewsFactory.kt` | RemoteViewsFactory 实现，提供列表数据 |
| RemoteViews 服务 | `SentenceWidgetService.kt` | RemoteViewsService 子类，返回 Factory |
| 小组件配置存取 | `WidgetPreferences.kt` | 背景色、文本色、字体等设置存储 |
| 小组件缓存 | `DataStoreManager.kt` | 诗词数据缓存，优先读取 |

---

## CONVENTIONS

### 数据获取约束
- **禁止使用 Hilt**：Widget 组件无法注入，必须使用 `JiZhiDatabase.getInstance(context)` 获取单例
- **同步查询**：`RemoteViewsFactory.onDataSetChanged()` 中使用 `runBlocking { }` 包裹协程
- **缓存优先**：先读取 `DataStoreManager.getWidgetCacheData()`，无缓存再请求 API

### RemoteViews 构建规范
```kotlin
val views = RemoteViews(context.packageName, R.layout.widget_sentence)
views.setTextViewText(R.id.widget_text, content)
views.setTextColor(R.id.widget_text, color)
views.setTextViewTextSize(R.id.widget_text, TypedValue.COMPLEX_UNIT_SP, 20f)
views.setOnClickPendingIntent(R.id.container, pendingIntent)
appWidgetManager.updateAppWidget(appWidgetId, views)
```

### Intent Action 常量
| Action | 用途 |
|--------|------|
| `ACTION_UPDATE_ALL` | WorkManager 定时更新所有小组件 |
| `ACTION_REFRESH` | 手动刷新单个小组件 |
| `ACTION_OPEN_CONFIG` | 点击打开主页面 |

### 字体设置反射
由于 RemoteViews 不直接支持 setTypeface，使用反射调用：
```kotlin
val method = RemoteViews::class.java.getMethod("setTypeface", Int::class.javaPrimitiveType, Typeface::class.java)
method.invoke(views, R.id.widget_text, typeface)
```

---

## ANTI-PATTERNS

1. **禁止在 Widget 中使用 @Inject / @HiltAndroidApp**
   - Widget 运行在独立进程，无法获取 Hilt 组件
   - 必须手动实例化：`JiZhiDatabase.getInstance(context)`

2. **禁止在主线程进行网络请求**
   - Widget 更新在主线程，必须使用协程或后台线程
   - `onDataSetChanged()` 中必须使用 `runBlocking` 同步查询 Room

3. **禁止直接启动 Activity**
   - 点击事件必须通过 `PendingIntent` 触发
   - 必须设置 `FLAG_ACTIVITY_NEW_TASK`

4. **禁止使用 Compose UI**
   - Widget 只支持 RemoteViews，不支持 Compose
   - 布局文件位于 `res/layout/widget_sentence.xml`

5. **禁止传递大型数据**
   - RemoteViews 有大小限制（约 1MB）
   - 诗词内容应简短，避免传递完整原文列表
