package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import ch.rmy.android.framework.utils.Destroyable

class PlayServicesUtilImpl(@Suppress("UNUSED_PARAMETER") context: Context) : PlayServicesUtil {
    override fun isPlayServicesAvailable(): Boolean =
        false

    override fun getLocation(onSuccess: (PlayServicesUtil.Location?) -> Unit, onError: (Exception) -> Unit): Destroyable {
        throw NotImplementedError()
    }
}
