package ch.rmy.android.http_shortcuts.http

import ch.rmy.android.http_shortcuts.extensions.getCaseInsensitive
import ch.rmy.android.http_shortcuts.utils.SizeLimitedReader
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.GZIPInputStream


class ShortcutResponse internal constructor(
    val url: String?,
    val headers: Map<String, String>,
    val statusCode: Int,
    content: InputStream?
) {

    val bodyAsString: String =
        content?.let { content ->
            if (isGzipped()) {
                parseGzippedToString(content)
            } else {
                parseToString(content)
            }
        } ?: ""

    private fun isGzipped(): Boolean = headers.getCaseInsensitive(HttpHeaders.CONTENT_ENCODING) == "gzip"

    val contentType: String
        get() = headers.getCaseInsensitive(HttpHeaders.CONTENT_TYPE)?.let { contentType ->
            contentType.split(';', limit = 2)[0].toLowerCase()
        }
            ?: TYPE_TEXT

    val cookies: Map<String, String>
        get() = headers.getCaseInsensitive(HttpHeaders.SET_COOKIE)
            ?.split(';')
            ?.map { it.split('=') }
            ?.associate { it.first() to (it.getOrNull(1) ?: "") }
            ?: emptyMap()

    companion object {

        const val TYPE_TEXT = "text/plain"
        const val TYPE_XML = "text/xml"
        const val TYPE_JSON = "application/json"
        const val TYPE_HTML = "text/html"
        const val TYPE_YAML = "text/yaml"
        const val TYPE_YAML_ALT = "application/x-yaml"

        private const val GZIP_BUFFER_SIZE = 16384

        private const val CONTENT_SIZE_LIMIT = 2 * 1000L * 1000L

        private fun parseGzippedToString(content: InputStream): String =
            GZIPInputStream(content).use { gzipStream ->
                parseToString(gzipStream)
            }

        private fun parseToString(content: InputStream): String =
            InputStreamReader(content).use { reader ->
                BufferedReader(SizeLimitedReader(reader, CONTENT_SIZE_LIMIT), GZIP_BUFFER_SIZE)
                    .use(BufferedReader::readText)
            }

    }

}
