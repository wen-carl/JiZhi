package com.jizhi

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.jizhi.data.local.DataStoreManager
import com.jizhi.worker.SentenceUpdateWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class JiZhiApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    override fun onCreate() {
        super.onCreate()
        schedulePeriodicWork()
    }

    private fun schedulePeriodicWork() {
        CoroutineScope(Dispatchers.IO).launch {
            val intervalHours = DataStoreManager.getUpdateIntervalHours(this@JiZhiApplication)
            if (intervalHours > 0) {
                SentenceUpdateWorker.scheduleUpdate(this@JiZhiApplication, intervalHours)
            }
        }
    }
}
