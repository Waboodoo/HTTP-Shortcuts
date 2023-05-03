package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import androidx.compose.runtime.Stable

@Stable
data class ToggleTypeViewState(
    val options: List<OptionItem>,
    val tooFewOptionsError: Boolean = false,
) : VariableTypeViewState {
    @Stable
    data class OptionItem(
        val id: String,
        val text: String,
    )
}
