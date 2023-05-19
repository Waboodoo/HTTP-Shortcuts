package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.VariableResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetVariableAction(val variableKeyOrId: VariableKeyOrId) : BaseAction() {

    @Inject
    lateinit var variableResolver: VariableResolver

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext): String =
        try {
            getVariableValue(variableKeyOrId, executionContext.variableManager)
        } catch (e: VariableNotFoundException) {
            try {
                resolveVariable(variableKeyOrId, executionContext.variableManager, executionContext.dialogHandle)
                getVariableValue(variableKeyOrId, executionContext.variableManager)
            } catch (e2: VariableNotFoundException) {
                throw ActionException {
                    getString(R.string.error_variable_not_found_read, variableKeyOrId)
                }
            }
        }

    private fun getVariableValue(variableKeyOrId: VariableKeyOrId, variableManager: VariableManager): String =
        variableManager.getVariableValueByKeyOrId(variableKeyOrId)
            ?: throw VariableNotFoundException()

    private suspend fun resolveVariable(variableKeyOrId: VariableKeyOrId, variableManager: VariableManager, dialogHandle: DialogHandle) {
        val variable = variableManager.getVariableByKeyOrId(variableKeyOrId)
            ?: throw VariableNotFoundException()

        withContext(Dispatchers.Main) {
            variableResolver.resolve(variableManager, requiredVariableIds = setOf(variable.id), dialogHandle)
        }
    }

    private class VariableNotFoundException : Throwable()
}
