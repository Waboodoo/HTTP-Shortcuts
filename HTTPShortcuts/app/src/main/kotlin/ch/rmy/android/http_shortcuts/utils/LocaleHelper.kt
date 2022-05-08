package ch.rmy.android.http_shortcuts.utils

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import java.util.Locale
import javax.inject.Inject

class LocaleHelper
@Inject
constructor(
    private val settings: Settings,
) {

    private var deviceLocale: Locale? = null

    fun applyLocaleFromSettings() {
        applyLocale(settings.language)
    }

    fun applyLocale(locale: String?) {
        if (deviceLocale == null) {
            deviceLocale = Locale.getDefault()
        }
        val preferredLocale = locale
            ?.let(::getLocale)
            ?: run {
                if (deviceLocale == Locale.getDefault()) {
                    return
                }
                deviceLocale!!
            }
        setLocale(preferredLocale)
    }

    private fun setLocale(locale: Locale) {
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.create(locale))
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
