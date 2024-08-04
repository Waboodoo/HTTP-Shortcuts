package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.logInfo
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKeyOrId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableRepository
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class SetVariableAction
@Inject
constructor(
    private val variableRepository: VariableRepository,
) : Action<SetVariableAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        logInfo("Setting variable value (${value.length} characters)")
        executionContext.variableManager.setVariableValueByKeyOrId(variableKeyOrId, value, storeOnly)
        val variable = try {
            variableRepository.getVariableByKeyOrId(variableKeyOrId)
        } catch (e: NoSuchElementException) {
            throw ActionException {
                getString(
                    R.string.error_variable_not_found_write,
                    variableKeyOrId,
                )
            }
        }
        variableRepository.setVariableValue(variable.id, value.truncate(MAX_VARIABLE_LENGTH))
    }

    data class Params(
        val variableKeyOrId: VariableKeyOrId,
        val value: String,
        val storeOnly: Boolean,
    )

    companion object {

        private const val MAX_VARIABLE_LENGTH = 40_000
    }
}
