package ch.rmy.android.http_shortcuts.activities.variablewidget

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.variablewidget.models.SelectableShortcut
import ch.rmy.android.http_shortcuts.activities.variablewidget.models.SelectableVariable
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

@Stable
data class VariableWidgetSettingsViewState(
    val selectableVariables: List<SelectableVariable>,
    val selectedVariable: SelectableVariable?,
    val variableValue: String?,
    val fontSize: Int,
    val title: String,
    val shortcutId: ShortcutId?,
    val selectableShortcuts: List<SelectableShortcut>,
) {
    val isSaveEnabled
        get() = selectedVariable != null
}
