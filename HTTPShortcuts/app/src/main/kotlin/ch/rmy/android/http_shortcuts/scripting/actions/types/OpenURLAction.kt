package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.applyIfNotNull
import ch.rmy.android.framework.extensions.runIfNotNull
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.data.dtos.TargetBrowser
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OpenURLAction
@Inject
constructor(
    private val activityProvider: ActivityProvider,
) : Action<OpenURLAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        withContext(Dispatchers.Main) {
            val uri = url.toUri()
            if (uri.scheme?.equals("file", ignoreCase = true) == true) {
                throw ActionException {
                    getString(R.string.error_opening_files_using_open_url_not_supported)
                }
            }
            try {
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
                                .setShareState(CustomTabsIntent.SHARE_STATE_ON)
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
                throw ActionException {
                    getString(R.string.error_no_app_found_for_url, url)
                }
            }
        }
    }

    data class Params(
        val url: String,
        val targetBrowser: TargetBrowser,
    )
}
