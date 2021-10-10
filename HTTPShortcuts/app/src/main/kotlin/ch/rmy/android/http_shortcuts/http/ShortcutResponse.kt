package ch.rmy.android.http_shortcuts.http

import android.content.Context
import android.net.Uri
import ch.rmy.android.http_shortcuts.exceptions.ResponseTooLargeException
import ch.rmy.android.http_shortcuts.utils.SizeLimitedReader
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale


class ShortcutResponse internal constructor(
    val url: String?,
    val headers: HttpHeaders,
    val statusCode: Int,
    val contentFile: Uri?,
    val timing: Long,
) {

    val contentType: String?
        get() = headers.getLast(HttpHeaders.CONTENT_TYPE)?.let { contentType ->
            contentType.split(';', limit = 2)[0].lowercase(locale = Locale.US)
        }

    val cookies: Map<String, String>
        get() = headers.getLast(HttpHeaders.SET_COOKIE)
            ?.split(';')
            ?.map { it.split('=') }
            ?.associate { it.first() to (it.getOrNull(1) ?: "") }
            ?: emptyMap()

    val headersAsMap: Map<String, String>
        get() = headers.toMultiMap()
            .mapValues { entry -> entry.value.last() }

    private var responseTooLarge = false

    private var cachedContentAsString: String? = null

    fun getContentAsString(context: Context): String =
        if (responseTooLarge) {
            throw ResponseTooLargeException(CONTENT_SIZE_LIMIT)
        } else {
            cachedContentAsString
                ?: run {
                    contentFile?.let {
                        InputStreamReader(context.contentResolver.openInputStream(it)).use { reader ->
                            try {
                                BufferedReader(SizeLimitedReader(reader, CONTENT_SIZE_LIMIT), BUFFER_SIZE)
                                    .use(BufferedReader::readText)
                            } catch (e: SizeLimitedReader.LimitReachedException) {
                                responseTooLarge = true
                                throw ResponseTooLargeException(e.limit)
                            }
                        }
                    }
                        ?: ""
                }
                    .also {
                        cachedContentAsString = it
                    }
        }

    companion object {

        private const val BUFFER_SIZE = 16384

        private const val CONTENT_SIZE_LIMIT = 1 * 1000L * 1000L
    }

}
