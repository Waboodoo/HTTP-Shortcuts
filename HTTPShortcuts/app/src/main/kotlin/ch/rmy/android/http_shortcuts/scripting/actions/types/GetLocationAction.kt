package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.PermissionManager
import ch.rmy.android.http_shortcuts.utils.PlayServicesUtil
import kotlinx.coroutines.CancellationException
import org.json.JSONObject
import javax.inject.Inject

class GetLocationAction : BaseAction() {

    @Inject
    lateinit var playServicesUtil: PlayServicesUtil

    @Inject
    lateinit var permissionManager: PermissionManager

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext): JSONObject {
        checkForPlayServices()
        requestLocationPermissionIfNeeded()
        return fetchLocation()
    }

    private fun checkForPlayServices() {
        if (!playServicesUtil.isPlayServicesAvailable()) {
            throw ActionException {
                // TODO: Localize
                "Play Services are required to get the device's location, but they are not installed or not available."
            }
        }
    }

    private suspend fun requestLocationPermissionIfNeeded() {
        val granted = permissionManager.requestLocationPermissionIfNeeded()
        if (!granted) {
            throw ActionException { context ->
                context.getString(R.string.error_failed_to_get_location)
            }
        }
    }

    private suspend fun fetchLocation(): JSONObject =
        try {
            playServicesUtil.getLocation().toResult()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logException(e)
            throw ActionException { context ->
                context.getString(R.string.error_failed_to_get_location)
            }
        }

    companion object {
        private fun PlayServicesUtil.Location?.toResult(): JSONObject =
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
