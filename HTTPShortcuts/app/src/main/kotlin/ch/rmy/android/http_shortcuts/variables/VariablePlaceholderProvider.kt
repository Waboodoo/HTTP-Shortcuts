package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VariablePlaceholderProvider
@Inject
constructor() {

    fun applyVariables(variables: List<VariableModel>) {
        placeholders = variables.map(::toPlaceholder)
    }

    var placeholders: List<VariablePlaceholder> = emptyList()
        private set

    val hasVariables
        get() = placeholders.isNotEmpty()

    fun findPlaceholderById(variableId: VariableId): VariablePlaceholder? =
        placeholders
            .firstOrNull { it.variableId == variableId }

    companion object {

        private fun toPlaceholder(variable: VariableModel) =
            VariablePlaceholder(
                variableId = variable.id,
                variableKey = variable.key,
                variableType = variable.variableType,
            )
    }
}
