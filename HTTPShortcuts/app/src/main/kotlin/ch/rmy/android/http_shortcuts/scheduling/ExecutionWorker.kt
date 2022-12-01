package ch.rmy.android.http_shortcuts.scheduling

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.dagger.getApplicationComponent
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.models.PendingExecutionModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ExecutionWorker(private val context: Context, workerParams: WorkerParameters) : CoroutineWorker(context, workerParams) {

    @Inject
    lateinit var pendingExecutionsRepository: PendingExecutionsRepository

    init {
        getApplicationComponent().inject(this)
    }

    override suspend fun doWork(): Result {
        return try {
            val executionId = inputData.getString(INPUT_EXECUTION_ID) ?: return Result.failure()
            RealmFactory.init(applicationContext)
            runPendingExecution(context, executionId)
            Result.success()
        } catch (e: NoSuchElementException) {
            Result.success()
        } catch (e: Exception) {
            logException(e)
            Result.failure()
        }
    }

    private suspend fun runPendingExecution(context: Context, id: String) {
        val pendingExecution = pendingExecutionsRepository.getPendingExecution(id)
        withContext(Dispatchers.Main) {
            runPendingExecution(context, pendingExecution)
        }
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
                .trigger("schedule")
                .startActivity(context)
        }
    }
}
