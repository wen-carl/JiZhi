# JiZhi Android 项目 - 智能体操作指南

**Generated**: 2026-02-01
**Branch**: 未指定

## 项目概述

JiZhi（今日诗词）是一款基于 Kotlin 2.0.21 + Jetpack Compose + Hilt + Room + Retrofit 构建的诗词展示应用。采用 Clean Architecture 与 MVVM 模式。

**API 来源**：今日诗词 API (jinrishici.com)

## 核心功能

| 功能 | 状态 | 实现位置 |
|------|------|----------|
| 每日推荐诗词 | ✅ 完成 | `MainScreen.kt` |
| 历史记录管理 | ✅ 完成 | `HistoryScreen.kt` |
| 诗词详情展示 | ✅ 完成 | `DetailActivity.kt` |
| 词牌名智能检测 | ✅ 完成 | `PoemType.kt` |
| 字体自定义配置 | ✅ 完成 | `SettingActivity.kt` |
| 小组件展示 | ✅ 完成 | `SentenceWidgetProvider.kt` |
| 小组件定时更新 | ✅ 完成 | `SentenceUpdateWorker.kt` |
| 背景色/文本色配置 | ✅ 完成 | `SettingActivity.kt` |
| 翻译展示 | ✅ 完成 | `DetailActivity.kt` |
| 喜欢/收藏功能 | ✅ 完成 | Repository 层 |

## 入口点

| 组件 | 文件 | 备注 |
|------|------|------|
| Application | `JiZhiApplication.kt` | @HiltAndroidApp，启动 WorkManager |
| Main Activity | `MainActivity.kt` | launchMode="singleTask"，支持小组件进入 |
| Hilt Modules | `DatabaseModule.kt`, `RepositoryModule.kt`, `NetworkModule.kt` | SingletonComponent 注入 |

## 代码结构

```
com.jizhi/
├── ui/
│   ├── main/
│   │   ├── MainActivity.kt           # 主 Activity，launchMode="singleTask"
│   │   ├── MainScreen.kt             # Compose 主页面
│   │   └── MainViewModel.kt          # 主页面 ViewModel
│   ├── history/
│   │   ├── HistoryActivity.kt        # 历史记录容器
│   │   ├── HistoryScreen.kt          # Compose 历史列表
│   │   └── HistoryViewModel.kt       # 历史记录 ViewModel
│   ├── detail/
│   │   ├── DetailActivity.kt         # 诗词详情页
│   │   └── DetailViewModel.kt        # 详情页 ViewModel
│   ├── setting/
│   │   └── SettingActivity.kt        # 设置页面（包含字体配置）
│   └── theme/                        # Compose 主题
│       ├── Theme.kt
│       ├── Color.kt
│       └── Type.kt
├── data/
│   ├── local/
│   │   ├── SentenceEntity.kt         # Room 实体
│   │   ├── SentenceDao.kt            # DAO 接口
│   │   ├── JiZhiDatabase.kt          # Room 数据库
│   │   └── WidgetSentenceDataStore.kt # DataStore 存储
│   ├── remote/
│   │   ├── SentenceResponse.kt       # API 响应模型
│   │   ├── JinrishiciApiService.kt   # Retrofit API 接口
│   │   └── JinrishiciClient.kt       # 网络客户端
│   ├── PoemType.kt                   # 诗词类型检测（800+词牌名）
│   ├── PoemFormatter.kt              # 内容格式化工具
│   └── WidgetPreferences.kt          # SharedPreferences 封装
├── di/                               # Hilt 模块
│   ├── DatabaseModule.kt
│   ├── NetworkModule.kt
│   └── RepositoryModule.kt
├── repository/
│   └── SentenceRepository.kt         # 数据仓库
├── worker/
│   └── SentenceUpdateWorker.kt       # WorkManager 定时任务
└── widget/
    ├── SentenceWidgetProvider.kt     # 小组件 Provider
    └── SentenceWidgetViewsFactory.kt # RemoteViews 工厂
```

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

### 响应数据结构

```kotlin
SentenceResponse(
    status: String,           // "success" | 其他
    data: SentenceData?,
    token: String,
    errCode: Int?,
    errMessage: String?
)

SentenceData(
    id: String,
    content: String,          // 精选句子
    popularity: Int,
    origin: SentenceOrigin?,  // 完整诗词信息
    matchTags: List<String>?,
    recommendedReason: String,
    cacheAt: String
)

SentenceOrigin(
    title: String,           // 诗词标题
    dynasty: String,          // 朝代（如"宋代"）
    author: String,           // 作者
    content: List<String>?,  // 原文（逐句）
    translate: List<String>?  // 翻译
)
```

## 诗词类型检测

### 检测逻辑 (PoemType.kt)

```kotlin
fun detectPoemType(title: String, content: String = ""): PoemType
```

**策略1：标题检测**
- 检测标题是否以词牌名开头（如"卜算子·咏梅"）
- 后跟分隔符：·（空格（【《

**策略2：内容格式检测**
- 诗句特征：每句字数相同（五言7字/七言14字）
- 词特征：句长短不一（差异>2字）

**词牌名数据库**（800+）：
- 2字词牌：浣溪沙、如梦令、扬州慢、梅花引等
- 3字词牌：卜算子、虞美人、清平乐、点绛唇等
- 4字词牌：水调歌头、水龙吟、满江红、长相思等
- 多字词牌：水调歌头·明月几时有、念奴娇·赤壁怀古等

### 使用示例

```kotlin
val poemType = detectPoemType("卜算子·咏梅", content)
when (poemType) {
    PoemType.POEM -> { /* 居中显示 */ }
    PoemType.CI -> { /* 左对齐显示 */ }
}
```

## 字体配置系统

### FontOption 数据类

```kotlin
data class FontOption(
    id: String,              // 唯一标识
    name: String,            // 显示名称
    fontFamily: String,      // Typeface 家族名称或字体路径
    isCustom: Boolean = false, // 是否自定义字体
    fontPath: String? = null  // 自定义字体文件路径
)
```

### 系统字体选项

| ID | 显示名称 | fontFamily |
|----|----------|------------|
| default | 系统默认 | default |
| sans_serif | 无衬线体 | sans-serif |
| serif | 衬线体 | serif |
| monospace | 等宽体 | monospace |

### 自定义字体流程

1. 用户点击「添加自定义字体」
2. 文件选择器选择 .ttf/.otf 文件
3. 字体文件复制到应用私有目录
4. 保存字体路径到 SharedPreferences
5. 刷新应用使用新字体

### 使用选中的字体

```kotlin
val fontOption = widgetPreferences.getSelectedFontOption(context)
val typeface = FontOptions.getTypeface(context, fontOption)
```

## 设置选项

### 更新周期 (UpdateIntervalOptions)

| 值(小时) | 显示名称 |
|---------|----------|
| 0.25 | 15分钟 |
| 0.5 | 30分钟 |
| 1 | 1小时 |
| 2 | 2小时 |
| 3 | 3小时 |
| 6 | 6小时 |
| 8 | 8小时 |
| 24 | 1天 |
| -1 | 永不自动更新 |

### 换行模式 (LineBreakMode)

| 模式 | 值 | 描述 |
|------|------|------|
| DEFAULT | 0 | 系统默认规则 |
| AUTO_PUNCTUATION | 1 | 短诗在标点后自动换行 |
| FORCE_PUNCTUATION | 2 | 强制在标点后换行 |

### 背景色预设 (BACKGROUND_COLORS)

透明、纯白、纯黑、浅灰、深灰、淡蓝、淡绿、淡黄、淡粉、淡紫、半透明黑、半透明白

### 文本色预设 (TEXT_COLORS)

纯白、纯黑、浅灰、深灰、淡红、淡蓝、淡绿、金色

## 约定

### 命名规范

| 类型 | 规范 | 示例 |
|------|------|------|
| Activity | `*Activity` | `MainActivity` |
| ViewModel | `*ViewModel` | `HistoryViewModel` |
| Repository | `*Repository` | `SentenceRepository` |
| Compose Screen | `*Screen` | `HistoryScreen` |
| State | `*UiState` | `MainUiState` |
| Entity | `*Entity` | `SentenceEntity` |
| DAO | `*Dao` | `SentenceDao` |
| Module | `*Module` | `DatabaseModule` |

### 架构规则

1. **禁止** 绕过 Hilt 手动实例化（除 Widget 组件）
2. **禁止** 使用 `as` / `as?` 类型转换
3. **禁止** 空 catch 块
4. ViewModel 只通过 Repository 访问数据，不直接调用 API
5. Widget 无法使用 Hilt，使用 `JiZhiDatabase.getInstance()`

### 构建命令

```bash
# Debug 构建
./gradlew assembleDebug

# Debug 构建并安装到设备
./gradlew installDebug

# Release 构建（自动签名）
./gradlew assembleRelease

# 运行单元测试
./gradlew testDebugUnitTest

# Lint 检查
./gradlew lintDebug

# 清理并构建
./gradlew clean assembleDebug
```

## 已知问题

1. **Widget 无法使用 Hilt**：`SentenceWidgetProvider` 使用 `JiZhiDatabase.getInstance()` 获取单例
2. **词牌名列表待完善**：`PoemType.kt` 中的词牌名列表可能不完整，需持续补充
3. **字体渲染**：自定义字体可能需要额外适配
4. **API 稳定性**：依赖第三方 API，可能需要错误处理增强

## 关键规则

1. 禁止抑制类型错误
2. 禁止提交代码（除非用户明确要求）
3. 修改后必须运行 `./gradlew assembleDebug`
4. 必须使用 `lsp_diagnostics` 验证
5. 单元测试通过后再提交
