package ch.rmy.android.http_shortcuts.activities.variables.editor.types.toggle

data class ToggleTypeViewState(
    val options: List<OptionItem>,
) {
    val isDraggingEnabled: Boolean
        get() = options.size > 1
}
