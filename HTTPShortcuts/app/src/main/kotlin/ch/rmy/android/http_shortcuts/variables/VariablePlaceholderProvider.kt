package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder
import ch.rmy.android.http_shortcuts.data.models.Variable

class VariablePlaceholderProvider {

    fun applyVariables(variables: List<Variable>) {
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

        private fun toPlaceholder(variable: Variable) =
            VariablePlaceholder(
                variableId = variable.id,
                variableKey = variable.key,
                variableType = variable.variableType,
            )
    }
}
