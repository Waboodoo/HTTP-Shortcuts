package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder
import ch.rmy.android.http_shortcuts.data.models.VariableModel

class VariablePlaceholderProvider {

    fun applyVariables(variables: List<VariableModel>) {
        placeholders = variables.map(::toPlaceholder)
        cache = placeholders
    }

    var placeholders: List<VariablePlaceholder> = cache
        private set

    val hasVariables
        get() = placeholders.isNotEmpty()

    fun findPlaceholderById(variableId: String): VariablePlaceholder? =
        placeholders
            .firstOrNull { it.variableId == variableId }

    companion object {

        private var cache: List<VariablePlaceholder> = emptyList()

        private fun toPlaceholder(variable: VariableModel) =
            VariablePlaceholder(
                variableId = variable.id,
                variableKey = variable.key,
                variableType = variable.variableType,
            )
    }
}
