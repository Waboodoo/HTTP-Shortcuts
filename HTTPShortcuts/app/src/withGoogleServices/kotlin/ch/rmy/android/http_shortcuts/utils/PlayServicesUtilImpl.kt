package ch.rmy.android.http_shortcuts.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailabilityLight
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.CancellationTokenSource
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.suspendCancellableCoroutine

class PlayServicesUtilImpl(
    private val context: Context,
) : PlayServicesUtil {

    override fun isPlayServicesAvailable(): Boolean =
        GoogleApiAvailabilityLight.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS

    @SuppressLint("MissingPermission")
    override suspend fun getLocation(): Location? =
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
                    continuation.resume(location)
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
    }
}
