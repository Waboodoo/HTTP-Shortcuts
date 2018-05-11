package ch.rmy.android.http_shortcuts.http

import com.android.volley.toolbox.HttpHeaderParser
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStreamReader
import java.nio.charset.Charset
import java.util.zip.GZIPInputStream


class ShortcutResponse internal constructor(val headers: Map<String, String>, val statusCode: Int, private val data: ByteArray) {

    val bodyAsString: String
        get() = try {
            if (isGzipped()) {
                parseGzippedByteArray()
            } else {
                parseByteArray()
            }
        } catch (e: Exception) {
            String(data)
        }

    private fun isGzipped(): Boolean = headers["Content-Encoding"] == "gzip"

    private fun parseByteArray(): String =
            data.toString(Charset.forName(HttpHeaderParser.parseCharset(headers, "UTF-8")))

    private fun parseGzippedByteArray(): String {
        val output = StringBuilder()
        GZIPInputStream(ByteArrayInputStream(data)).use { gzipStream ->
            InputStreamReader(gzipStream).use { reader ->
                BufferedReader(reader, GZIP_BUFFER_SIZE).use { inStream ->
                    while (true) {
                        val line = inStream.readLine() ?: break
                        output.append(line).append("\n")
                    }
                }
            }
        }
        return output.toString()
    }

    val contentType: String
        get() = if (headers.containsKey(HEADER_CONTENT_TYPE)) {
            headers[HEADER_CONTENT_TYPE]!!.split(";".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0].toLowerCase()
        } else {
            TYPE_TEXT
        }

    companion object {

        const val TYPE_TEXT = "text/plain"
        const val TYPE_XML = "text/xml"
        const val TYPE_JSON = "application/json"

        private const val GZIP_BUFFER_SIZE = 16384

        private const val HEADER_CONTENT_TYPE = "Content-Type"
    }

}
