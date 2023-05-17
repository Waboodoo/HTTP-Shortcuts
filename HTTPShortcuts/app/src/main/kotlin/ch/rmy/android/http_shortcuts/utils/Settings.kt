package ch.rmy.android.http_shortcuts.utils

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.utils.PreferencesStore
import ch.rmy.android.framework.utils.UUIDUtils
import ch.rmy.android.http_shortcuts.data.enums.ShortcutClickBehavior
import javax.inject.Inject

class Settings
@Inject
constructor(
    context: Context,
) : PreferencesStore(context) {

    val userId: String
        get() = getString(KEY_USER_ID)
            ?: run {
                UUIDUtils.newUUID()
                    .also {
                        putString(KEY_USER_ID, it)
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

    var changeLogLastVersion: String?
        get() = getString(KEY_CHANGE_LOG_LAST_VERSION)
        set(version) = putString(KEY_CHANGE_LOG_LAST_VERSION, version)

    var theme: String
        get() = getString(KEY_THEME) ?: THEME_BLUE
        set(theme) = putString(KEY_THEME, theme)

    var darkThemeSetting: String
        get() = getString(KEY_DARK_THEME) ?: DARK_THEME_AUTO
        set(value) {
            putString(KEY_DARK_THEME, value)
        }

    var useLegacyExportFormat: Boolean
        get() = getBoolean(KEY_LEGACY_EXPORT_FORMAT)
        set(value) {
            putBoolean(KEY_LEGACY_EXPORT_FORMAT, value)
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

    companion object {

        const val LANGUAGE_DEFAULT = "default"

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
        private const val KEY_CHANGE_LOG_LAST_VERSION = "change_log_last_seen_version"
        private const val KEY_NETWORK_RESTRICTION_PERMANENTLY_HIDDEN = "network_restriction_permanently_hidden"
        private const val KEY_REMOTE_EDIT_SERVER = "remote_edit_server"
        private const val KEY_REMOTE_EDIT_DEVICE_ID = "remote_edit_device_id"
        private const val KEY_REMOTE_EDIT_PASSWORD = "remote_edit_password"
        private const val KEY_THEME = "theme"
        private const val KEY_DARK_THEME = "dark_theme"
        private const val KEY_LEGACY_EXPORT_FORMAT = "use_legacy_export_format"
        private const val KEY_PREVIOUS_ICON_COLOR = "previous_icon_color"
    }
}
