package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import ch.rmy.android.http_shortcuts.BuildConfig

object UserAgentProvider {

    fun getUserAgent(context: Context): String =
        Settings(context).userAgent
            ?: getDefaultUserAgent()

    private fun getDefaultUserAgent(): String {
        val base = "HttpShortcuts/${BuildConfig.VERSION_NAME}"
        val userAgent = System.getProperty("http.agent")
            ?.filter { c ->
                (c > '\u001f' || c == '\t') && c < '\u007f'
            }
            ?: return base
        val start = userAgent.indexOf("(")
        if (start == -1) {
            return base
        }
        return "$base ${userAgent.substring(start)}"
    }
}
