# JiZhi 今日诗词 - 应用架构与设计

## 应用介绍

**JiZhi（今日诗词）** 是一款展示每日推荐诗词的 Android 应用，通过整合今日诗词 API，为用户提供优美诗词的欣赏与收藏功能。

### 核心功能

| 功能 | 描述 |
|------|------|
| **每日推荐** | 从 API 获取每日推荐诗词，包含诗句、作者、朝代、推荐理由 |
| **历史记录** | 自动保存已获取的诗句，支持查看历史记录、滑动删除 |
| **诗词详情** | 完整诗词展示，支持原文和翻译，智能诗词/词牌类型检测 |
| **桌面小组件** | 支持桌面小组件展示精选句子，支持自定义颜色和字体 |
| **后台更新** | WorkManager 定时后台更新句子，可配置更新周期 |
| **离线缓存** | Room 数据库本地存储，支持离线查看 |
| **字体配置** | 支持系统字体和自定义字体（.ttf/.otf） |
| **个性化设置** | 换行模式、背景色、文本色、字体等配置 |

---

## 技术架构

### 整体架构

```
┌─────────────────────────────────────────────────────────────────┐
│                         UI Layer                                 │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐  ┌─────────┐│
│  │  MainScreen │  │HistoryScreen│  │ DetailScreen│  │ Setting ││
│  │  (Compose)  │  │  (Compose)  │  │  (Compose)  │  │Screen   ││
│  └──────┬──────┘  └──────┬──────┘  └──────┬──────┘  └────┬────┘│
│         │                │                │               │     │
│  ┌──────┴────────────────┴────────────────┴───────────────┴────┐│
│  │                    ViewModels (MVVM)                         ││
│  │  MainViewModel  │  HistoryViewModel │ DetailViewModel        ││
│  └─────────────────┴───────────────────┴────────────────────────┘│
└─────────────────────────────┬────────────────────────────────────┘
                              │
┌─────────────────────────────┴────────────────────────────────────┐
│                          Data Layer                               │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │              SentenceRepository                             │  │
│  │  - 网络请求 (Retrofit)                                      │  │
│  │  - 本地存储 (Room)                                          │  │
│  │  - 配置管理 (SharedPreferences/DataStore)                   │  │
│  │  - 统一的数据访问入口                                        │  │
│  └─────────────────────────┬──────────────────────────────────┘  │
│                            │                                       │
│    ┌───────────────────────┼───────────────────────┐              │
│    │                       │                       │              │
│  Network               Local                    Config             │
│  ┌──────────────────┐  ┌──────────────────┐  ┌────────────────┐  │
│  │JinrishiciApiServ │  │ Room Database    │  │SharedPreferenc │  │
│  │- getSentence()   │  │- SentenceEntity  │  │- WidgetPref    │  │
│  │- getToken()      │  │- SentenceDao     │  │- Font options  │  │
│  └──────────────────┘  └──────────────────┘  └────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────┴────────────────────────────────────┐
│                   Dependency Injection (Hilt)                     │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────────┐          │
│  │NetworkModule│  │DatabaseModule│  │RepositoryModule │          │
│  └─────────────┘  └──────────────┘  └─────────────────┘          │
└─────────────────────────────────────────────────────────────────┘
                              │
┌─────────────────────────────┴────────────────────────────────────┐
│                    Background Processing                          │
│  ┌────────────────────────────────────────────────────────────┐  │
│  │              WorkManager                                    │  │
│  │  - SentenceUpdateWorker (定时更新)                          │  │
│  │  - App Startup 初始化                                       │  │
│  └────────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 目录结构

```
com.jizhi/
├── ui/                        # UI 层
│   ├── main/                  # 主页面
│   │   ├── MainActivity.kt    # 主 Activity（launchMode=singleTask）
│   │   ├── MainScreen.kt      # Compose 主页面
│   │   └── MainViewModel.kt   # 主页面 ViewModel
│   ├── history/               # 历史记录
│   │   ├── HistoryActivity.kt # 历史记录容器 Activity
│   │   ├── HistoryScreen.kt   # Compose 历史列表
│   │   └── HistoryViewModel.kt# 历史记录 ViewModel
│   ├── detail/                # 诗词详情
│   │   ├── DetailActivity.kt  # 详情页 Activity
│   │   └── DetailViewModel.kt # 详情页 ViewModel
│   ├── setting/               # 设置页面
│   │   └── SettingActivity.kt # 设置页面（含字体/颜色配置）
│   └── theme/                 # Compose 主题
│       ├── Theme.kt           # Material 3 主题配置
│       ├── Color.kt           # 颜色定义
│       └── Type.kt            # 字体配置
├── data/                      # 数据层
│   ├── local/                 # Room 数据库
│   │   ├── SentenceEntity.kt  # 句子实体（包含原文和翻译）
│   │   ├── SentenceDao.kt     # DAO 接口
│   │   ├── JiZhiDatabase.kt   # Room 数据库配置
│   │   └── WidgetSentenceDataStore.kt # DataStore 存储
│   ├── remote/                # 网络请求
│   │   ├── SentenceResponse.kt     # API 响应模型
│   │   ├── JinrishiciApiService.kt # Retrofit API 接口
│   │   └── JinrishiciClient.kt     # 网络客户端（含令牌管理）
│   ├── PoemType.kt            # 诗词类型检测（800+词牌名）
│   ├── PoemFormatter.kt       # 内容格式化工具（换行处理）
│   └── WidgetPreferences.kt   # SharedPreferences 封装（含字体配置）
├── di/                        # Hilt 模块
│   ├── DatabaseModule.kt      # 数据库模块
│   ├── NetworkModule.kt       # 网络模块
│   └── RepositoryModule.kt    # 仓库模块
├── repository/                # 数据仓库
│   └── SentenceRepository.kt  # 句子数据仓库
├── worker/                    # WorkManager
│   └── SentenceUpdateWorker.kt# 定时更新 Worker
└── widget/                    # 桌面小组件
    ├── SentenceWidgetProvider.kt  # AppWidgetProvider
    ├── SentenceWidgetViewsFactory.kt # RemoteViews 工厂
    └── SentenceWidgetService.kt   # 小组件服务
```

---

## 设计思想

### 1. Clean Architecture + MVVM

采用 Clean Architecture 原则，将应用分为三层：

- **UI 层**：负责展示和用户交互，使用 Jetpack Compose 实现声明式 UI
- **数据层**：统一管理网络请求和本地存储，对上层屏蔽数据来源细节
- **依赖注入层**：使用 Hilt 统一管理所有依赖，实现解耦

### 2. 依赖注入 (Hilt)

所有依赖通过 Hilt 注入，避免手动创建实例：

```kotlin
// ViewModel 注入示例
@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: SentenceRepository
) : ViewModel()

// Activity 注入示例
@AndroidEntryPoint
class MainActivity : ComponentActivity()
```

### 3. 数据流设计

使用 Kotlin Flow 和 StateFlow 实现响应式数据流：

```kotlin
// ViewModel 中的状态
private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

// Composable 中收集状态
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

### 4. 错误处理

使用 `Result<T>` 包装可能失败的操作：

```kotlin
suspend fun getTodaySentence(): Result<SentenceResponse> {
    return try {
        val response = apiService.getSentence(token)
        if (response.status == "success") {
            Result.success(response)
        } else {
            Result.failure(Exception(response.errMessage))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
}
```

### 5. 后台任务

使用 WorkManager 管理后台任务，确保任务在应用退出后仍能执行：

```kotlin
@HiltWorker
class SentenceUpdateWorker @Inject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: SentenceRepository
) : CoroutineWorker(context, params) {
    // 定时更新句子
}
```

### 6. 版本目录管理

使用 Gradle Version Catalog (`libs.versions.toml`) 集中管理依赖版本：

```toml
[versions]
kotlin = "2.0.21"
ksp = "2.0.21-1.0.25"
agp = "8.7.2"
hilt = "2.51.1"
room = "2.6.1"
composeBom = "2024.06.00"

[libraries]
kotlin = { group = "org.jetbrains.kotlin", name = "kotlin", version.ref = "kotlin" }
hilt = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
```

### 7. 诗词类型检测

通过词牌名数据库和内容格式智能判断诗词类型：

```kotlin
fun detectPoemType(title: String, content: String = ""): PoemType {
    // 策略1：标题检测 - 检测词牌名格式（如"卜算子·咏梅"）
    // 策略2：内容格式检测 - 诗句每句字数相同，词句长短不一
    // 词牌名数据库：800+ 词牌名，含变体
}
```

### 8. 字体配置系统

支持系统字体和自定义字体的灵活配置：

```kotlin
data class FontOption(
    id: String,           // 唯一标识
    name: String,         // 显示名称
    fontFamily: String,   // Typeface 家族名称或字体路径
    isCustom: Boolean,    // 是否自定义字体
    fontPath: String?     // 自定义字体文件路径
)

// 使用选中的字体
val fontOption = widgetPreferences.getSelectedFontOption(context)
val typeface = FontOptions.getTypeface(context, fontOption)
```

---

## 第三方库依赖

| 库 | 用途 | 版本 |
|---|---|---|
| **Jetpack Compose** | 声明式 UI 框架 | 2024.06.00 |
| **Hilt** | 依赖注入 | 2.51.1 |
| **Room** | 本地数据库 | 2.6.1 |
| **Retrofit** | 网络请求 | 2.11.0 |
| **WorkManager** | 后台任务调度 | 2.9.0 |
| **App Startup** | 应用启动优化 | 1.1.1 |
| **Kotlin Coroutines** | 异步编程 | 1.8.1 |
| **Material 3** | Material Design 组件 | 1.12.0 |
| **OkHttp** | HTTP 客户端 | 4.12.0 |
| **Gson** | JSON 解析 | 2.11.0 |

---

## API 集成

### 今日诗词 API

| 端点 | 方法 | 用途 |
|------|------|------|
| `/token` | GET | 获取用户令牌 |
| `/sentence` | GET | 获取每日诗句 |

### 令牌管理
- 自动获取和刷新令牌
- 令牌本地缓存（SharedPreferences）
- 无效令牌自动重新获取

---

## 关键设计决策

### 1. 小组件无法使用 Hilt

`SentenceWidgetProvider` 继承自 `AppWidgetProvider`，由系统实例化，无法使用 Hilt 注入。解决方案：使用 `JiZhiDatabase.getInstance()` 直接获取数据库单例。

### 2. 诗词/词牌智能检测

- 标题检测：识别词牌名格式（如"词牌名·标题"）
- 内容检测：诗句每句字数相同，词句长短不一
- 800+ 词牌名数据库，含常见变体

### 3. 字体配置持久化

- 系统字体：通过 `SharedPreferences` 保存字体 ID
- 自定义字体：字体文件复制到应用私有目录，保存路径
- 运行时通过 `FontOptions.getTypeface()` 加载字体

### 4. 配置实时生效

设置变更后，通过广播刷新小组件，确保配置实时生效：

```kotlin
// 发送广播刷新小组件
val intent = Intent(SentenceWidgetProvider.ACTION_UPDATE_ALL)
context.sendBroadcast(intent)
```
