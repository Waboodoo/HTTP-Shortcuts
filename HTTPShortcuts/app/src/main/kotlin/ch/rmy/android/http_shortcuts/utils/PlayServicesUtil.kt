package ch.rmy.android.http_shortcuts.utils

interface PlayServicesUtil {
    fun isPlayServicesAvailable(): Boolean

    suspend fun getLocation(): Location?

    data class Location(
        val latitude: Double?,
        val longitude: Double?,
        val accuracy: Float?,
    )
}
