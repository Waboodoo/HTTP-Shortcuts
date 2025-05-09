package ch.rmy.android.http_shortcuts.activities.execute.usecases

import ch.rmy.android.http_shortcuts.data.enums.ParameterType.FILE
import ch.rmy.android.http_shortcuts.data.enums.ResponseFailureOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseSuccessOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType.NOTIFICATION
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType.TOAST
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import ch.rmy.android.http_shortcuts.utils.PermissionManager
import ch.rmy.android.http_shortcuts.variables.ResolvedVariableValues
import javax.inject.Inject

class CheckHeadlessExecutionUseCase
@Inject
constructor(
    private val permissionManager: PermissionManager,
    private val networkUtil: NetworkUtil,
) {
    operator fun invoke(
        shortcut: Shortcut,
        requestParameters: List<RequestParameter>,
        variableValues: ResolvedVariableValues = ResolvedVariableValues.empty,
    ): Boolean {
        val usesNoOutput = shortcut.responseSuccessOutput == ResponseSuccessOutput.NONE &&
            shortcut.responseFailureOutput == ResponseFailureOutput.NONE
        val usesCodeAfterExecution = shortcut.codeOnSuccess.isNotEmpty() || shortcut.codeOnFailure.isNotEmpty()
        val usesFiles = shortcut.usesGenericFileBody() || (shortcut.usesRequestParameters() && requestParameters.any { it.parameterType == FILE })
        val storesResponse = shortcut.responseStoreDirectoryId != null
        return (usesNoOutput || (shortcut.responseUiType in setOf(TOAST, NOTIFICATION) && permissionManager.hasNotificationPermission())) &&
            !usesCodeAfterExecution &&
            !usesFiles &&
            !storesResponse &&
            !shortcut.isWaitForNetwork &&
            shortcut.wifiSsid.isNullOrEmpty() &&
            !networkUtil.isNetworkPerformanceRestricted() &&
            computeVariablesSize(variableValues) < MAX_VARIABLES_SIZE
    }

    private fun computeVariablesSize(variableValues: ResolvedVariableValues): Int =
        variableValues.globalVariableValues.computeSize() +
            variableValues.localVariablesValues.computeSize()

    private fun Map<String, String>.computeSize() =
        entries.sumOf { (key, value) -> key.length + value.length }

    companion object {
        private const val MAX_VARIABLES_SIZE = 8000
    }
}
