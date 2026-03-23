# JiZhi UI 层 — 智能体操作指南

## OVERVIEW

UI 层负责视图渲染、用户交互和状态管理，采用 MVVM + Compose 模式。

## WHERE TO LOOK

| 任务 | 文件 |
|------|------|
| 修改主界面布局/逻辑 | `main/MainScreen.kt`, `main/MainViewModel.kt` |
| 添加主界面新功能 | `main/MainActivity.kt` (入口配置) |
| 修改历史记录列表 | `history/HistoryScreen.kt`, `history/HistoryViewModel.kt` |
| 修改诗词详情页 | `detail/DetailActivity.kt`, `detail/DetailViewModel.kt` |
| 修改设置页面 | `setting/SettingActivity.kt` |
| 调整主题/颜色 | `theme/Theme.kt`, `theme/Color.kt` |
| 调整字体排版 | `theme/Type.kt` |

## CONVENTIONS

### Activity 生命周期
- MainActivity 使用 `launchMode="singleTask"`，支持小组件冷启动进入
- Activity 之间通过 Intent 传参，禁止全局单例共享状态
- 所有 Activity 继承 AppCompatActivity 或 ComponentActivity

### Compose 状态管理
- ViewModel 使用 `StateFlow` 而非 LiveData
- UiState 数据类置于 ViewModel 同文件底部
- Screen 组件接收 ViewModel 作为参数，不直接创建实例

### 主题系统
- Color.kt 定义 Compose 颜色常量，Android 资源颜色在 colors.xml
- Type.kt 定义 TextStyle，使用 MaterialTheme 语义化
- 深色/浅色主题通过 Theme.kt 中的 colorScheme 控制

## ANTI-PATTERNS

1. **禁止** 在 Activity 中直接调用 API，使用 ViewModel + Repository
2. **禁止** 在 Compose 中使用 rememberSaveable 存储业务数据，应存 Room
3. **禁止** Activity 持有 ViewModel 引用后自行销毁，应由系统管理
4. **禁止** 硬编码颜色值，应使用 Theme 定义的语义化颜色
5. **禁止** 在 UI 层处理数据转换逻辑，应在 Repository 或 Formatter 中完成
