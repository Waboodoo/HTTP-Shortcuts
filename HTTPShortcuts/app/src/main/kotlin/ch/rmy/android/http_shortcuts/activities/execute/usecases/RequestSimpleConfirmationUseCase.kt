package ch.rmy.android.http_shortcuts.activities.execute.usecases

import ch.rmy.android.framework.extensions.toLocalizable
import ch.rmy.android.framework.utils.localization.StringResLocalizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.execute.DialogHandle
import ch.rmy.android.http_shortcuts.activities.execute.ExecuteDialogState
import javax.inject.Inject

class RequestSimpleConfirmationUseCase
@Inject
constructor() {
    suspend operator fun invoke(shortcutName: String, dialogHandle: DialogHandle) {
        dialogHandle.showDialog(
            ExecuteDialogState.GenericConfirm(
                title = shortcutName.toLocalizable(),
                message = StringResLocalizable(R.string.dialog_message_confirm_shortcut_execution),
            ),
        )
    }
}
