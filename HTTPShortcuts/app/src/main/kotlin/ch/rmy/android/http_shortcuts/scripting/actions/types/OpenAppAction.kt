package ch.rmy.android.http_shortcuts.scripting.actions.types

import android.content.ActivityNotFoundException
import android.content.Context
import ch.rmy.android.framework.extensions.startActivity
import ch.rmy.android.http_shortcuts.R
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.exceptions.ActionException
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OpenAppAction(private val packageName: String) : BaseAction() {

    @Inject
    lateinit var context: Context

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext) {
        withContext(Dispatchers.Main) {
            try {
                context.packageManager.getLaunchIntentForPackage(packageName)
                    ?.startActivity(activityProvider.getActivity())
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
