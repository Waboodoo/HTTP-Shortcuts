package ch.rmy.android.http_shortcuts.activities.variables

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.variables.models.VariableListItem

@Stable
data class VariablesViewState(
    val dialogState: VariablesDialogState? = null,
    val variables: List<VariableListItem> = emptyList(),
) {
    val isSortButtonEnabled
        get() = variables.size > 1
}
