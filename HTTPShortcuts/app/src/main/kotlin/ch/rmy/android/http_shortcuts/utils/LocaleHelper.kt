package ch.rmy.android.http_shortcuts.utils

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.getSystemService
import androidx.core.os.LocaleListCompat
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences
import java.util.Locale
import javax.inject.Inject

class LocaleHelper
@Inject
constructor(
    private val context: Context,
    private val userPreferences: UserPreferences,
) {

    fun applyLocaleFromSettings() {
        val storedPreferredLanguage = userPreferences.language
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService<LocaleManager>()!!.applicationLocales
                .get(0)
                .let { locale ->
                    if (locale?.language != storedPreferredLanguage?.split('-')?.first()) {
                        userPreferences.language = locale?.language.runIfNotNull(locale?.country) {
                            plus("-$it")
                        }
                    }
                }
        }
        applyLocale(storedPreferredLanguage)
    }

    fun applyLocale(locale: String?) {
        val preferredLocale = locale
            ?.let(::getLocale)
        setLocale(preferredLocale)
    }

    private fun setLocale(locale: Locale?) {
        AppCompatDelegate.setApplicationLocales(
            if (locale != null) LocaleListCompat.create(locale) else LocaleListCompat.getEmptyLocaleList(),
        )
    }

    @Suppress("DEPRECATION")
    private fun getLocale(localeSpec: String): Locale {
        val localeParts = localeSpec.split('-')
        val language = localeParts[0]
        val country = localeParts.getOrNull(1)?.removePrefix("r")
        return if (country != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                Locale.of(language, country)
            } else {
                Locale(language, country)
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
                Locale.of(language)
            } else {
                Locale(language)
            }
        }
    }
}
