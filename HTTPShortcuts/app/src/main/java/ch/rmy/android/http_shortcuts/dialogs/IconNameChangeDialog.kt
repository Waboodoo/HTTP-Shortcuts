package ch.rmy.android.http_shortcuts.dialogs

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.Settings

class IconNameChangeDialog(private val context: Context) : DismissableDialog(context) {

    private val settings: Settings = Settings(context)

    override val message: String
        get() = context.getString(R.string.warning_icon_or_name_change)

    override var isPermanentlyDismissed: Boolean
        get() = settings.isIconNameWarningPermanentlyHidden
        set(value) {
            settings.isIconNameWarningPermanentlyHidden = value
        }

}
