# Changelog

所有变更按照时间倒序排列。

## [v1.1.0] - 2026-02-01

### 新增功能

#### 诗词详情页 (Detail Activity)
- 完整诗词内容展示，支持原文和翻译
- **智能诗词类型检测**：通过词牌名和内容格式判断诗词/词牌
  - 800+ 词牌名数据库
  - 内容行长分析（诗每句字数相同，词句长短不一）
- 翻译卡片展示（当 API 返回翻译时）
- 居中显示诗词，左对齐显示词牌
- 标点符号处智能换行
- 名句高亮显示
- 喜欢状态切换
- 标签/分类展示

#### 字体配置 (Font Customization)
- 系统字体选择：默认、无衬线体、衬线体、等宽体
- **自定义字体支持**：导入 .ttf/.otf 字体文件
- 字体文件保存在应用私有目录
- 设置页面提供字体选择器和添加字体功能
- 全应用统一使用选中的字体

#### 增强设置页面
- **小组件更新周期**：15分钟至24小时，或"永不自动更新"
- **诗词显示换行模式**：
  - 默认：系统默认规则
  - 智能标点换行：短诗在标点后自动换行
  - 强制标点换行：每句后强制换行
- **小组件背景色**：12种预设颜色 + 自定义 ARGB
- **小组件文本颜色**：8种预设颜色 + 自定义 ARGB
- **应用字体设置**（见上方字体配置）
- 配置变更实时刷新小组件

#### 小组件增强
- 支持自定义背景色和文本颜色
- 支持自定义字体
- 支持多种换行模式
- 显示标题、作者、朝代信息
- 完整原文展示（originContentList）
- 点击进入应用

#### 诗词类型检测 (PoemType.kt)
- 词牌名列表：800+ 词牌名，含变体
- 2字词牌：浣溪沙、如梦令、扬州慢等
- 3字词牌：卜算子、虞美人、清平乐等
- 多字词牌：水调歌头、念奴娇、沁园春等
- 智能检测逻辑避免误判

### 技术优化

#### UI/UX 优化
- Compose UI 现代设计
- 喜欢按钮弹簧动画效果
- 历史记录滑动删除
- 设置页面 BottomSheet 选择器
- 颜色预览和自定义对话框
- 字体预览功能

#### 数据层优化
- SentenceEntity 扩展支持原文和翻译列表
- SentenceResponse 完整解析
- PoemFormatter 智能换行处理
- WidgetPreferences 集中配置管理

#### 构建配置
- **ProGuard/R8 混淆规则**：
  - Gson 序列化保护
  - Retrofit/OkHttp 规则
  - Room 实体保护
  - Hilt 注入规则
  - Compose 运行时保护
- **Release 签名配置**：使用 jz.jks 签名文件
- **启用代码混淆**：Release 构建自动混淆

### 依赖版本

| 库 | 版本 |
|---|---|
| Kotlin | 2.0.21 |
| AGP | 8.7.2 |
| Gradle | 8.9 |
| Compose BOM | 2024.06.00 |
| Hilt | 2.51.1 |
| Room | 2.6.1 |
| Retrofit | 2.11.0 |
| WorkManager | 2.9.0 |
| OkHttp | 4.12.0 |
| Gson | 2.11.0 |

### 构建命令

```bash
# Debug 构建
./gradlew assembleDebug

# Debug 构建并安装
./gradlew installDebug

# Release 构建（需签名）
./gradlew assembleRelease

# 运行单元测试
./gradlew testDebugUnitTest

# Lint 检查
./gradlew lintDebug

# 清理并构建
./gradlew clean assembleDebug
```

---

## [v1.0.1] - 2025-02-01

### 版本升级

- **AGP 升级** 8.5.2 → 8.7.2
  - 支持 compileSdk 36
  - Gradle 8.9 配合使用

- **SDK 版本升级**
  - compileSdk: 34 → 36
  - targetSdk: 34 → 36
  - 添加 `android.suppressUnsupportedCompileSdk=36` 配置

### 依赖版本

| 库 | 版本 |
|---|------|
| Kotlin | 2.0.21 |
| AGP | 8.7.2 |
| Gradle | 8.9 |
| Compose BOM | 2024.06.00 |
| Hilt | 2.51.1 |
| Room | 2.6.1 |
| Retrofit | 2.11.0 |
| WorkManager | 2.9.0 |
| App Startup | 1.1.1 |
| Kotlin Coroutines | 1.8.1 |

### 构建配置

- **Java 版本**: 17
- **目标 SDK**: 36
- **最小 SDK**: 24

---

## [v1.0.0] - 2025-02-01

### 新增功能

- **主页面** - 今日推荐诗词展示页面
  - 使用 Jetpack Compose 实现声明式 UI
  - 显示诗句内容、作者、朝代信息
  - 刷新按钮获取新诗句
  - 加载状态和错误状态展示

- **历史记录** - 历史诗句查看功能
  - Room 数据库本地存储
  - 支持查看所有历史记录
  - 保留最近 100 条数据

- **桌面小组件** - 桌面励志句子展示
  - 支持 4x1 尺寸小组件
  - 显示精选句子
  - 点击刷新功能

- **后台更新** - WorkManager 定时更新
  - 可配置更新间隔
  - 应用退出后继续运行
  - App Startup 优化启动

### 技术优化

- **Kotlin 升级** 2.0.0 → 2.0.21
- **Gradle 升级** 8.2 → 8.7
- **AGP 升级** 8.2.0 → 8.5.2
- **KSP 升级** 1.9.20-1.0.14 → 2.0.21-1.0.25
- **依赖版本目录** - 新增 `libs.versions.toml`
  - 集中管理所有依赖版本
  - 使用 `alias()` 和 `libs.xxx` 声明依赖

- **Hilt 集成**
  - `@AndroidEntryPoint` 注入 Activity
  - `@HiltViewModel` 注入 ViewModel
  - `@Module` + `@InstallIn` 声明模块

- **架构重构**
  - `MainActivity` 迁移至 `ui/main/` 目录
  - `MainViewModel` 迁移至 `ui/main/` 目录
  - `MainScreen` 迁移至 `ui/main/` 目录

### 依赖版本

| 库 | 版本 |
|---|------|
| Kotlin | 2.0.21 |
| Compose BOM | 2024.06.00 |
| Hilt | 2.51.1 |
| Room | 2.6.1 |
| Retrofit | 2.11.0 |
| WorkManager | 2.9.0 |
| App Startup | 1.1.1 |

### 构建配置

- **Java 版本**: 17
- **目标 SDK**: 36
- **最小 SDK**: 24

### 构建命令

```bash
# Debug 构建
./gradlew assembleDebug

# Release 构建
./gradlew assembleRelease

# 运行测试
./gradlew test

# Lint 检查
./gradlew lintDebug
```

---

## 初始项目结构

项目初始包含以下基础配置：

- Android Gradle Plugin 配置
- Kotlin + Compose 环境
- Hilt 依赖注入框架
- Room 数据库配置
- Retrofit 网络请求
- WorkManager 后台任务
- App Startup 启动优化
- Material 3 主题配置
