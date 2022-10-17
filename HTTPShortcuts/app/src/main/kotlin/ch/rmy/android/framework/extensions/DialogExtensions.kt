package ch.rmy.android.framework.extensions

import android.app.Activity
import android.app.Dialog
import android.view.WindowManager

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

fun <T : Dialog> T.showOrElse(block: () -> Unit) {
    showIfPossible() ?: block()
}
