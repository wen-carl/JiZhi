package com.jizhi.ui.setting

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.jizhi.Constants
import com.jizhi.LanguageConstants
import com.jizhi.LocaleManager
import com.jizhi.R
import com.jizhi.data.BackgroundColorOption
import com.jizhi.data.FontOption
import com.jizhi.data.LineBreakMode
import com.jizhi.data.LineBreakModeOptions
import com.jizhi.data.UpdateIntervalOptions
import com.jizhi.data.WidgetColors
import com.jizhi.data.WidgetPreferences
import com.jizhi.ui.main.MainActivity
import com.jizhi.ui.theme.JiZhiTheme
import com.jizhi.widget.SentenceWidgetProvider
import com.jizhi.worker.SentenceUpdateWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * 设置页面
 * 配置小组件更新周期、换行模式和背景色
 */
@AndroidEntryPoint
class SettingActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val locale = LocaleManager.getSavedLocale(newBase)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            JiZhiTheme {
                SettingScreen(
                    onBackClick = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val widgetPreferences = remember { WidgetPreferences(context) }
    val scope = rememberCoroutineScope()

    var selectedInterval by remember { mutableFloatStateOf(1f) }
    var selectedLineBreakMode by remember { mutableIntStateOf(0) }
    var backgroundColor by remember { mutableIntStateOf(0) }
    var selectedColorName by remember { mutableStateOf("透明") }
    var textColor by remember { mutableIntStateOf(0xFFFFFFFF.toInt()) }
    var selectedTextColorName by remember { mutableStateOf("纯白") }
    var selectedFontId by remember { mutableStateOf("serif") }
    var selectedFontName by remember { mutableStateOf("衬线体") }
    var fontOptions by remember { mutableStateOf<List<FontOption>>(emptyList()) }

    // 初始化加载数据
    LaunchedEffect(Unit) {
        selectedInterval = widgetPreferences.getUpdateInterval(0)
        selectedLineBreakMode = widgetPreferences.getLineBreakMode().value
        backgroundColor = widgetPreferences.getWidgetBackgroundColor()
        textColor = widgetPreferences.getWidgetTextColor()
        fontOptions = widgetPreferences.getAllOptions()
        selectedFontId = widgetPreferences.getSelectedFontId()
        selectedFontName = getFontDisplayName(selectedFontId, fontOptions)
        selectedColorName = findColorName(backgroundColor, WidgetColors.BACKGROUND_COLORS)
        selectedTextColorName = findColorName(textColor, WidgetColors.TEXT_COLORS)
    }

    // BottomSheet 状态
    var showIntervalSheet by remember { mutableStateOf(false) }
    var showLineBreakSheet by remember { mutableStateOf(false) }
    var showColorSheet by remember { mutableStateOf(false) }
    var showTextColorSheet by remember { mutableStateOf(false) }
    var showCustomColorDialog by remember { mutableStateOf(false) }
    var showCustomTextColorDialog by remember { mutableStateOf(false) }
    var showFontSheet by remember { mutableStateOf(false) }
    var showAddFontDialog by remember { mutableStateOf(false) }

    // 文件选择器
    rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // 复制字体文件到应用私有目录
            copyFontToPrivateDir(context, it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.content_description_back_alt)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 语言设置
            item {
                LanguageSettingItem(
                    onLanguageSelected = { language ->
                        LocaleManager.setLocale(context, language)
                    }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // 小组件更新周期设置
            item {
                SettingItem(
                    title = stringResource(R.string.widget_update_interval),
                    value = getIntervalDisplayText(selectedInterval),
                    onClick = { showIntervalSheet = true }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // 换行模式设置
            item {
                SettingItem(
                    title = stringResource(R.string.line_break_poem_display),
                    value = LineBreakModeOptions.options.find { it.first == selectedLineBreakMode }?.second
                        ?: "",
                    onClick = { showLineBreakSheet = true }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // 字体设置
            item {
                SettingItemWithFontPreview(
                    title = stringResource(R.string.app_font),
                    value = selectedFontName,
                    onClick = { showFontSheet = true }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // 小组件背景色设置
            item {
                SettingItemWithPreview(
                    title = stringResource(R.string.widget_background_color),
                    value = selectedColorName,
                    color = backgroundColor,
                    onClick = { showColorSheet = true }
                )
            }

            item {
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            }

            // 小组件文本颜色设置
            item {
                SettingItemWithPreview(
                    title = stringResource(R.string.widget_text_color),
                    value = selectedTextColorName,
                    color = textColor,
                    onClick = { showTextColorSheet = true }
                )
            }
        }
    }

    // 更新周期选择 BottomSheet
    if (showIntervalSheet) {
        ModalBottomSheet(
            onDismissRequest = { showIntervalSheet = false }
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = stringResource(R.string.select_interval),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )

                UpdateIntervalOptions.options.forEachIndexed { index, (hours, displayText) ->
                    Column {
                        ListItem(
                            headlineContent = { Text(displayText) },
                            trailingContent = {
                                if (selectedInterval == hours) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = stringResource(R.string.widget_selected),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            modifier = Modifier.clickable {
                                selectedInterval = hours
                                scope.launch {
                                    widgetPreferences.saveUpdateInterval(0, hours)
                                }
                                SentenceUpdateWorker.scheduleUpdate(context, hours)
                                showIntervalSheet = false
                            }
                        )
                        if (index < UpdateIntervalOptions.options.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }
        }
    }

    // 换行模式选择 BottomSheet
    if (showLineBreakSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLineBreakSheet = false }
        ) {
            Column(modifier = Modifier.padding(bottom = 32.dp)) {
                Text(
                    text = stringResource(R.string.select_line_break),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
                )

                LineBreakModeOptions.options.forEachIndexed { index, (value, displayName) ->
                    Column {
                        ListItem(
                            headlineContent = { Text(displayName) },
                            supportingContent = {
                                Text(
                                    text = LineBreakModeOptions.descriptions[value] ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            trailingContent = {
                                if (selectedLineBreakMode == value) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = stringResource(R.string.widget_selected),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            },
                            modifier = Modifier.clickable {
                                selectedLineBreakMode = value
                                scope.launch {
                                    widgetPreferences.saveLineBreakMode(
                                        LineBreakMode.fromValue(
                                            value
                                        )
                                    )
                                }
                                showLineBreakSheet = false
                            }
                        )
                        if (index < LineBreakModeOptions.options.lastIndex) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }
        }
    }

    // 背景色选择 BottomSheet
    if (showColorSheet) {
        ColorPickerBottomSheet(
            currentColor = backgroundColor,
            currentColorName = selectedColorName,
            onColorSelected = { color, name ->
                backgroundColor = color
                selectedColorName = name
                scope.launch {
                    widgetPreferences.saveWidgetBackgroundColor(color)
                }
                // 发送广播刷新小组件
                val intent = Intent(Constants.ACTION_UPDATE_ALL)
                context.sendBroadcast(intent)
                showColorSheet = false
            },
            onCustomClick = { showCustomColorDialog = true },
            onDismiss = { showColorSheet = false },
            colorOptions = WidgetColors.BACKGROUND_COLORS,
            title = stringResource(R.string.select_background_color)
        )
    }

    // 自定义颜色对话框
    if (showCustomColorDialog) {
        CustomColorDialog(
            currentColor = backgroundColor,
            onDismiss = { showCustomColorDialog = false },
            onConfirm = { color, name ->
                backgroundColor = color
                selectedColorName = name
                scope.launch {
                    widgetPreferences.saveWidgetBackgroundColor(color)
                }
                // 发送广播刷新小组件
                sendWidgetUpdateBroadcast(context)
                showCustomColorDialog = false
            }
        )
    }

    // 文本颜色选择 BottomSheet
    if (showTextColorSheet) {
        ColorPickerBottomSheet(
            currentColor = textColor,
            currentColorName = selectedTextColorName,
            onColorSelected = { color, name ->
                textColor = color
                selectedTextColorName = name
                scope.launch {
                    widgetPreferences.saveWidgetTextColor(color)
                }
                // 发送广播刷新小组件
                sendWidgetUpdateBroadcast(context)
                showTextColorSheet = false
            },
            onCustomClick = { showCustomTextColorDialog = true },
            onDismiss = { showTextColorSheet = false },
            colorOptions = WidgetColors.TEXT_COLORS
        )
    }

    // 自定义文本颜色对话框
    if (showCustomTextColorDialog) {
        CustomColorDialog(
            currentColor = textColor,
            onDismiss = { showCustomTextColorDialog = false },
            onConfirm = { color, name ->
                textColor = color
                selectedTextColorName = name
                scope.launch {
                    widgetPreferences.saveWidgetTextColor(color)
                }
                // 发送广播刷新小组件
                sendWidgetUpdateBroadcast(context)
                showCustomTextColorDialog = false
            }
        )
    }

    // 字体选择 BottomSheet
    if (showFontSheet) {
        FontPickerBottomSheet(
            currentFontId = selectedFontId,
            currentFontName = selectedFontName,
            fontOptions = fontOptions,
            onFontSelected = { fontId, fontName ->
                selectedFontId = fontId
                selectedFontName = fontName
                scope.launch {
                    widgetPreferences.saveSelectedFontId(fontId)
                }
                showFontSheet = false
            },
            onAddFontClick = { showAddFontDialog = true },
            onDismiss = { showFontSheet = false }
        )
    }

    // 添加字体对话框
    if (showAddFontDialog) {
        AddFontDialog(
            onDismiss = { showAddFontDialog = false },
            onFontAdded = { fontPath ->
                scope.launch {
                    widgetPreferences.addCustomFontPath(fontPath)
                }
                showAddFontDialog = false
            }
        )
    }
}

/**
 * 发送小组件更新广播
 */
private fun sendWidgetUpdateBroadcast(context: android.content.Context) {
    val intent = Intent(context, SentenceWidgetProvider::class.java).apply {
        action = Constants.ACTION_UPDATE_ALL
    }
    context.sendBroadcast(intent)
}

@Composable
private fun SettingItem(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(value) },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun SettingItemWithPreview(
    title: String,
    value: String,
    color: Int,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(value) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.ui.graphics.Color(color))
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

/**
 * 颜色选择 BottomSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColorPickerBottomSheet(
    currentColor: Int,
    currentColorName: String,
    onColorSelected: (Int, String) -> Unit,
    onCustomClick: () -> Unit,
    onDismiss: () -> Unit,
    colorOptions: List<BackgroundColorOption> = WidgetColors.BACKGROUND_COLORS,
    title: String = stringResource(R.string.select_color)
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 预设颜色网格
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val chunks = colorOptions.chunked(4)
                items(chunks) { rowColors ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowColors.forEach { option ->
                            ColorOptionItem(
                                option = option,
                                isSelected = currentColor == option.color,
                                onClick = { onColorSelected(option.color, option.name) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        // 填充空白
                        repeat(4 - rowColors.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 自定义选项
            ListItem(
                headlineContent = { Text(stringResource(R.string.custom_color)) },
                supportingContent = { Text("输入ARGB格式颜色值") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingContent = {
                    if (currentColorName == "自定义") {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.widget_selected),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                modifier = Modifier.clickable(onClick = onCustomClick)
            )
        }
    }
}

@Composable
private fun ColorOptionItem(
    option: BackgroundColorOption,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(androidx.compose.ui.graphics.Color(option.color))
                .then(
                    if (isSelected) {
                        Modifier.border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    } else {
                        Modifier.border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                    }
                )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = option.name,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 自定义颜色对话框
 */
@Composable
private fun CustomColorDialog(
    currentColor: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var colorText by remember { mutableStateOf(String.format("#%08X", currentColor)) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var previewColor by remember { mutableStateOf(currentColor) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("自定义背景色") },
        text = {
            Column {
                Text(
                    text = "输入ARGB格式颜色值（如 #80000000）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = colorText,
                    onValueChange = { newValue ->
                        colorText = newValue
                        val parsed = parseArgbColor(newValue)
                        if (parsed != null) {
                            previewColor = parsed
                            errorMessage = null
                        } else {
                            errorMessage = "格式无效，请输入 #AARRGGBB 或 RRGGBB 格式"
                        }
                    },
                    label = { Text("颜色值") },
                    isError = errorMessage != null,
                    supportingText = errorMessage?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 颜色预览
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.preview_label),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(androidx.compose.ui.graphics.Color(previewColor))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                MaterialTheme.shapes.small
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = String.format("#%08X", previewColor),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val parsed = parseArgbColor(colorText)
                    if (parsed != null) {
                        onConfirm(parsed, "自定义")
                    }
                },
                enabled = errorMessage == null && parseArgbColor(colorText) != null
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 解析ARGB颜色字符串为Int
 */
private fun parseArgbColor(colorStr: String): Int? {
    return try {
        var str = colorStr.trim()
        if (str.startsWith("#")) {
            str = str.substring(1)
        }
        if (str.length == 6) {
            // 添加透明度（完全不透明）
            str = "FF$str"
        }
        if (str.length == 8) {
            java.lang.Long.parseLong(str, 16).toInt()
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * 字体预览设置项
 */
@Composable
private fun SettingItemWithFontPreview(
    title: String,
    value: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        supportingContent = { Text(value) },
        trailingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

/**
 * 字体选择 BottomSheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FontPickerBottomSheet(
    currentFontId: String,
    currentFontName: String,
    fontOptions: List<FontOption>,
    onFontSelected: (String, String) -> Unit,
    onAddFontClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.select_font),
                style = MaterialTheme.typography.titleLarge
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 字体选项列表
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(fontOptions) { fontOption ->
                    ListItem(
                        headlineContent = { Text(fontOption.name) },
                        supportingContent = {
                            if (fontOption.isCustom) {
                                Text(
                                    text = fontOption.fontFamily,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        leadingContent = {
                            if (fontOption.isCustom) {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        trailingContent = {
                            if (currentFontId == fontOption.id) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = stringResource(R.string.widget_selected),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        modifier = Modifier.clickable {
                            onFontSelected(fontOption.id, fontOption.name)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 添加自定义字体
            ListItem(
                headlineContent = { Text(stringResource(R.string.add_custom_font)) },
                supportingContent = { Text("从文件选择 .ttf 或 .otf 字体文件") },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                modifier = Modifier.clickable(onClick = onAddFontClick)
            )
        }
    }
}

/**
 * 添加字体对话框
 */
@Composable
private fun AddFontDialog(
    onDismiss: () -> Unit,
    onFontAdded: (String) -> Unit
) {
    val context = LocalContext.current
    val fontFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            // 复制字体文件到应用私有目录
            val fontPath = copyFontToPrivateDir(context, it)
            if (fontPath != null) {
                onFontAdded(fontPath)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加自定义字体") },
        text = {
            Column {
                Text(
                    text = "选择字体文件（.ttf 或 .otf 格式）",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "提示：自定义字体会应用到整个应用，包括首页、详情页、历史记录和小组件。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    fontFilePicker.launch("font/*")
                }
            ) {
                Text("选择文件")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 获取字体显示名称
 */
private fun getFontDisplayName(fontId: String, fontOptions: List<FontOption>): String {
    return fontOptions.find { it.id == fontId }?.name ?: "衬线体"
}

/**
 * 复制字体文件到应用私有目录
 */
private fun copyFontToPrivateDir(context: android.content.Context, uri: Uri): String? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        val fileName = "font_${System.currentTimeMillis()}.ttf"
        val fontFile = context.getFileStreamPath(fileName)

        fontFile.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        inputStream.close()

        fontFile.absolutePath
    } catch (e: Exception) {
        null
    }
}

/**
 * 获取颜色名称
 */
private fun findColorName(color: Int, colors: List<BackgroundColorOption>): String {
    return colors.find { it.color == color }?.name ?: "透明"
}

/**
 * 获取更新频率显示文本
 */
private fun getIntervalDisplayText(intervalHours: Float): String {
    return when (intervalHours) {
        0.25f -> "15分钟"
        0.5f -> "30分钟"
        1f -> "1小时"
        2f -> "2小时"
        3f -> "3小时"
        6f -> "6小时"
        8f -> "8小时"
        24f -> "1天"
        -1f -> "永不自动更新"
        else -> "1小时"
    }
}

/**
 * 语言设置项
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSettingItem(
    onLanguageSelected: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as? android.app.Activity
    var showLanguageSheet by remember { mutableStateOf(false) }
    val locale by LocaleManager.currentLocale.collectAsState()

    val currentLanguage = when (locale.language) {
        "zh" -> "chinese"
        "en" -> "english"
        else -> "system"
    }

    ListItem(
        headlineContent = { Text(stringResource(R.string.language_setting)) },
        supportingContent = {
            Text(
                when (currentLanguage) {
                    "chinese" -> stringResource(R.string.language_chinese)
                    "english" -> stringResource(R.string.language_english)
                    else -> stringResource(R.string.language_system)
                }
            )
        },
        trailingContent = {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        modifier = Modifier.clickable { showLanguageSheet = true }
    )

    // 语言选择 BottomSheet
    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false }
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Text(
                    text = stringResource(R.string.language_select),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // 跟随系统
                LanguageOptionItem(
                    title = stringResource(R.string.language_system),
                    isSelected = currentLanguage == "system",
                    onClick = {
                        LocaleManager.setLocale(context, LanguageConstants.Language.SYSTEM.value)
                        showLanguageSheet = false
                        onLanguageSelected(LanguageConstants.Language.SYSTEM.value)
                        activity?.recreate()
                    }
                )

                // 中文
                LanguageOptionItem(
                    title = stringResource(R.string.language_chinese),
                    isSelected = currentLanguage == "chinese",
                    onClick = {
                        LocaleManager.setLocale(context, LanguageConstants.Language.CHINESE.value)
                        showLanguageSheet = false
                        onLanguageSelected(LanguageConstants.Language.CHINESE.value)
                        activity?.recreate()
                    }
                )

                // 英文
                LanguageOptionItem(
                    title = stringResource(R.string.language_english),
                    isSelected = currentLanguage == "english",
                    onClick = {
                        LocaleManager.setLocale(context, LanguageConstants.Language.ENGLISH.value)
                        showLanguageSheet = false
                        onLanguageSelected(LanguageConstants.Language.ENGLISH.value)
                        activity?.recreate()
                    }
                )
            }
        }
    }
}

/**
 * 语言选项项
 */
@Composable
private fun LanguageOptionItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        trailingContent = {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}
