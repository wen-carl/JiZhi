package com.jizhi.ui.main

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jizhi.data.local.SentenceEntity
import com.jizhi.data.remote.JinrishiciClient
import com.jizhi.repository.SentenceRepository
import com.jizhi.ui.detail.DetailActivity
import com.jizhi.widget.SentenceWidgetProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 清理朝代文本，移除"代"后缀
 */
private fun String.cleanDynasty(): String {
    return if (this.endsWith("代")) {
        this.dropLast(1)
    } else {
        this
    }
}

/**
 * 主页面 ViewModel
 * 使用 Flow 监听数据库变化，实时更新喜欢状态
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SentenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MainUiState>(MainUiState.Loading)
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var currentSentenceId: String? = null
    private var isRefreshing = false
    private var isFromWidget = false
    private var collectJob: Job? = null

    // 不在 init 中自动加载，由 MainScreen 的 LaunchedEffect 决定加载方式

    /**
     * 加载今日诗词
     * @param fromWidget 是否从小组件进入（如果是，则直接从 SharedPreferences 读取，不调用API）
     */
    fun loadTodaySentence(fromWidget: Boolean = false) {
        if (fromWidget) {
            // 从小组件进入，直接从 SharedPreferences 读取小组件显示的内容
            loadFromWidget()
        } else {
            // 正常进入，调用API获取新句子
            loadFromApi()
        }
    }

    /**
     * 从小组件 SharedPreferences 加载数据
     * 先尝试用 ID 查询数据库，有记录则显示喜欢按钮
     */
    private fun loadFromWidget() {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            try {
                val widgetData = JinrishiciClient.getWidgetSentenceDetail(context)
                if (widgetData != null) {
                    currentSentenceId = widgetData.id

                    // 先尝试从数据库查询，数据库中有记录时会显示喜欢按钮
                    if (widgetData.id.isNotEmpty()) {
                        val sentence = repository.getSentenceById(widgetData.id)
                        if (sentence != null) {
                            // 数据库中有记录，显示 Success 状态（带喜欢按钮）
                            _uiState.value = MainUiState.Success(sentence)
                            startSentenceListener(sentence.id)
                        } else {
                            // 数据库中没有记录，显示 WidgetSentence 状态（无喜欢按钮）
                            _uiState.value = MainUiState.WidgetSentence(
                                id = widgetData.id,
                                content = widgetData.content,
                                originContentList = widgetData.originContentList,
                                title = widgetData.title,
                                dynasty = widgetData.dynasty,
                                author = widgetData.author,
                                isFavorite = widgetData.isFavorite
                            )
                        }
                    } else {
                        // 没有 ID，直接显示 WidgetSentence 状态
                        _uiState.value = MainUiState.WidgetSentence(
                            id = widgetData.id,
                            content = widgetData.content,
                            originContentList = widgetData.originContentList,
                            title = widgetData.title,
                            dynasty = widgetData.dynasty,
                            author = widgetData.author,
                            isFavorite = widgetData.isFavorite
                        )
                    }
                } else {
                    _uiState.value = MainUiState.Error("小组件暂无数据")
                }
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    /**
     * 从数据库加载最新句子（备用方案）
     */
    private fun loadFromDatabase() {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            try {
                repository.getRecentSentences(1).collectLatest { sentences ->
                    val sentence = sentences.firstOrNull()
                    if (sentence != null) {
                        _uiState.value = MainUiState.Success(sentence)
                        currentSentenceId = sentence.id
                        startSentenceListener(sentence.id)
                    } else {
                        // 数据库没有数据，从 SharedPreferences 读取
                        val widgetData = JinrishiciClient.getWidgetSentenceDetail(context)
                        if (widgetData != null) {
                            _uiState.value = MainUiState.WidgetSentence(
                                content = widgetData.content,
                                originContentList = widgetData.originContentList,
                                title = widgetData.title,
                                dynasty = widgetData.dynasty,
                                author = widgetData.author
                            )
                            currentSentenceId = "widget_${widgetData.content.hashCode()}"
                        } else {
                            _uiState.value = MainUiState.Error("暂无数据")
                        }
                    }
                }
            } catch (e: Exception) {
                _uiState.value = MainUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    /**
     * 从API加载今日诗词
     */
    private fun loadFromApi() {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading

            repository.getTodaySentence()
                .onSuccess { response ->
                    val sentenceId = response.data?.id
                    val sentence = response.data?.let { data ->
                        val origin = data.origin
                        val originContentList = origin?.content ?: emptyList()
                        val translateList = origin?.translate ?: emptyList()

                        SentenceEntity(
                            id = data.id,
                            content = data.content,
                            originContentList = originContentList,
                            translateList = translateList,
                            popularity = data.popularity,
                            title = origin?.title ?: "未知",
                            dynasty = (origin?.dynasty ?: "未知").cleanDynasty(),
                            author = origin?.author ?: "未知",
                            matchTags = data.matchTags?.joinToString(",") ?: "",
                            recommendedReason = data.recommendedReason,
                            cacheAt = data.cacheAt,
                            isFavorite = false
                        )
                    }

                    if (sentence != null && sentenceId != null) {
                        _uiState.value = MainUiState.Success(sentence)
                        currentSentenceId = sentenceId
                        startSentenceListener(sentenceId)
                        updateWidget()
                        // 保存到 SharedPreferences 供小组件使用
                        JinrishiciClient.saveTodaySentenceForWidget(
                            context,
                            id = sentence.id,
                            content = sentence.content,
                            originContentList = sentence.originContentList,
                            translateList = sentence.translateList,
                            title = sentence.title,
                            dynasty = sentence.dynasty,
                            author = sentence.author,
                            isFavorite = sentence.isFavorite
                        )
                    } else {
                        _uiState.value = MainUiState.Error("获取数据失败")
                    }
                }
                .onFailure { e ->
                    _uiState.value = MainUiState.Error(e.message ?: "网络请求失败")
                }
        }
    }

    /**
     * 发送广播更新所有小组件
     */
    private fun updateWidget() {
        val intent = Intent(context, SentenceWidgetProvider::class.java).apply {
            action = SentenceWidgetProvider.ACTION_UPDATE_ALL
        }
        context.sendBroadcast(intent)
    }

    /**
     * 启动句子状态监听
     * 使用 Flow 监听数据库变化，实时更新喜欢状态
     */
    private fun startSentenceListener(sentenceId: String) {
        collectJob?.cancel()

        collectJob = viewModelScope.launch {
            repository.getSentenceByIdFlow(sentenceId).collectLatest { sentence ->
                if (sentence != null) {
                    _uiState.value = MainUiState.Success(sentence)
                    currentSentenceId = sentence.id
                }
            }
        }
    }

    /**
     * 切换喜欢状态
     * 同时更新数据库和小组件 SharedPreferences
     */
    fun toggleFavorite() {
        currentSentenceId ?: return

        viewModelScope.launch {
            when (val currentState = _uiState.value) {
                is MainUiState.Success -> {
                    // 数据库中有记录，直接切换喜欢状态
                    val sentence = currentState.sentence
                    val newFavoriteState = !sentence.isFavorite

                    repository.updateFavorite(sentence.id, newFavoriteState)

                    // 同步更新小组件 SharedPreferences 的喜欢状态
                    JinrishiciClient.updateWidgetFavorite(context, newFavoriteState)
                }

                is MainUiState.WidgetSentence -> {
                    // 数据库中没有记录，先保存到数据库
                    val widgetSentence = currentState
                    val newFavoriteState = !widgetSentence.isFavorite

                    // 保存到数据库
                    val entity = SentenceEntity(
                        id = widgetSentence.id.ifEmpty { "widget_${widgetSentence.content.hashCode()}" },
                        content = widgetSentence.content,
                        originContentList = widgetSentence.originContentList,
                        popularity = 0,
                        title = widgetSentence.title,
                        dynasty = widgetSentence.dynasty,
                        author = widgetSentence.author,
                        matchTags = "",
                        recommendedReason = "",
                        cacheAt = "",
                        isFavorite = newFavoriteState,
                        isFromWidget = true
                    )
                    repository.updateFavorite(entity.id, newFavoriteState)

                    // 更新 UI 为 Success 状态
                    _uiState.value = MainUiState.Success(sentence = entity.copy())
                    currentSentenceId = entity.id
                    startSentenceListener(entity.id)

                    // 同步更新小组件 SharedPreferences 的喜欢状态
                    JinrishiciClient.updateWidgetFavorite(context, newFavoriteState)
                }

                else -> {}
            }
        }
    }

    /**
     * 显示诗词详情
     * 直接传递数据，不查询数据库
     */
    fun showDetail(sentence: SentenceEntity) {
        val intent = Intent(context, DetailActivity::class.java).apply {
            putExtra(DetailActivity.EXTRA_ID, sentence.id)
            putExtra(DetailActivity.EXTRA_CONTENT, sentence.content)
            putExtra(DetailActivity.EXTRA_ORIGIN_CONTENT, sentence.originContent)
            putExtra(DetailActivity.EXTRA_TRANSLATE, sentence.translate)
            putExtra(DetailActivity.EXTRA_TITLE, sentence.title)
            putExtra(DetailActivity.EXTRA_DYNASTY, sentence.dynasty)
            putExtra(DetailActivity.EXTRA_AUTHOR, sentence.author)
            putExtra(DetailActivity.EXTRA_IS_FAVORITE, sentence.isFavorite)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * 显示小组件数据的详情页
     * 使用真实 ID，如果数据库中有记录则显示喜欢按钮
     */
    fun showWidgetDetail(widgetSentence: MainUiState.WidgetSentence) {
        val intent = Intent(context, DetailActivity::class.java).apply {
            // 使用真实 ID，数据库中有记录时会显示喜欢按钮
            putExtra(DetailActivity.EXTRA_ID, widgetSentence.id)
            putExtra(DetailActivity.EXTRA_CONTENT, widgetSentence.content)
            putExtra(DetailActivity.EXTRA_ORIGIN_CONTENT, widgetSentence.originContent)
            putExtra(DetailActivity.EXTRA_TRANSLATE, "")
            putExtra(DetailActivity.EXTRA_TITLE, widgetSentence.title)
            putExtra(DetailActivity.EXTRA_DYNASTY, widgetSentence.dynasty)
            putExtra(DetailActivity.EXTRA_AUTHOR, widgetSentence.author)
            putExtra(DetailActivity.EXTRA_IS_FAVORITE, widgetSentence.isFavorite)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    /**
     * 刷新诗句（带防抖）
     */
    fun refresh() {
        if (isRefreshing) return
        isRefreshing = true
        loadTodaySentence()
        // 1秒后重置防抖标志
        viewModelScope.launch {
            delay(1000)
            isRefreshing = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        collectJob?.cancel()
    }
}

/**
 * 主页面 UI 状态
 */
sealed class MainUiState {
    data object Loading : MainUiState()
    data class Success(val sentence: SentenceEntity) : MainUiState()
    data class Error(val message: String) : MainUiState()

    /**
     * 小组件句子状态（无数据库记录时使用）
     */
    data class WidgetSentence(
        val id: String = "",
        val content: String,
        val originContentList: List<String> = emptyList(),
        val title: String,
        val dynasty: String,
        val author: String,
        val isFavorite: Boolean = false
    ) : MainUiState() {
        /**
         * 获取拼接后的全文（向后兼容）
         */
        val originContent: String
            get() = originContentList.joinToString("\n")
    }
}
