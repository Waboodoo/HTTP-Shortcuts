package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

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
                    Intent(Intent.ACTION_VIEW, uri)
                        .startActivity(activity)
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
    )
}
