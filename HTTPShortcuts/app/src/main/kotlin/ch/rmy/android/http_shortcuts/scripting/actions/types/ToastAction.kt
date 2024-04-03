package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.Toaster
import ch.rmy.android.http_shortcuts.variables.Variables
import javax.inject.Inject

class ToastAction
@Inject
constructor(
    private val settings: Settings,
    private val toaster: Toaster,
) : Action<ToastAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(
            message,
            executionContext.variableManager.getVariableValuesByIds(),
        )
        if (finalMessage.isEmpty()) {
            return
        }
        toaster.showToast(HTMLUtil.toSpanned(finalMessage), long = true, isForeground = !settings.useExperimentalExecutionMode)
    }

    data class Params(
        val message: String,
    )
}
