package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets.models.ItemWrapper

@Stable
data class CodeSnippetPickerViewState(
    val dialogState: CodeSnippetPickerDialogState? = null,
    val items: List<ItemWrapper> = emptyList(),
    val searchQuery: String = "",
)
