package ch.rmy.android.framework.extensions

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.annotation.UiThread
import androidx.core.net.toUri
import ch.rmy.android.http_shortcuts.R

@UiThread
fun Context.showToast(message: CharSequence, long: Boolean = false) {
    Toast.makeText(this, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}

@UiThread
fun Context.showToast(@StringRes message: Int, long: Boolean = false) {
    Toast.makeText(this, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}

fun Context.openURL(url: Uri) {
    try {
        Intent(Intent.ACTION_VIEW, url)
            .startActivity(this)
    } catch (e: ActivityNotFoundException) {
        showToast(R.string.error_not_supported)
    }
}

fun Context.openURL(url: String) {
    openURL(url.toUri())
}

fun Context.isDarkThemeEnabled() =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

fun Context.getActivity(): Activity? {
    if (this is Activity) {
        return this
    }
    var context = this
    while (context is ContextWrapper) {
        context = context.baseContext
        if (context is Activity) {
            return context
        }
    }
    return null
}
