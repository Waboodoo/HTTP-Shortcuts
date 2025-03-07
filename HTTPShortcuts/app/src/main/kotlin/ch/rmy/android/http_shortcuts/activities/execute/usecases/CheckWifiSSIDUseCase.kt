package ch.rmy.android.http_shortcuts.activities.execute.usecases

import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.exceptions.MissingLocationPermissionException
import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import ch.rmy.android.http_shortcuts.utils.PermissionManager
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class CheckWifiSSIDUseCase
@Inject
constructor(
    private val networkUtil: NetworkUtil,
    private val permissionManager: PermissionManager,
) {
    suspend operator fun invoke(shortcutName: String, wifiSsid: String, dialogHandle: DialogHandle) {
        if (wifiSsid.isEmpty()) {
            return
        }
        showPermissionRationaleIfNeeded(dialogHandle)
        requestLocationPermission()
        if (networkUtil.getCurrentSsid().orEmpty() != wifiSsid) {
            showWifiSwitcherDialog(shortcutName, wifiSsid, dialogHandle)
            throw CancellationException("Cancelling because not connected to the desired Wi-Fi")
        }
    }

    private suspend fun showPermissionRationaleIfNeeded(dialogHandle: DialogHandle) {
        if (permissionManager.shouldShowRationaleForLocationPermission()) {
            dialogHandle.showDialog(
                ExecuteDialogState.GenericConfirm(
                    title = StringResLocalizable(R.string.title_permission_dialog),
                    message = StringResLocalizable(R.string.message_permission_rational),
                ),
            )
        }
    }

    private suspend fun requestLocationPermission() {
        val granted = permissionManager.requestLocationPermissionIfNeeded()
        if (!granted) {
            throw MissingLocationPermissionException()
        }
    }

    private suspend fun showWifiSwitcherDialog(shortcutName: String, wifiSSID: String, dialogHandle: DialogHandle) {
        dialogHandle.showDialog(
            ExecuteDialogState.GenericConfirm(
                title = shortcutName.toLocalizable(),
                message = StringResLocalizable(R.string.message_wrong_wifi_network, wifiSSID),
                confirmButton = StringResLocalizable(R.string.action_label_select),
            ),
        )
        networkUtil.showWifiPicker()
    }
}
