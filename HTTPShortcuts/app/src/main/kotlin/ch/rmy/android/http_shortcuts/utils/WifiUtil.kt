package ch.rmy.android.http_shortcuts.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiManager
import androidx.appcompat.app.AppCompatActivity
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.showToast
import ch.rmy.android.http_shortcuts.extensions.startActivity

object WifiUtil {

    fun getCurrentSsid(context: Context): String =
        (context.applicationContext.getSystemService(AppCompatActivity.WIFI_SERVICE) as WifiManager)
            .connectionInfo
            .ssid
            ?.trim('"')
            ?: ""

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
