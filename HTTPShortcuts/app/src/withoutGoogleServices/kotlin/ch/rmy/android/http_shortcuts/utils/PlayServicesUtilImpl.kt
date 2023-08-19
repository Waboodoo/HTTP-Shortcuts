package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.location.Location

class PlayServicesUtilImpl(@Suppress("UNUSED_PARAMETER") context: Context) : PlayServicesUtil {
    override fun isPlayServicesAvailable(): Boolean =
        false

    override suspend fun getLocation(): Location? {
        throw NotImplementedError()
    }
}
