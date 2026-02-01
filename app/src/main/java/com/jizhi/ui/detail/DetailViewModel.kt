package com.jizhi.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jizhi.data.local.SentenceEntity
import com.jizhi.repository.SentenceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 详情页面 ViewModel
 * 优先从数据库加载，如果不存在则显示传递的数据
 */
@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: SentenceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var currentSentenceId: String? = null

    /**
     * 根据 ID 加载句子
     * 优先从数据库加载，不存在时显示 Intent 传递的数据
     */
    fun loadSentence(sentenceId: String, intentData: IntentDataHolder? = null) {
        if (sentenceId.isEmpty()) {
            // 没有 ID，直接显示 Intent 数据
            intentData?.let {
                _uiState.value = DetailUiState.Success(
                    sentence = it.toSentenceEntity(),
                    canToggleFavorite = false
                )
            } ?: run {
                _uiState.value = DetailUiState.Error("无效的诗句 ID")
            }
            return
        }

        currentSentenceId = sentenceId

        viewModelScope.launch {
            _uiState.value = DetailUiState.Loading

            // 尝试从数据库加载
            val sentence = repository.getSentenceById(sentenceId)
            if (sentence != null) {
                // 数据库中有记录，正常显示
                _uiState.value = DetailUiState.Success(
                    sentence = sentence,
                    canToggleFavorite = true
                )
            } else {
                // 数据库中没有记录，显示 Intent 传递的数据
                intentData?.let {
                    _uiState.value = DetailUiState.Success(
                        sentence = it.toSentenceEntity(),
                        canToggleFavorite = false
                    )
                } ?: run {
                    _uiState.value = DetailUiState.Error("未找到该诗句")
                }
            }
        }
    }

    /**
     * 切换喜欢状态
     */
    fun toggleFavorite() {
        currentSentenceId ?: return

        viewModelScope.launch {
            try {
                val currentState = _uiState.value
                if (currentState is DetailUiState.Success) {
                    val sentence = currentState.sentence
                    repository.updateFavorite(sentence.id, !sentence.isFavorite)
                }
            } catch (e: Exception) {
                _uiState.value = DetailUiState.Error(e.message ?: "操作失败")
            }
        }
    }

}

/**
 * Intent 传递的数据持有者
 */
data class IntentDataHolder(
    val content: String,
    val originContentList: List<String> = emptyList(),
    val translateList: List<String> = emptyList(),
    val title: String,
    val dynasty: String,
    val author: String,
    val isFavorite: Boolean = false
) {
    fun toSentenceEntity(): SentenceEntity {
        return SentenceEntity(
            id = "intent_${content.hashCode()}",
            content = content,
            originContentList = originContentList,
            translateList = translateList,
            popularity = 0,
            title = title,
            dynasty = dynasty,
            author = author,
            matchTags = "",
            recommendedReason = "",
            cacheAt = "",
            isFavorite = isFavorite,
            isFromWidget = true
        )
    }
}

/**
 * 详情页面 UI 状态
 */
sealed class DetailUiState {
    data object Loading : DetailUiState()
    data class Success(
        val sentence: SentenceEntity,
        val canToggleFavorite: Boolean = true
    ) : DetailUiState()

    data class Error(val message: String) : DetailUiState()
}
