package ch.rmy.android.http_shortcuts.activities.execute.usecases

import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.showAndAwaitDismissal
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import javax.inject.Inject

class ShowShortcutNotFoundDialogUseCase
@Inject
constructor(
    private val activityProvider: ActivityProvider,
) {
    suspend operator fun invoke() {
        DialogBuilder(activityProvider.getActivity())
            .title(R.string.dialog_title_error)
            .message(R.string.shortcut_not_found)
            .positive(R.string.dialog_ok)
            .showAndAwaitDismissal()
    }
}
