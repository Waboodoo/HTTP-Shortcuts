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
}
