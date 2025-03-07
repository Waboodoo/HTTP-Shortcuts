package ch.rmy.android.http_shortcuts.http

import ch.rmy.android.framework.extensions.getCaseInsensitive
import okhttp3.Headers

class HttpHeaders private constructor(private val headers: Map<String, List<String>>) {

    fun getLast(name: String): String? =
        headers.getCaseInsensitive(name)
            ?.lastOrNull()

    fun toMultiMap(): Map<String, List<String>> = headers

    companion object {

        fun parse(headers: Headers) =
            HttpHeaders(
                headers.names()
                    .associateWith { name -> headers.values(name) },
            )

        const val AUTHORIZATION = "Authorization"

        const val CONNECTION = "Connection"

        const val CONTENT_DISPOSITION = "Content-Disposition"

        const val CONTENT_LENGTH = "Content-Length"

        const val CONTENT_TYPE = "Content-Type"

        const val SET_COOKIE = "Set-Cookie"

        const val USER_AGENT = "User-Agent"
    }
}
