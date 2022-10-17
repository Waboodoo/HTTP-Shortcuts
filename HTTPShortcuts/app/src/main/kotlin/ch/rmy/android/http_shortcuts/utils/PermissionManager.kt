package ch.rmy.android.http_shortcuts.utils

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import androidx.core.app.ActivityCompat
import com.markodevcic.peko.Peko
import com.markodevcic.peko.PermissionResult
import javax.inject.Inject

class PermissionManager
@Inject
constructor() {

    @Inject
    lateinit var activityProvider: ActivityProvider

    suspend fun requestFileStoragePermissionIfNeeded() =
        requestPermissionIfNeeded(READ_EXTERNAL_STORAGE)

    suspend fun requestLocationPermissionIfNeeded(): Boolean =
        requestPermissionIfNeeded(ACCESS_FINE_LOCATION)

    fun shouldShowRationaleForLocationPermission(): Boolean =
        ActivityCompat.shouldShowRequestPermissionRationale(activityProvider.getActivity(), ACCESS_FINE_LOCATION)

    private suspend fun requestPermissionIfNeeded(permission: String): Boolean =
        Peko.requestPermissionsAsync(activityProvider.getActivity(), permission) is PermissionResult.Granted
}
