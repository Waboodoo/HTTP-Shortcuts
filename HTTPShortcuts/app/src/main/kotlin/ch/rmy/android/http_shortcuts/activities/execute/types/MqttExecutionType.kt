package ch.rmy.android.http_shortcuts.activities.execute.types

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionStatus
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.http.FileUploadManager
import ch.rmy.android.http_shortcuts.scripting.ResultHandler
import ch.rmy.android.http_shortcuts.scripting.ScriptExecutor
import ch.rmy.android.http_shortcuts.utils.MqttUtil
import ch.rmy.android.http_shortcuts.variables.VariableManager
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MqttExecutionType
@Inject
constructor(
    private val mqttUtil: MqttUtil,
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
            val username = injectVariables(shortcut.authUsername, variableManager)
            val password = injectVariables(shortcut.authPassword, variableManager)
            val useAuthentication = username.isNotEmpty() || password.isNotEmpty()
            try {
                mqttUtil.sendMessages(
                    serverUri = injectVariables(shortcut.url, variableManager),
                    username = username.takeIf { useAuthentication },
                    password = password.takeIf { useAuthentication },
                    messages = MqttUtil.getMessagesFromBody(injectVariables(shortcut.bodyContent, variableManager)),
                )
            } catch (e: MqttUtil.MqttUtilException) {
                throw ActionException {
                    getString(R.string.error_failed_to_send_mqtt, e.message)
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
