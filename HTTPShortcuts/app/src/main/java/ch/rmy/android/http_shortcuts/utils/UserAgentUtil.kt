package ch.rmy.android.http_shortcuts.utils

import ch.rmy.android.http_shortcuts.BuildConfig

object UserAgentUtil {

    val userAgent: String
        get() {
            val base = "HttpShortcuts/" + BuildConfig.VERSION_NAME
            val userAgent = System.getProperty("http.agent")
            val start = userAgent.indexOf("(")
            val end = userAgent.indexOf(")")
            if (start == -1 || end == -1 || start > end) {
                return base
            }
            return base + " " + userAgent.substring(start, end + 1)
        }

}
