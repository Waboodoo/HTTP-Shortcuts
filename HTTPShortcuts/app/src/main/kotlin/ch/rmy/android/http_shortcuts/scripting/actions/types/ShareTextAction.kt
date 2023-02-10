package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.extensions.truncate
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import ch.rmy.android.http_shortcuts.utils.ActivityProvider
import ch.rmy.android.http_shortcuts.utils.ShareUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ShareTextAction(private val text: String) : BaseAction() {

    @Inject
    lateinit var activityProvider: ActivityProvider

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext) {
        withContext(Dispatchers.Main) {
            text
                .takeUnlessEmpty()
                ?.let {
                    ShareUtil.shareText(
                        activityProvider.getActivity(),
                        text.truncate(MAX_LENGTH),
                    )
                }
        }
    }

    companion object {
        private const val MAX_LENGTH = 200_000
    }
}
