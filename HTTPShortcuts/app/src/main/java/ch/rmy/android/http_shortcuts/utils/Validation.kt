package ch.rmy.android.http_shortcuts.utils

import android.text.TextUtils

object Validation {

    fun isEmpty(string: String): Boolean {
        return string.matches("^\\s*$".toRegex())
    }

    fun isAcceptableUrl(url: String): Boolean {
        return !TextUtils.isEmpty(url) &&
                !url.equals("http://", ignoreCase = true) &&
                !url.equals("https://", ignoreCase = true)
    }

}
