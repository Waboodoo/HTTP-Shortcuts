package ch.rmy.android.http_shortcuts.activities.execute.usecases

import ch.rmy.android.http_shortcuts.data.enums.ParameterType.FILE
import ch.rmy.android.http_shortcuts.data.enums.ResponseFailureOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseSuccessOutput
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType.NOTIFICATION
import ch.rmy.android.http_shortcuts.data.enums.ResponseUiType.TOAST
import ch.rmy.android.http_shortcuts.data.models.RequestParameter
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import ch.rmy.android.http_shortcuts.utils.PermissionManager
import ch.rmy.android.http_shortcuts.variables.ResolvedVariableValues
import javax.inject.Inject

class CheckHeadlessExecutionUseCase
@Inject
constructor(
    private val permissionManager: PermissionManager,
    private val networkUtil: NetworkUtil,
    private val userPreferences: UserPreferences,
) {
    operator fun invoke(
        shortcut: Shortcut,
        requestParameters: List<RequestParameter>,
        variableValues: ResolvedVariableValues = ResolvedVariableValues.empty,
    ): Boolean {
        if (userPreferences.isHeadlessModeDisabled) {
            return false
        }
        val usesNoOutput = shortcut.responseSuccessOutput == ResponseSuccessOutput.NONE &&
            shortcut.responseFailureOutput == ResponseFailureOutput.NONE
        val usesNotificationBasedOutput = shortcut.responseUiType in setOf(TOAST, NOTIFICATION) && permissionManager.hasNotificationPermission()
        if (!usesNoOutput && !usesNotificationBasedOutput) {
            return false
        }
        val usesCodeAfterExecution = shortcut.codeOnSuccess.isNotEmpty() || shortcut.codeOnFailure.isNotEmpty()
        if (usesCodeAfterExecution) {
            return false
        }
        val usesFiles = shortcut.usesGenericFileBody() || (shortcut.usesRequestParameters() && requestParameters.any { it.parameterType == FILE })
        if (usesFiles) {
            return false
        }
        val storesResponse = shortcut.responseStoreDirectoryId != null
        if (storesResponse) {
            return false
        }
        if (shortcut.isWaitForNetwork) {
            return false
        }
        if (!shortcut.wifiSsid.isNullOrEmpty()) {
            return false
        }
        if (networkUtil.isNetworkPerformanceRestricted()) {
            return false
        }
        return computeVariablesSize(variableValues) < MAX_VARIABLES_SIZE
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
