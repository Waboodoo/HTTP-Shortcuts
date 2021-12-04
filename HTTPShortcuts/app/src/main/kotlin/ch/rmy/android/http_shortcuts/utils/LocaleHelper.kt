package ch.rmy.android.http_shortcuts.utils

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import java.util.Locale

object LocaleHelper {

    private var deviceLocale: Locale? = null

    fun applyLocale(context: Context): Context {
        if (deviceLocale == null) {
            deviceLocale = Locale.getDefault()
        }
        val preferredLocale = getPersistedLocale(context)
            ?.let(::getLocale)
            ?: run {
                if (deviceLocale == Locale.getDefault()) {
                    return context
                }
                deviceLocale!!
            }
        return setLocale(context, preferredLocale)
    }

    private fun getPersistedLocale(context: Context): String? =
        Settings(context).language

    private fun setLocale(context: Context, locale: Locale): Context {
        Locale.setDefault(locale)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, locale)
        } else {
            updateResourcesLegacy(context, locale)
        }
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

    @TargetApi(Build.VERSION_CODES.N)
    private fun updateResources(context: Context, locale: Locale): Context =
        context.createConfigurationContext(
            context.resources.configuration
                .apply {
                    setLocale(locale)
                    setLayoutDirection(locale)
                }
        )

    @Suppress("DEPRECATION")
    private fun updateResourcesLegacy(context: Context, locale: Locale): Context {
        val resources = context.resources
        val configuration = resources.configuration
        configuration.locale = locale
        configuration.setLayoutDirection(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }
}
