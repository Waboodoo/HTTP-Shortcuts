package ch.rmy.android.http_shortcuts.activities.execute.types

import ch.rmy.android.framework.extensions.logException
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionStatus
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.http.FileUploadManager
import ch.rmy.android.http_shortcuts.scripting.ResultHandler
import ch.rmy.android.http_shortcuts.scripting.ScriptExecutor
import ch.rmy.android.http_shortcuts.utils.WakeOnLanUtil
import ch.rmy.android.http_shortcuts.variables.VariableManager
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class WakeOnLanExecutionType
@Inject
constructor(
    private val wakeOnLanUtil: WakeOnLanUtil,
) : ExecutionType() {
    override fun invoke(
        params: ExecutionParams,
        shortcut: Shortcut,
        requestHeaders: List<RequestHeader>,
        requestParameters: List<RequestParameter>,
        variableManager: VariableManager,
        resultHandler: ResultHandler,
        fileUploadResult: FileUploadManager.Result?,
        dialogHandle: DialogHandle,
        scriptExecutor: ScriptExecutor,
    ): Flow<ExecutionStatus> =
        flow {
            val macAddress = injectVariables(shortcut.wolMacAddress, variableManager)
            try {
                wakeOnLanUtil.send(
                    macAddress = macAddress,
                    ipAddress = shortcut.wolBroadcastAddress.takeUnlessEmpty(),
                    port = shortcut.wolPort,
                )
            } catch (e: CancellationException) {
                throw e
            } catch (_: WakeOnLanUtil.InvalidMACAddressException) {
                throw UserException.create {
                    getString(R.string.error_action_type_send_wol_invalid_mac_address, macAddress)
                }
            } catch (e: Exception) {
                logException(e)
                throw UserException.create {
                    getString(R.string.error_action_type_send_wol_failed, e.message)
                }
            }
            emit(
                ExecutionStatus.WrappingUp(
                    variableManager.getVariableValuesByIds(),
                    result = resultHandler.getResult(),
                ),
            )
        }
}
