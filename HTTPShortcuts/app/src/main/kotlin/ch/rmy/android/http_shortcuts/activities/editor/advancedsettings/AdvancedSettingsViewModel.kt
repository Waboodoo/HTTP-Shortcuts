package ch.rmy.android.http_shortcuts.activities.editor.advancedsettings

import android.app.Application
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.activities.editor.BasicShortcutEditorViewModel
import ch.rmy.android.http_shortcuts.data.Transactions
import ch.rmy.android.http_shortcuts.data.models.ClientCertParams
import ch.rmy.android.http_shortcuts.data.models.Shortcut
import ch.rmy.android.http_shortcuts.extensions.context
import ch.rmy.android.http_shortcuts.utils.StringUtils
import io.reactivex.Completable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class AdvancedSettingsViewModel(application: Application) : BasicShortcutEditorViewModel(application) {

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

    fun setTimeout(timeout: Duration): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.timeout = timeout.inWholeMilliseconds.toInt()
        }

    fun setAdvancedSettings(host: String, port: Int?, ssid: String): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.let { shortcut ->
                shortcut.proxyHost = host.trim().takeUnless { it.isEmpty() }
                shortcut.proxyPort = port
                shortcut.wifiSsid = ssid.trim()
            }
        }

    fun getTimeoutSubtitle(shortcut: Shortcut): CharSequence =
        getTimeoutText(shortcut.timeout.milliseconds)

    fun getTimeoutText(timeout: Duration) =
        StringUtils.getDurationText(context, timeout)

    fun setClientCertParams(clientCertParams: ClientCertParams?): Completable =
        Transactions.commit { realm ->
            getShortcut(realm)?.clientCertParams = clientCertParams
        }

    fun getClientCertSubtitle(shortcut: Shortcut) =
        shortcut.clientCertParams.let { clientCertParams ->
            when (clientCertParams) {
                is ClientCertParams.Alias -> context.getString(R.string.label_subtitle_client_cert_in_use, clientCertParams.alias)
                is ClientCertParams.File -> context.getString(R.string.label_subtitle_client_cert_file_in_use)
                else -> context.getString(R.string.label_subtitle_no_client_cert)
            }
        }
}
