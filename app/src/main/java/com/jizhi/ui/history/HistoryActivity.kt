package com.jizhi.ui.history

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * 历史记录 Activity
 * 使用纯 Compose 界面
 */
@AndroidEntryPoint
class HistoryActivity : ComponentActivity() {

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
