package com.jizhi.worker

import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.jizhi.data.remote.JinrishiciApiService
import com.jizhi.widget.SentenceWidgetProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

/**
 * 句子更新 Worker
 * 使用 WorkManager 定时从 API 获取新句子并更新小组件
 * 支持 Hilt 依赖注入
 */
@HiltWorker
class SentenceUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: JinrishiciApiService
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val WORK_NAME = "sentence_update_work"
        const val PREFS_NAME = "jizhi_widget_prefs"
        const val KEY_INTERVAL_HOURS = "update_interval_hours"

        /**
         * 安排定时更新任务
         *
         * @param context 上下文
         * @param intervalHours 更新间隔（小时），<= 0 表示不更新
         */
        fun scheduleUpdate(context: Context, intervalHours: Float) {
            if (intervalHours <= 0) {
                // 取消定时任务
                WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
                return
            }

            val workRequest = PeriodicWorkRequestBuilder<SentenceUpdateWorker>(
                intervalHours.toLong(),
                TimeUnit.HOURS,
                // Flex interval: 允许在周期结束前15分钟内的任意时间执行
                15,
                TimeUnit.MINUTES
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )
        }

        /**
         * 取消定时更新任务
         */
        fun cancelUpdate(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /**
         * 立即执行一次更新
         */
        fun executeNow(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<SentenceUpdateWorker>()
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // 获取 Token
            val prefs = context.getSharedPreferences("jizhi_token_prefs", Context.MODE_PRIVATE)
            var token = prefs.getString("user_token", null)

            if (token.isNullOrEmpty()) {
                // 获取新 Token
                val tokenResponse = apiService.getToken()
                if (tokenResponse.status == "success") {
                    token = tokenResponse.data
                    prefs.edit().putString("user_token", token).apply()
                } else {
                    return@withContext Result.retry()
                }
            }

            // 获取新句子
            val response = apiService.getSentence(token)
            if (response.status == "success" && response.data != null) {
                // 发送广播更新所有小组件
                val intent = Intent(context, SentenceWidgetProvider::class.java).apply {
                    action = SentenceWidgetProvider.ACTION_UPDATE_ALL
                }
                context.sendBroadcast(intent)

                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) {
                Result.retry()
            } else {
                Result.failure()
            }
        }
    }
}
