package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.Intent
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.exceptions.DialogCancellationException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.PermissionManager
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SetWireguardTunnelStateAction
@Inject
constructor(
    private val activityProvider: ActivityProvider,
    private val permissionManager: PermissionManager,
) : Action<SetWireguardTunnelStateAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        val hadPermission = permissionManager.hasWireguardPermission()
        requestLocationPermissionIfNeeded()
        if (!hadPermission) {
            try {
                executionContext.dialogHandle.showDialog(
                    ExecuteDialogState.GenericMessage(
                        message = StringResLocalizable(R.string.wireguard_setup_instructions),
                    ),
                )
            } catch (e: DialogCancellationException) {
                // Ignore cancellation and continue
            }
        }
        sendBroadcast(tunnel, state)
    }

    private suspend fun requestLocationPermissionIfNeeded() {
        val granted = permissionManager.requestWireguardPermissionIfNeeded()
        if (!granted) {
            throw ActionException {
                getString(R.string.wireguard_permission_not_granted)
            }
        }
    }

    private suspend fun sendBroadcast(tunnel: String, state: Boolean) {
        val intent = Intent(if (state) ACTION_TUNNEL_UP else ACTION_TUNNEL_DOWN)
            .setPackage("com.wireguard.android")
            .setClassName("com.wireguard.android", "com.wireguard.android.model.TunnelManager\$IntentReceiver")
            .putExtra("tunnel", tunnel)
        withContext(Dispatchers.Main) {
            activityProvider.withActivity { activity ->
                activity.sendBroadcast(intent)
            }
        }
    }

    data class Params(
        val tunnel: String,
        val state: Boolean,
    )

    companion object {
        private const val ACTION_TUNNEL_UP = "com.wireguard.android.action.SET_TUNNEL_UP"
        private const val ACTION_TUNNEL_DOWN = "com.wireguard.android.action.SET_TUNNEL_DOWN"
    }
}
