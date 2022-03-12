package ch.rmy.android.http_shortcuts.activities.icons.usecases

import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R

class GetBulkDeletionDialogUseCase {

    operator fun invoke(onDeletionConfirmed: () -> Unit) =
        DialogState.create {
            message(R.string.confirm_delete_all_unused_custom_icons_message)
                .positive(R.string.dialog_delete) {
                    onDeletionConfirmed()
                }
                .negative(R.string.dialog_cancel)
                .build()
        }
}
