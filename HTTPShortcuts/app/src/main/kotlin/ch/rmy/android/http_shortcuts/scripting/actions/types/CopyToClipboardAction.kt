package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CopyToClipboardAction
@Inject
constructor(
    private val clipboardUtil: ClipboardUtil,
) : Action<CopyToClipboardAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        withContext(Dispatchers.Main) {
            text
                .takeUnlessEmpty()
                ?.let(clipboardUtil::copyToClipboard)
        }
    }

    data class Params(
        val text: String,
    )
}
