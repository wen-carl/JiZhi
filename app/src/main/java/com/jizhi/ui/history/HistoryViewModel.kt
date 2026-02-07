package com.jizhi.ui.history

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jizhi.Constants
import com.jizhi.data.local.SentenceEntity
import com.jizhi.data.remote.JinrishiciClient
import com.jizhi.repository.SentenceRepository
import com.jizhi.widget.SentenceWidgetProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 历史记录页面ViewModel
 */
@HiltViewModel
class HistoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: SentenceRepository
) : ViewModel() {

    // 句子列表状态
    private val _sentences = MutableStateFlow<List<SentenceEntity>>(emptyList())
    val sentences: StateFlow<List<SentenceEntity>> = _sentences.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 错误信息
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // 句子总数
    private val _totalCount = MutableStateFlow(0)
    val totalCount: StateFlow<Int> = _totalCount.asStateFlow()

    init {
        // 加载历史记录
        loadHistory()
        // 获取总数
        loadCount()
    }

    /**
     * 加载历史记录
     */
    fun loadHistory() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.getAllSentences().collectLatest { list ->
                    _sentences.value = list
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = "加载历史记录失败：${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * 获取句子总数
     */
    private fun loadCount() {
        viewModelScope.launch {
            try {
                val count = repository.getSentenceCount()
                _totalCount.value = count
            } catch (e: Exception) {
                // 忽略计数错误
            }
        }
    }

    /**
     * 删除指定句子
     */
    fun deleteSentence(id: String) {
        viewModelScope.launch {
            try {
                repository.deleteSentence(id)
                loadCount()
            } catch (e: Exception) {
                _error.value = "删除失败：${e.message}"
            }
        }
    }

    /**
     * 清空所有历史记录
     */
    fun clearAllHistory() {
        viewModelScope.launch {
            try {
                repository.clearAllHistory()
                _sentences.value = emptyList()
                _totalCount.value = 0
            } catch (e: Exception) {
                _error.value = "清空失败：${e.message}"
            }
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * 切换喜欢状态
     */
    fun toggleFavorite(sentence: SentenceEntity) {
        viewModelScope.launch {
            try {
                repository.updateFavorite(sentence.id, !sentence.isFavorite)
                // 重新加载数据，确保所有页面同步显示最新的喜欢状态
                loadHistory()
            } catch (e: Exception) {
                _error.value = "操作失败：${e.message}"
            }
        }
    }

    /**
     * 将指定句子更新到小组件
     */
    fun updateWidget(sentence: SentenceEntity) {
        viewModelScope.launch {
            JinrishiciClient.saveTodaySentenceForWidget(
                context = context,
                id = sentence.id,
                content = sentence.content,
                originContentList = sentence.originContentList,
                translateList = sentence.translateList,
                title = sentence.title,
                dynasty = sentence.dynasty,
                author = sentence.author,
                isFavorite = sentence.isFavorite
            )
            val intent = Intent(context, SentenceWidgetProvider::class.java).apply {
                action = Constants.ACTION_UPDATE_ALL
            }
            context.sendBroadcast(intent)
        }
    }
}
