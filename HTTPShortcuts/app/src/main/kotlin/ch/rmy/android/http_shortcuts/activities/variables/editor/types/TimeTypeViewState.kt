package ch.rmy.android.http_shortcuts.activities.variables.editor.types

import androidx.compose.runtime.Stable

@Stable
data class TimeTypeViewState(
    val timeFormat: String,
    val rememberValue: Boolean,
    val invalidFormat: Boolean = false,
) : VariableTypeViewState
