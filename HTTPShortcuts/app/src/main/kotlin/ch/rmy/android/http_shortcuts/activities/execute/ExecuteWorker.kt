package ch.rmy.android.http_shortcuts.activities.execute

import android.content.Context
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import ch.rmy.android.http_shortcuts.activities.misc.host.HostActivity
import ch.rmy.android.http_shortcuts.extensions.context
import ch.rmy.android.http_shortcuts.utils.GsonUtil
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltWorker
class ExecuteWorker
@AssistedInject
constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val executionFactory: ExecutionFactory,
    private val dialogHandler: ExecuteDialogHandler,
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = coroutineScope {
        try {
            logInfo("ExecuteWorker starting")
            val params = GsonUtil.gson.fromJson(inputData.getString("executionParams")!!, ExecutionParams::class.java)

            launch {
                dialogHandler.dialogState.collect { dialogState ->
                    if (dialogState != null) {
                        logInfo("ExecuteWorker processing dialog")
                        try {
                            val result = HostActivity.showDialog(context, dialogState)
                            dialogHandler.onDialogResult(result)
                        } catch (e: CancellationException) {
                            dialogHandler.onDialogDismissed()
                        }
                    }
                }
            }

            val execution = executionFactory.createExecution(params, dialogHandler)
            execution.execute().collect { executionStatus ->
                logInfo("EXECUTION STATUS: $executionStatus")
            }

            logInfo("EXECUTION done")
            Result.success()
        } catch (e: CancellationException) {
            Result.success()
        } catch (e: Throwable) {
            logInfo("EXECUTION failed")
            logException(e)
            Result.failure()
        }
    }

    class Starter
    @Inject
    constructor(
        private val context: Context,
    ) {
        operator fun invoke(
            executionParams: ExecutionParams,
        ) {
            with(WorkManager.getInstance(context)) {
                cancelAllWorkByTag("executeWorker")
                enqueue(
                    OneTimeWorkRequestBuilder<ExecuteWorker>()
                        .runIf(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        }
                        .addTag("executeWorker")
                        .setInputData(
                            Data.Builder()
                                .putString("executionParams", GsonUtil.toJson(executionParams))
                                .build()
                        )
                        .build()
                )
            }
        }
    }
}
