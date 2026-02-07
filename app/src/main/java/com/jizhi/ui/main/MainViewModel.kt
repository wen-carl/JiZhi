package com.jizhi.ui.main

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jizhi.Constants
import com.jizhi.data.local.SentenceEntity
import com.jizhi.data.remote.JinrishiciClient
import com.jizhi.repository.SentenceRepository
import com.jizhi.ui.detail.DetailActivity
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

    fun loadTodaySentence(fromWidget: Boolean = false) {
        if (fromWidget) {
            loadFromWidget()
        } else {
            loadFromApi()
        }
    }

    private fun loadFromWidget() {
        viewModelScope.launch {
            _uiState.value = MainUiState.Loading
            try {
                val widgetData = JinrishiciClient.getWidgetSentenceDetail(context)
                if (widgetData != null) {
                    currentSentenceId = widgetData.id

                    if (widgetData.id.isNotEmpty()) {
                        val sentence = repository.getSentenceById(widgetData.id)
                        if (sentence != null) {
                            _uiState.value = MainUiState.Success(sentence)
                            startSentenceListener(sentence.id)
                        } else {
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
                            dynasty = (origin?.dynasty ?: "未知"),
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

    private fun updateWidget() {
        val intent = Intent(Constants.ACTION_UPDATE_ALL)
        context.sendBroadcast(intent)
    }

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

    fun toggleFavorite() {
        currentSentenceId ?: return

        viewModelScope.launch {
            when (val currentState = _uiState.value) {
                is MainUiState.Success -> {
                    val sentence = currentState.sentence
                    val newFavoriteState = !sentence.isFavorite

                    repository.updateFavorite(sentence.id, newFavoriteState)

                    JinrishiciClient.updateWidgetFavorite(context, newFavoriteState)
                }

                is MainUiState.WidgetSentence -> {
                    val widgetSentence = currentState
                    val newFavoriteState = !widgetSentence.isFavorite

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

                    _uiState.value = MainUiState.Success(sentence = entity.copy())
                    currentSentenceId = entity.id
                    startSentenceListener(entity.id)

                    JinrishiciClient.updateWidgetFavorite(context, newFavoriteState)
                }

                else -> {}
            }
        }
    }

    fun showDetail(sentence: SentenceEntity) {
        val intent = Intent(context, DetailActivity::class.java).apply {
            putExtra(Constants.EXTRA_ID, sentence.id)
            putExtra(Constants.EXTRA_CONTENT, sentence.content)
            putExtra(Constants.EXTRA_ORIGIN_CONTENT, sentence.originContent)
            putExtra(Constants.EXTRA_TRANSLATE, sentence.translate)
            putExtra(Constants.EXTRA_TITLE, sentence.title)
            putExtra(Constants.EXTRA_DYNASTY, sentence.dynasty)
            putExtra(Constants.EXTRA_AUTHOR, sentence.author)
            putExtra(Constants.EXTRA_IS_FAVORITE, sentence.isFavorite)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun showWidgetDetail(widgetSentence: MainUiState.WidgetSentence) {
        val intent = Intent(context, DetailActivity::class.java).apply {
            putExtra(Constants.EXTRA_ID, widgetSentence.id)
            putExtra(Constants.EXTRA_CONTENT, widgetSentence.content)
            putExtra(Constants.EXTRA_ORIGIN_CONTENT, widgetSentence.originContent)
            putExtra(Constants.EXTRA_TRANSLATE, "")
            putExtra(Constants.EXTRA_TITLE, widgetSentence.title)
            putExtra(Constants.EXTRA_DYNASTY, widgetSentence.dynasty)
            putExtra(Constants.EXTRA_AUTHOR, widgetSentence.author)
            putExtra(Constants.EXTRA_IS_FAVORITE, widgetSentence.isFavorite)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(intent)
    }

    fun refresh() {
        if (isRefreshing) return
        isRefreshing = true
        loadFromApi()
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

sealed class MainUiState {
    data object Loading : MainUiState()
    data class Success(val sentence: SentenceEntity) : MainUiState()
    data class Error(val message: String) : MainUiState()

    data class WidgetSentence(
        val id: String = "",
        val content: String,
        val originContentList: List<String> = emptyList(),
        val translateList: List<String> = emptyList(),
        val title: String,
        val dynasty: String,
        val author: String,
        val isFavorite: Boolean = false
    ) : MainUiState() {
        val originContent: String
            get() = originContentList.joinToString(Constants.NEWLINE)
    }
}
