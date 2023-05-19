package ch.rmy.android.http_shortcuts.history

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.RealmFactoryImpl
import ch.rmy.android.http_shortcuts.data.domains.history.HistoryRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Duration.Companion.hours

class HistoryCleanUpWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @Inject
    lateinit var historyRepository: HistoryRepository

    init {
        getApplicationComponent().inject(this)
    }

    override suspend fun doWork(): Result =
        try {
            RealmFactoryImpl.init(applicationContext)
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
                        .build()
                )
            }
        }
    }

    companion object {
        private const val TAG = "history-cleanup"

        private val MAX_AGE = 8.hours
    }
}
