package ch.rmy.android.http_shortcuts.extensions

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import ch.rmy.android.http_shortcuts.R
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

fun Activity.sendMail(address: String, subject: String, text: String, title: String) {
    try {
        Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$address"))
            .putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
            .putExtra(Intent.EXTRA_SUBJECT, subject)
            .putExtra(Intent.EXTRA_TEXT, text)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            .let {
                Intent.createChooser(it, title)
            }
            .startActivity(this)
    } catch (e: ActivityNotFoundException) {
        showToast(R.string.error_not_supported)
    }
}