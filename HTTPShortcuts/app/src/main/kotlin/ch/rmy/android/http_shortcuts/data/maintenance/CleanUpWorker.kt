package ch.rmy.android.http_shortcuts.data.maintenance

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CleanUpWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    @Inject
    lateinit var appRepository: AppRepository

    init {
        getApplicationComponent().inject(this)
    }

    override suspend fun doWork(): Result =
        try {
            appRepository.deleteUnusedData()
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
                    OneTimeWorkRequestBuilder<CleanUpWorker>()
                        .addTag(TAG)
                        .setInitialDelay(5, TimeUnit.SECONDS)
                        .build()
                )
            }
        }
    }

    companion object {
        private const val TAG = "realm-cleanup"
    }
}
