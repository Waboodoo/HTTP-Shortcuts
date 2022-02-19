package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.ActivityNotFoundException
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import io.reactivex.Completable

class OpenAppAction(private val packageName: String) : BaseAction() {

    override fun execute(executionContext: ExecutionContext): Completable =
        Completable.fromAction {
            try {
                executionContext.context.packageManager.getLaunchIntentForPackage(packageName)
                    ?.startActivity(executionContext.context)
                    ?: throw ActionException { context ->
                        context.getString(R.string.error_no_app_found, packageName)
                    }
            } catch (e: ActivityNotFoundException) {
                throw ActionException { context ->
                    context.getString(R.string.error_no_app_found, packageName)
                }
            }
        }
}
