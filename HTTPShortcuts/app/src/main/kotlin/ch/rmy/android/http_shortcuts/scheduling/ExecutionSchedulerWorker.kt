package ch.rmy.android.http_shortcuts.scheduling

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.extensions.context
import javax.inject.Inject

class ExecutionSchedulerWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    @Inject
    lateinit var executionScheduler: ExecutionScheduler

    init {
        getApplicationComponent().inject(this)
    }

    override suspend fun doWork(): Result {
        RealmFactory.init(context)
        return try {
            executionScheduler.schedule()
            Result.success()
        } catch (e: Exception) {
            logException(e)
            Result.failure()
        }
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
                    OneTimeWorkRequestBuilder<ExecutionSchedulerWorker>()
                        .addTag(TAG)
                        .build()
                )
            }
        }
    }

    companion object {
        private const val TAG = "executions_worker"
    }
}
