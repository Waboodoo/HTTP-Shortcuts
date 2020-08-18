package ch.rmy.android.http_shortcuts.activities.editor

import android.app.Application
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.Repository
import ch.rmy.android.http_shortcuts.data.Transactions
import ch.rmy.android.http_shortcuts.data.enums.ShortcutExecutionType
import ch.rmy.android.http_shortcuts.data.models.Header
import ch.rmy.android.http_shortcuts.data.models.Parameter
import ch.rmy.android.http_shortcuts.data.models.ResponseHandling
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Shortcut.Companion.TEMPORARY_ID
import ch.rmy.android.http_shortcuts.extensions.getCaseInsensitive
import ch.rmy.android.http_shortcuts.extensions.getQuantityString
import ch.rmy.android.http_shortcuts.extensions.getString
import ch.rmy.android.http_shortcuts.extensions.type
import ch.rmy.android.http_shortcuts.http.HttpHeaders
import ch.rmy.android.http_shortcuts.icons.Icons
import ch.rmy.android.http_shortcuts.scripting.shortcuts.TriggerShortcutManager
import ch.rmy.android.http_shortcuts.utils.RxUtils
import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.utils.Validation
import ch.rmy.curlcommand.CurlCommand
import io.reactivex.Completable
import io.reactivex.Single
import io.realm.Realm
import java.net.URLDecoder

class ShortcutEditorViewModel(application: Application) : BasicShortcutEditorViewModel(application) {

    var isInitialized: Boolean = false
        private set

    var isSaving: Boolean = false

    private var categoryId: String? = null
    private var shortcutId: String? = null
    private var initialIcon: String? = null
    private var executionType: String? = null

    fun init(categoryId: String?, shortcutId: String?, curlCommand: CurlCommand?, executionType: String): Completable {
        if (isInitialized) {
            return Completable.complete()
        }
        this.categoryId = categoryId
        this.shortcutId = shortcutId
        this.executionType = executionType
        return Transactions
            .commit { realm ->
                val shortcut = if (shortcutId == null) {
                    initialIcon = Icons.getRandomInitialIcon(getApplication())
                    realm.copyToRealmOrUpdate(createNewShortcut())
                } else {
                    Repository.copyShortcut(realm, Repository.getShortcutById(realm, shortcutId)!!, TEMPORARY_ID)
                }

                curlCommand?.let { curlCommand ->
                    importFromCurl(realm, shortcut, curlCommand)
                }
            }
            .doOnComplete {
                isInitialized = true
            }
    }

    private fun createNewShortcut(): Shortcut =
        Shortcut(
            id = TEMPORARY_ID,
            iconName = initialIcon,
            executionType = executionType,
            responseHandling = if (executionType == ShortcutExecutionType.APP.type) {
                ResponseHandling()
            } else {
                null
            }
        )

    fun hasChanges(): Boolean {
        val oldShortcut = shortcutId
            ?.let { Repository.getShortcutById(persistedRealm, it)!! }
            ?: createNewShortcut()
        val newShortcut = getShortcut(persistedRealm) ?: return false
        return !newShortcut.isSameAs(oldShortcut)
    }

    fun setNameAndDescription(name: String, description: String): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.apply {
                this.name = name
                this.description = description
            }
        }

    fun setIconName(iconName: String?): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.apply {
                this.iconName = iconName
            }
        }

    fun trySave(): Single<SaveResult> {
        val id = shortcutId ?: newUUID()
        var name = ""
        var iconName: String? = null
        return Transactions
            .commit { realm ->
                val shortcut = Repository.getShortcutById(realm, TEMPORARY_ID)!!
                name = shortcut.name
                iconName = shortcut.iconName
                validateShortcut(shortcut)

                val newShortcut = Repository.copyShortcut(realm, shortcut, id)
                if (shortcutId == null && categoryId != null) {
                    Repository.getCategoryById(realm, categoryId!!)
                        ?.shortcuts
                        ?.add(newShortcut)
                }

                Repository.deleteShortcut(realm, TEMPORARY_ID)
            }
            .andThen(RxUtils.single {
                SaveResult(
                    id = id,
                    name = name,
                    iconName = iconName
                )
            })
    }

    private fun validateShortcut(shortcut: Shortcut) {
        if (shortcut.name.isBlank()) {
            throw ShortcutValidationError(VALIDATION_ERROR_EMPTY_NAME)
        }
        if (shortcut.type.usesUrl && !Validation.isAcceptableUrl(shortcut.url)) {
            throw ShortcutValidationError(VALIDATION_ERROR_INVALID_URL)
        }
    }

    fun getToolbarSubtitle(shortcut: Shortcut): CharSequence? =
        when (shortcut.type) {
            ShortcutExecutionType.BROWSER -> getString(R.string.subtitle_editor_toolbar_browser_shortcut)
            ShortcutExecutionType.SCRIPTING -> getString(R.string.subtitle_editor_toolbar_scripting_shortcut)
            ShortcutExecutionType.TRIGGER -> getString(R.string.subtitle_editor_toolbar_trigger_shortcut)
            else -> null
        }

    fun getBasicSettingsSubtitle(shortcut: Shortcut): CharSequence =
        if (shortcut.type == ShortcutExecutionType.BROWSER) {
            if (shortcut.url.isEmpty() || shortcut.url == "http://") {
                getString(R.string.subtitle_basic_request_settings_url_only_prompt)
            } else {
                shortcut.url
            }
        } else {
            if (shortcut.url.isEmpty() || shortcut.url == "http://") {
                getString(R.string.subtitle_basic_request_settings_prompt)
            } else {
                getString(
                    R.string.subtitle_basic_request_settings_pattern,
                    shortcut.method,
                    shortcut.url
                )
            }
        }

    fun getHeadersSettingsSubtitle(shortcut: Shortcut): CharSequence =
        getQuantityString(
            shortcut.headers.size,
            R.string.subtitle_request_headers_none,
            R.plurals.subtitle_request_headers_pattern
        )

    fun getRequestBodySettingsSubtitle(shortcut: Shortcut): CharSequence =
        if (shortcut.allowsBody()) {
            when (shortcut.requestBodyType) {
                Shortcut.REQUEST_BODY_TYPE_FORM_DATA,
                Shortcut.REQUEST_BODY_TYPE_X_WWW_FORM_URLENCODE,
                -> getQuantityString(
                    shortcut.parameters.size,
                    R.string.subtitle_request_body_params_none,
                    R.plurals.subtitle_request_body_params_pattern,
                )
                Shortcut.REQUEST_BODY_TYPE_FILE -> getString(R.string.subtitle_request_body_file)
                else -> if (shortcut.bodyContent.isBlank()) {
                    getString(R.string.subtitle_request_body_none)
                } else {
                    getString(R.string.subtitle_request_body_custom, shortcut.contentType)
                }
            }
        } else {
            getString(R.string.subtitle_request_body_not_available, shortcut.method)
        }

    fun getAuthenticationSettingsSubtitle(shortcut: Shortcut): CharSequence =
        when (shortcut.authentication) {
            Shortcut.AUTHENTICATION_BASIC -> getString(R.string.subtitle_authentication_basic)
            Shortcut.AUTHENTICATION_DIGEST -> getString(R.string.subtitle_authentication_digest)
            Shortcut.AUTHENTICATION_BEARER -> getString(R.string.subtitle_authentication_bearer)
            else -> getString(R.string.subtitle_authentication_none)
        }

    fun getScriptingSubtitle(shortcut: Shortcut): CharSequence =
        getString(when (shortcut.type) {
            ShortcutExecutionType.SCRIPTING -> R.string.label_scripting_scripting_shortcuts_subtitle
            ShortcutExecutionType.BROWSER -> R.string.label_scripting_browser_shortcuts_subtitle
            else -> R.string.label_scripting_subtitle
        })

    fun getTriggerShortcutsSubtitle(shortcut: Shortcut): CharSequence {
        val count = TriggerShortcutManager.getTriggeredShortcutsFromCode(shortcut.codeOnPrepare).size
        return getQuantityString(
            count,
            R.string.label_trigger_shortcuts_subtitle_none,
            R.plurals.label_trigger_shortcuts_subtitle
        )
    }

    data class SaveResult(val id: String, val name: String, val iconName: String?)

    companion object {

        const val VALIDATION_ERROR_EMPTY_NAME = 1
        const val VALIDATION_ERROR_INVALID_URL = 2

        private fun importFromCurl(realm: Realm, shortcut: Shortcut, curlCommand: CurlCommand) {
            shortcut.method = curlCommand.method
            shortcut.url = curlCommand.url
            shortcut.username = curlCommand.username
            shortcut.password = curlCommand.password
            if (curlCommand.username.isNotEmpty() || curlCommand.password.isNotEmpty()) {
                shortcut.authentication = Shortcut.AUTHENTICATION_BASIC
            }
            shortcut.timeout = curlCommand.timeout

            if (curlCommand.usesBinaryData) {
                shortcut.requestBodyType = Shortcut.REQUEST_BODY_TYPE_FILE
            } else if (curlCommand.isFormData || curlCommand.data.all { data -> data.count { it == '=' } == 1 }) {
                shortcut.requestBodyType = if (curlCommand.isFormData) {
                    Shortcut.REQUEST_BODY_TYPE_FORM_DATA
                } else {
                    Shortcut.REQUEST_BODY_TYPE_X_WWW_FORM_URLENCODE
                }
                prepareParameters(realm, curlCommand, shortcut)
            } else {
                shortcut.bodyContent = curlCommand.data.joinToString(separator = "&")
                shortcut.requestBodyType = Shortcut.REQUEST_BODY_TYPE_CUSTOM_TEXT
            }
            curlCommand.headers.getCaseInsensitive(HttpHeaders.CONTENT_TYPE)
                ?.let {
                    shortcut.contentType = it
                }
            curlCommand.headers.forEach { (key, value) ->
                if (!key.equals(HttpHeaders.CONTENT_TYPE, ignoreCase = true)) {
                    shortcut.headers.add(realm.copyToRealm(Header(key = key, value = value)))
                }
            }
        }

        private fun prepareParameters(realm: Realm, curlCommand: CurlCommand, shortcut: Shortcut) {
            curlCommand.data.forEach { potentialParameter ->
                potentialParameter.split("=")
                    .takeIf { it.size == 2 }
                    ?.let { parameterParts ->
                        val key = parameterParts[0]
                        val value = parameterParts[1]
                        val parameter = if (value.startsWith("@") && curlCommand.isFormData) {
                            Parameter(
                                key = decode(key),
                                type = Parameter.TYPE_FILE
                            )
                        } else {
                            Parameter(
                                key = decode(key),
                                value = decode(value)
                            )
                        }
                        shortcut.parameters.add(realm.copyToRealm(parameter))
                    }
            }
        }

        private fun decode(text: String): String =
            try {
                URLDecoder.decode(text, "utf-8")
            } catch (e: IllegalArgumentException) {
                text
            }

    }

}