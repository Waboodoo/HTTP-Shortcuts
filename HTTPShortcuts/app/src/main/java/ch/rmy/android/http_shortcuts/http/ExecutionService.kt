package ch.rmy.android.http_shortcuts.http

import android.app.IntentService
import android.content.Intent
import ch.rmy.android.http_shortcuts.realm.Controller
import ch.rmy.android.http_shortcuts.utils.Connectivity
import ch.rmy.android.http_shortcuts.utils.IntentUtil
import java.util.*

class ExecutionService : IntentService(ExecutionService::class.java.name) {

    override fun onHandleIntent(intent: Intent?) {
        val controller = Controller()
        try {

            while (Connectivity.isNetworkConnected(this)) {
                val pendingExecutions = controller.shortcutsPendingExecution
                if (pendingExecutions.isEmpty()) {
                    break
                }

                val pendingExecution = pendingExecutions.first()
                val id = pendingExecution.shortcutId
                val variableValues = HashMap<String, String>()
                for (resolvedVariable in pendingExecution.resolvedVariables!!) {
                    variableValues.put(resolvedVariable.key!!, resolvedVariable.value!!)
                }

                controller.removePendingExecution(pendingExecution)

                try {
                    Thread.sleep(INITIAL_DELAY.toLong())
                    executeShortcut(id, variableValues)
                } catch (e: InterruptedException) {
                    break
                }

            }
        } finally {
            controller.destroy()
        }
    }

    private fun executeShortcut(id: Long, variableValues: HashMap<String, String>) {
        val shortcutIntent = IntentUtil.createIntent(this, id, variableValues)
        startActivity(shortcutIntent)
    }

    companion object {

        private const val INITIAL_DELAY = 1500
    }

}
