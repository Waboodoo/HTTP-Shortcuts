package ch.rmy.android.http_shortcuts.activities.categories.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.categories.CategoriesViewModel

class GetDeletionDialogUseCase {

    @CheckResult
    operator fun invoke(categoryId: String, viewModel: CategoriesViewModel) =
        DialogState.create {
            message(R.string.confirm_delete_category_message)
                .positive(R.string.dialog_delete) {
                    viewModel.onCategoryDeletionConfirmed(categoryId)
                }
                .negative(R.string.dialog_cancel)
                .build()
        }
}
