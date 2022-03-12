package ch.rmy.android.http_shortcuts.activities.variables.editor.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariablesViewModel

class GetContextMenuDialogUseCase {

    @CheckResult
    operator fun invoke(variableId: String, title: Localizable, viewModel: VariablesViewModel) =
        DialogState.create {
            title(title)
                .item(R.string.action_edit) {
                    viewModel.onEditOptionSelected(variableId)
                }
                .item(R.string.action_duplicate) {
                    viewModel.onDuplicateOptionSelected(variableId)
                }
                .item(R.string.action_delete) {
                    viewModel.onDeletionOptionSelected(variableId)
                }
                .build()
        }
}
