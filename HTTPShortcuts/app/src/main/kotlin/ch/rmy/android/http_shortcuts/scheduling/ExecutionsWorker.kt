package ch.rmy.android.http_shortcuts.scheduling

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.RxWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.rmy.android.http_shortcuts.data.RealmFactory
import io.reactivex.Single

class ExecutionsWorker(context: Context, workerParams: WorkerParameters) :
    RxWorker(context, workerParams) {

    override fun createWork(): Single<Result> =
        Single.defer {
            RealmFactory.init(applicationContext)
            ExecutionScheduler(applicationContext)
                .schedule()
                .toSingleDefault(Result.success())
                .onErrorReturnItem(Result.failure())
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
