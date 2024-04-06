package ch.rmy.android.http_shortcuts.activities.categories.editor

import androidx.compose.runtime.Stable

@Stable
sealed class CategoryEditorDialogState {
    data class ColorPicker(val initialColor: Int) : CategoryEditorDialogState()
    data object DiscardWarning : CategoryEditorDialogState()
}
