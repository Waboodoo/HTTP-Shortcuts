package ch.rmy.android.http_shortcuts.scheduling

import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.RealmFactoryImpl
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
        RealmFactoryImpl.init(context)
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
                        .runIf(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        }
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
