package ch.rmy.android.framework.extensions

import android.app.Activity
import android.content.Intent

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
