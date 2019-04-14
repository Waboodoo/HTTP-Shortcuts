package ch.rmy.android.http_shortcuts.utils

import android.app.Activity
import android.app.Dialog
import androidx.appcompat.app.AlertDialog
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
import com.afollestad.materialdialogs.MaterialDialog

fun MaterialDialog.Builder.showIfPossible(): MaterialDialog? {
    if ((context as? Activity)?.isFinishing == true) {
        return null
    }
    return show()
}

fun MenuDialogBuilder.showIfPossible(): MaterialDialog? {
    if ((context as? Activity)?.isFinishing == true) {
        return null
    }
    return show()
}

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