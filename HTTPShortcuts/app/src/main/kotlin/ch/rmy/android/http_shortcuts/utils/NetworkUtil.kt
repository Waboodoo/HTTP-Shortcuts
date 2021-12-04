package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED
import android.net.wifi.WifiManager
import android.os.Build
import android.os.PowerManager

object NetworkUtil {

    fun isNetworkConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return connectivityManager.activeNetworkInfo?.isConnected == true
    }

    fun isNetworkPerformanceRestricted(context: Context) =
        isDataSaveModeEnabled(context) || isBatterySaveModeEnabled(context)

    private fun isBatterySaveModeEnabled(context: Context): Boolean =
        (context.getSystemService(Context.POWER_SERVICE) as PowerManager).isPowerSaveMode

    private fun isDataSaveModeEnabled(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            (
                connectivityManager.isActiveNetworkMetered &&
                    connectivityManager.restrictBackgroundStatus == RESTRICT_BACKGROUND_STATUS_ENABLED
                )
        } else {
            false
        }

    fun getIPV4Address(context: Context): String? =
        (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .connectionInfo
            .ipAddress
            .takeUnless { it == 0 }
            ?.let(::formatIPV4Address)

    private fun formatIPV4Address(ip: Int): String =
        (ip shr 0 and 0xFF).toString() + "." +
            (ip shr 8 and 0xFF) + "." +
            (ip shr 16 and 0xFF) + "." +
            (ip shr 24 and 0xFF)
}
