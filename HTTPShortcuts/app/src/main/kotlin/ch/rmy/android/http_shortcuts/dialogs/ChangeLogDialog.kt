package ch.rmy.android.http_shortcuts.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.view.LayoutInflater
import android.webkit.WebView
import android.widget.CheckBox
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.resolveSafely
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import com.afollestad.materialdialogs.MaterialDialog
import org.jdeferred2.Promise
import org.jdeferred2.impl.DeferredObject

class ChangeLogDialog(private val context: Context, private val whatsNew: Boolean): Dialog {

    private val settings: Settings = Settings(context)

    override fun shouldShow(): Boolean {
        if (isPermanentlyHidden) {
            return false
        }
        val lastSeenVersion = settings.changeLogLastVersion
        return version != lastSeenVersion && lastSeenVersion != 0
    }

    private val isPermanentlyHidden: Boolean
        get() = settings.isChangeLogPermanentlyHidden

    @SuppressLint("InflateParams")
    override fun show(): Promise<Unit, Unit, Unit> {
        settings.changeLogLastVersion = version

        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.changelog_dialog, null)
        val webview = view.findViewById<WebView>(R.id.changelog_webview)
        val showAtStartupCheckbox = view.findViewById<CheckBox>(R.id.checkbox_show_at_startup)

        val deferred = DeferredObject<Unit, Unit, Unit>()

        val showing = MaterialDialog.Builder(context)
            .customView(view, false)
            .title(if (whatsNew) R.string.changelog_title_whats_new else R.string.changelog_title)
            .positiveText(android.R.string.ok)
            .dismissListener {
                deferred.resolveSafely(Unit)
            }
            .showIfPossible()

        if (!showing) {
            deferred.resolveSafely(Unit)
        }

        webview.loadUrl(CHANGELOG_ASSET_URL)

        showAtStartupCheckbox.isChecked = !isPermanentlyHidden
        showAtStartupCheckbox.setOnCheckedChangeListener { _, isChecked ->
            settings.isChangeLogPermanentlyHidden = !isChecked
        }

        return deferred.promise()
    }

    private val version
        get() = try {
            context.packageManager
                .getPackageInfo(context.packageName, 0)
                .versionCode / 1000000
        } catch (e: NameNotFoundException) {
            0
        }

    companion object {

        private const val CHANGELOG_ASSET_URL = "file:///android_asset/changelog.html"

    }

}
