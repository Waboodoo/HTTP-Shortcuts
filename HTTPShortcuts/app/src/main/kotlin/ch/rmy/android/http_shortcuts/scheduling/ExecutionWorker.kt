package ch.rmy.android.http_shortcuts.scheduling

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import ch.rmy.android.http_shortcuts.activities.ExecuteActivity
import ch.rmy.android.http_shortcuts.data.DataSource
import ch.rmy.android.http_shortcuts.data.RealmFactory
import ch.rmy.android.http_shortcuts.data.models.PendingExecution

class ExecutionWorker(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    override fun doWork(): Result {
        val executionId = inputData.getString(INPUT_EXECUTION_ID) ?: return Result.failure()

        RealmFactory.init(applicationContext)
        runPendingExecution(context, executionId)
        return Result.success()
    }

    companion object {
        const val INPUT_EXECUTION_ID = "id"

        private fun runPendingExecution(context: Context, id: String) {
            val pendingExecution = DataSource.getPendingExecution(id) ?: return
            runPendingExecution(context, pendingExecution)
        }

        fun runPendingExecution(context: Context, pendingExecution: PendingExecution) {
            DataSource.deletePendingExecution(pendingExecution.id)
            val shortcutId = pendingExecution.shortcutId
            val tryNumber = pendingExecution.tryNumber
            val variableValues = pendingExecution.resolvedVariables
                .associate { variable -> variable.key to variable.value }
            val recursionDepth = pendingExecution.recursionDepth

            ExecuteActivity.IntentBuilder(context, shortcutId)
                .variableValues(variableValues)
                .tryNumber(tryNumber)
                .recursionDepth(recursionDepth)
                .startActivity(context)

        }
    }
}