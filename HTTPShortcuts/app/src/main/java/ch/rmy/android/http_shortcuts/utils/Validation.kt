package ch.rmy.android.http_shortcuts.utils

object Validation {

    fun isAcceptableUrl(url: String) =
            !url.isBlank() &&
                    !url.equals("http://", ignoreCase = true) &&
                    !url.equals("https://", ignoreCase = true)

}
