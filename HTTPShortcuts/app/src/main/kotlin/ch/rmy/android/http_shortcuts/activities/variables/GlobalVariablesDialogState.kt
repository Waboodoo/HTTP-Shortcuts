package ch.rmy.android.http_shortcuts.activities.variables

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey

@Stable
sealed class GlobalVariablesDialogState {
    @Stable
    data object Creation : GlobalVariablesDialogState()

    @Stable
    data class ContextMenu(
        val showUse: Boolean,
        val variableKey: VariableKey,
    ) : GlobalVariablesDialogState()

    @Stable
    data class Delete(
        val variableKey: VariableKey,
        val shortcutNames: List<String>,
    ) : GlobalVariablesDialogState()
}
