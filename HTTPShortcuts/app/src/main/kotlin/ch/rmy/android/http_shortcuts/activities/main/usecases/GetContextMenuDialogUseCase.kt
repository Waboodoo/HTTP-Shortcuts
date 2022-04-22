package ch.rmy.android.http_shortcuts.activities.main.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.ShortcutListViewModel
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId

class GetContextMenuDialogUseCase {

    @CheckResult
    operator fun invoke(shortcutId: ShortcutId, title: String, isPending: Boolean, isMovable: Boolean, viewModel: ShortcutListViewModel) =
        DialogState.create(DIALOG_ID) {
            title(title)
                .item(R.string.action_place) {
                    viewModel.onPlaceOnHomeScreenOptionSelected(shortcutId)
                }
                .item(R.string.action_run) {
                    viewModel.onExecuteOptionSelected(shortcutId)
                }
                .runIf(isPending) {
                    item(R.string.action_cancel_pending) {
                        viewModel.onCancelPendingExecutionOptionSelected(shortcutId)
                    }
                }
                .separator()
                .item(R.string.action_edit) {
                    viewModel.onEditOptionSelected(shortcutId)
                }
                .runIf(isMovable) {
                    item(R.string.action_move) {
                        viewModel.onMoveOptionSelected(shortcutId)
                    }
                }
                .item(R.string.action_duplicate) {
                    viewModel.onDuplicateOptionSelected(shortcutId)
                }
                .item(R.string.action_delete) {
                    viewModel.onDeleteOptionSelected(shortcutId)
                }
                .separator()
                .item(R.string.action_shortcut_information) {
                    viewModel.onShowInfoOptionSelected(shortcutId)
                }
                .item(R.string.action_export) {
                    viewModel.onExportOptionSelected(shortcutId)
                }
                .build()
        }

    companion object {
        private const val DIALOG_ID = "shortcut-context-menu"
    }
}
