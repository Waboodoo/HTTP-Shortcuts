package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import ch.rmy.android.http_shortcuts.extensions.takeUnlessEmpty

class Settings(context: Context) {

    private val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    val userId: String
        get() = preferences.getString(KEY_USER_ID, null)
            ?: run {
                UUIDUtils.newUUID()
                    .also {
                        putString(KEY_USER_ID, it)
                    }
            }

    val language: String?
        get() = preferences.getString(KEY_LANGUAGE, LANGUAGE_DEFAULT)?.takeUnless { it == LANGUAGE_DEFAULT }

    val clickBehavior: String
        get() = preferences.getString(KEY_CLICK_BEHAVIOR, CLICK_BEHAVIOR_RUN)!!

    val isCrashReportingAllowed: Boolean
        get() = preferences.getString(KEY_CRASH_REPORTING, "true") != "false"

    var importUrl: String
        get() = preferences.getString(KEY_IMPORT_URL, "") ?: ""
        set(url) = putString(KEY_IMPORT_URL, url)

    var isChangeLogPermanentlyHidden: Boolean
        get() = preferences.getBoolean(KEY_CHANGE_LOG_PERMANENTLY_HIDDEN, false)
        set(hidden) = putBoolean(KEY_CHANGE_LOG_PERMANENTLY_HIDDEN, hidden)

    var isNetworkRestrictionWarningPermanentlyHidden: Boolean
        get() = preferences.getBoolean(KEY_NETWORK_RESTRICTION_PERMANENTLY_HIDDEN, false)
        set(hidden) = putBoolean(KEY_NETWORK_RESTRICTION_PERMANENTLY_HIDDEN, hidden)

    var changeLogLastVersion: Long?
        get() = try {
            preferences.getLong(KEY_CHANGE_LOG_LAST_VERSION, -1L)
        } catch (e: ClassCastException) {
            preferences.getInt(KEY_CHANGE_LOG_LAST_VERSION, -1).toLong()
        }
            .takeUnless { it == -1L }
        set(version) = putLong(KEY_CHANGE_LOG_LAST_VERSION, version ?: -1L)

    var theme: String
        get() = preferences.getString(KEY_THEME, THEME_BLUE)!!
        set(theme) = putString(KEY_THEME, theme)

    val darkThemeSetting: String
        get() = preferences.getString(KEY_DARK_THEME, DARK_THEME_AUTO)!!

    var isForceForegroundEnabled: Boolean
        get() = preferences.getBoolean(KEY_FORCE_FOREGROUND, false)
        set(value) = putBoolean(KEY_FORCE_FOREGROUND, value)

    val useLegacyExportFormat: Boolean
        get() = preferences.getBoolean(KEY_LEGACY_EXPORT_FORMAT, false)

    var remoteEditServerUrl: String?
        get() = preferences.getString(KEY_REMOTE_EDIT_SERVER, null)?.takeUnlessEmpty()
        set(value) = putString(KEY_REMOTE_EDIT_SERVER, value ?: "")

    var remoteEditDeviceId: String?
        get() = preferences.getString(KEY_REMOTE_EDIT_DEVICE_ID, null)?.takeUnlessEmpty()
        set(value) = putString(KEY_REMOTE_EDIT_DEVICE_ID, value ?: "")

    var remoteEditPassword: String?
        get() = preferences.getString(KEY_REMOTE_EDIT_PASSWORD, null)?.takeUnlessEmpty()
        set(value) = putString(KEY_REMOTE_EDIT_PASSWORD, value ?: "")

    private fun putString(key: String, value: String) {
        preferences.edit().putString(key, value).apply()
    }

    private fun putBoolean(key: String, value: Boolean) {
        preferences.edit().putBoolean(key, value).apply()
    }

    private fun putLong(key: String, value: Long) {
        preferences.edit().putLong(key, value).apply()
    }

    companion object {

        const val LANGUAGE_DEFAULT = "default"

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

        private const val KEY_USER_ID = "user_id"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_CLICK_BEHAVIOR = "click_behavior"
        private const val KEY_CRASH_REPORTING = "crash_reporting"
        private const val KEY_IMPORT_URL = "import_url"
        private const val KEY_CHANGE_LOG_PERMANENTLY_HIDDEN = "change_log_permanently_hidden"
        private const val KEY_CHANGE_LOG_LAST_VERSION = "change_log_last_version"
        private const val KEY_NETWORK_RESTRICTION_PERMANENTLY_HIDDEN = "network_restriction_permanently_hidden"
        private const val KEY_REMOTE_EDIT_SERVER = "remote_edit_server"
        private const val KEY_REMOTE_EDIT_DEVICE_ID = "remote_edit_device_id"
        private const val KEY_REMOTE_EDIT_PASSWORD = "remote_edit_password"
        private const val KEY_THEME = "theme"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_FORCE_FOREGROUND = "force_foreground"
        private const val KEY_LEGACY_EXPORT_FORMAT = "use_legacy_export_format"

    }

}
