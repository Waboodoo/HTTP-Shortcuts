package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Context
import android.net.wifi.WifiManager
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Single

class WifiIPAction : BaseAction() {

    override fun executeForValue(executionContext: ExecutionContext): Single<String> =
        Single.fromCallable {
            getIPAddress(executionContext.context)
        }

    private fun getIPAddress(context: Context): String =
        (context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager)
            .connectionInfo
            .ipAddress
            .takeUnless { it == 0 }
            ?.let { ipAddress ->
                formatIPAddress(ipAddress)
            }
            ?: ""

    companion object {
        private fun formatIPAddress(ip: Int): String =
            (ip shr 0 and 0xFF).toString() + "." +
                (ip shr 8 and 0xFF) + "." +
                (ip shr 16 and 0xFF) + "." +
                (ip shr 24 and 0xFF)
    }

}