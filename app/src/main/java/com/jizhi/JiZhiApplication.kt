package com.jizhi

import android.app.Application
import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.jizhi.worker.SentenceUpdateWorker
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

/**
 * 应用 Application 类
 * 使用 Hilt 进行依赖注入
 * 配置 WorkManager
 */
@HiltAndroidApp
class JiZhiApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    /**
     * WorkManager 配置
     * 使用 HiltWorkerFactory 支持带注入的 Worker
     */
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    /**
     * 应用启动时安排定时任务
     */
    override fun onCreate() {
        super.onCreate()
        schedulePeriodicWork()
    }

    /**
     * 根据保存的配置安排定时任务
     */
    private fun schedulePeriodicWork() {
        val prefs = getSharedPreferences("jizhi_widget_prefs", MODE_PRIVATE)
        val intervalHours = prefs.getFloat("update_interval_hours", 1f)

        if (intervalHours > 0) {
            SentenceUpdateWorker.scheduleUpdate(this, intervalHours)
        }
    }
}
