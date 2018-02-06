package ch.rmy.android.http_shortcuts.dialogs

import android.content.Context
import android.widget.CheckBox
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.utils.Settings
import ch.rmy.android.http_shortcuts.utils.showIfPossible
import com.afollestad.materialdialogs.MaterialDialog

class IconNameChangeDialog(private val context: Context) {

    private val settings: Settings = Settings(context)

    fun show(callback: MaterialDialog.SingleButtonCallback) {
        MaterialDialog.Builder(context)
                .positiveText(R.string.dialog_ok)
                .onPositive(callback)
                .customView(R.layout.dialog_icon_name_changes, true)
                .cancelable(false)
                .canceledOnTouchOutside(false)
                .build()
                .also {
                    val checkBox = it.findViewById(R.id.checkbox_do_not_show_again) as CheckBox
                    checkBox.setOnCheckedChangeListener { _, isChecked ->
                        settings.isIconNameWarningPermanentlyHidden = isChecked
                    }
                }
                .showIfPossible()
    }

    fun shouldShow() = !settings.isIconNameWarningPermanentlyHidden

}
