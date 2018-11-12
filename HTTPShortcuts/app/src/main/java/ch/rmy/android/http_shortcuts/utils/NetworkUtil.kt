package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.RESTRICT_BACKGROUND_STATUS_ENABLED
import android.os.Build
import android.os.PowerManager


object NetworkUtil {

    fun isNetworkPerformanceRestricted(context: Context) =
            isDataSaveModeEnabled(context) || isBatterySaveModeEnabled(context)

    private fun isBatterySaveModeEnabled(context: Context): Boolean {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            powerManager.isPowerSaveMode
        } else {
            false
        }
    }

    private fun isDataSaveModeEnabled(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            (connectivityManager.isActiveNetworkMetered
                    && connectivityManager.restrictBackgroundStatus == RESTRICT_BACKGROUND_STATUS_ENABLED)
        } else {
            false
        }
    }

}