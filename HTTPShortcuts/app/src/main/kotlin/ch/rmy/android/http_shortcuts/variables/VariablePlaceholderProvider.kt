package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.data.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.data.models.Variable

class VariablePlaceholderProvider(private val variables: ListLiveData<Variable> = object : ListLiveData<Variable>() {}) {

    val placeholders
        get() = variables.map(::toPlaceholder)

    val constantsPlaceholders
        get() = variables
            .filter { it.isConstant }
            .map(::toPlaceholder)

    val hasVariables
        get() = variables.isNotEmpty()

    val hasConstants
        get() = variables.any { it.isConstant }

    fun findPlaceholderById(variableId: String): VariablePlaceholder? =
        variables
            .firstOrNull { it.id == variableId }
            ?.let(::toPlaceholder)

    companion object {

        private fun toPlaceholder(variable: Variable) =
            VariablePlaceholder(
                variableId = variable.id,
                variableKey = variable.key
            )

    }

}