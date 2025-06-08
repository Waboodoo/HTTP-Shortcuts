package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import ch.rmy.android.http_shortcuts.BuildConfig
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

object UserAgentProvider {

    fun getUserAgent(context: Context): String =
        EntryPointAccessors.fromApplication<UserAgentProviderEntryPoint>(context)
            .userPreferences()
            .userAgent
            ?: getDefaultUserAgent()

    fun getDefaultUserAgent(): String {
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

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface UserAgentProviderEntryPoint {
        fun userPreferences(): UserPreferences
    }
}
