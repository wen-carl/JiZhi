# 几枝 - 今日诗词

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-2.2.20-blue?logo=kotlin)
![Jetpack Compose](https://img.shields.io/badge/Compose-BOM%202024.02.00-blue)
![License](https://img.shields.io/badge/License-Apache%202.0-green)
![Min SDK](https://img.shields.io/badge/Min%20SDK-24-orange)

</div>

「几枝」是一款优雅的 Android 诗词展示应用，每日为你推荐一首经典诗词。应用采用纯 Kotlin 开发，结合 Jetpack Compose 现代 UI 框架，带来流畅自然的阅读体验。

## 功能特点

### 核心功能
- **每日诗词**：每日自动获取并展示精选诗词名句
- **历史记录**：自动保存浏览过的诗词，支持查看和删除
- **诗词详情**：展示完整诗词内容、作者、朝代信息
- **翻译展示**：提供诗词的白话文翻译
- **喜欢收藏**：一键收藏喜欢的诗词

### 智能识别
- **词牌名检测**：智能识别词牌名，自动切换排版样式（诗 vs 词）
- **800+ 词牌名库**：内置完整的词牌名数据库

### 桌面小组件
- **多种尺寸**：支持 1x1、2x2、2x4、3x4 多种小组件尺寸
- **定时更新**：支持 15 分钟到 1 天的自定义更新频率
- **背景色配置**：提供 12 种预设背景色，支持自定义颜色
- **文字色配置**：提供 8 种预设文字色
- **字体自定义**：支持系统字体切换和自定义字体文件

### 个性化设置
- **多语言支持**：支持中文、英文、跟随系统
- **换行模式**：默认、智能标点、强制标点三种模式
- **主题适配**：深色/浅色主题自动适配

## 技术栈

| 类别 | 技术 |
|------|------|
| **语言** | Kotlin 2.2.20 |
| **UI 框架** | Jetpack Compose (BOM 2024.02.00) |
| **依赖注入** | Hilt 2.50 |
| **本地存储** | Room + DataStore |
| **网络请求** | Retrofit + OkHttp |
| **后台任务** | WorkManager |
| **架构模式** | MVVM + Clean Architecture |

## 项目结构

```
com.jizhi/
├── ui/                          # UI 层
│   ├── main/                   # 主页面 (MainActivity, MainScreen)
│   ├── history/                # 历史记录页面
│   ├── detail/                 # 诗词详情页面
│   ├── setting/                # 设置页面
│   └── theme/                  # Compose 主题配置
├── data/                       # 数据层
│   ├── local/                  # 本地数据 (Room, DataStore)
│   ├── remote/                  # 远程 API
│   ├── PoemType.kt             # 词牌名检测
│   └── PoemFormatter.kt        # 内容格式化
├── di/                         # Hilt 依赖注入模块
├── repository/                 # 数据仓库
├── worker/                     # WorkManager 后台任务
└── widget/                     # 桌面小组件
```

## 快速开始

### 环境要求

- Android Studio Arctic Fox 或更高版本
- JDK 17+
- Android SDK 36

### 构建项目

```bash
# Debug 构建
./gradlew assembleDebug

# Debug 构建并安装到设备
./gradlew installDebug

# Release 构建
./gradlew assembleRelease
```

### 项目导入

1. 克隆项目到本地
2. 使用 Android Studio 打开项目
3. 等待 Gradle 同步完成
4. 运行或构建

## API 说明

应用数据来源于 [今日诗词 API](https://www.jinrishici.com/)，感谢提供免费的开源诗词数据接口。

| 端点 | 方法 | 用途 |
|------|------|------|
| `/token` | GET | 获取用户令牌 |
| `/sentence` | GET | 获取每日诗句 |

## 贡献指南

欢迎提交 Issue 和 Pull Request！

1. Fork 本项目
2. 创建功能分支 (`git checkout -b feature/xxx`)
3. 提交更改 (`git commit -m 'Add xxx'`)
4. 推送分支 (`git push origin feature/xxx`)
5. 创建 Pull Request

## 许可证

本项目基于 Apache License 2.0 许可证开源。

---

<div align="center">

**让每一次打开手机，都遇见一首诗**

</div>
