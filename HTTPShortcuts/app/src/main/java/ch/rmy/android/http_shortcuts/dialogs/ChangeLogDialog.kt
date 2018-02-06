package ch.rmy.android.http_shortcuts.dialogs

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.TextView
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.HTMLUtil
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import com.afollestad.materialdialogs.MaterialDialog

class ChangeLogDialog(private val context: Context, private val whatsNew: Boolean) {

    private val settings: Settings = Settings(context)

    fun shouldShow(): Boolean {
        if (isPermanentlyHidden) {
            return false
        }
        val lastSeenVersion = settings.changeLogLastVersion
        return version != lastSeenVersion && lastSeenVersion != 0
    }

    private val isPermanentlyHidden: Boolean
        get() = settings.isChangeLogPermanentlyHidden

    @SuppressLint("InflateParams")
    fun show() {
        settings.changeLogLastVersion = version

        val layoutInflater = LayoutInflater.from(context)
        val view = layoutInflater.inflate(R.layout.changelog_dialog, null)
        val changelogText = view.findViewById<TextView>(R.id.changelog_text)
        val showAtStartupCheckbox = view.findViewById<CheckBox>(R.id.checkbox_show_at_startup)

        MaterialDialog.Builder(context)
                .customView(view, false)
                .title(if (whatsNew) R.string.changelog_title_whats_new else R.string.changelog_title)
                .positiveText(android.R.string.ok)
                .showIfPossible()

        changelogText.text = HTMLUtil.getHTML(context, R.string.changelog_text)
        showAtStartupCheckbox.isChecked = !isPermanentlyHidden
        showAtStartupCheckbox.setOnCheckedChangeListener { _, isChecked ->
            settings.isChangeLogPermanentlyHidden = !isChecked
        }
    }

    private val version
        get() = try {
            context.packageManager
                    .getPackageInfo(context.packageName, 0)
                    .versionCode / 1000000
        } catch (e: NameNotFoundException) {
            0
        }

}
