package ch.rmy.android.http_shortcuts.activities.icons.usecases

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.createDialogState
import ch.rmy.android.http_shortcuts.icons.ShortcutIcon
import javax.inject.Inject

class GetDeletionDialogUseCase
@Inject
constructor() {

    operator fun invoke(icon: ShortcutIcon.CustomIcon, message: Localizable, onDeletionConfirmed: (ShortcutIcon.CustomIcon) -> Unit) =
        createDialogState {
            message(message)
                .positive(R.string.dialog_delete) {
                    onDeletionConfirmed(icon)
                }
                .negative(R.string.dialog_cancel)
                .build()
        }
}
