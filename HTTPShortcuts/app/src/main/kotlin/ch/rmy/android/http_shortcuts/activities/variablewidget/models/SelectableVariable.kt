package ch.rmy.android.http_shortcuts.activities.variablewidget.models

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.variables.GlobalVariableId
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey

@Stable
data class SelectableVariable(
    val variableId: GlobalVariableId,
    val variableKey: VariableKey,
)
