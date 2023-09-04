package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.ShareUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ShareTextAction
@Inject
constructor(
    private val activityProvider: ActivityProvider,
) : Action<ShareTextAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        withContext(Dispatchers.Main) {
            text
                .takeUnlessEmpty()
                ?.let {
                    ShareUtil.shareText(
                        activityProvider.getActivity(),
                        text.truncate(MAX_LENGTH),
                    )
                }
        }
    }

    data class Params(
        val text: String,
    )

    companion object {
        private const val MAX_LENGTH = 200_000
    }
}
