package ch.rmy.android.http_shortcuts.scheduling

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.ExecutionId
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import ch.rmy.android.http_shortcuts.data.enums.PendingExecutionType
import ch.rmy.android.http_shortcuts.data.enums.ShortcutTriggerType
import ch.rmy.android.http_shortcuts.data.models.PendingExecution
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.time.Duration

@HiltWorker
class ExecutionWorker
@AssistedInject
constructor(
    @Assisted
    private val context: Context,
    @Assisted
    params: WorkerParameters,
    private val pendingExecutionsRepository: PendingExecutionsRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        try {
            val executionId = inputData.getString(INPUT_EXECUTION_ID) ?: return Result.failure()
            runPendingExecution(context, executionId)
        } catch (e: NoSuchElementException) {
            // Nothing to do here
        } catch (e: Exception) {
            logException(e)
        }
        return Result.success()
    }

    private suspend fun runPendingExecution(context: Context, id: String) {
        val pendingExecution = pendingExecutionsRepository.getPendingExecution(id)
        withContext(Dispatchers.Main) {
            runPendingExecution(context, pendingExecution)
        }
    }

    class Starter
    @Inject
    constructor(
        private val context: Context,
    ) {
        operator fun invoke(pendingExecutionId: ExecutionId, delay: Duration? = null, withNetworkConstraints: Boolean = false) {
            with(WorkManager.getInstance(context)) {
                cancelAllWorkByTag(TAG)
                enqueue(
                    OneTimeWorkRequestBuilder<ExecutionWorker>()
                        .addTag(TAG)
                        .setInputData(
                            Data.Builder()
                                .putString(INPUT_EXECUTION_ID, pendingExecutionId)
                                .build()
                        )
                        .runIfNotNull(delay) {
                            setInitialDelay(it.inWholeMilliseconds, TimeUnit.MILLISECONDS)
                        }
                        .runIf(withNetworkConstraints) {
                            setConstraints(
                                Constraints.Builder()
                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                    .build()
                            )
                        }
                        .build()
                )
            }
        }
    }

    companion object {
        private const val TAG = "execution_scheduler"

        const val INPUT_EXECUTION_ID = "id"

        fun runPendingExecution(context: Context, pendingExecution: PendingExecution) {
            ExecuteActivity.IntentBuilder(shortcutId = pendingExecution.shortcutId)
                .variableValues(
                    pendingExecution.resolvedVariables
                        .associate { variable -> variable.key to variable.value }
                )
                .tryNumber(pendingExecution.tryNumber)
                .recursionDepth(pendingExecution.recursionDepth)
                .executionId(pendingExecution.id)
                .trigger(if (pendingExecution.type == PendingExecutionType.REPEAT) ShortcutTriggerType.REPETITION else ShortcutTriggerType.SCHEDULE)
                .startActivity(context)
        }
    }
}
