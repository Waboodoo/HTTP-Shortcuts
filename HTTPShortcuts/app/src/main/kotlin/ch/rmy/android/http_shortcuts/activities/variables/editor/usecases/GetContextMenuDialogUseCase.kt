package ch.rmy.android.http_shortcuts.activities.variables.editor.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariablesViewModel
import ch.rmy.android.http_shortcuts.data.domains.variables.VariableId
import ch.rmy.android.http_shortcuts.extensions.createDialogState
import javax.inject.Inject

class GetContextMenuDialogUseCase
@Inject
constructor() {

    @CheckResult
    operator fun invoke(variableId: VariableId, title: Localizable, viewModel: VariablesViewModel) =
        createDialogState {
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
