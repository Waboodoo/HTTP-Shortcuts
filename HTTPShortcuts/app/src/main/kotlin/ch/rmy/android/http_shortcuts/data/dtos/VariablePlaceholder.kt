package ch.rmy.android.http_shortcuts.data.dtos

import ch.rmy.android.http_shortcuts.data.enums.VariableType

data class VariablePlaceholder(
    val variableId: String,
    val variableKey: String,
    val variableType: VariableType,
)
