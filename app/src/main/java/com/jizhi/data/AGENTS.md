# JiZhi 数据层

**目录**: `app/src/main/java/com/jizhi/data/`

## OVERVIEW

数据层负责本地存储（Room）、远程API调用（Retrofit）、数据模型定义，是应用的核心业务数据来源。

## WHERE TO LOOK

| 任务 | 文件 |
|------|------|
| 诗词数据实体 | `local/SentenceEntity.kt` |
| 数据库操作DAO | `local/SentenceDao.kt` |
| Room数据库实例 | `local/JiZhiDatabase.kt` |
| 本地缓存DataStore | `local/WidgetSentenceDataStore.kt`, `DataStoreManager.kt` |
| API响应模型 | `remote/SentenceResponse.kt` |
| Retrofit API接口 | `remote/JinrishiciApiService.kt` |
| 网络客户端配置 | `remote/JinrishiciClient.kt` |
| 诗词类型检测 | `PoemType.kt` (800+词牌名) |
| 内容格式化 | `PoemFormatter.kt` |
| 设置持久化 | `WidgetPreferences.kt` |

## CONVENTIONS

### Room 实体规范
- 实体类使用 `@Entity(tableName = "xxx")`
- 主键使用 `@PrimaryKey`，类型为 `String` (API返回ID)
- 字段使用 `@ColumnInfo(name = "xxx")` 映射列名
- 继承 `Serializable` 或使用 `typeConverters` 处理复杂类型

### DataStore 缓存
- Widget 缓存使用 `DataStoreManager.getWidgetCacheData()`
- 缓存失效时间：1小时
- 无网络时返回缓存数据，保证离线可用

### API 错误处理
- `SentenceResponse.status == "success"` 表示成功
- 错误码 `errCode != 0` 时记录日志并尝试重新获取 token
- 网络异常捕获后返回空数据，UI 层处理 Loading/Error 状态

### 诗词格式化
- `PoemFormatter.formatContent()` 处理原文换行
- `PoemFormatter.formatTranslation()` 处理译文格式化
- 检测换行模式：`LineBreakMode` (DEFAULT/AUTO_PUNCTUATION/FORCE_PUNCTUATION)

## ANTI-PATTERNS

1. **禁止** 在 data 层直接使用 `Context`（应通过 Repository 层注入）
2. **禁止** 在 data 层处理 UI 状态（返回原始数据，由 ViewModel 转换）
3. **禁止** 在 Entity 中存储非持久化字段（Room 无法管理）
4. **禁止** 直接在 Widget 中创建 Repository（无法注入，使用 Database 单例）
5. **禁止** 忽略 API 错误码（`errCode` 非零时必须处理）
