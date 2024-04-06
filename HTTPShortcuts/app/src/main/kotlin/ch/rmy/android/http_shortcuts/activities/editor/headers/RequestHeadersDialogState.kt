package ch.rmy.android.http_shortcuts.activities.editor.headers

import androidx.compose.runtime.Stable

@Stable
sealed class RequestHeadersDialogState {
    @Stable
    data object AddHeader : RequestHeadersDialogState()

    @Stable
    data class EditHeader(
        val id: String,
        val key: String,
        val value: String,
    ) : RequestHeadersDialogState()
}
