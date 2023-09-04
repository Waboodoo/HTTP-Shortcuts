package ch.rmy.android.http_shortcuts.data.maintenance

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.rmy.android.framework.data.RealmUnavailableException
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltWorker
class CleanUpWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val appRepository: AppRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result =
        try {
            appRepository.deleteUnusedData()
            Result.success()
        } catch (e: RealmUnavailableException) {
            Result.failure()
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
                        .setConstraints(
                            Constraints.Builder()
                                .setRequiresBatteryNotLow(true)
                                .setRequiresStorageNotLow(true)
                                .build()
                        )
                        .build()
                )
            }
        }
    }

    companion object {
        private const val TAG = "realm-cleanup"
    }
}
