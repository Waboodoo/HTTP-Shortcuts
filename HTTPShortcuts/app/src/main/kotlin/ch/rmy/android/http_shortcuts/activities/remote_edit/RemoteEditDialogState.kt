package ch.rmy.android.http_shortcuts.activities.remote_edit

import ch.rmy.android.framework.utils.localization.Localizable

sealed class RemoteEditDialogState {
    data class Progress(val text: Localizable) : RemoteEditDialogState()
    data class Error(val message: Localizable) : RemoteEditDialogState()
    data class EditServerUrl(
        val currentServerAddress: String,
    ) : RemoteEditDialogState()
}
