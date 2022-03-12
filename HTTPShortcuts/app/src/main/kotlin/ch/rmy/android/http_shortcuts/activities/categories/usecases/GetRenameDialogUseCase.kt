package ch.rmy.android.http_shortcuts.activities.categories.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.categories.CategoriesViewModel

class GetRenameDialogUseCase {

    @CheckResult
    operator fun invoke(categoryId: String, prefill: String, viewModel: CategoriesViewModel) =
        DialogState.create {
            title(R.string.title_rename_category)
                .textInput(
                    hint = context.getString(R.string.placeholder_category_name),
                    prefill = prefill,
                    allowEmpty = false,
                    maxLength = NAME_MAX_LENGTH
                ) { input ->
                    viewModel.onRenameDialogConfirmed(categoryId, newName = input)
                }
                .build()
        }

    companion object {
        private const val NAME_MAX_LENGTH = 20
    }
}
