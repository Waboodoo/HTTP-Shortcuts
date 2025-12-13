package ch.rmy.android.http_shortcuts.http

import android.os.Build
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

    fun encode(text: String): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            URLEncoder.encode(text, Charsets.UTF_8)
        } else {
            URLEncoder.encode(text, "UTF-8")
        }
            .replace("+", "%20")

    fun sanitize(text: String): String =
        text.replace("\"", "")

    fun getMediaType(contentType: String?): MediaType =
        try {
            (contentType ?: DEFAULT_CONTENT_TYPE).toMediaType()
        } catch (_: IllegalArgumentException) {
            throw InvalidContentTypeException(contentType!!)
        }
}
