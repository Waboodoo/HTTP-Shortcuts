package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetVariableAction
@Inject
constructor(
    private val variableResolver: VariableResolver,
) : Action<GetVariableAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): String {
        var value = executionContext.variableManager.getVariableValues()[variableKeyOrId]
        if (value == null) {
            resolveVariable(variableKeyOrId, executionContext.variableManager, executionContext.dialogHandle)
            value = executionContext.variableManager.getVariableValues()[variableKeyOrId]
            if (value == null) {
                throw ActionException {
                    getString(R.string.error_variable_not_found_read, variableKeyOrId.value)
                }
            }
        }
        return value
    }

    private suspend fun resolveVariable(variableKeyOrId: VariableKeyOrId, variableManager: VariableManager, dialogHandle: DialogHandle) {
        withContext(Dispatchers.Main) {
            variableResolver.resolve(variableManager, variableKeysOrIds = setOf(variableKeyOrId), dialogHandle)
        }
    }

    data class Params(
        val variableKeyOrId: VariableKeyOrId,
    )
}
