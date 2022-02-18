package ch.rmy.android.http_shortcuts.activities.editor.headers

import ch.rmy.android.http_shortcuts.data.models.Variable

data class RequestHeadersViewState(
    val headerItems: List<HeaderListItem> = emptyList(),
    val variables: List<Variable>? = null,
) {
    val isDraggingEnabled: Boolean
        get() = headerItems.size > 1
}
