package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.variables.Variables
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ToastAction(private val message: String) : BaseAction() {

    override suspend fun execute(executionContext: ExecutionContext) {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(
            message,
            executionContext.variableManager.getVariableValuesByIds(),
        )
        if (finalMessage.isEmpty()) {
            return
        }
        withContext(Dispatchers.Main) {
            executionContext.context.showToast(HTMLUtil.format(finalMessage), long = true)
        }
    }
}
