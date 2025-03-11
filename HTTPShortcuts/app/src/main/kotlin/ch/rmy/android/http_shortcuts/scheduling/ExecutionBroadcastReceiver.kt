package ch.rmy.android.http_shortcuts.scheduling

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.ExecutionId
import ch.rmy.android.http_shortcuts.data.domains.pending_executions.PendingExecutionsRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class ExecutionBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var pendingExecutionsRepository: PendingExecutionsRepository

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    override fun onReceive(context: Context, intent: Intent) {
        val pendingExecution = try {
            runBlocking {
                pendingExecutionsRepository.getPendingExecution(intent.extractId())
            }
        } catch (e: NoSuchElementException) {
            alarmScheduler.cancelAlarm(intent.extractId(), intent.extractRequestCode())
            return
        }
        ExecutionWorker.runPendingExecution(context, pendingExecution)
    }

    private fun Intent.extractId(): ExecutionId =
        getIntExtra(EXTRA_EXECUTION_ID, 0)

    private fun Intent.extractRequestCode(): Int =
        getIntExtra(EXTRA_REQUEST_CODE, 0)

    companion object {
        private const val EXTRA_EXECUTION_ID = "id"
        private const val EXTRA_REQUEST_CODE = "requestCode"

        fun createIntent(context: Context, id: ExecutionId, requestCode: Int): Intent =
            Intent(context, ExecutionBroadcastReceiver::class.java)
                .putExtra(EXTRA_EXECUTION_ID, id)
                .putExtra(EXTRA_REQUEST_CODE, requestCode)
    }
}
