package ch.rmy.android.http_shortcuts.activities.main.usecases

import androidx.annotation.CheckResult
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.MainViewModel
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.extensions.createDialogState
import javax.inject.Inject

class GetShortcutCreationDialogUseCase
@Inject
constructor() {

    @CheckResult
    operator fun invoke(viewModel: MainViewModel): DialogState =
        createDialogState(DIALOG_ID) {
            title(R.string.title_create_new_shortcut_options_dialog)
                .item(R.string.button_create_new) {
                    viewModel.onCreationDialogOptionSelected(ShortcutExecutionType.APP)
                }
                .item(R.string.button_curl_import) {
                    viewModel.onCurlImportOptionSelected()
                }
                .separator()
                .item(
                    nameRes = R.string.button_create_trigger_shortcut,
                    descriptionRes = R.string.button_description_create_trigger_shortcut,
                ) {
                    viewModel.onCreationDialogOptionSelected(ShortcutExecutionType.TRIGGER)
                }
                .item(
                    nameRes = R.string.button_create_browser_shortcut,
                    descriptionRes = R.string.button_description_create_browser_shortcut,
                ) {
                    viewModel.onCreationDialogOptionSelected(ShortcutExecutionType.BROWSER)
                }
                .item(
                    nameRes = R.string.button_create_scripting_shortcut,
                    descriptionRes = R.string.button_description_create_scripting_shortcut,
                ) {
                    viewModel.onCreationDialogOptionSelected(ShortcutExecutionType.SCRIPTING)
                }
                .neutral(R.string.dialog_help) {
                    viewModel.onCreationDialogHelpButtonClicked()
                }
                .build()
        }

    companion object {
        private const val DIALOG_ID = "shortcut-creation"
    }
}
