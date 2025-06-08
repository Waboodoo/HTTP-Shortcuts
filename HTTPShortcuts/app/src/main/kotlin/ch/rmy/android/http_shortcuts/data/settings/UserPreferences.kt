package ch.rmy.android.http_shortcuts.data.settings

import android.content.Context
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.utils.PreferencesStore
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class UserPreferences
@Inject
constructor(
    context: Context,
) : PreferencesStore(context) {

    var language: String?
        get() = getString(KEY_LANGUAGE)?.takeUnless { it == LANGUAGE_DEFAULT }
        set(value) = putString(KEY_LANGUAGE, value ?: LANGUAGE_DEFAULT)

    var clickBehavior: ShortcutClickBehavior
        get() = getString(KEY_CLICK_BEHAVIOR)?.let { ShortcutClickBehavior.Companion.parse(it) } ?: ShortcutClickBehavior.RUN
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

    var darkThemeSetting: String
        get() = getString(KEY_DARK_THEME) ?: DARK_THEME_AUTO
        set(value) {
            putString(KEY_DARK_THEME, value)
        }

    var remoteEditServerUrl: String?
        get() = getString(KEY_REMOTE_EDIT_SERVER)?.takeUnlessEmpty()
        set(value) = putString(KEY_REMOTE_EDIT_SERVER, value ?: "")

    var remoteEditPassword: String?
        get() = getString(KEY_REMOTE_EDIT_PASSWORD)?.takeUnlessEmpty()
        set(value) = putString(KEY_REMOTE_EDIT_PASSWORD, value ?: "")

    var userAgent: String?
        get() = getString(KEY_USER_AGENT)?.takeUnlessEmpty()
        set(value) = putString(KEY_USER_AGENT, value ?: "")

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

        private const val KEY_LANGUAGE = "language"
        private const val KEY_CLICK_BEHAVIOR = "click_behavior"
        private const val KEY_SHOW_HIDDEN_SHORTCUTS = "show_hidden_shortcuts"
        private const val KEY_CRASH_REPORTING = "crash_reporting"
        private const val KEY_REMOTE_EDIT_SERVER = "remote_edit_server"
        private const val KEY_REMOTE_EDIT_PASSWORD = "remote_edit_password"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_USER_AGENT = "user_agent"
        private const val KEY_COLOR_THEME = "color_theme"
        private const val KEY_HISTORY_USE_RELATIVE_TIMES = "history_relative_times"
    }
}
