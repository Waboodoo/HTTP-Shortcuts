package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.SharedPreferences
import android.os.Environment
import android.preference.PreferenceManager

class Settings(context: Context) {

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val clickBehavior: String
        get() = preferences.getString(KEY_CLICK_BEHAVIOR, CLICK_BEHAVIOR_RUN)!!

    val isCrashReportingAllowed: Boolean
        get() = preferences.getString(KEY_CRASH_REPORTING, "true") != "false"

    var importExportDirectory: String?
        get() = preferences.getString(KEY_IMPORT_EXPORT_DIR, Environment.getExternalStorageDirectory().path)
        set(path) {
            if (path != null) {
                preferences.edit().putString(KEY_IMPORT_EXPORT_DIR, path).apply()
            }
        }

    var isChangeLogPermanentlyHidden: Boolean
        get() = preferences.getBoolean(KEY_CHANGE_LOG_PERMANENTLY_HIDDEN, false)
        set(hidden) = preferences.edit().putBoolean(KEY_CHANGE_LOG_PERMANENTLY_HIDDEN, hidden).apply()

    var isNetworkRestrictionWarningPermanentlyHidden: Boolean
        get() = preferences.getBoolean(KEY_NETWORK_RESTRICTION_PERMANENTLY_HIDDEN, false)
        set(hidden) = preferences.edit().putBoolean(KEY_NETWORK_RESTRICTION_PERMANENTLY_HIDDEN, hidden).apply()

    var wasVariableIntroShown: Boolean
        get() = preferences.getBoolean(KEY_VARIABLE_INTRO_SHOWN, false)
        set(shown) = preferences.edit().putBoolean(KEY_VARIABLE_INTRO_SHOWN, shown).apply()

    var changeLogLastVersion: Long
        get() = try {
            preferences.getLong(KEY_CHANGE_LOG_LAST_VERSION, 0L)
        } catch (e: ClassCastException) {
            preferences.getInt(KEY_CHANGE_LOG_LAST_VERSION, 0).toLong()
        }
        set(version) = preferences.edit().putLong(KEY_CHANGE_LOG_LAST_VERSION, version).apply()

    var isIconNameWarningPermanentlyHidden: Boolean
        get() = preferences.getBoolean(KEY_ICON_NAME_CHANGE_PERMANENTLY_HIDDEN, false)
        set(hidden) = preferences.edit().putBoolean(KEY_ICON_NAME_CHANGE_PERMANENTLY_HIDDEN, hidden).apply()

    var theme: String
        get() = preferences.getString(KEY_THEME, THEME_BLUE)!!
        set(theme) = preferences.edit().putString(KEY_THEME, theme).apply()

    val darkThemeSetting: String
        get() = preferences.getString(KEY_DARK_THEME, DARK_THEME_AUTO)!!

    companion object {

        const val CLICK_BEHAVIOR_RUN = "run"
        const val CLICK_BEHAVIOR_EDIT = "edit"
        const val CLICK_BEHAVIOR_MENU = "menu"

        const val THEME_BLUE = "blue"
        const val THEME_GREEN = "green"
        const val THEME_ORANGE = "orange"
        const val THEME_RED = "red"
        const val THEME_GREY = "grey"
        const val THEME_PURPLE = "purple"
        const val THEME_INDIGO = "indigo"

        const val DARK_THEME_ON = "on"
        const val DARK_THEME_OFF = "off"
        const val DARK_THEME_AUTO = "auto"

        private const val KEY_CLICK_BEHAVIOR = "click_behavior"
        private const val KEY_CRASH_REPORTING = "crash_reporting"
        private const val KEY_IMPORT_EXPORT_DIR = "import_export_dir"
        private const val KEY_CHANGE_LOG_PERMANENTLY_HIDDEN = "change_log_permanently_hidden"
        private const val KEY_CHANGE_LOG_LAST_VERSION = "change_log_last_version"
        private const val KEY_ICON_NAME_CHANGE_PERMANENTLY_HIDDEN = "icon_name_change_permanently_hidden"
        private const val KEY_NETWORK_RESTRICTION_PERMANENTLY_HIDDEN = "network_restriction_permanently_hidden"
        private const val KEY_THEME = "theme"
        private const val KEY_VARIABLE_INTRO_SHOWN = "variable_intro_shown"
        private const val KEY_DARK_THEME = "dark_theme"

    }

}
