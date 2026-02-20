package ch.rmy.android.http_shortcuts.extensions

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.FileUriExposedException
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.showToast
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.R

fun Context.openURL(url: Uri) {
    try {
        Intent(Intent.ACTION_VIEW, url)
            .startActivity(this)
    } catch (_: ActivityNotFoundException) {
        showToast(getString(R.string.error_no_app_found_for_url, url.toString()))
    } catch (_: SecurityException) {
        showToast(getString(R.string.error_permission_required_for_url, url.toString()))
    } catch (_: FileUriExposedException) {
        showToast(R.string.error_unsupported_file_url)
    }
}

fun Context.openURL(url: String) {
    openURL(url.toUri())
}
