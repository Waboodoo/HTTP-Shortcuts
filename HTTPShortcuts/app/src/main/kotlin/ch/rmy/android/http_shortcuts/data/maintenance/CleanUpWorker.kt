package ch.rmy.android.http_shortcuts.data.maintenance

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.RxWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.app.AppRepository
import io.reactivex.Single
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CleanUpWorker(context: Context, params: WorkerParameters) : RxWorker(context, params) {

    @Inject
    lateinit var appRepository: AppRepository

    init {
        getApplicationComponent().inject(this)
    }

    override fun createWork(): Single<Result> =
        appRepository.deleteUnusedData()
            .toSingleDefault(Result.success())
            .onErrorReturnItem(Result.failure())

    companion object {

        private const val TAG = "realm-cleanup"

        fun schedule(context: Context) {
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
}
