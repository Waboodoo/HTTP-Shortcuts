package ch.rmy.android.http_shortcuts.activities.editor.headers

import ch.rmy.android.framework.viewmodel.viewstate.DialogState

data class RequestHeadersViewState(
    val dialogState: DialogState? = null,
    val headerItems: List<HeaderListItem> = emptyList(),
) {
    val isDraggingEnabled: Boolean
        get() = headerItems.size > 1
}
