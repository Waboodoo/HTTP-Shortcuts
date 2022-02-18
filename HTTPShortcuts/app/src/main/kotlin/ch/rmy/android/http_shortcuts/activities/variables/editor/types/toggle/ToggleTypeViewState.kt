package ch.rmy.android.http_shortcuts.activities.variables.editor.types.toggle

import ch.rmy.android.http_shortcuts.data.models.Variable

data class ToggleTypeViewState(
    val variables: List<Variable>? = null,
    val options: List<OptionItem>,
) {
    val isDraggingEnabled: Boolean
        get() = options.size > 1
}
