package ch.rmy.android.framework.extensions

import android.app.Activity
import android.content.Intent
import androidx.annotation.StringRes
import ch.rmy.android.framework.utils.SnackbarManager

fun Activity.showSnackbar(@StringRes message: Int, long: Boolean = false) {
    showSnackbar(getText(message), long)
}

fun Activity.showSnackbar(message: CharSequence, long: Boolean = false) {
    SnackbarManager.showSnackbar(this, message, long)
}

fun Activity.finishWithoutAnimation() {
    overridePendingTransition(0, 0)
    finish()
    overridePendingTransition(0, 0)
}

fun Activity.restartWithoutAnimation() {
    overridePendingTransition(0, 0)
    finish()
    startActivity(Intent(this, this::class.java))
    overridePendingTransition(0, 0)
}
