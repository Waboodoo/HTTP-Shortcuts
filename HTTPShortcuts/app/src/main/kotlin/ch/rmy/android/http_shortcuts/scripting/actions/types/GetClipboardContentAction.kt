package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetClipboardContentAction
@Inject
constructor(
    private val clipboardUtil: ClipboardUtil,
    private val activityProvider: ActivityProvider,
) : Action<Unit> {
    override suspend fun Unit.execute(executionContext: ExecutionContext): String? =
        withContext(Dispatchers.Main) {
            activityProvider.withActivity {
                clipboardUtil.getFromClipboard()
                    ?.toString()
                    ?.takeUnlessEmpty()
            }
        }
}
