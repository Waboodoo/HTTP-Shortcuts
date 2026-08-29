package ch.rmy.android.http_shortcuts.activities.execute.usecases

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsIntent.SHARE_STATE_ON
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.applyIfNotNull
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.dtos.TargetBrowser
import ch.rmy.android.http_shortcuts.exceptions.InvalidUrlException
import ch.rmy.android.http_shortcuts.extensions.userError
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
                userError {
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
                            .setShareState(SHARE_STATE_ON)
                            .build()
                            .applyIfNotNull(targetBrowser.packageName) {
                                intent.setPackage(it)
                            }
                        intent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        intent.launchUrl(activity, uri)
                    }
                }
            }
        } catch (_: ActivityNotFoundException) {
            userError {
                if (targetBrowser.packageName != null) {
                    getString(R.string.error_browser_not_found_or_cannot_handle_url, targetBrowser.packageName)
                } else {
                    getString(R.string.error_no_app_found_for_url, url)
                }
            }
        } catch (_: SecurityException) {
            userError {
                getString(R.string.error_permission_required_for_url, url)
            }
        }
    }
}
