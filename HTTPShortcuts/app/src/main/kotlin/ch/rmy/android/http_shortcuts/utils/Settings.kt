package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.utils.PreferencesStore
import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Settings
@Inject
constructor(
    context: Context,
) : PreferencesStore(context) {

    val deviceId: String
        get() = getString(DEVICE_ID)
            ?: run {
                UUIDUtils.newUUID()
                    .uppercase()
                    .replace("-", "")
                    .take(20)
                    .also {
                        putString(DEVICE_ID, it)
                    }
            }

    var language: String?
        get() = getString(KEY_LANGUAGE)?.takeUnless { it == LANGUAGE_DEFAULT }
        set(value) = putString(KEY_LANGUAGE, value ?: LANGUAGE_DEFAULT)

    var clickBehavior: ShortcutClickBehavior
        get() = ShortcutClickBehavior.parse(getString(KEY_CLICK_BEHAVIOR))
        set(value) {
            putString(KEY_CLICK_BEHAVIOR, value.type)
        }

    var showHiddenShortcuts: Boolean
        get() = getBoolean(KEY_SHOW_HIDDEN_SHORTCUTS)
        set(value) {
            putBoolean(KEY_SHOW_HIDDEN_SHORTCUTS, value)
        }

    var isCrashReportingAllowed: Boolean
        get() = (getString(KEY_CRASH_REPORTING) ?: "true") != "false"
        set(value) {
            putString(KEY_CRASH_REPORTING, value.toString())
        }

    var importUrl: Uri?
        get() = getString(KEY_IMPORT_URL)?.takeUnlessEmpty()?.toUri()
        set(url) = putString(KEY_IMPORT_URL, url.toString())

    var isChangeLogPermanentlyHidden: Boolean
        get() = getBoolean(KEY_CHANGE_LOG_PERMANENTLY_HIDDEN)
        set(hidden) = putBoolean(KEY_CHANGE_LOG_PERMANENTLY_HIDDEN, hidden)

    var isNetworkRestrictionWarningPermanentlyHidden: Boolean
        get() = getBoolean(KEY_NETWORK_RESTRICTION_PERMANENTLY_HIDDEN)
        set(hidden) = putBoolean(KEY_NETWORK_RESTRICTION_PERMANENTLY_HIDDEN, hidden)

    var isExternalUrlWarningPermanentlyHidden: Boolean
        get() = getBoolean(KEY_EXTERNAL_URL_WARNING_PERMANENTLY_HIDDEN)
        set(hidden) = putBoolean(KEY_EXTERNAL_URL_WARNING_PERMANENTLY_HIDDEN, hidden)

    var isMalformedJsonWarningPermanentlyHidden: Boolean
        get() = getBoolean(KEY_MALFORMED_JSON_WARNING_PERMANENTLY_HIDDEN)
        set(hidden) = putBoolean(KEY_MALFORMED_JSON_WARNING_PERMANENTLY_HIDDEN, hidden)

    var changeLogLastVersion: String?
        get() = getString(KEY_CHANGE_LOG_LAST_VERSION)
        set(version) = putString(KEY_CHANGE_LOG_LAST_VERSION, version)

    var darkThemeSetting: String
        get() = getString(KEY_DARK_THEME) ?: DARK_THEME_AUTO
        set(value) {
            putString(KEY_DARK_THEME, value)
        }

    var remoteEditServerUrl: String?
        get() = getString(KEY_REMOTE_EDIT_SERVER)?.takeUnlessEmpty()
        set(value) = putString(KEY_REMOTE_EDIT_SERVER, value ?: "")

    var remoteEditDeviceId: String?
        get() = getString(KEY_REMOTE_EDIT_DEVICE_ID)?.takeUnlessEmpty()
        set(value) = putString(KEY_REMOTE_EDIT_DEVICE_ID, value ?: "")

    var remoteEditPassword: String?
        get() = getString(KEY_REMOTE_EDIT_PASSWORD)?.takeUnlessEmpty()
        set(value) = putString(KEY_REMOTE_EDIT_PASSWORD, value ?: "")

    var previousIconColor: Int?
        get() = getInt(KEY_PREVIOUS_ICON_COLOR)
        set(value) = putInt(KEY_PREVIOUS_ICON_COLOR, value ?: 0)

    var userAgent: String?
        get() = getString(KEY_USER_AGENT)?.takeUnlessEmpty()
        set(value) = putString(KEY_USER_AGENT, value ?: "")

    var useExperimentalExecutionMode: Boolean
        get() = getBoolean(KEY_EXPERIMENTAL_EXECUTION_MODE)
        set(value) = putBoolean(KEY_EXPERIMENTAL_EXECUTION_MODE, value)

    var colorTheme: String
        get() = getString(KEY_COLOR_THEME) ?: "default"
        set(value) {
            val newValue = value.takeIf { it == "dynamic-color" } ?: "default"
            _colorThemeFlow.value = newValue
            putString(KEY_COLOR_THEME, newValue)
        }

    var useRelativeTimesInHistory: Boolean
        get() = getBoolean(KEY_HISTORY_USE_RELATIVE_TIMES)
        set(value) = putBoolean(KEY_HISTORY_USE_RELATIVE_TIMES, value)

    private val _colorThemeFlow = MutableStateFlow(colorTheme)
    val colorThemeFlow = _colorThemeFlow.asStateFlow()

    companion object {

        const val LANGUAGE_DEFAULT = "default"

        const val DARK_THEME_ON = "on"
        const val DARK_THEME_OFF = "off"
        const val DARK_THEME_AUTO = "auto"

        private const val DEVICE_ID = "device_id"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_CLICK_BEHAVIOR = "click_behavior"
        private const val KEY_SHOW_HIDDEN_SHORTCUTS = "show_hidden_shortcuts"
        private const val KEY_CRASH_REPORTING = "crash_reporting"
        private const val KEY_IMPORT_URL = "import_url"
        private const val KEY_CHANGE_LOG_PERMANENTLY_HIDDEN = "change_log_permanently_hidden"
        private const val KEY_CHANGE_LOG_LAST_VERSION = "change_log_last_seen_version"
        private const val KEY_NETWORK_RESTRICTION_PERMANENTLY_HIDDEN = "network_restriction_permanently_hidden"
        private const val KEY_EXTERNAL_URL_WARNING_PERMANENTLY_HIDDEN = "external_url_warning_permanently_hidden"
        private const val KEY_REMOTE_EDIT_SERVER = "remote_edit_server"
        private const val KEY_REMOTE_EDIT_DEVICE_ID = "remote_edit_device_id"
        private const val KEY_REMOTE_EDIT_PASSWORD = "remote_edit_password"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_PREVIOUS_ICON_COLOR = "previous_icon_color"
        private const val KEY_USER_AGENT = "user_agent"
        private const val KEY_EXPERIMENTAL_EXECUTION_MODE = "experimental_execution_mode"
        private const val KEY_COLOR_THEME = "color_theme"
        private const val KEY_HISTORY_USE_RELATIVE_TIMES = "history_relative_times"
        private const val KEY_MALFORMED_JSON_WARNING_PERMANENTLY_HIDDEN = "malformed_json_warning_permanently_hidden"
    }
}
