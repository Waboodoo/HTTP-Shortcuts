package ch.rmy.android.http_shortcuts.activities.variables

import ch.rmy.android.framework.viewmodel.viewstate.DialogState

data class VariablesViewState(
    val dialogState: DialogState? = null,
    val variables: List<VariableListItem> = emptyList(),
) {
    val isDraggingEnabled: Boolean
        get() = variables.size > 1
}
