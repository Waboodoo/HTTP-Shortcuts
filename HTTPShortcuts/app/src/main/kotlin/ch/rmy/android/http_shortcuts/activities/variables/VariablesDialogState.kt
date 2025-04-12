package ch.rmy.android.http_shortcuts.activities.variables

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableKey

@Stable
sealed class VariablesDialogState {
    @Stable
    data object Creation : VariablesDialogState()

    @Stable
    data class ContextMenu(
        val showUse: Boolean,
        val variableKey: VariableKey,
    ) : VariablesDialogState()

    @Stable
    data class Delete(
        val variableKey: VariableKey,
        val shortcutNames: List<String>,
    ) : VariablesDialogState()
}
