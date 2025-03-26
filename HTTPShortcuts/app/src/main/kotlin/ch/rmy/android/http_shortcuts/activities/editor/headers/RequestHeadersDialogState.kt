package ch.rmy.android.http_shortcuts.activities.editor.headers

import androidx.compose.runtime.Stable
import ch.rmy.android.http_shortcuts.data.domains.request_headers.RequestHeaderId

@Stable
sealed class RequestHeadersDialogState {
    @Stable
    data object AddHeader : RequestHeadersDialogState()

    @Stable
    data class EditHeader(
        val id: RequestHeaderId,
        val key: String,
        val value: String,
    ) : RequestHeadersDialogState()
}
