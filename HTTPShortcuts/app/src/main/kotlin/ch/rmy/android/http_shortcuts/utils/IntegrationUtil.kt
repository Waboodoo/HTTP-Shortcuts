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

    fun isTermuxAvailable(): Boolean =
        isAppInstalled("com.termux")

    fun isWireguardAvailable(): Boolean =
        isAppInstalled("com.wireguard.android")

    private fun isAppInstalled(packageName: String) =
        try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: PackageManager.NameNotFoundException) {
            false
        }
}
