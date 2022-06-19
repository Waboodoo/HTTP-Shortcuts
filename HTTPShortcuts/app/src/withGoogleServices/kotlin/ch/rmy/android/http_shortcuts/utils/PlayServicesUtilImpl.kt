package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.location.Location
import ch.rmy.android.framework.utils.Destroyable
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.time.Duration.Companion.seconds

class PlayServicesUtilImpl(
    private val context: Context,
) : PlayServicesUtil {

    override fun isPlayServicesAvailable(): Boolean =
        GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

    override fun getLocation(
        onSuccess: (PlayServicesUtil.Location?) -> Unit,
        onError: (Exception) -> Unit,
    ): Destroyable {
        val cancellationTokenSource = CancellationTokenSource()
        LocationServices.getFusedLocationProviderClient(context)
            .getCurrentLocation(
                CurrentLocationRequest.Builder()
                    .setDurationMillis(MAX_LOOKUP_TIME.inWholeMilliseconds)
                    .build(),
                cancellationTokenSource.token,
            )
            .addOnSuccessListener { location: Location? ->
                onSuccess(location?.toDataObject())
            }
            .addOnFailureListener(onError)

        return object : Destroyable {
            override fun destroy() {
                cancellationTokenSource.cancel()
            }
        }
    }

    companion object {
        private val MAX_LOOKUP_TIME = 20.seconds

        private fun Location.toDataObject() =
            PlayServicesUtil.Location(
                latitude = latitude,
                longitude = longitude,
                accuracy = if (hasAccuracy()) accuracy else null,
            )
    }
}
