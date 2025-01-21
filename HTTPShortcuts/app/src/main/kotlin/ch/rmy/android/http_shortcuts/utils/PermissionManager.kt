package ch.rmy.android.http_shortcuts.utils

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.POST_NOTIFICATIONS
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import com.markodevcic.peko.Peko
import com.markodevcic.peko.PermissionResult
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

    suspend fun requestWireguardPermissionIfNeeded(): Boolean =
        requestPermissionIfNeeded(WIREGUARD_PERMISSION)

    fun hasWireguardPermission(): Boolean =
        ActivityCompat.checkSelfPermission(context, WIREGUARD_PERMISSION) == PackageManager.PERMISSION_GRANTED

    suspend fun shouldShowRationaleForLocationPermission(): Boolean =
        activityProvider.withActivity { activity ->
            ActivityCompat.shouldShowRequestPermissionRationale(activity, ACCESS_FINE_LOCATION)
        }

    fun hasNotificationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(context, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

    private suspend fun requestPermissionIfNeeded(permission: String): Boolean =
        activityProvider.withActivity { activity ->
            Peko.requestPermissionsAsync(activity, permission) is PermissionResult.Granted
        }

    companion object {
        private const val WIREGUARD_PERMISSION = "com.wireguard.android.permission.CONTROL_TUNNELS"
    }
}
