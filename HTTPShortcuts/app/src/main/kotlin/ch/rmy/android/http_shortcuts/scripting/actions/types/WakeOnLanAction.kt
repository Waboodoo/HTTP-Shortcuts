package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.WakeOnLanUtil
import javax.inject.Inject
import kotlinx.coroutines.CancellationException

class WakeOnLanAction
@Inject
constructor(
    private val wakeOnLanUtil: WakeOnLanUtil,
) : Action<WakeOnLanAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        try {
            wakeOnLanUtil.send(
                macAddress = macAddress,
                ipAddress = ipAddress,
                port = port,
            )
        } catch (e: CancellationException) {
            throw e
        } catch (_: WakeOnLanUtil.InvalidMACAddressException) {
            throw ActionException {
                getString(R.string.error_action_type_send_wol_invalid_mac_address, macAddress)
            }
        } catch (e: Exception) {
            logException(e)
            throw ActionException {
                getString(R.string.error_action_type_send_wol_failed, e.message)
            }
        }
    }

    data class Params(
        val macAddress: String,
        val ipAddress: String?,
        val port: Int?,
    )
}
