package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.graphics.Color
import androidx.core.content.res.use
import ch.rmy.android.framework.extensions.color
import ch.rmy.android.framework.extensions.isDarkThemeEnabled
import ch.rmy.android.http_shortcuts.R

class ThemeHelper(context: Context) {

    val theme: Int
    val transparentTheme: Int
    val statusBarColor: Int

    private val isDarkThemeEnabled: Boolean = context.isDarkThemeEnabled()

    init {
        val themeId = Settings(context).theme
        theme = when (themeId) {
            Settings.THEME_GREEN -> R.style.LightThemeAlt1
            Settings.THEME_RED -> R.style.LightThemeAlt2
            Settings.THEME_PURPLE -> R.style.LightThemeAlt3
            Settings.THEME_GREY -> R.style.LightThemeAlt4
            Settings.THEME_ORANGE -> R.style.LightThemeAlt5
            Settings.THEME_INDIGO -> R.style.LightThemeAlt6
            else -> R.style.LightThemeAlt0
        }
        transparentTheme = when (themeId) {
            Settings.THEME_GREEN -> R.style.LightThemeTransparentAlt1
            Settings.THEME_RED -> R.style.LightThemeTransparentAlt2
            Settings.THEME_PURPLE -> R.style.LightThemeTransparentAlt3
            Settings.THEME_GREY -> R.style.LightThemeTransparentAlt4
            Settings.THEME_ORANGE -> R.style.LightThemeTransparentAlt5
            Settings.THEME_INDIGO -> R.style.LightThemeTransparentAlt6
            else -> R.style.LightThemeTransparentAlt0
        }
        statusBarColor = if (isDarkThemeEnabled) {
            Color.BLACK
        } else {
            when (themeId) {
                Settings.THEME_GREEN -> color(context, R.color.primary_dark_alt1)
                Settings.THEME_RED -> color(context, R.color.primary_dark_alt2)
                Settings.THEME_PURPLE -> color(context, R.color.primary_dark_alt3)
                Settings.THEME_GREY -> color(context, R.color.primary_dark_alt4)
                Settings.THEME_ORANGE -> color(context, R.color.primary_dark_alt5)
                Settings.THEME_INDIGO -> color(context, R.color.primary_dark_alt6)
                else -> color(context, R.color.primary_dark_alt0)
            }
        }
    }

    fun getPrimaryColor(context: Context) =
        if (isDarkThemeEnabled) {
            color(context, R.color.primary_color)
        } else {
            context.obtainStyledAttributes(intArrayOf(R.attr.colorPrimary)).use { attributes ->
                attributes.getColor(0, color(context, R.color.primary_alt0))
            }
        }
}
