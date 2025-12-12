package ch.rmy.android.http_shortcuts.activities.variablewidget

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.variablewidget.models.SelectableVariable

@Stable
data class VariableWidgetSettingsViewState(
    val selectableVariables: List<SelectableVariable>,
    val selectedVariable: SelectableVariable?,
    val variableValue: String?,
    val fontSize: Int,
    val title: String,
) {
    val isSaveEnabled
        get() = selectedVariable != null
}
