package ch.rmy.android.http_shortcuts.utils

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences.Companion.DARK_THEME_OFF
import ch.rmy.android.http_shortcuts.data.settings.UserPreferences.Companion.DARK_THEME_ON

object DarkThemeHelper {

    fun applyDarkThemeSettings(setting: String) {
        AppCompatDelegate.setDefaultNightMode(getDarkThemeSetting(setting))
    }

    private fun getDarkThemeSetting(setting: String) =
        when (setting) {
            DARK_THEME_ON -> AppCompatDelegate.MODE_NIGHT_YES
            DARK_THEME_OFF -> AppCompatDelegate.MODE_NIGHT_NO
            else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            } else {
                AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            }
        }
}
