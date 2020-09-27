package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import android.app.Application
import ch.rmy.android.http_shortcuts.activities.editor.BasicShortcutEditorViewModel
import ch.rmy.android.http_shortcuts.data.Transactions
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.context
import ch.rmy.android.http_shortcuts.utils.StringUtils
import io.reactivex.Completable

class AdvancedSettingsViewModel(application: Application) : BasicShortcutEditorViewModel(application) {

    fun setWaitForConnection(waitForConnection: Boolean): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.isWaitForNetwork = waitForConnection
        }

    fun setFollowRedirects(followRedirects: Boolean): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.followRedirects = followRedirects
        }

    fun setAcceptAllCertificates(acceptAllCertificates: Boolean): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.acceptAllCertificates = acceptAllCertificates
        }

    fun setAcceptCookies(acceptCookies: Boolean): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.acceptCookies = acceptCookies
        }

    fun setTimeout(timeout: Int): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.timeout = timeout
        }

    fun setProxy(host: String, port: Int?): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.let { shortcut ->
                shortcut.proxyHost = host.trim().takeUnless { it.isEmpty() }
                shortcut.proxyPort = port
            }
        }

    fun setSsid(ssid: String): Completable =
            Transactions.commit { realm ->
                getShortcut(realm)?.ssid = ssid.trim()
            }

    fun getTimeoutSubtitle(shortcut: Shortcut): CharSequence =
        getTimeoutText(shortcut.timeout)

    fun getTimeoutText(timeout: Int) =
        StringUtils.getDurationText(context, timeout)
}