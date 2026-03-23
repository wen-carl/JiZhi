package com.jizhi.ui.main

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.jizhi.Constants
import com.jizhi.LocaleManager
import com.jizhi.ui.history.HistoryActivity
import com.jizhi.ui.setting.SettingActivity
import com.jizhi.ui.theme.JiZhiTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale

/**
 * 主活动 - 显示今日推荐诗词
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        // 应用保存的语言设置
        val locale = LocaleManager.getSavedLocale(newBase)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 检查是否从小组件进入
        val fromWidget = intent.getBooleanExtra(Constants.EXTRA_FROM_WIDGET, false)

        if (fromWidget) {
            handleWidgetEntry()
        } else {
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
