package ch.rmy.android.http_shortcuts.activities.execute.usecases

import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling.Companion.FAILURE_OUTPUT_NONE
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling.Companion.SUCCESS_OUTPUT_NONE
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling.Companion.UI_TYPE_TOAST
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.hasFileParameter
import ch.rmy.android.http_shortcuts.utils.NetworkUtil
import ch.rmy.android.http_shortcuts.utils.PermissionManager
import javax.inject.Inject

class CheckHeadlessExecutionUseCase
@Inject
constructor(
    private val permissionManager: PermissionManager,
    private val networkUtil: NetworkUtil,
) {
    operator fun invoke(shortcut: Shortcut, variableValuesByIds: Map<VariableId, String> = emptyMap()): Boolean {
        val responseHandling = shortcut.responseHandling ?: return false
        val usesNoOutput = responseHandling.successOutput == SUCCESS_OUTPUT_NONE && responseHandling.failureOutput == FAILURE_OUTPUT_NONE
        val usesToastOutput = responseHandling.uiType == UI_TYPE_TOAST
        val usesCodeAfterExecution = shortcut.codeOnSuccess.isNotEmpty() || shortcut.codeOnFailure.isNotEmpty()
        val usesFiles = shortcut.usesGenericFileBody() || shortcut.hasFileParameter()
        val storesResponse = responseHandling.storeDirectoryId != null
        return (usesNoOutput || (usesToastOutput && permissionManager.hasNotificationPermission())) &&
            !usesCodeAfterExecution &&
            !usesFiles &&
            !storesResponse &&
            !shortcut.isWaitForNetwork &&
            shortcut.wifiSsid.isEmpty() &&
            !networkUtil.isNetworkPerformanceRestricted() &&
            computeVariablesSize(variableValuesByIds) < MAX_VARIABLES_SIZE
    }

    private fun computeVariablesSize(variableValuesByIds: Map<VariableId, String>): Int =
        variableValuesByIds.entries.sumOf { (id, value) -> id.length + value.length }

    companion object {
        private const val MAX_VARIABLES_SIZE = 8000
    }
}
