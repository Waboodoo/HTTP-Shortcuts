package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.graphics.Color
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.extensions.color
import ch.rmy.android.http_shortcuts.extensions.isDarkThemeEnabled

class ThemeHelper(context: Context) {

    val isDarkThemeEnabled: Boolean
    val theme: Int
    val statusBarColor: Int

    init {
        isDarkThemeEnabled = context.isDarkThemeEnabled()

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
            context.obtainStyledAttributes(intArrayOf(R.attr.colorPrimary)).getColor(0, color(context, R.color.primary_alt0))
        }

}
