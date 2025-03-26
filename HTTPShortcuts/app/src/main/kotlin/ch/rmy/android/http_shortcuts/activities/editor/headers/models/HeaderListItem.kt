package ch.rmy.android.http_shortcuts.activities.editor.headers.models

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.request_headers.RequestHeaderId

@Stable
data class HeaderListItem(
    val id: RequestHeaderId,
    val key: String,
    val value: String,
)
