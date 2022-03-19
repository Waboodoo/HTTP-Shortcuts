package ch.rmy.android.http_shortcuts.activities.remote_edit

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState

data class RemoteEditViewState(
    val dialogState: DialogState? = null,
    val deviceId: String,
    val password: String,
    val instructions: Localizable,
) {
    val canUpload
        get() = password.isNotEmpty()

    val canDownload
        get() = password.isNotEmpty()
}
