package ch.rmy.android.http_shortcuts.scheduling

import android.content.Context
import androidx.work.RxWorker
import androidx.work.WorkerParameters
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.models.PendingExecutionModel
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import javax.inject.Inject

class ExecutionWorker(private val context: Context, workerParams: WorkerParameters) :
    RxWorker(context, workerParams) {

    @Inject
    lateinit var pendingExecutionsRepository: PendingExecutionsRepository

    init {
        getApplicationComponent().inject(this)
    }

    override fun createWork(): Single<Result> =
        Single.defer {
            val executionId = inputData.getString(INPUT_EXECUTION_ID) ?: return@defer Single.just(Result.failure())
            RealmFactory.init(applicationContext)
            runPendingExecution(context, executionId)
                .toSingleDefault(Result.success())
        }
            .onErrorResumeNext { error ->
                if (error is NoSuchElementException) {
                    Single.just(Result.success())
                } else {
                    logException(error)
                    Single.just(Result.failure())
                }
            }

    private fun runPendingExecution(context: Context, id: String): Completable =
        pendingExecutionsRepository
            .getPendingExecution(id)
            .flatMapCompletable { pendingExecution ->
                Completable.fromAction {
                    runPendingExecution(context, pendingExecution)
                }
                    .subscribeOn(AndroidSchedulers.mainThread())
            }

    companion object {
        const val INPUT_EXECUTION_ID = "id"

        fun runPendingExecution(context: Context, pendingExecution: PendingExecutionModel) {
            ExecuteActivity.IntentBuilder(shortcutId = pendingExecution.shortcutId)
                .variableValues(
                    pendingExecution.resolvedVariables
                        .associate { variable -> variable.key to variable.value }
                )
                .tryNumber(pendingExecution.tryNumber)
                .recursionDepth(pendingExecution.recursionDepth)
                .executionId(pendingExecution.id)
                .startActivity(context)
        }

        fun runPollingExecution(context: Context, shortcut: ShortcutModel) {
            ExecuteActivity.IntentBuilder(shortcutId = shortcut.id)
                .recursionDepth(0)
                .executionId(shortcut.id)
                .startActivity(context)
        }
    }
}
