package ch.rmy.android.http_shortcuts.activities.editor

import android.app.Application
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.Repository
import ch.rmy.android.http_shortcuts.data.Transactions
import ch.rmy.android.http_shortcuts.data.models.Header
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.data.models.Shortcut.Companion.TEMPORARY_ID
import ch.rmy.android.http_shortcuts.extensions.getQuantityString
import ch.rmy.android.http_shortcuts.extensions.getString
import ch.rmy.android.http_shortcuts.icons.Icons
import ch.rmy.android.http_shortcuts.utils.UUIDUtils.newUUID
import ch.rmy.android.http_shortcuts.utils.Validation
import ch.rmy.curlcommand.CurlCommand
import io.reactivex.Completable
import io.reactivex.Single
import io.realm.Realm
import org.apache.http.HttpHeaders

class ShortcutEditorViewModel(application: Application) : BasicShortcutEditorViewModel(application) {

    fun init(categoryId: String?, shortcutId: String?, curlCommand: CurlCommand?, createBrowserShortcut: Boolean): Completable {
        if (isInitialized) {
            return Completable.complete()
        }
        this.categoryId = categoryId
        this.shortcutId = shortcutId
        return Transactions.commit { realm ->
            val shortcut = if (shortcutId == null) {
                realm.copyToRealmOrUpdate(Shortcut.createNew(
                    id = TEMPORARY_ID,
                    iconName = Icons.getDefaultIcon(getApplication()),
                    browserShortcut = createBrowserShortcut
                ))
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

    var isInitialized: Boolean = false
        private set

    private var categoryId: String? = null
    private var shortcutId: String? = null

    fun hasChanges(): Boolean {
        val oldShortcut = shortcutId
            ?.let { Repository.getShortcutById(persistedRealm, it)!! }
            ?: Shortcut.createNew(iconName = Icons.getDefaultIcon(getApplication()))
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
        var name: String = ""
        var iconName: String? = null
        val nameOrIconChanged = hasNameOrIconChanges()
        return Transactions.commit { realm ->
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
            .andThen(Single.create {
                it.onSuccess(SaveResult(
                    id = id,
                    name = name,
                    iconName = iconName,
                    nameOrIconChanged = nameOrIconChanged
                ))
            })
    }

    private fun hasNameOrIconChanges(): Boolean {
        val oldShortcut = shortcutId
            ?.let { Repository.getShortcutById(persistedRealm, it)!! }
            ?: return false
        val newShortcut = getShortcut(persistedRealm) ?: return false
        return oldShortcut.name != newShortcut.name || oldShortcut.iconName != newShortcut.iconName
    }

    private fun validateShortcut(shortcut: Shortcut) {
        if (shortcut.name.isBlank()) {
            throw ShortcutValidationError(VALIDATION_ERROR_EMPTY_NAME)
        }
        if (!Validation.isAcceptableUrl(shortcut.url)) {
            throw ShortcutValidationError(VALIDATION_ERROR_INVALID_URL)
        }
    }

    // TODO: Find a way to not having to pass in 'shortcut'
    fun getBasicSettingsSubtitle(shortcut: Shortcut): CharSequence =
        if (shortcut.isBrowserShortcut) {
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
                Shortcut.REQUEST_BODY_TYPE_X_WWW_FORM_URLENCODE -> getQuantityString(
                    shortcut.parameters.size,
                    R.string.subtitle_request_body_params_none,
                    R.plurals.subtitle_request_body_params_pattern
                )
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
            else -> getString(R.string.subtitle_authentication_none)
        }

    data class SaveResult(val id: String, val name: String, val iconName: String?, val nameOrIconChanged: Boolean)

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
            shortcut.bodyContent = curlCommand.data
            shortcut.requestBodyType = Shortcut.REQUEST_BODY_TYPE_CUSTOM_TEXT
            curlCommand.headers.entries
                .find { it.key.equals(HttpHeaders.CONTENT_TYPE, ignoreCase = true) }
                ?.let {
                    shortcut.contentType = it.value
                }
            curlCommand.headers.forEach { (key, value) ->
                if (!key.equals(HttpHeaders.CONTENT_TYPE, ignoreCase = true)) {
                    shortcut.headers.add(realm.copyToRealm(Header(key = key, value = value)))
                }
            }
        }

    }

}