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
import com.jizhi.Constants
import com.jizhi.data.remote.JinrishiciApiService
import com.jizhi.data.remote.JinrishiciClient
import com.jizhi.widget.SentenceWidgetProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class SentenceUpdateWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val apiService: JinrishiciApiService
) : CoroutineWorker(context, workerParams) {

    companion object {
        fun scheduleUpdate(context: Context, intervalHours: Float) {
            if (intervalHours <= 0) {
                WorkManager.getInstance(context).cancelUniqueWork(Constants.WORK_NAME)
                return
            }

            val workRequest = PeriodicWorkRequestBuilder<SentenceUpdateWorker>(
                intervalHours.toLong(),
                TimeUnit.HOURS,
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
                    Constants.WORK_NAME,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )
        }

        fun cancelUpdate(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(Constants.WORK_NAME)
        }

        fun executeNow(context: Context) {
            val workRequest = OneTimeWorkRequestBuilder<SentenceUpdateWorker>()
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            var token = JinrishiciClient.getSavedToken(context)

            if (token.isEmpty()) {
                val tokenResponse = apiService.getToken()
                if (tokenResponse.status == "success") {
                    token = tokenResponse.data
                    JinrishiciClient.saveToken(context, token)
                } else {
                    return@withContext Result.retry()
                }
            }

            val response = apiService.getSentence(token)
            if (response.status == "success" && response.data != null) {
                val intent = Intent(context, SentenceWidgetProvider::class.java).apply {
                    action = Constants.ACTION_UPDATE_ALL
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
