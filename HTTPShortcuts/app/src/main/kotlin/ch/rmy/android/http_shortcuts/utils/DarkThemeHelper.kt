package ch.rmy.android.http_shortcuts.utils

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate

object DarkThemeHelper {

    fun applyDarkThemeSettings(setting: String) {
        AppCompatDelegate.setDefaultNightMode(getDarkThemeSetting(setting))
    }

    private fun getDarkThemeSetting(setting: String) =
        when (setting) {
            Settings.DARK_THEME_ON -> AppCompatDelegate.MODE_NIGHT_YES
            Settings.DARK_THEME_OFF -> AppCompatDelegate.MODE_NIGHT_NO
            else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            } else {
                AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY
            }
        }
}
