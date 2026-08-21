package ch.rmy.android.http_shortcuts.utils

import android.Manifest
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.markodevcic.peko.PermissionRequester
import com.markodevcic.peko.allGranted
import javax.inject.Inject

class PermissionManager
@Inject
constructor(
    private val context: Context,
    private val activityProvider: ActivityProvider,
) {
    suspend fun requestLocationPermissionIfNeeded(): Boolean =
        requestPermissionIfNeeded(ACCESS_FINE_LOCATION)

    suspend fun requestNotificationPermissionIfNeeded(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionIfNeeded(POST_NOTIFICATIONS)
        } else {
            true
        }

    suspend fun requestTermuxPermissionIfNeeded(): Boolean =
        requestPermissionIfNeeded(TERMUX_PERMISSION)

    fun hasTermuxPermission(): Boolean =
        hasPermission(TERMUX_PERMISSION)

    suspend fun requestWireguardPermissionIfNeeded(): Boolean =
        requestPermissionIfNeeded(WIREGUARD_PERMISSION)

    fun hasWireguardPermission(): Boolean =
        hasPermission(WIREGUARD_PERMISSION)

    suspend fun shouldShowRationaleForLocationPermission(): Boolean =
        activityProvider.withActivity { activity ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, ACCESS_FINE_LOCATION)
        }

    fun hasNotificationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(POST_NOTIFICATIONS)
        } else {
            true
        }

    fun hasLocalNetworkPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.CINNAMON_BUN) {
            hasPermission(Manifest.permission.ACCESS_LOCAL_NETWORK)
        } else {
            true
        }

    private fun hasPermission(permission: String) =
        ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED

    private suspend fun requestPermissionIfNeeded(permission: String): Boolean {
        PermissionRequester.initialize(context)
        return PermissionRequester.instance()
            .request(permission)
            .allGranted()
    }

    companion object {
        private const val WIREGUARD_PERMISSION = "com.wireguard.android.permission.CONTROL_TUNNELS"
        private const val TERMUX_PERMISSION = "com.termux.permission.RUN_COMMAND"
    }
}
