package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.variables.Variables
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ToastAction
@Inject
constructor(
    private val context: Context,
) : Action<ToastAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        val finalMessage = Variables.rawPlaceholdersToResolvedValues(
            message,
            executionContext.variableManager.getVariableValuesByIds(),
        )
        if (finalMessage.isEmpty()) {
            return
        }
        withContext(Dispatchers.Main) {
            context.showToast(HTMLUtil.toSpanned(finalMessage), long = true)
        }
    }

    data class Params(
        val message: String,
    )
}
