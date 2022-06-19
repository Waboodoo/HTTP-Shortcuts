package ch.rmy.android.http_shortcuts.utils

import ch.rmy.android.framework.utils.Destroyable

interface PlayServicesUtil {
    fun isPlayServicesAvailable(): Boolean

    fun getLocation(
        onSuccess: (Location?) -> Unit,
        onError: (Exception) -> Unit,
    ): Destroyable

    data class Location(
        val latitude: Double?,
        val longitude: Double?,
        val accuracy: Float?,
    )
}
