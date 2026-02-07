package com.jizhi.ui.detail

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.jizhi.Constants
import com.jizhi.R
import com.jizhi.data.LineBreakMode
import com.jizhi.data.PoemType
import com.jizhi.data.WidgetPreferences
import com.jizhi.data.detectPoemType
import com.jizhi.data.local.SentenceEntity
import com.jizhi.ui.theme.JiZhiTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * 诗词详情页面
 * 优先从数据库加载，如果不存在则显示传递的数据
 */
@AndroidEntryPoint
class DetailActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            JiZhiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DetailScreen(
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DetailScreen(
    onBackClick: () -> Unit,
    viewModel: DetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 初始化加载
    LaunchedEffect(Unit) {
        val sentenceId = activity?.intent?.getStringExtra(Constants.EXTRA_ID) ?: ""
        val intentData = if (activity?.intent?.hasExtra(Constants.EXTRA_CONTENT) == true) {
            val originContentStr =
                activity.intent.getStringExtra(Constants.EXTRA_ORIGIN_CONTENT) ?: ""
            val originContentList = if (originContentStr.isNotEmpty()) {
                originContentStr.split(Constants.NEWLINE).filter { it.isNotBlank() }
            } else emptyList()

            val translateStr = activity.intent.getStringExtra(Constants.EXTRA_TRANSLATE) ?: ""
            val translateList = if (translateStr.isNotEmpty()) {
                translateStr.split(Constants.NEWLINE).filter { it.isNotBlank() }
            } else emptyList()

            IntentDataHolder(
                content = activity.intent.getStringExtra(Constants.EXTRA_CONTENT) ?: "",
                originContentList = originContentList,
                translateList = translateList,
                title = activity.intent.getStringExtra(Constants.EXTRA_TITLE) ?: "",
                dynasty = activity.intent.getStringExtra(Constants.EXTRA_DYNASTY) ?: "",
                author = activity.intent.getStringExtra(Constants.EXTRA_AUTHOR) ?: "",
                isFavorite = activity.intent.getBooleanExtra(
                    Constants.EXTRA_IS_FAVORITE,
                    false
                )
            )
        } else null

        viewModel.loadSentence(sentenceId, intentData)
    }

    val successState = uiState as? DetailUiState.Success
    val sentence = successState?.sentence
    val canToggleFavorite = successState?.canToggleFavorite ?: false

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("诗词详情") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    // 数据库中没有该记录时，不显示喜欢按钮
                    if (canToggleFavorite) {
                        sentence?.let { s ->
                            val heartColor by animateColorAsState(
                                targetValue = if (s.isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                                label = "heartColor"
                            )
                            IconButton(onClick = { viewModel.toggleFavorite() }) {
                                Icon(
                                    imageVector = if (s.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = if (s.isFavorite) "取消喜欢" else "喜欢",
                                    tint = heartColor
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is DetailUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DetailUiState.Success -> {
                sentence?.let { s ->
                    DetailContent(
                        sentence = s,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }

            is DetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (uiState as DetailUiState.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetailContent(
    sentence: SentenceEntity,
    modifier: Modifier = Modifier
) {
    // 获取换行模式
    val context = LocalContext.current
    var lineBreakMode by remember { mutableStateOf<com.jizhi.data.LineBreakMode?>(null) }
    LaunchedEffect(Unit) {
        lineBreakMode = WidgetPreferences(context).getLineBreakMode()
    }

    // 判断诗词类型
    val poemType = remember(sentence.title, sentence.content) {
        detectPoemType(sentence.title, sentence.content)
    }

    // 名句（用于高亮）
    val highlightedContent = remember(sentence.content) {
        sentence.content
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "《${sentence.title}》",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Serif,
                    lineHeight = 36.sp  // 标题换行行距
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "【${sentence.dynasty}】${sentence.author}",
                    fontSize = 16.sp,
                    fontFamily = FontFamily.Serif,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                HorizontalDivider()

                Spacer(modifier = Modifier.height(16.dp))

                // 根据诗词类型显示内容
                when (poemType) {
                    PoemType.POEM -> {
                        // 诗：居中对齐，使用原文列表，为空时用 content
                        val displayLines = if (sentence.originContentList.isNotEmpty()) {
                            sentence.originContentList
                        } else {
                            // 如果没有原文，用 content 模拟一行
                            listOf(sentence.content)
                        }
                        if (lineBreakMode != null) {
                            PoemContent(
                                originLines = displayLines,
                                highlightedContent = highlightedContent,
                                lineBreakMode = lineBreakMode!!
                            )
                        }
                    }

                    PoemType.CI -> {
                        // 词：左对齐，为空时用 content
                        val displayLines = if (sentence.originContentList.isNotEmpty()) {
                            sentence.originContentList
                        } else {
                            listOf(sentence.content)
                        }
                        if (lineBreakMode != null) {
                            CiContent(
                                originLines = displayLines,
                                highlightedContent = highlightedContent,
                                lineBreakMode = lineBreakMode!!
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 翻译卡片
        if (sentence.hasTranslate) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "翻译",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    sentence.translateList.forEach { translateLine ->
                        Text(
                            text = translateLine,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.9f),
                            lineHeight = 30.sp
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        // 推荐理由
        if (sentence.recommendedReason.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "推荐理由",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = sentence.recommendedReason,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // 标签
        if (sentence.matchTags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "标签",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sentence.matchTags.split(",").forEach { tag ->
                            val trimmedTag = tag.trim()
                            if (trimmedTag.isNotEmpty()) {
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(trimmedTag) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * 诗的内容显示组件
 * 居中对齐，每句一行。单行能显示全就单行，不能就在标点符号处折行。
 * 使用 replace 逻辑加粗名句。
 */
@Composable
private fun PoemContent(
    originLines: List<String>,
    highlightedContent: String,
    lineBreakMode: LineBreakMode,
    modifier: Modifier = Modifier
) {
    // 清理名句（去除标点）
    val cleanHighlight = highlightedContent.replace("[，。！？、]".toRegex(), "").trim()

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        originLines.forEach { line ->
            // 根据换行模式处理
            val displayText = when (lineBreakMode) {
                LineBreakMode.DEFAULT -> line
                LineBreakMode.AUTO_PUNCTUATION -> {
                    if (line.length <= 20) {
                        line.replace("。", "。\n")
                            .replace("，", "，\n")
                            .replace("！", "！\n")
                            .replace("？", "？\n")
                            .trim()
                    } else {
                        line
                    }
                }

                LineBreakMode.FORCE_PUNCTUATION -> {
                    val result = StringBuilder()
                    line.forEachIndexed { index, char ->
                        result.append(char)
                        if (char == '，' || (char == '。' && index < line.length - 1)) {
                            result.append('\n')
                        }
                    }
                    result.toString().trim()
                }
            }

            // 按换行符分割显示
            displayText.split("\n").forEach { segment ->
                if (segment.isNotBlank()) {
                    Text(
                        text = buildAnnotatedStringWithHighlight(segment, cleanHighlight),
                        fontSize = 24.sp,
                        fontFamily = FontFamily.Serif,
                        textAlign = TextAlign.Center,
                        lineHeight = 36.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

/**
 * 词的内容显示组件
 * 左对齐，在标点符号处折行。使用 replace 逻辑加粗名句。
 */
@Composable
private fun CiContent(
    originLines: List<String>,
    highlightedContent: String,
    lineBreakMode: LineBreakMode,
    modifier: Modifier = Modifier
) {
    // 清理名句（去除标点）
    val cleanHighlight = highlightedContent.replace("[，。！？、]".toRegex(), "").trim()

    Column(modifier = modifier.fillMaxWidth()) {
        originLines.forEach { line ->
            // 按标点分割（保留标点）
            val segments = mutableListOf<String>()
            val sb = StringBuilder()
            for (char in line) {
                sb.append(char)
                if (char in "，。！？、") {
                    segments.add(sb.toString().trim())
                    sb.clear()
                }
            }
            if (sb.isNotEmpty()) {
                segments.add(sb.toString().trim())
            }

            // 显示每句
            segments.forEach { segment ->
                Text(
                    text = buildAnnotatedStringWithHighlight(segment, cleanHighlight),
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Serif,
                    lineHeight = 36.sp
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

/**
 * 使用 replace 逻辑构建带高亮的 AnnotatedString
 * 将匹配到的名句加粗
 */
@Composable
private fun buildAnnotatedStringWithHighlight(
    text: String,
    highlight: String
): AnnotatedString {
    return if (highlight.isNotEmpty()) {
        val cleanText = text.replace("[，。！？、]".toRegex(), "").trim()
        val isMatch = cleanText.contains(highlight) ||
                highlight.contains(cleanText) ||
                (highlight.length >= 4 && cleanText.contains(highlight.take(4)))

        if (isMatch) {
            AnnotatedString.Builder(text).apply {
                addStyle(SpanStyle(fontWeight = FontWeight.Bold), 0, text.length)
            }.toAnnotatedString()
        } else {
            AnnotatedString(text)
        }
    } else {
        AnnotatedString(text)
    }
}
