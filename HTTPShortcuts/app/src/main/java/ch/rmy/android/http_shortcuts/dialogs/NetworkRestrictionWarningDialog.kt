package ch.rmy.android.http_shortcuts.dialogs

import android.content.Context
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import ch.rmy.android.http_shortcuts.utils.Settings

class NetworkRestrictionWarningDialog(private val context: Context) : DismissableDialog(context) {

    private val settings: Settings = Settings(context)

    override fun shouldShow(): Boolean =
        NetworkUtil.isNetworkPerformanceRestricted(context) && super.shouldShow()

    override val message: String
        get() = context.getString(R.string.warning_data_saver_battery_saver_enabled)

    override var isPermanentlyDismissed: Boolean
        get() = settings.isNetworkRestrictionWarningPermanentlyHidden
        set(value) {
            settings.isNetworkRestrictionWarningPermanentlyHidden = value
        }
}