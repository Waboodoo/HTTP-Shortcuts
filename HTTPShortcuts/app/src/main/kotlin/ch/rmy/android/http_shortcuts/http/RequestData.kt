package ch.rmy.android.http_shortcuts.http

import androidx.core.net.toUri
import ch.rmy.android.http_shortcuts.exceptions.InvalidUrlException
import ch.rmy.android.http_shortcuts.utils.Validation

data class RequestData(
    val url: String,
    val username: String,
    val password: String,
    val authToken: String,
    val body: String,
    val proxy: ProxyParams?,
    val contentType: String?,
) {

    val uri
        get() = url.toUri()

    init {
        if (!Validation.isValidHttpUrl(uri)) {
            throw InvalidUrlException(url)
        }
    }
}
