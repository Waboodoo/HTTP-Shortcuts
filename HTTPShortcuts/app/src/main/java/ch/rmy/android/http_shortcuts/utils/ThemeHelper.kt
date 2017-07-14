package ch.rmy.android.http_shortcuts.utils

import android.content.Context

import ch.rmy.android.http_shortcuts.R

class ThemeHelper(context: Context) {

    val theme: Int
    val statusBarColor: Int

    init {
        val themeId = Settings(context).theme

        when (themeId) {
            Settings.THEME_GREEN -> {
                theme = R.style.LightThemeAlt1
                statusBarColor = UIUtil.getColor(context, R.color.primary_dark_alt1)
            }
            Settings.THEME_RED -> {
                theme = R.style.LightThemeAlt2
                statusBarColor = UIUtil.getColor(context, R.color.primary_dark_alt2)
            }
            Settings.THEME_PURPLE -> {
                theme = R.style.LightThemeAlt3
                statusBarColor = UIUtil.getColor(context, R.color.primary_dark_alt3)
            }
            Settings.THEME_GREY -> {
                theme = R.style.LightThemeAlt4
                statusBarColor = UIUtil.getColor(context, R.color.primary_dark_alt4)
            }
            Settings.THEME_ORANGE -> {
                theme = R.style.LightThemeAlt5
                statusBarColor = UIUtil.getColor(context, R.color.primary_dark_alt5)
            }
            Settings.THEME_INDIGO -> {
                theme = R.style.LightThemeAlt6
                statusBarColor = UIUtil.getColor(context, R.color.primary_dark_alt6)
            }
            else -> {
                theme = R.style.LightTheme
                statusBarColor = UIUtil.getColor(context, R.color.primary_dark)
            }
        }
    }

}
