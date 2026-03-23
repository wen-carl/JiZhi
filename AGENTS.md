# JiZhi Android 项目 - 智能体操作指南

**Generated**: 2026-03-18
**Branch**: 未指定
**Kotlin**: 2.0.21 | **Compose**: BOM 2024.02.00 | **Hilt**: 2.50

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

<!-- gitnexus:start -->
# GitNexus — Code Intelligence

This project is indexed by GitNexus as **JiZhi** (791 symbols, 1087 relationships, 0 execution flows). Use the GitNexus MCP tools to understand code, assess impact, and navigate safely.

> If any GitNexus tool warns the index is stale, run `npx gitnexus analyze` in terminal first.

## Always Do

- **MUST run impact analysis before editing any symbol.** Before modifying a function, class, or method, run `gitnexus_impact({target: "symbolName", direction: "upstream"})` and report the blast radius (direct callers, affected processes, risk level) to the user.
- **MUST run `gitnexus_detect_changes()` before committing** to verify your changes only affect expected symbols and execution flows.
- **MUST warn the user** if impact analysis returns HIGH or CRITICAL risk before proceeding with edits.
- When exploring unfamiliar code, use `gitnexus_query({query: "concept"})` to find execution flows instead of grepping. It returns process-grouped results ranked by relevance.
- When you need full context on a specific symbol — callers, callees, which execution flows it participates in — use `gitnexus_context({name: "symbolName"})`.

## When Debugging

1. `gitnexus_query({query: "<error or symptom>"})` — find execution flows related to the issue
2. `gitnexus_context({name: "<suspect function>"})` — see all callers, callees, and process participation
3. `READ gitnexus://repo/JiZhi/process/{processName}` — trace the full execution flow step by step
4. For regressions: `gitnexus_detect_changes({scope: "compare", base_ref: "main"})` — see what your branch changed

## When Refactoring

- **Renaming**: MUST use `gitnexus_rename({symbol_name: "old", new_name: "new", dry_run: true})` first. Review the preview — graph edits are safe, text_search edits need manual review. Then run with `dry_run: false`.
- **Extracting/Splitting**: MUST run `gitnexus_context({name: "target"})` to see all incoming/outgoing refs, then `gitnexus_impact({target: "target", direction: "upstream"})` to find all external callers before moving code.
- After any refactor: run `gitnexus_detect_changes({scope: "all"})` to verify only expected files changed.

## Never Do

- NEVER edit a function, class, or method without first running `gitnexus_impact` on it.
- NEVER ignore HIGH or CRITICAL risk warnings from impact analysis.
- NEVER rename symbols with find-and-replace — use `gitnexus_rename` which understands the call graph.
- NEVER commit changes without running `gitnexus_detect_changes()` to check affected scope.

## Tools Quick Reference

| Tool | When to use | Command |
|------|-------------|---------|
| `query` | Find code by concept | `gitnexus_query({query: "auth validation"})` |
| `context` | 360-degree view of one symbol | `gitnexus_context({name: "validateUser"})` |
| `impact` | Blast radius before editing | `gitnexus_impact({target: "X", direction: "upstream"})` |
| `detect_changes` | Pre-commit scope check | `gitnexus_detect_changes({scope: "staged"})` |
| `rename` | Safe multi-file rename | `gitnexus_rename({symbol_name: "old", new_name: "new", dry_run: true})` |
| `cypher` | Custom graph queries | `gitnexus_cypher({query: "MATCH ..."})` |

## Impact Risk Levels

| Depth | Meaning | Action |
|-------|---------|--------|
| d=1 | WILL BREAK — direct callers/importers | MUST update these |
| d=2 | LIKELY AFFECTED — indirect deps | Should test |
| d=3 | MAY NEED TESTING — transitive | Test if critical path |

## Resources

| Resource | Use for |
|----------|---------|
| `gitnexus://repo/JiZhi/context` | Codebase overview, check index freshness |
| `gitnexus://repo/JiZhi/clusters` | All functional areas |
| `gitnexus://repo/JiZhi/processes` | All execution flows |
| `gitnexus://repo/JiZhi/process/{name}` | Step-by-step execution trace |

## Self-Check Before Finishing

Before completing any code modification task, verify:
1. `gitnexus_impact` was run for all modified symbols
2. No HIGH/CRITICAL risk warnings were ignored
3. `gitnexus_detect_changes()` confirms changes match expected scope
4. All d=1 (WILL BREAK) dependents were updated

## CLI

- Re-index: `npx gitnexus analyze`
- Check freshness: `npx gitnexus status`
- Generate docs: `npx gitnexus wiki`

<!-- gitnexus:end -->
