package ch.rmy.android.http_shortcuts.activities.icons.usecases

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.createDialogState
import javax.inject.Inject

class GetBulkDeletionDialogUseCase
@Inject
constructor() {

    operator fun invoke(onDeletionConfirmed: () -> Unit) =
        createDialogState {
            message(R.string.confirm_delete_all_unused_custom_icons_message)
                .positive(R.string.dialog_delete) {
                    onDeletionConfirmed()
                }
                .negative(R.string.dialog_cancel)
                .build()
        }
}
