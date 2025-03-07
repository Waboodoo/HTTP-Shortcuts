package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.os.Build
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        context.packageManager.getLaunchIntentSenderForPackage(packageName)
                            .sendIntent(activity, 0, null, null, null)
                    } else {
                        context.packageManager.getLaunchIntentForPackage(packageName)
                            ?.startActivity(activity)
                            ?: throwUnsupportedError(packageName)
                    }
                }
            } catch (e: ActivityNotFoundException) {
                throwUnsupportedError(packageName)
            } catch (e: SendIntentException) {
                throwUnsupportedError(packageName)
            }
        }
    }

    private fun throwUnsupportedError(packageName: String): Nothing {
        throw ActionException {
            getString(R.string.error_no_app_found, packageName)
        }
    }

    data class Params(
        val packageName: String,
    )
}
