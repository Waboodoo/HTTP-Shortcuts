package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.ShareUtil
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ShareTextAction
@Inject
constructor(
    private val activityProvider: ActivityProvider,
    private val shareUtil: ShareUtil,
) : Action<ShareTextAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        withContext(Dispatchers.Main) {
            text
                .takeUnlessEmpty()
                ?.let {
                    activityProvider.withActivity { activity ->
                        shareUtil.shareText(
                            activity,
                            text.truncate(MAX_LENGTH),
                        )
                    }
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
