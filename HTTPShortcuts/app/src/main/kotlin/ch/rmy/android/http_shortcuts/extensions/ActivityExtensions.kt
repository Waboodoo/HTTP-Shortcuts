package ch.rmy.android.http_shortcuts.extensions

import android.app.Activity
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar

fun Activity.showSnackbar(@StringRes message: Int, long: Boolean = false) {
    showSnackbar(getString(message), long)
}

fun Activity.showSnackbar(message: CharSequence, long: Boolean = false) {
    val baseView = findViewById<ViewGroup>(android.R.id.content).getChildAt(0)
    Snackbar.make(baseView, message, if (long) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT).show()
}

fun Fragment.showSnackbar(@StringRes message: Int, long: Boolean = false) {
    activity?.showSnackbar(message, long)
}

fun Fragment.showSnackbar(message: CharSequence, long: Boolean = false) {
    activity?.showSnackbar(message, long)
}