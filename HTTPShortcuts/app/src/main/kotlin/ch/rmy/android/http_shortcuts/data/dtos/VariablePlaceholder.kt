package ch.rmy.android.http_shortcuts.data.dtos

import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.enums.VariableType

data class VariablePlaceholder(
    val variableId: VariableId,
    val variableKey: String,
    val variableType: VariableType,
)
