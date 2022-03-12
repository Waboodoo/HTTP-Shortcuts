package ch.rmy.android.http_shortcuts.activities.variables.editor.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.variables.VariablesViewModel

class GetDeletionDialogUseCase {

    @CheckResult
    operator fun invoke(variableId: String, title: String, message: Localizable, viewModel: VariablesViewModel) =
        DialogState.create {
            title(title)
                .message(message)
                .positive(R.string.dialog_delete) {
                    viewModel.onDeletionConfirmed(variableId)
                }
                .negative(R.string.dialog_cancel)
                .build()
        }
}
