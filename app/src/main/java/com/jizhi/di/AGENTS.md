# JiZhi DI 模块

**目录**: `app/src/main/java/com/jizhi/di/`

## OVERVIEW

提供 Hilt 依赖注入配置，管理数据库、网络、Repository 三大核心依赖的生命周期。

## WHERE TO LOOK

| 任务 | 文件 |
|------|------|
| 配置 Room 数据库 | `DatabaseModule.kt` |
| 配置 Retrofit/OkHttp | `NetworkModule.kt` |
| 注入 Repository | `RepositoryModule.kt` |
| 查看 API 基础地址 | `Constants.kt` (API_BASE_URL) |

## CONVENTIONS

### Module 声明规范
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object XxxModule
```

### Provides 方法规范
- 所有 `@Provides` 必须搭配 `@Singleton`
- 使用 `@ApplicationContext` 获取 Context
- OkHttp 超时统一配置：30秒连接/读取/写入
- Retrofit 默认使用 GsonConverterFactory
- Database 必须使用 `JiZhiDatabase.getInstance(context)` 获取单例（确保与 Widget 兼容）

### 依赖注入顺序
1. `NetworkModule` → OkHttpClient → Retrofit → JinrishiciApiService
2. `DatabaseModule` → JiZhiDatabase → SentenceDao
3. `RepositoryModule` → SentenceRepository (依赖上述两者)

## ANTI-PATTERNS

**禁止在此目录出现：**
- ❌ `@Binds` 替代 `@Provides`（仅接口绑定使用）
- ❌ 非 SingletonComponent（如 ActivityComponent、ViewModelComponent）
- ❌ 手动 `new` 实例化依赖（必须通过 Module 提供）
- ❌ 不同作用域的 Scope（如 `@ActivityScope`）
- ❌ 绕过 Module 直接在 @Inject 构造函数依赖非单例对象
