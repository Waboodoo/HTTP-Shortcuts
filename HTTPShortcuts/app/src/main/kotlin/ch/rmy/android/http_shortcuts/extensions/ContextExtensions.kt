package ch.rmy.android.http_shortcuts.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
import ch.rmy.android.http_shortcuts.R

fun Context.showToast(message: String, long: Boolean = false) {
    Toast.makeText(this, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}

fun Context.showToast(@StringRes message: Int, long: Boolean = false) {
    Toast.makeText(this, message, if (long) Toast.LENGTH_LONG else Toast.LENGTH_SHORT).show()
}

fun Context.openURL(url: String) {
    try {
        Intent(Intent.ACTION_VIEW, Uri.parse(url))
            .startActivity(this)
    } catch (e: ActivityNotFoundException) {
        showToast(R.string.error_not_supported)
    }
}

fun Context.isDarkThemeEnabled() =
    resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES