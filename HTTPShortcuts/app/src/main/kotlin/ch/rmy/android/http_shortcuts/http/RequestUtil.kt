package ch.rmy.android.http_shortcuts.http

import ch.rmy.android.http_shortcuts.exceptions.InvalidContentTypeException
import java.net.URLEncoder
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType

object RequestUtil {

    const val FORM_MULTIPART_BOUNDARY = "----53014704754052338"
    const val FORM_MULTIPART_CONTENT_TYPE = "multipart/form-data; boundary=$FORM_MULTIPART_BOUNDARY"
    const val FORM_URLENCODE_CONTENT_TYPE = "application/x-www-form-urlencoded"
    const val FORM_URLENCODE_CONTENT_TYPE_WITH_CHARSET = "application/x-www-form-urlencoded; charset=UTF-8"

    private const val DEFAULT_CONTENT_TYPE = "text/plain"
    private const val PARAMETER_ENCODING = "UTF-8"

    fun encode(text: String): String =
        URLEncoder.encode(text, PARAMETER_ENCODING)

    fun sanitize(text: String): String =
        text.replace("\"", "")

    fun getMediaType(contentType: String?): MediaType =
        try {
            (contentType ?: DEFAULT_CONTENT_TYPE).toMediaType()
        } catch (e: IllegalArgumentException) {
            throw InvalidContentTypeException(contentType!!)
        }
}
