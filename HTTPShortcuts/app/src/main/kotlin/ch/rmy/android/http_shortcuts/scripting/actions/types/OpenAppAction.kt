package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.ActivityNotFoundException
import android.content.Context
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OpenAppAction
@Inject
constructor(
    private val context: Context,
    private val activityProvider: ActivityProvider,
) : Action<OpenAppAction.Params> {
    override suspend fun Params.execute(executionContext: ExecutionContext) {
        withContext(Dispatchers.Main) {
            try {
                activityProvider.withActivity { activity ->
                    context.packageManager.getLaunchIntentForPackage(packageName)
                        ?.startActivity(activity)
                        ?: throwUnsupportedError()
                }
            } catch (e: ActivityNotFoundException) {
                throwUnsupportedError()
            }
        }
    }

    private fun throwUnsupportedError(): Nothing {
        throw ActionException {
            getString(R.string.error_no_app_found, packageName)
        }
    }

    data class Params(
        val packageName: String,
    )
}
