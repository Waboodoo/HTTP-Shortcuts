package ch.rmy.android.http_shortcuts.activities.variables

data class VariablesViewState(
    val variables: List<VariableListItem> = emptyList(),
) {
    val isDraggingEnabled: Boolean
        get() = variables.size > 1
}
