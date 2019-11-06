package ch.rmy.android.http_shortcuts.extensions

import android.app.Activity
import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import com.afollestad.materialdialogs.MaterialDialog

fun AlertDialog.Builder.showIfPossible(): AlertDialog? {
    if ((context as? Activity)?.isFinishing == true) {
        return null
    }
    return show()
}

fun Dialog.showIfPossible(): Dialog? {
    if ((context as? Activity)?.isFinishing == true) {
        return null
    }
    this.show()
    return this
}

fun MaterialDialog.showIfPossible(): Dialog? {
    if ((context as? Activity)?.isFinishing == true) {
        return null
    }
    this.show()
    return this
}