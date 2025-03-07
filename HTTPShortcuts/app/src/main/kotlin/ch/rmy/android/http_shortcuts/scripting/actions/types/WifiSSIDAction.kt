package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.Manifest
import androidx.core.app.ActivityCompat
import androidx.fragment.app.FragmentActivity
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import ch.rmy.android.http_shortcuts.utils.PermissionManager
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WifiSSIDAction
@Inject
constructor(
    private val permissionManager: PermissionManager,
    private val activityProvider: ActivityProvider,
    private val networkUtil: NetworkUtil,
) : Action<Unit> {
    override suspend fun Unit.execute(executionContext: ExecutionContext): String? {
        activityProvider.withActivity { activity ->
            ensureLocationPermissionIsEnabled(activity, executionContext.dialogHandle)
        }
        return networkUtil.getCurrentSsid()
    }

    private suspend fun ensureLocationPermissionIsEnabled(activity: FragmentActivity, dialogHandle: DialogHandle) {
        withContext(Dispatchers.Main) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                dialogHandle.showDialog(
                    ExecuteDialogState.GenericConfirm(
                        title = StringResLocalizable(R.string.title_permission_dialog),
                        message = StringResLocalizable(R.string.message_permission_rational),
                    ),
                )
            }
            requestLocationPermissionIfNeeded()
        }
    }

    private suspend fun requestLocationPermissionIfNeeded() {
        val granted = permissionManager.requestLocationPermissionIfNeeded()
        if (!granted) {
            throw ActionException {
                getString(R.string.error_failed_to_get_wifi_ssid)
            }
        }
    }
}
