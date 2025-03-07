package ch.rmy.android.http_shortcuts.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Criteria
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.CancellationSignal
import androidx.core.content.getSystemService
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull

class LocationLookup
@Inject
constructor(
    private val context: Context,
    private val playServicesUtil: PlayServicesUtil,
) {
    @SuppressLint("MissingPermission")
    suspend fun getLocation(): LocationData? {
        if (playServicesUtil.isPlayServicesAvailable()) {
            return playServicesUtil.getLocation()?.toDataObject()
        }

        val locationManager = context.getSystemService<LocationManager>()
            ?: return null
        val provider = locationManager.getBestProvider(
            Criteria().apply {
                accuracy = Criteria.ACCURACY_FINE
            },
            true,
        )

        if (provider != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return withTimeoutOrNull(MAX_LOOKUP_TIME) {
                suspendCancellableCoroutine<Location> { continuation ->
                    val cancellationSignal = CancellationSignal()
                    continuation.invokeOnCancellation {
                        cancellationSignal.cancel()
                    }
                    locationManager.getCurrentLocation(
                        provider,
                        cancellationSignal,
                        context.mainExecutor,
                    ) { t -> continuation.resume(t) }
                }
            }
                ?.toDataObject()
        }

        return withContext(Dispatchers.Default) {
            try {
                locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
                    ?.takeUnless { it.isStale() }
                    ?.toDataObject()
            } catch (e: IllegalArgumentException) {
                throw ActionException {
                    "Cannot determine device's location, location provider not available"
                }
            }
        }
    }

    data class LocationData(
        val latitude: Double?,
        val longitude: Double?,
        val accuracy: Float?,
    )

    companion object {
        private val MAX_LOOKUP_TIME = 20.seconds

        internal fun Location.isStale() =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                elapsedRealtimeAgeMillis
            } else {
                System.currentTimeMillis() - time
            }
                .milliseconds > 5.minutes

        internal fun Location.toDataObject() =
            LocationData(
                latitude = latitude,
                longitude = longitude,
                accuracy = if (hasAccuracy()) accuracy else null,
            )
    }
}
