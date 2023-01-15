package ch.rmy.android.http_shortcuts.activities.editor.executionsettings.usecases

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.createDialogState
import javax.inject.Inject

class GetAppOverlayDialogUseCase
@Inject
constructor() {

    operator fun invoke(onConfirm: () -> Unit): DialogState =
        createDialogState {
            message(
                Localizable.create { context ->
                    context.getString(R.string.message_run_repeatedly_dialog_configure_app_overlay, context.getString(R.string.dialog_configure))
                }
            )
                .positive(R.string.dialog_configure) {
                    onConfirm()
                }
                .build()
        }
}
