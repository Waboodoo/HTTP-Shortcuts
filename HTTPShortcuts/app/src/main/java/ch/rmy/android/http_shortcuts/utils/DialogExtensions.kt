package ch.rmy.android.http_shortcuts.utils

import android.app.Activity
import android.app.Dialog
import android.support.v7.app.AlertDialog
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
import com.afollestad.materialdialogs.MaterialDialog

fun MaterialDialog.Builder.showIfPossible() {
    if ((context as? Activity)?.isFinishing == true) {
        return
    }
    this.show()
}

fun MenuDialogBuilder.showIfPossible() {
    if ((context as? Activity)?.isFinishing == true) {
        return
    }
    this.show()
}

fun AlertDialog.Builder.showIfPossible() {
    if ((context as? Activity)?.isFinishing == true) {
        return
    }
    this.show()
}

fun Dialog.showIfPossible() {
    if ((context as? Activity)?.isFinishing == true) {
        return
    }
    this.show()
}