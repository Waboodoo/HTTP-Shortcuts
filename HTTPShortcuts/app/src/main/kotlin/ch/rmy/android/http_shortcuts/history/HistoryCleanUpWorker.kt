package ch.rmy.android.http_shortcuts.history

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.data.domains.history.HistoryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

@HiltWorker
class HistoryCleanUpWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val historyRepository: HistoryRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result =
        try {
            historyRepository.deleteOldEvents(MAX_AGE)
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
                    OneTimeWorkRequestBuilder<HistoryCleanUpWorker>()
                        .addTag(TAG)
                        .setInitialDelay(15, TimeUnit.SECONDS)
                        .build(),
                )
            }
        }
    }

    companion object {
        private const val TAG = "history-cleanup"

        private val MAX_AGE = 12.hours
    }
}
