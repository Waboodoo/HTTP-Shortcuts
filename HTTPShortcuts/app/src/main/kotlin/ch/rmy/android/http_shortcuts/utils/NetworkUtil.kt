package ch.rmy.android.http_shortcuts.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.R

object NetworkUtil {

    fun isNetworkConnected(context: Context): Boolean =
        context.getSystemService<ConnectivityManager>()
            ?.activeNetworkInfo
            ?.isConnected
            ?: false

    fun isNetworkPerformanceRestricted(context: Context) =
        isDataSaverModeEnabled(context) || isBatterySaverModeEnabled(context)

    private fun isBatterySaverModeEnabled(context: Context): Boolean =
        context.getSystemService<PowerManager>()
            ?.isPowerSaveMode
            ?: false

    private fun isDataSaverModeEnabled(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.getSystemService<ConnectivityManager>()
                ?.run { isActiveNetworkMetered && restrictBackgroundStatus == RESTRICT_BACKGROUND_STATUS_ENABLED }
                ?: false
        } else {
            false
        }

    fun getCurrentSsid(context: Context): String? =
        context.applicationContext.getSystemService<WifiManager>()
            ?.connectionInfo
            ?.ssid
            ?.trim('"')

    fun getIPV4Address(context: Context): String? =
        context.applicationContext.getSystemService<WifiManager>()
            ?.connectionInfo
            ?.ipAddress
            ?.takeUnless { it == 0 }
            ?.let(::formatIPV4Address)

    private fun formatIPV4Address(ip: Int): String =
        "${ip shr 0 and 0xFF}.${ip shr 8 and 0xFF}.${ip shr 16 and 0xFF}.${ip shr 24 and 0xFF}"

    fun showWifiPicker(activity: AppCompatActivity) {
        try {
            Intent(WifiManager.ACTION_PICK_WIFI_NETWORK)
                .putExtra("extra_prefs_show_button_bar", true)
                .putExtra("wifi_enable_next_on_connect", true)
                .startActivity(activity)
        } catch (e: ActivityNotFoundException) {
            activity.showToast(R.string.error_not_supported)
        }
    }
}
