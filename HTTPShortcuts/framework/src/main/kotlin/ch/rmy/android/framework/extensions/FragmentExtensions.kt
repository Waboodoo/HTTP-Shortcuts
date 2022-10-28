package ch.rmy.android.framework.extensions

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment

inline fun <T : Fragment> T.addArguments(block: Bundle.() -> Unit): T =
    apply {
        arguments = Bundle().apply(block)
    }

fun Fragment.showSnackbar(@StringRes message: Int, long: Boolean = false) {
    activity?.showSnackbar(message, long)
}

fun Fragment.showSnackbar(message: CharSequence, long: Boolean = false) {
    activity?.showSnackbar(message, long)
}
