package ch.rmy.android.http_shortcuts.activities.remote_edit

import androidx.compose.runtime.Stable

@Stable
data class RemoteEditViewState(
    val dialogState: RemoteEditDialogState? = null,
    val deviceId: String,
    val password: String,
    val hostAddress: String,
) {
    val canUpload
        get() = password.isNotEmpty()

    val canDownload
        get() = password.isNotEmpty()
}
