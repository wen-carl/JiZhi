package com.jizhi.ui.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jizhi.R
import com.jizhi.data.PoemFormatter
import com.jizhi.data.WidgetPreferences
import com.jizhi.data.local.SentenceEntity
import kotlinx.coroutines.async

/**
 * 主页面内容
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    fromWidget: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 根据入口类型加载数据
    LaunchedEffect(fromWidget) {
        viewModel.loadTodaySentence(fromWidget = fromWidget)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_bar_title)) },
                actions = {
                    val isFavorite = when (uiState) {
                        is MainUiState.Success -> (uiState as MainUiState.Success).sentence.isFavorite
                        is MainUiState.WidgetSentence -> (uiState as MainUiState.WidgetSentence).isFavorite
                        else -> false
                    }
                    val canToggleFavorite = uiState is MainUiState.Success

                    val heartColor by animateColorAsState(
                        targetValue = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "heartColor"
                    )

                    val heartScale by animateFloatAsState(
                        targetValue = if (isFavorite) 1.2f else 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "heartScale"
                    )

                    IconButton(
                        onClick = { viewModel.toggleFavorite() },
                        enabled = canToggleFavorite
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = stringResource(R.string.content_description_favorite),
                            tint = if (canToggleFavorite) heartColor else heartColor.copy(alpha = 0.5f),
                            modifier = Modifier.scale(heartScale)
                        )
                    }

                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.content_description_settings),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onHistoryClick) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = stringResource(R.string.content_description_history),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (val state = uiState) {
                is MainUiState.Loading -> {
                    LoadingContent()
                }

                is MainUiState.Success -> {
                    SuccessContent(
                        sentence = state.sentence,
                        onRefresh = { viewModel.refresh() },
                        onSentenceClick = { viewModel.showDetail(state.sentence) }
                    )
                }

                is MainUiState.WidgetSentence -> {
                    WidgetSentenceContent(
                        widgetSentence = state,
                        onRefresh = { viewModel.refresh() },
                        onSentenceClick = { viewModel.showWidgetDetail(state) }
                    )
                }

                is MainUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.refresh() }
                    )
                }

                else -> {
                    LoadingContent()
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.loading_message))
    }
}

@Composable
private fun SuccessContent(
    sentence: SentenceEntity,
    onRefresh: () -> Unit,
    onSentenceClick: () -> Unit
) {
    // 获取换行模式并格式化内容
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var lineBreakMode by remember { mutableStateOf<com.jizhi.data.LineBreakMode?>(null) }
    LaunchedEffect(Unit) {
        lineBreakMode = scope.async {
            WidgetPreferences(context).getLineBreakMode()
        }.await()
    }
    val formattedContent = remember(sentence.content, lineBreakMode) {
        lineBreakMode?.let { mode ->
            PoemFormatter.format(sentence.content, mode)
        } ?: sentence.content
    }
    val lines = remember(formattedContent) { PoemFormatter.splitLines(formattedContent) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 诗词卡片（可点击进入详情）
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSentenceClick),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 诗词内容 - 主内容，大字体
                if (lines.size > 1) {
                    // 多行显示
                    lines.forEach { line ->
                        Text(
                            text = line,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 48.sp,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Text(
                        text = formattedContent,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 48.sp,
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 标题信息 - 副内容，小字体，浅色
                Text(
                    text = sentence.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.format_dynasty_author, sentence.dynasty, sentence.author),
                    fontSize = 13.sp,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRefresh,
            modifier = Modifier
                .height(52.dp)
                .widthIn(min = 120.dp),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text(stringResource(R.string.refresh_button))
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text(stringResource(R.string.retry_button))
        }
    }
}

/**
 * 小组件数据内容组件（无数据库记录时直接显示）
 */
@Composable
fun WidgetSentenceContent(
    widgetSentence: MainUiState.WidgetSentence,
    onRefresh: () -> Unit,
    onSentenceClick: () -> Unit
) {
    // 获取换行模式并格式化内容
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()
    var lineBreakMode by remember { mutableStateOf<com.jizhi.data.LineBreakMode?>(null) }
    LaunchedEffect(Unit) {
        lineBreakMode = scope.async {
            WidgetPreferences(context).getLineBreakMode()
        }.await()
    }
    val formattedContent = remember(widgetSentence.content, lineBreakMode) {
        lineBreakMode?.let { mode ->
            PoemFormatter.format(widgetSentence.content, mode)
        } ?: widgetSentence.content
    }
    val lines = remember(formattedContent) { PoemFormatter.splitLines(formattedContent) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 小组件数据卡片（可点击进入详情）
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSentenceClick),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (lines.size > 1) {
                    // 多行显示
                    lines.forEach { line ->
                        Text(
                            text = line,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            lineHeight = 48.sp,
                            fontFamily = FontFamily.Serif,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                } else {
                    Text(
                        text = formattedContent,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        lineHeight = 48.sp,
                        fontFamily = FontFamily.Serif,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider(
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = widgetSentence.title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.format_dynasty_author, widgetSentence.dynasty, widgetSentence.author),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRefresh,
            modifier = Modifier
                .height(52.dp)
                .widthIn(min = 120.dp),
            shape = RoundedCornerShape(26.dp)
        ) {
            Text(stringResource(R.string.refresh_button))
        }
    }
}
