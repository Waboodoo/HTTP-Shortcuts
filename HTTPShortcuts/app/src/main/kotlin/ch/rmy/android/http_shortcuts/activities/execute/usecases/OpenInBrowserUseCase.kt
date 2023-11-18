package ch.rmy.android.http_shortcuts.activities.execute.usecases

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.SHARE_STATE_OFF
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.applyIfNotNull
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.dtos.TargetBrowser
import ch.rmy.android.http_shortcuts.exceptions.BrowserNotFoundException
import ch.rmy.android.http_shortcuts.exceptions.InvalidUrlException
import ch.rmy.android.http_shortcuts.exceptions.UserException
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.Validation
import javax.inject.Inject

class OpenInBrowserUseCase
@Inject
constructor(
    private val activityProvider: ActivityProvider,
) {

    suspend operator fun invoke(url: String, targetBrowser: TargetBrowser) {
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

            activityProvider.withActivity { activity ->
                when (targetBrowser) {
                    is TargetBrowser.Browser -> {
                        Intent(Intent.ACTION_VIEW, uri)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            .runIfNotNull(targetBrowser.packageName) {
                                setPackage(it)
                            }
                            .startActivity(activity)
                    }
                    is TargetBrowser.CustomTabs -> {
                        val intent = CustomTabsIntent.Builder()
                            .setShareState(SHARE_STATE_OFF)
                            .build()
                            .applyIfNotNull(targetBrowser.packageName) {
                                intent.setPackage(it)
                            }
                        intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.launchUrl(activity, uri)
                    }
                }
            }
        } catch (e: ActivityNotFoundException) {
            targetBrowser.packageName?.let {
                throw BrowserNotFoundException(it)
            }
            throw UserException.create {
                getString(R.string.error_no_app_found_for_url, url)
            }
        }
    }
}
