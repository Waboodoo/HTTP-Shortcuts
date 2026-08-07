package ch.rmy.android.http_shortcuts.data.settings

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.utils.PreferencesStore
import ch.rmy.android.http_shortcuts.data.domains.categories.CategoryId
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeviceLocalPreferences
@Inject
constructor(
    context: Context,
) : PreferencesStore(context) {
    val deviceId: String
        get() = getString(KEY_DEVICE_ID)
            ?: run {
                generateDeviceId()
                    .also {
                        putString(KEY_DEVICE_ID, it)
                    }
            }

    var firstSeenVersionCode: Long?
        get() = getLong(KEY_FIRST_SEEN_VERSION_CODE)
        set(value) = putLong(KEY_FIRST_SEEN_VERSION_CODE, value ?: 0L)

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

    var previousIconColor: Int?
        get() = getInt(KEY_PREVIOUS_ICON_COLOR)
        set(value) = putInt(KEY_PREVIOUS_ICON_COLOR, value ?: 0)

    var lastFilesCleanupTime: Instant
        get() = getLong(KEY_LAST_FILES_CLEANUP_TIME)?.let(Instant::ofEpochMilli) ?: Instant.MIN
        set(value) = putLong(KEY_LAST_FILES_CLEANUP_TIME, value.toEpochMilli())

    var isAwareOfVariablePlaceholders: Boolean
        get() = getBoolean(KEY_AWARE_OF_VARIABLE_PLACEHOLDERS)
        set(value) = putBoolean(KEY_AWARE_OF_VARIABLE_PLACEHOLDERS, value)

    var isAwareOfResponseHandling: Boolean
        get() = getBoolean(KEY_AWARE_OF_RESPONSE_HANDLING)
        set(value) = putBoolean(KEY_AWARE_OF_RESPONSE_HANDLING, value)

    var isAwareOfRunningInBackgroundLimitations: Boolean
        get() = getBoolean(KEY_AWARE_OF_RUN_IN_BACKGROUND_LIMITATIONS)
        set(value) = putBoolean(KEY_AWARE_OF_RUN_IN_BACKGROUND_LIMITATIONS, value)

    var isAwareOfShortcutUnhiding: Boolean
        get() = getBoolean(KEY_AWARE_OF_SHORTCUT_UNHIDING)
        set(value) = putBoolean(KEY_AWARE_OF_SHORTCUT_UNHIDING, value)

    var isAwareOfSectionPopulation: Boolean
        get() = getBoolean(KEY_AWARE_OF_SECTION_POPULATION)
        set(value) = putBoolean(KEY_AWARE_OF_SECTION_POPULATION, value)

    var isAwareOfMaterialIconsInfo: Boolean
        get() = getBoolean(KEY_AWARE_OF_MATERIAL_ICONS_INFO)
        set(value) = putBoolean(KEY_AWARE_OF_MATERIAL_ICONS_INFO, value)

    var isAwareOfAppIconChange: Boolean
        get() = getBoolean(KEY_AWARE_OF_APP_ICON_CHANGE)
        set(value) = putBoolean(KEY_AWARE_OF_APP_ICON_CHANGE, value)

    var lastActiveCategoryId: CategoryId?
        get() = getString(KEY_LAST_ACTIVE_CATEGORY_ID)
        set(value) = putString(KEY_LAST_ACTIVE_CATEGORY_ID, value)

    var syncErrorCount: Int
        get() = getInt(KEY_SYNC_ERROR_COUNT) ?: 0
        set(value) = putInt(KEY_SYNC_ERROR_COUNT, value)

    var syncTooManyErrors: Boolean
        get() = getBoolean(KEY_SYNC_TOO_MANY_ERRORS)
        set(value) = putBoolean(KEY_SYNC_TOO_MANY_ERRORS, value)

    companion object {
        private const val KEY_DEVICE_ID = "device_id_v2"
        private const val KEY_FIRST_SEEN_VERSION_CODE = "first_version_code"
        private const val KEY_IMPORT_URL = "import_url"
        private const val KEY_CHANGE_LOG_PERMANENTLY_HIDDEN = "change_log_permanently_hidden"
        private const val KEY_CHANGE_LOG_LAST_VERSION = "change_log_last_seen_version"
        private const val KEY_NETWORK_RESTRICTION_PERMANENTLY_HIDDEN = "network_restriction_permanently_hidden"
        private const val KEY_EXTERNAL_URL_WARNING_PERMANENTLY_HIDDEN = "external_url_warning_permanently_hidden"
        private const val KEY_PREVIOUS_ICON_COLOR = "previous_icon_color"
        private const val KEY_LAST_FILES_CLEANUP_TIME = "last_files_cleanup_time"
        private const val KEY_MALFORMED_JSON_WARNING_PERMANENTLY_HIDDEN = "malformed_json_warning_permanently_hidden"
        private const val KEY_AWARE_OF_RUN_IN_BACKGROUND_LIMITATIONS = "aware_of_run_in_background_limitations"
        private const val KEY_AWARE_OF_RESPONSE_HANDLING = "aware_of_response_handling"
        private const val KEY_AWARE_OF_SHORTCUT_UNHIDING = "aware_of_shortcut_unhiding"
        private const val KEY_AWARE_OF_VARIABLE_PLACEHOLDERS = "aware_of_variable_placeholders"
        private const val KEY_AWARE_OF_SECTION_POPULATION = "aware_of_section_population"
        private const val KEY_AWARE_OF_MATERIAL_ICONS_INFO = "aware_of_material_icons_info"
        private const val KEY_AWARE_OF_APP_ICON_CHANGE = "aware_of_app_icon_change"
        private const val KEY_LAST_ACTIVE_CATEGORY_ID = "last_active_category_id"
        private const val KEY_SYNC_ERROR_COUNT = "sync_error_count"
        private const val KEY_SYNC_TOO_MANY_ERRORS = "sync_too_many_errors"

        // Intentionally excluding I and O to avoid mixing them up with 1 and 0
        private const val DEVICE_ID_CHARACTERS = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789"

        private fun generateDeviceId(): String =
            CharArray(10) { DEVICE_ID_CHARACTERS.random() }
                .joinToString(separator = "")
    }
}
