package ch.rmy.android.http_shortcuts.utils

import android.support.annotation.StringRes
import android.support.v4.app.Fragment
import android.view.View
import ch.rmy.android.http_shortcuts.R
import com.afollestad.materialdialogs.MaterialDialog

var View.visible: Boolean
    get() = this.visibility == View.VISIBLE
    set(value) {
        this.visibility = if (value) {
            View.VISIBLE
        } else {
            View.GONE
        }
    }

fun Fragment.showMessageDialog(@StringRes stringRes: Int) {
    MaterialDialog.Builder(context!!)
            .content(stringRes)
            .positiveText(R.string.dialog_ok)
            .show()
}