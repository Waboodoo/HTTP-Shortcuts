package ch.rmy.android.http_shortcuts.utils

import android.app.Activity
import android.app.Dialog
import android.support.v7.app.AlertDialog
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
import com.afollestad.materialdialogs.MaterialDialog

fun MaterialDialog.Builder.showIfPossible(): Boolean {
    if ((context as? Activity)?.isFinishing == true) {
        return false
    }
    this.show()
    return true
}

fun MenuDialogBuilder.showIfPossible(): Boolean {
    if ((context as? Activity)?.isFinishing == true) {
        return false
    }
    this.show()
    return true
}

fun AlertDialog.Builder.showIfPossible(): Boolean {
    if ((context as? Activity)?.isFinishing == true) {
        return false
    }
    this.show()
    return true
}

fun Dialog.showIfPossible(): Boolean {
    if ((context as? Activity)?.isFinishing == true) {
        return false
    }
    this.show()
    return true
}