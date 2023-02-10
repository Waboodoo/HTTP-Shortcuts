package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import androidx.core.content.getSystemService
import androidx.core.net.toUri
import javax.inject.Inject

class RestrictionsUtil
@Inject
constructor(
    private val context: Context,
) {
    private val packageName: String
        get() = context.packageName

    fun isBatterySaverModeEnabled(): Boolean =
        context.getSystemService<PowerManager>()
            ?.isPowerSaveMode
            ?: false

    fun isDataSaverModeEnabled(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.getSystemService<ConnectivityManager>()
                ?.run { isActiveNetworkMetered && restrictBackgroundStatus == ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED }
                ?: false
        } else false

    fun canRequestIgnoreBatteryOptimization(): Boolean =
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.M

    fun getRequestIgnoreBatteryOptimizationIntent(): Intent? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, "package:$packageName".toUri())
        } else null

    fun isIgnoringBatteryOptimizations(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            context.getSystemService<PowerManager>()
                ?.isIgnoringBatteryOptimizations(packageName)
                ?: true
        } else true

    fun hasPermissionEditor() =
        Build.MANUFACTURER?.equals("xiaomi", ignoreCase = true) == true

    fun getPermissionEditorIntent(): Intent =
        Intent("miui.intent.action.APP_PERM_EDITOR")
            .setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity")
            .putExtra("extra_pkgname", packageName)
}
