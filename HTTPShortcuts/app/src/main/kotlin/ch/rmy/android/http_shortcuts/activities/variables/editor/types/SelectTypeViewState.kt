package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import androidx.compose.runtime.Stable

@Stable
data class SelectTypeViewState(
    val options: List<OptionItem>,
    val tooFewOptionsError: Boolean = false,
    val isMultiSelect: Boolean,
    val separator: String,
) : VariableTypeViewState {
    @Stable
    data class OptionItem(
        val id: String,
        val label: String,
        val text: String,
    )
}
