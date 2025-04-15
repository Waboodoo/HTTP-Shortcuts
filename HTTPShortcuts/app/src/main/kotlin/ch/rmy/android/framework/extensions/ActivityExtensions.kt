package ch.rmy.android.framework.extensions

import android.app.Activity

fun Activity.finishWithoutAnimation() {
    overridePendingTransition(0, 0)
    finish()
    overridePendingTransition(0, 0)
}
