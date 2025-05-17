package ch.rmy.android.http_shortcuts.data.dtos

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.enums.VariableType

@Stable
data class GlobalVariablePlaceholder(
    val globalVariableId: GlobalVariableId,
    val variableKey: String,
    val variableType: VariableType,
)
