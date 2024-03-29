package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetClipboardContentAction
@Inject
constructor(
    private val clipboardUtil: ClipboardUtil,
) : Action<Unit> {
    override suspend fun Unit.execute(executionContext: ExecutionContext): String? =
        withContext(Dispatchers.Main) {
            clipboardUtil.getFromClipboard()
                ?.toString()
                ?.takeUnlessEmpty()
        }
}
