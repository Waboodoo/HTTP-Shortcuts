package ch.rmy.android.http_shortcuts.activities.execute.usecases

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.runIf
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.BrowserNotFoundException
import ch.rmy.android.http_shortcuts.exceptions.InvalidUrlException
import ch.rmy.android.http_shortcuts.exceptions.UnsupportedFeatureException
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.Validation
import javax.inject.Inject

class OpenInBrowserUseCase
@Inject
constructor(
    private val activityProvider: ActivityProvider,
) {

    operator fun invoke(url: String, browserPackageName: String) {
        try {
            val uri = url.toUri()
            if (!Validation.isValidUrl(uri)) {
                throw InvalidUrlException(url)
            }
            if (uri.scheme?.equals("file", ignoreCase = true) == true) {
                throw UserException.create {
                    getString(R.string.error_unsupported_file_url)
                }
            }
            Intent(Intent.ACTION_VIEW, uri)
                .runIf(browserPackageName.isNotEmpty()) {
                    setPackage(browserPackageName)
                }
                .startActivity(activityProvider.getActivity())
        } catch (e: ActivityNotFoundException) {
            if (browserPackageName.isNotEmpty()) {
                throw BrowserNotFoundException(browserPackageName)
            }
            throw UserException.create {
                getString(R.string.error_no_app_found_for_url, url)
            }
        }
    }
}
