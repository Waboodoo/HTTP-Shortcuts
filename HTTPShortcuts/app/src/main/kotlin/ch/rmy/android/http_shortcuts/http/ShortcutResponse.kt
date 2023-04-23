package ch.rmy.android.http_shortcuts.http

import android.content.Context
import android.net.Uri
import ch.rmy.android.http_shortcuts.exceptions.ResponseTooLargeException
import ch.rmy.android.http_shortcuts.extensions.readIntoString
import ch.rmy.android.http_shortcuts.utils.SizeLimitedReader
import java.nio.charset.Charset
import java.util.Locale

class ShortcutResponse internal constructor(
    val url: String,
    val headers: HttpHeaders,
    val statusCode: Int,
    val contentFile: Uri?,
    val timing: Long,
) {

    val contentType: String?
        get() = headers.getLast(HttpHeaders.CONTENT_TYPE)?.let { contentType ->
            contentType.split(';', limit = 2)[0].lowercase(locale = Locale.US)
        }

    val charset: Charset by lazy {
        headers.getLast(HttpHeaders.CONTENT_TYPE)
            ?.split("charset=", limit = 2)?.getOrNull(1)
            ?.let {
                try {
                    Charset.forName(it)
                } catch (e: Exception) {
                    null
                }
            }
            ?: Charsets.UTF_8
    }

    val cookiesAsMultiMap: Map<String, List<String>> by lazy {
        headers.toMultiMap()
            .filterKeys { it.equals(HttpHeaders.SET_COOKIE, ignoreCase = true) }
            .values
            .flatten()
            .map { it.split(';', limit = 2).first() }
            .map { it.split('=', limit = 2) }
            .map { it[0] to it.getOrElse(1) { "" } }
            .let { cookies ->
                mutableMapOf<String, MutableList<String>>()
                    .apply {
                        cookies.forEach { (key, value) ->
                            getOrPut(key) { mutableListOf() }.add(value)
                        }
                    }
            }
    }

    val headersAsMultiMap: Map<String, List<String>> by lazy {
        headers.toMultiMap()
    }

    private var responseTooLarge = false

    private var cachedContentAsString: String? = null

    fun getContentAsString(context: Context): String =
        if (responseTooLarge) {
            throw ResponseTooLargeException(CONTENT_SIZE_LIMIT)
        } else {
            cachedContentAsString
                ?: run {
                    try {
                        contentFile
                            ?.readIntoString(context, CONTENT_SIZE_LIMIT, charset)
                    } catch (e: SizeLimitedReader.LimitReachedException) {
                        responseTooLarge = true
                        throw ResponseTooLargeException(e.limit)
                    }
                        ?: ""
                }
        }
            .also {
                cachedContentAsString = it
            }

    companion object {
        private const val CONTENT_SIZE_LIMIT = 1 * 1000L * 1000L
    }
}
