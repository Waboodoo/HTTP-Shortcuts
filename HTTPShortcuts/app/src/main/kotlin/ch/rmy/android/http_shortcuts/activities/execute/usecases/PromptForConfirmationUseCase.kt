package ch.rmy.android.http_shortcuts.activities.execute.usecases

import ch.rmy.android.framework.extensions.resume
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.showOrElse
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.DialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import javax.inject.Inject

class PromptForConfirmationUseCase
@Inject
constructor(
    private val activityProvider: ActivityProvider,
) {
    suspend operator fun invoke(shortcutName: String) {
        withContext(Dispatchers.Main) {
            suspendCancellableCoroutine<Unit> { continuation ->
                DialogBuilder(activityProvider.getActivity())
                    .title(shortcutName)
                    .message(R.string.dialog_message_confirm_shortcut_execution)
                    .dismissListener {
                        continuation.cancel()
                    }
                    .positive(R.string.dialog_ok) {
                        continuation.resume()
                    }
                    .negative(R.string.dialog_cancel)
                    .showOrElse {
                        continuation.cancel()
                    }
            }
        }
    }
}
