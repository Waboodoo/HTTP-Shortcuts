package ch.rmy.android.http_shortcuts.utils

import android.net.Uri

object Validation {

    fun isAcceptableUrl(url: String) =
            !url.isBlank() &&
                    !url.equals("http://", ignoreCase = true) &&
                    !url.equals("https://", ignoreCase = true)

    fun isValidUrl(uri: Uri) =
            uri.scheme?.equals("http", ignoreCase = true) == true
                    || uri.scheme?.equals("https", ignoreCase = true) == true

}
