package com.jizhi.ui.history

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import com.jizhi.LocaleManager
import dagger.hilt.android.AndroidEntryPoint

/**
 * 历史记录 Activity
 * 使用纯 Compose 界面
 */
@AndroidEntryPoint
class HistoryActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val locale = LocaleManager.getSavedLocale(newBase)
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(locale)
        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HistoryScreen(
                viewModel = hiltViewModel(),
                onBackClick = { finish() }
            )
        }
    }
}
