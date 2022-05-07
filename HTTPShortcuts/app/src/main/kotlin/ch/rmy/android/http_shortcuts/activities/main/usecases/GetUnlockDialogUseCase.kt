package ch.rmy.android.http_shortcuts.activities.main.usecases

import android.text.InputType
import androidx.annotation.CheckResult
import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.MainViewModel
import javax.inject.Inject

class GetUnlockDialogUseCase
@Inject
constructor() {

    @CheckResult
    operator fun invoke(viewModel: MainViewModel, message: Localizable) =
        DialogState.create {
            title(R.string.dialog_title_unlock_app)
                .message(message)
                .positive(R.string.button_unlock_app)
                .textInput(inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) { input ->
                    viewModel.onUnlockDialogSubmitted(input)
                }
                .negative(R.string.dialog_cancel)
                .build()
        }
}
