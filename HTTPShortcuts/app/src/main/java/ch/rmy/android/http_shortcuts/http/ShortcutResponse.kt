package ch.rmy.android.http_shortcuts.http

import com.android.volley.toolbox.HttpHeaderParser
import java.nio.charset.Charset

class ShortcutResponse internal constructor(private val headers: Map<String, String>, private val data: ByteArray) {

    val bodyAsString: String
        get() {
            return try {
                data.toString(Charset.forName(HttpHeaderParser.parseCharset(headers, "UTF-8")))
            } catch (e: Exception) {
                String(data)
            }
        }

    val contentType: String
        get() {
            if (headers.containsKey(HEADER_CONTENT_TYPE)) {
                return headers[HEADER_CONTENT_TYPE]!!.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].toLowerCase()
            }
            return TYPE_TEXT
        }

    companion object {

        const val TYPE_TEXT = "text/plain"
        const val TYPE_XML = "text/xml"
        const val TYPE_JSON = "application/json"

        private const val HEADER_CONTENT_TYPE = "Content-Type"
    }

}
