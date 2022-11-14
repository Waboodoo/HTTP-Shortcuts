package ch.rmy.android.http_shortcuts.utils

import android.content.Context

class PlayServicesUtilImpl(@Suppress("UNUSED_PARAMETER") context: Context) : PlayServicesUtil {
    override fun isPlayServicesAvailable(): Boolean =
        false

    override suspend fun getLocation(): PlayServicesUtil.Location? {
        throw NotImplementedError()
    }
}
