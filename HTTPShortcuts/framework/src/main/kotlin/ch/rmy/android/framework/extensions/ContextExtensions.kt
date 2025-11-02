package ch.rmy.android.framework.extensions

import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.annotation.UiThread

@UiThread
fun Context.showToast(message: CharSequence, long: Boolean = false) {
    Toast.makeText(this, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}

@UiThread
fun Context.showToast(@StringRes message: Int, long: Boolean = false) {
    Toast.makeText(this, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}

fun Context.isDarkThemeEnabled() =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
