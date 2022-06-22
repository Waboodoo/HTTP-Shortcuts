package ch.rmy.android.http_shortcuts.activities.variables.editor.types.select

data class SelectTypeViewState(
    val options: List<OptionItem>,
    val isMultiSelect: Boolean,
    val separator: String,
) {
    val separatorEnabled
        get() = isMultiSelect

    val isDraggingEnabled: Boolean
        get() = options.size > 1
}
