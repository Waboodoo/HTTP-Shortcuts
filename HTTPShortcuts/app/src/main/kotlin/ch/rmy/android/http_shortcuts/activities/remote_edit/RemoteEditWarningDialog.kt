package ch.rmy.android.http_shortcuts.activities.remote_edit

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dialogs.DismissableDialog
import ch.rmy.android.http_shortcuts.utils.Settings

class RemoteEditWarningDialog(
    private val context: Context,
) : DismissableDialog(context, isCancelable = true) {

    private val settings: Settings = Settings(context)

    override val message: String
        get() = context.getString(R.string.warning_remote_edit)

    override var isPermanentlyDismissed: Boolean
        get() = settings.isRemoteEditWarningPermanentlyHidden
        set(value) {
            settings.isRemoteEditWarningPermanentlyHidden = value
        }
}