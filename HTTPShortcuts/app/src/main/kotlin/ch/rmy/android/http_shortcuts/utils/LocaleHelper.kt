package ch.rmy.android.http_shortcuts.utils

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import java.util.Locale

object LocaleHelper {

    fun applyLocale(context: Context): Context {
        return setLocale(context, getPersistedLocale(context) ?: return context)
    }

    private fun getPersistedLocale(context: Context): String? =
        Settings(context).language

    fun setLocale(context: Context, localeSpec: String): Context {
        val localeParts = localeSpec.split('-')
        val language = localeParts[0]
        val country = localeParts.getOrNull(1)
        val locale = if (country != null) {
            Locale(language, country)
        } else {
            Locale(language)
        }
        Locale.setDefault(locale)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            updateResources(context, locale)
        } else {
            updateResourcesLegacy(context, locale)
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLayoutDirection(locale)
        }
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return context
    }
}