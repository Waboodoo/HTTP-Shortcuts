package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.text.Html
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import javax.inject.Inject

class HtmlDecodeAction
@Inject
constructor() : Action<HtmlDecodeAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext): String =
        Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY).toString()

    data class Params(
        val text: String,
    )
}
