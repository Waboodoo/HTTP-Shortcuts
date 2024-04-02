package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.pm.PackageManager
import ch.rmy.android.http_shortcuts.plugin.TaskerIntent
import javax.inject.Inject

class IntegrationUtil
@Inject
constructor(
    private val context: Context,
) {
    fun isTaskerAvailable(): Boolean =
        TaskerIntent.isTaskerInstalled(context)

    fun isWireguardAvailable(): Boolean =
        try {
            context.packageManager.getPackageInfo("com.wireguard.android", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
}
