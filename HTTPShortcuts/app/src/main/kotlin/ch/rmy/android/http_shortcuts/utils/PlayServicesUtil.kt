package ch.rmy.android.http_shortcuts.utils

import android.location.Location

interface PlayServicesUtil {
    fun isPlayServicesAvailable(): Boolean

    suspend fun getLocation(): Location?
}
