package ch.rmy.android.http_shortcuts.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import ch.rmy.android.http_shortcuts.activities.misc.second_launcher.SecondLauncherActivity
import javax.inject.Inject

class SecondaryLauncherManager
@Inject
constructor(
    private val context: Context,
) {
    fun setSecondaryLauncherVisibility(visible: Boolean) {
        context.packageManager.setComponentEnabledSetting(
            ComponentName(context, SecondLauncherActivity::class.java),
            if (visible) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP,
        )
    }
}
