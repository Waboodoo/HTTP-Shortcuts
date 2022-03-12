package ch.rmy.android.http_shortcuts.activities.main.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.MainViewModel

class GetToolbarTitleChangeDialogUseCase {

    @CheckResult
    operator fun invoke(viewModel: MainViewModel, oldTitle: String) =
        DialogState.create {
            title(R.string.title_set_title)
                .textInput(
                    prefill = oldTitle,
                    allowEmpty = true,
                    maxLength = TITLE_MAX_LENGTH,
                ) { newTitle ->
                    viewModel.onToolbarTitleChangeSubmitted(newTitle)
                }
                .build()
        }

    companion object {
        private const val TITLE_MAX_LENGTH = 50
    }
}
