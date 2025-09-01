package ch.rmy.android.framework.extensions

import android.content.Context
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
