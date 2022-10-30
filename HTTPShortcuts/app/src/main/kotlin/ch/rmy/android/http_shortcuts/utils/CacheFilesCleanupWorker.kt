package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.utils.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.minutes

class CacheFilesCleanupWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result =
        try {
            withContext(Dispatchers.IO) {
                FileUtil.deleteOldCacheFiles(applicationContext, maxCacheFileAge = 5.minutes)
            }
            Result.success()
        } catch (e: Exception) {
            logException(e)
            Result.failure()
        }

    companion object {

        private const val TAG = "cache-file-cleanup"

        fun schedule(context: Context) {
            with(WorkManager.getInstance(context)) {
                cancelAllWorkByTag(TAG)
                enqueue(
                    OneTimeWorkRequestBuilder<CacheFilesCleanupWorker>()
                        .addTag(TAG)
                        .build()
                )
            }
        }
    }
}
