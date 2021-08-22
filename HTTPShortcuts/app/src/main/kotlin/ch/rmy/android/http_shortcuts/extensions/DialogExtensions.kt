package ch.rmy.android.http_shortcuts.extensions

import android.app.Activity
import android.app.Dialog
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog

fun AlertDialog.Builder.showIfPossible(): AlertDialog? {
    if ((context as? Activity)?.isFinishing == true) {
        return null
    }
    return tryOrLog {
        try {
            show()
        } catch (e: WindowManager.BadTokenException) {
            null
        }
    }
}

fun <T : Dialog> T.showIfPossible(): T? {
    if ((context as? Activity)?.isFinishing == true) {
        return null
    }
    return tryOrLog {
        try {
            show()
            this
        } catch (e: WindowManager.BadTokenException) {
            null
        }
    }
}
