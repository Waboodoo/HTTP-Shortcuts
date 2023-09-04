package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.LocationLookup
import ch.rmy.android.http_shortcuts.utils.PermissionManager
import kotlinx.coroutines.CancellationException
import org.json.JSONObject
import javax.inject.Inject

class GetLocationAction
@Inject
constructor(
    private val locationLookup: LocationLookup,
    private val permissionManager: PermissionManager,
) : Action<Unit> {
    override suspend fun Unit.execute(executionContext: ExecutionContext): JSONObject {
        requestLocationPermissionIfNeeded()
        return fetchLocation()
    }

    private suspend fun requestLocationPermissionIfNeeded() {
        val granted = permissionManager.requestLocationPermissionIfNeeded()
        if (!granted) {
            throw ActionException {
                getString(R.string.error_failed_to_get_location)
            }
        }
    }

    private suspend fun fetchLocation(): JSONObject =
        try {
            locationLookup.getLocation().toResult()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logException(e)
            throw ActionException {
                getString(R.string.error_failed_to_get_location)
            }
        }

    companion object {
        internal fun LocationLookup.LocationData?.toResult(): JSONObject =
            when {
                this != null -> {
                    createJSONObject(
                        status = "success",
                        latitude = latitude,
                        longitude = longitude,
                        accuracy = accuracy,
                    )
                }
                else -> {
                    createJSONObject(
                        status = "unknown",
                    )
                }
            }

        private fun createJSONObject(
            status: String,
            latitude: Double? = null,
            longitude: Double? = null,
            accuracy: Float? = null,
        ): JSONObject =
            JSONObject(
                mapOf(
                    "status" to status,
                    "coordinates" to if (latitude != null && longitude != null) "$latitude,$longitude" else null,
                    "latitude" to latitude,
                    "longitude" to longitude,
                    "accuracy" to accuracy,
                )
            )
    }
}
