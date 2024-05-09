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
import kotlinx.coroutines.flow.collect
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
        logInfo("ExecuteWorker started")

        val dialogJob = launch {
            dialogHandler.dialogState.collect { dialogState ->
                logInfo("ExecuteWorker processing dialog")
                if (dialogState != null) {
                    try {
                        val result = HostActivity.showDialog(context, dialogState)
                        logInfo("ExecuteWorker dialog result received")
                        dialogHandler.onDialogResult(result)
                    } catch (e: CancellationException) {
                        logInfo("ExecuteWorker dialog cancelled")
                        dialogHandler.onDialogDismissed()
                    }
                }
            }
        }

        try {
            val execution = executionFactory.createExecution(getParams(), dialogHandler)
            execution.execute().collect()

            logInfo("ExecuteWorker finished")
        } catch (e: CancellationException) {
            // Nothing to do here
        } catch (e: Throwable) {
            logException(e)
        } finally {
            dialogJob.cancel()
        }
        Result.success()
    }

    private fun getParams() =
        GsonUtil.gson.fromJson(inputData.getString(KEY_PARAMS)!!, ExecutionParams::class.java)

    class Starter
    @Inject
    constructor(
        private val context: Context,
    ) {
        operator fun invoke(
            executionParams: ExecutionParams,
        ) {
            with(WorkManager.getInstance(context)) {
                cancelAllWorkByTag(TAG)
                enqueue(
                    OneTimeWorkRequestBuilder<ExecuteWorker>()
                        .runIf(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                        }
                        .addTag(TAG)
                        .setInputData(
                            Data.Builder()
                                .putString(KEY_PARAMS, GsonUtil.toJson(executionParams))
                                .build()
                        )
                        .build()
                )
            }
        }
    }

    companion object {
        private const val TAG = "executeWorker"
        private const val KEY_PARAMS = "executionParams"
    }
}
