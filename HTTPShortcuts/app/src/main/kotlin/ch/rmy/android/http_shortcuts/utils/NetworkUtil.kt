package ch.rmy.android.http_shortcuts.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import androidx.core.content.getSystemService
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.R
import javax.inject.Inject

class NetworkUtil
@Inject
constructor(
    private val context: Context,
    private val activityProvider: ActivityProvider,
    private val restrictionsUtil: RestrictionsUtil,
) {

    fun isNetworkConnected(): Boolean =
        context.getSystemService<ConnectivityManager>()
            ?.activeNetworkInfo
            ?.isConnected
            ?: false

    fun isNetworkPerformanceRestricted() =
        restrictionsUtil.isDataSaverModeEnabled() || restrictionsUtil.isBatterySaverModeEnabled()

    fun getCurrentSsid(): String? =
        context.applicationContext.getSystemService<WifiManager>()
            ?.connectionInfo
            ?.ssid
            ?.trim('"')

    fun getIPV4Address(): String? =
        context.applicationContext.getSystemService<WifiManager>()
            ?.connectionInfo
            ?.ipAddress
            ?.takeUnless { it == 0 }
            ?.let(::formatIPV4Address)

    private fun formatIPV4Address(ip: Int): String =
        "${ip shr 0 and 0xFF}.${ip shr 8 and 0xFF}.${ip shr 16 and 0xFF}.${ip shr 24 and 0xFF}"

    fun showWifiPicker() {
        try {
            Intent(WifiManager.ACTION_PICK_WIFI_NETWORK)
                .putExtra("extra_prefs_show_button_bar", true)
                .putExtra("wifi_enable_next_on_connect", true)
                .startActivity(activityProvider.getActivity())
        } catch (e: ActivityNotFoundException) {
            context.showToast(R.string.error_not_supported)
        }
    }
}
