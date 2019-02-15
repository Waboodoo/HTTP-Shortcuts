package ch.rmy.android.http_shortcuts.utils

import android.net.Uri

object Validation {

    fun isAcceptableUrl(url: String) =
        url.matches("^(http(s?)://.+)|((h(t(t(p(s)?)?)?)?)?\\{\\{[a-z0-9]{1,20}}}.*)".toRegex(RegexOption.IGNORE_CASE))

    fun isValidUrl(uri: Uri) =
        uri.scheme?.let { scheme ->
            scheme.equals("http", ignoreCase = true)
                || scheme.equals("https", ignoreCase = true)
        } ?: false

}
