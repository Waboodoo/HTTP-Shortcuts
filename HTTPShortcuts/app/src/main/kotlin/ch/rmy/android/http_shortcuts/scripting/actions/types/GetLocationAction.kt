package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.LocationLookup
import ch.rmy.android.http_shortcuts.utils.PermissionManager
import ch.rmy.android.scripting.JsObject
import ch.rmy.android.scripting.ScriptingEngine
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class GetLocationAction
@Inject
constructor(
    private val locationLookup: LocationLookup,
    private val permissionManager: PermissionManager,
) : Action<Unit> {
    override suspend fun Unit.execute(executionContext: ExecutionContext): JsObject? {
        requestLocationPermissionIfNeeded()
        return fetchLocation()?.toResult(executionContext.scriptingEngine)
    }

    private suspend fun requestLocationPermissionIfNeeded() {
        val granted = permissionManager.requestLocationPermissionIfNeeded()
        if (!granted) {
            throw ActionException {
                getString(R.string.error_failed_to_get_location)
            }
        }
    }

    private suspend fun fetchLocation(): LocationLookup.LocationData? =
        try {
            locationLookup.getLocation()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logException(e)
            throw ActionException {
                getString(R.string.error_failed_to_get_location)
            }
        }

    internal fun LocationLookup.LocationData?.toResult(scriptingEngine: ScriptingEngine): JsObject {
        val location = this
        return scriptingEngine.buildJsObject {
            property("status", if (location != null) "success" else "unknown")
            property("coordinates", if (location?.latitude != null && location.longitude != null) "$latitude,$longitude" else null)
            property("latitude", location?.latitude)
            property("longitude", location?.longitude)
            property("accuracy", location?.accuracy)
        }
    }
}
