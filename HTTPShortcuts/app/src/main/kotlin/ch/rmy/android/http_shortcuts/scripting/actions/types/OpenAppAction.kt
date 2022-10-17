package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.ActivityNotFoundException
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class OpenAppAction(private val packageName: String) : BaseAction() {

    override suspend fun execute(executionContext: ExecutionContext) {
        withContext(Dispatchers.Main) {
            try {
                executionContext.context.packageManager.getLaunchIntentForPackage(packageName)
                    ?.startActivity(executionContext.context)
                    ?: throwUnsupportedError()
            } catch (e: ActivityNotFoundException) {
                throwUnsupportedError()
            }
        }
    }

    private fun throwUnsupportedError(): Nothing {
        throw ActionException { context ->
            context.getString(R.string.error_no_app_found, packageName)
        }
    }
}
