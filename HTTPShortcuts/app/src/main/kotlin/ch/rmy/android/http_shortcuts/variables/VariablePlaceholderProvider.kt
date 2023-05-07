package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.toVariablePlaceholder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VariablePlaceholderProvider
@Inject
constructor() {

    fun applyVariables(variables: List<Variable>) {
        placeholders = variables.map(Variable::toVariablePlaceholder)
    }

    var placeholders: List<VariablePlaceholder> = emptyList()
        private set

    fun findPlaceholderById(variableId: VariableId): VariablePlaceholder? =
        placeholders
            .firstOrNull { it.variableId == variableId }
}
