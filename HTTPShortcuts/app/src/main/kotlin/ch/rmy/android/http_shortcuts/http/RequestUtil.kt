package ch.rmy.android.http_shortcuts.http

import ch.rmy.android.http_shortcuts.exceptions.InvalidContentTypeException
import okhttp3.MediaType
import java.net.URLEncoder

object RequestUtil {

    const val FORM_MULTIPART_BOUNDARY = "----53014704754052338"
    private const val DEFAULT_CONTENT_TYPE = "text/plain"
    private const val PARAMETER_ENCODING = "UTF-8"

    fun encode(text: String): String =
        URLEncoder.encode(text, PARAMETER_ENCODING)

    fun sanitize(text: String): String =
        text.replace("\"", "")

    fun getMediaType(contentType: String?) =
        try {
            MediaType.get(contentType ?: DEFAULT_CONTENT_TYPE)
        } catch (e: IllegalArgumentException) {
            throw InvalidContentTypeException(contentType!!)
        }

}