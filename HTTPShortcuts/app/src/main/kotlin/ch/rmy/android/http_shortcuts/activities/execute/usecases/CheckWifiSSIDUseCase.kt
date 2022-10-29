package ch.rmy.android.http_shortcuts.activities.execute.usecases

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.MissingLocationPermissionException
import ch.rmy.android.http_shortcuts.extensions.showAndAwaitDismissal
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import ch.rmy.android.http_shortcuts.utils.PermissionManager
import kotlinx.coroutines.CancellationException
import javax.inject.Inject

class CheckWifiSSIDUseCase
@Inject
constructor(
    private val activityProvider: ActivityProvider,
    private val networkUtil: NetworkUtil,
    private val permissionManager: PermissionManager,
) {
    suspend operator fun invoke(shortcutName: String, wifiSsid: String) {
        if (wifiSsid.isEmpty()) {
            return
        }
        showPermissionRationaleIfNeeded()
        requestLocationPermission()
        if (networkUtil.getCurrentSsid().orEmpty() != wifiSsid) {
            showWifiSwitcherDialog(shortcutName, wifiSsid)
            throw CancellationException()
        }
    }

    private suspend fun showPermissionRationaleIfNeeded() {
        if (permissionManager.shouldShowRationaleForLocationPermission()) {
            DialogBuilder(activityProvider.getActivity())
                .title(R.string.title_permission_dialog)
                .message(R.string.message_permission_rational)
                .positive(R.string.dialog_ok)
                .showAndAwaitDismissal()
        }
    }

    private suspend fun requestLocationPermission() {
        val granted = permissionManager.requestLocationPermissionIfNeeded()
        if (!granted) {
            throw MissingLocationPermissionException()
        }
    }

    private suspend fun showWifiSwitcherDialog(shortcutName: String, wifiSSID: String) {
        DialogBuilder(activityProvider.getActivity())
            .title(shortcutName)
            .message(Localizable.create { it.getString(R.string.message_wrong_wifi_network, wifiSSID) })
            .positive(R.string.action_label_select) {
                networkUtil.showWifiPicker()
            }
            .negative(R.string.dialog_cancel)
            .showAndAwaitDismissal()
    }
}
