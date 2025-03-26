package ch.rmy.android.http_shortcuts.activities.execute.types

import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionParams
import ch.rmy.android.http_shortcuts.activities.execute.models.ExecutionStatus
import ch.rmy.android.http_shortcuts.data.models.RequestHeader
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.http.FileUploadManager
import ch.rmy.android.http_shortcuts.scripting.ResultHandler
import ch.rmy.android.http_shortcuts.scripting.ScriptExecutor
import ch.rmy.android.http_shortcuts.variables.VariableManager
import ch.rmy.android.http_shortcuts.variables.Variables
import kotlinx.coroutines.flow.Flow

abstract class ExecutionType {
    abstract operator fun invoke(
        params: ExecutionParams,
        shortcut: Shortcut,
        requestHeaders: List<RequestHeader>,
        requestParameters: List<RequestParameter>,
        variableManager: VariableManager,
        resultHandler: ResultHandler,
        fileUploadResult: FileUploadManager.Result?,
        dialogHandle: DialogHandle,
        scriptExecutor: ScriptExecutor,
    ): Flow<ExecutionStatus>

    protected fun injectVariables(string: String, variableManager: VariableManager): String =
        Variables.rawPlaceholdersToResolvedValues(string, variableManager.getVariableValuesByIds())
}
