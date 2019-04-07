package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.realm.livedata.ListLiveData
import ch.rmy.android.http_shortcuts.realm.models.Variable

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

    fun findPlaceholder(variableKey: String): VariablePlaceholder? =
        variables
            .firstOrNull { it.key == variableKey }
            ?.let(::toPlaceholder)

    companion object {

        private fun toPlaceholder(variable: Variable) =
            VariablePlaceholder(
                variableKey = variable.key
            )

    }

}