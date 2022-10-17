package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.core.net.toUri
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OpenURLAction(private val url: String) : BaseAction() {

    override suspend fun execute(executionContext: ExecutionContext) {
        withContext(Dispatchers.Main) {
            val uri = url.toUri()
            if (uri.scheme?.equals("file", ignoreCase = true) == true) {
                throw ActionException {
                    "Opening files using openUrl() is not supported"
                }
            }
            try {
                Intent(Intent.ACTION_VIEW, uri)
                    .startActivity(executionContext.context)
            } catch (e: ActivityNotFoundException) {
                throw ActionException { context ->
                    context.getString(R.string.error_no_app_found_for_url, url)
                }
            }
        }
    }
}
