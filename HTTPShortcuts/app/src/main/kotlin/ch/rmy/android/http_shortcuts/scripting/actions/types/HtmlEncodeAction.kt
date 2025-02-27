package ch.rmy.android.http_shortcuts.scripting.actions.types

import androidx.core.text.htmlEncode
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class HtmlEncodeAction
@Inject
constructor() : Action<HtmlEncodeAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): String =
        text.htmlEncode()

    data class Params(
        val text: String,
    )
}
