package ch.rmy.android.http_shortcuts.activities.execute.usecases

import ch.rmy.android.http_shortcuts.data.models.ResponseHandlingModel.Companion.FAILURE_OUTPUT_NONE
import ch.rmy.android.http_shortcuts.data.models.ResponseHandlingModel.Companion.SUCCESS_OUTPUT_NONE
import ch.rmy.android.http_shortcuts.data.models.ResponseHandlingModel.Companion.UI_TYPE_TOAST
import ch.rmy.android.http_shortcuts.data.models.ShortcutModel
import ch.rmy.android.http_shortcuts.utils.PermissionManager
import javax.inject.Inject

class CheckHeadlessExecutionUseCase
@Inject
constructor(
    private val permissionManager: PermissionManager,
) {
    operator fun invoke(shortcut: ShortcutModel): Boolean {
        val responseHandling = shortcut.responseHandling ?: return false
        val usesNoOutput = responseHandling.successOutput == SUCCESS_OUTPUT_NONE && responseHandling.failureOutput == FAILURE_OUTPUT_NONE
        val usesToastOutput = responseHandling.uiType == UI_TYPE_TOAST
        val usesCodeAfterExecution = shortcut.codeOnSuccess.isNotEmpty() || shortcut.codeOnFailure.isNotEmpty()
        return (usesNoOutput || (usesToastOutput && permissionManager.hasNotificationPermission())) &&
            !usesCodeAfterExecution &&
            !shortcut.isWaitForNetwork
    }
}
