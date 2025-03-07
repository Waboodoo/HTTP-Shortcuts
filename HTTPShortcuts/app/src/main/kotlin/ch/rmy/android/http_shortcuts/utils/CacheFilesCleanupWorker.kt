package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.utils.FileUtil
import ch.rmy.android.http_shortcuts.extensions.context
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class CacheFilesCleanupWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result =
        try {
            withContext(Dispatchers.IO) {
                FileUtil.deleteOldCacheFiles(context, maxCacheFileAge = 5.minutes)
            }
            Result.success()
        } catch (e: Exception) {
            logException(e)
            Result.failure()
        }

    class Starter
    @Inject
    constructor(
        private val context: Context,
    ) {
        operator fun invoke() {
            with(WorkManager.getInstance(context)) {
                cancelAllWorkByTag(TAG)
                enqueue(
                    OneTimeWorkRequestBuilder<CacheFilesCleanupWorker>()
                        .addTag(TAG)
                        .setInitialDelay(CLEANUP_DELAY.inWholeMilliseconds, TimeUnit.MILLISECONDS)
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiresBatteryNotLow(true)
                                .build(),
                        )
                        .build(),
                )
            }
        }
    }

    companion object {
        private const val TAG = "cache-file-cleanup"
        private val CLEANUP_DELAY = 10.seconds
    }
}
