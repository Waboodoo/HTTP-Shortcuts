package ch.rmy.android.http_shortcuts.activities.main.usecases

import ch.rmy.android.framework.utils.localization.Localizable
import ch.rmy.android.framework.viewmodel.viewstate.DialogState
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.main.ShortcutListViewModel
import ch.rmy.android.http_shortcuts.data.domains.shortcuts.ShortcutId
import javax.inject.Inject

class GetShortcutDeletionDialogUseCase
@Inject
constructor() {

    operator fun invoke(shortcutId: ShortcutId, title: Localizable, viewModel: ShortcutListViewModel) =
        DialogState.create {
            title(title)
                .message(R.string.confirm_delete_shortcut_message)
                .positive(R.string.dialog_delete) {
                    viewModel.onDeletionConfirmed(shortcutId)
                }
                .negative(R.string.dialog_cancel)
                .build()
        }
}
