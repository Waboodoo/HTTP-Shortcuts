package ch.rmy.android.http_shortcuts.activities.editor.headers

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.activities.editor.headers.models.HeaderListItem

@Stable
data class RequestHeadersViewState(
    val dialogState: RequestHeadersDialogState? = null,
    val headerItems: List<HeaderListItem> = emptyList(),
)
