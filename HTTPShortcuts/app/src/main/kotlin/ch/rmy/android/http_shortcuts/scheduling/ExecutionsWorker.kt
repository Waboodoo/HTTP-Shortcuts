package ch.rmy.android.http_shortcuts.scheduling

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.RealmFactory
import javax.inject.Inject

class ExecutionsWorker(context: Context, workerParams: WorkerParameters) :
    CoroutineWorker(context, workerParams) {

    @Inject
    lateinit var executionScheduler: ExecutionScheduler

    init {
        getApplicationComponent().inject(this)
    }

    override suspend fun doWork(): Result {
        RealmFactory.init(applicationContext)
        return try {
            executionScheduler.schedule()
            Result.success()
        } catch (e: Exception) {
            logException(e)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "executions_worker"

        fun schedule(context: Context) {
            with(WorkManager.getInstance(context)) {
                cancelAllWorkByTag(TAG)
                enqueue(
                    OneTimeWorkRequestBuilder<ExecutionsWorker>()
                        .addTag(TAG)
                        .build()
                )
            }
        }
    }
}
