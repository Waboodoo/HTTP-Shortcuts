package ch.rmy.android.http_shortcuts.activities.editor.headers

import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.data.models.VariableModel

data class RequestHeadersViewState(
    val dialogState: DialogState? = null,
    val headerItems: List<HeaderListItem> = emptyList(),
    val variables: List<VariableModel>? = null,
) {
    val isDraggingEnabled: Boolean
        get() = headerItems.size > 1
}
