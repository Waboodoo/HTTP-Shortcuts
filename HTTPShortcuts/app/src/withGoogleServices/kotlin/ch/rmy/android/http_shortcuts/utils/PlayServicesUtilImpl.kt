package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.location.Location
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.seconds

class PlayServicesUtilImpl(
    private val context: Context,
) : PlayServicesUtil {

    override fun isPlayServicesAvailable(): Boolean =
        GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

    override suspend fun getLocation(): PlayServicesUtil.Location? =
        suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()
            LocationServices.getFusedLocationProviderClient(context)
                .getCurrentLocation(
                    CurrentLocationRequest.Builder()
                        .setDurationMillis(MAX_LOOKUP_TIME.inWholeMilliseconds)
                        .build(),
                    cancellationTokenSource.token,
                )
                .addOnSuccessListener { location: Location? ->
                    continuation.resume(location?.toDataObject())
                }
                .addOnFailureListener { error ->
                    continuation.resumeWithException(error)
                }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }

    companion object {
        private val MAX_LOOKUP_TIME = 20.seconds

        internal fun Location.toDataObject() =
            PlayServicesUtil.Location(
                latitude = latitude,
                longitude = longitude,
                accuracy = if (hasAccuracy()) accuracy else null,
            )
    }
}
