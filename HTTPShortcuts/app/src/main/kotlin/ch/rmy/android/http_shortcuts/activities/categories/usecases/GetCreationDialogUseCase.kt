package ch.rmy.android.http_shortcuts.activities.categories.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.categories.CategoriesViewModel

class GetCreationDialogUseCase {

    @CheckResult
    operator fun invoke(viewModel: CategoriesViewModel) =
        DialogState.create {
            title(R.string.title_create_category)
                .textInput(
                    hint = context.getString(R.string.placeholder_category_name),
                    allowEmpty = false,
                    maxLength = NAME_MAX_LENGTH,
                    callback = viewModel::onCreateDialogConfirmed,
                )
                .build()
        }

    companion object {
        private const val NAME_MAX_LENGTH = 20
    }
}
