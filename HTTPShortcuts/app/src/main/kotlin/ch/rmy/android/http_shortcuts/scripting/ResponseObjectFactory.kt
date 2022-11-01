package ch.rmy.android.http_shortcuts.scripting

import android.content.Context
import androidx.annotation.Keep
import ch.rmy.android.framework.extensions.getCaseInsensitive
import ch.rmy.android.framework.extensions.tryOrLog
import ch.rmy.android.http_shortcuts.exceptions.ResponseTooLargeException
import ch.rmy.android.http_shortcuts.http.ShortcutResponse
import org.liquidplayer.javascript.JSContext
import org.liquidplayer.javascript.JSFunction
import org.liquidplayer.javascript.JSObject
import javax.inject.Inject

class ResponseObjectFactory
@Inject
constructor(
    private val context: Context,
) {

    fun create(jsContext: JSContext, response: ShortcutResponse): JSObject =
        JSObject(jsContext).apply {
            property(
                "body",
                try {
                    response.getContentAsString(this@ResponseObjectFactory.context)
                } catch (e: ResponseTooLargeException) {
                    ""
                }
            )
            property("headers", tryOrLog { response.headersAsMultiMap }, READ_ONLY)
            property("cookies", tryOrLog { response.cookiesAsMultiMap }, READ_ONLY)
            property("statusCode", response.statusCode, READ_ONLY)
            property(
                "getHeader",
                object : JSFunction(jsContext, "run") {
                    @Suppress("unused")
                    @Keep
                    fun run(headerName: String): String? =
                        response.headers.getLast(headerName)
                },
                READ_ONLY,
            )
            property(
                "getCookie",
                object : JSFunction(jsContext, "run") {
                    @Suppress("unused")
                    @Keep
                    fun run(cookieName: String): String? =
                        response.cookiesAsMultiMap.getCaseInsensitive(cookieName)?.last()
                },
                READ_ONLY,
            )
        }

    companion object {
        private const val READ_ONLY =
            JSContext.JSPropertyAttributeReadOnly or JSContext.JSPropertyAttributeDontDelete
    }
}
