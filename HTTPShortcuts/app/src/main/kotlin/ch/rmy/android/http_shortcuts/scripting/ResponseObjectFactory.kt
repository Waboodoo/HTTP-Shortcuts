package ch.rmy.android.http_shortcuts.scripting

import android.content.Context
import ch.rmy.android.framework.extensions.getCaseInsensitive
import ch.rmy.android.http_shortcuts.exceptions.ResponseTooLargeException
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import ch.rmy.android.scripting.JsObject
import ch.rmy.android.scripting.ScriptingEngine
import javax.inject.Inject

class ResponseObjectFactory
@Inject
constructor(
    private val context: Context,
) {
    fun create(scriptingEngine: ScriptingEngine, response: ShortcutResponse): JsObject =
        scriptingEngine.buildJsObject {
            property(
                "body",
                try {
                    response.getContentAsString(this@ResponseObjectFactory.context)
                } catch (_: ResponseTooLargeException) {
                    ""
                },
            )
            property("headers", response.headersAsMultiMap)
            property("cookies", response.cookiesAsMultiMap)
            property("statusCode", response.statusCode)
            function("getHeader") { args ->
                val headerName = args.getString(0) ?: ""
                response.headers.getLast(headerName)
            }
            function("getCookie") { args ->
                val cookieName = args.getString(0) ?: ""
                response.cookiesAsMultiMap.getCaseInsensitive(cookieName)?.last()
            }
        }
}
