package ch.rmy.android.http_shortcuts.activities.editor.scripting.codesnippets

import ch.rmy.android.framework.viewmodel.viewstate.DialogState

data class CodeSnippetPickerViewState(
    val dialogState: DialogState? = null,
    val items: List<ItemWrapper>,
)
