package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import androidx.compose.runtime.Stable

@Stable
data class TextTypeViewState(
    val rememberValue: Boolean,
    val isMultilineCheckboxVisible: Boolean,
    val isMultiline: Boolean,
) : VariableTypeViewState
