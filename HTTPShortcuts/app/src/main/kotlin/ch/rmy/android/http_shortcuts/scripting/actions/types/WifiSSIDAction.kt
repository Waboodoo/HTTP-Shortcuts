package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.Manifest
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import ch.rmy.android.framework.extensions.resume
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.extensions.showOrElse
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import ch.rmy.android.http_shortcuts.utils.PermissionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject

class WifiSSIDAction : BaseAction() {

    @Inject
    lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var activityProvider: ActivityProvider

    @Inject
    lateinit var networkUtil: NetworkUtil

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext): String? {
        ensureLocationPermissionIsEnabled(activityProvider.getActivity())
        return networkUtil.getCurrentSsid()
    }

    private suspend fun ensureLocationPermissionIsEnabled(activity: FragmentActivity) {
        withContext(Dispatchers.Main) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                suspendCancellableCoroutine<Unit> { continuation ->
                    DialogBuilder(activity)
                        .title(activity.getString(R.string.title_permission_dialog))
                        .message(activity.getString(R.string.message_permission_rational))
                        .positive(R.string.dialog_ok)
                        .dismissListener {
                            continuation.resume()
                        }
                        .showOrElse {
                            continuation.cancel()
                        }
                }
            }
            requestLocationPermissionIfNeeded()
        }
    }

    private suspend fun requestLocationPermissionIfNeeded() {
        val granted = permissionManager.requestLocationPermissionIfNeeded()
        if (!granted) {
            throw ActionException { context ->
                context.getString(R.string.error_failed_to_get_wifi_ssid)
            }
        }
    }
}
