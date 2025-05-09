package ch.rmy.android.http_shortcuts.activities.variables

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.variables.models.GlobalVariableListItem

@Stable
data class VariablesViewState(
    val dialogState: GlobalVariablesDialogState? = null,
    val variables: List<GlobalVariableListItem> = emptyList(),
) {
    val isSortButtonEnabled
        get() = variables.size > 1
}
