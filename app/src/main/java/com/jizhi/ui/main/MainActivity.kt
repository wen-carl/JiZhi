package com.jizhi.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.jizhi.ui.history.HistoryActivity
import com.jizhi.ui.setting.SettingActivity
import com.jizhi.ui.theme.JiZhiTheme
import com.jizhi.widget.SentenceWidgetProvider
import dagger.hilt.android.AndroidEntryPoint

/**
 * 主活动 - 显示今日推荐诗词
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 检查是否从小组件进入
        val fromWidget = intent.getBooleanExtra(SentenceWidgetProvider.EXTRA_FROM_WIDGET, false)

        if (fromWidget) {
            // 从小组件进入
            handleWidgetEntry()
        } else {
            // 正常进入
            showNormalEntry()
        }
    }

    /**
     * 处理从小组件进入的情况
     */
    private fun handleWidgetEntry() {
        showNormalEntry(fromWidget = true)
    }

    /**
     * 显示主页面
     */
    private fun showNormalEntry(fromWidget: Boolean = false) {
        setContent {
            JiZhiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        viewModel = hiltViewModel(),
                        onHistoryClick = {
                            startActivity(Intent(this, HistoryActivity::class.java))
                        },
                        onSettingsClick = {
                            startActivity(Intent(this, SettingActivity::class.java))
                        },
                        fromWidget = fromWidget
                    )
                }
            }
        }
    }
}
