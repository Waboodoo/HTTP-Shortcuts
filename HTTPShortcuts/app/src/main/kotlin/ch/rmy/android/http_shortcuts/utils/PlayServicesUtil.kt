package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import javax.inject.Inject

class PlayServicesUtil
@Inject
constructor(
    private val context: Context,
) {
    fun isPlayServicesAvailable(): Boolean =
        GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS
}
