package ch.rmy.android.http_shortcuts.utils

import android.app.LocaleManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.getSystemService
import androidx.core.os.LocaleListCompat
import ch.rmy.android.framework.extensions.runIfNotNull
import java.util.Locale
import javax.inject.Inject

class LocaleHelper
@Inject
constructor(
    private val context: Context,
    private val settings: Settings,
) {

    fun applyLocaleFromSettings() {
        val storedPreferredLanguage = settings.language
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService<LocaleManager>()!!.applicationLocales
                .get(0)
                .let { locale ->
                    if (locale?.language != storedPreferredLanguage?.split('-')?.first()) {
                        settings.language = locale?.language.runIfNotNull(locale?.country) {
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

    private fun getLocale(localeSpec: String): Locale {
        val localeParts = localeSpec.split('-')
        val language = localeParts[0]
        val country = localeParts.getOrNull(1)
        return if (country != null) {
            Locale(language, country)
        } else {
            Locale(language)
        }
    }
}
