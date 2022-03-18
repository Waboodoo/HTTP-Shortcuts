package ch.rmy.android.http_shortcuts.activities.main.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.extensions.mapIf
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.ShortcutListViewModel

class GetContextMenuDialogUseCase {

    @CheckResult
    operator fun invoke(shortcutId: String, title: String, isPending: Boolean, isMovable: Boolean, viewModel: ShortcutListViewModel) =
        DialogState.create {
            title(title)
                .item(R.string.action_place) {
                    viewModel.onPlaceOnHomeScreenOptionSelected(shortcutId)
                }
                .item(R.string.action_run) {
                    viewModel.onExecuteOptionSelected(shortcutId)
                }
                .mapIf(isPending) {
                    item(R.string.action_cancel_pending) {
                        viewModel.onCancelPendingExecutionOptionSelected(shortcutId)
                    }
                }
                .separator()
                .item(R.string.action_edit) {
                    viewModel.onEditOptionSelected(shortcutId)
                }
                .mapIf(isMovable) {
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
}