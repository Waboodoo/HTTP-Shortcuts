package ch.rmy.android.http_shortcuts.variables

import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.dtos.VariablePlaceholder
import ch.rmy.android.http_shortcuts.data.models.GlobalVariable
import ch.rmy.android.http_shortcuts.extensions.toVariablePlaceholder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VariablePlaceholderProvider
@Inject
constructor() {

    fun applyVariables(variables: List<GlobalVariable>) {
        placeholders = variables.map(GlobalVariable::toVariablePlaceholder)
    }

    var placeholders: List<VariablePlaceholder> = emptyList()
        private set

    fun findPlaceholderById(globalVariableId: GlobalVariableId): VariablePlaceholder? =
        placeholders
            .firstOrNull { it.globalVariableId == globalVariableId }
}
