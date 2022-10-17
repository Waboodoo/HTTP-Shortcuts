package ch.rmy.android.http_shortcuts.scripting.actions.types

import ch.rmy.android.framework.extensions.takeUnlessEmpty
import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.scripting.ExecutionContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CopyToClipboardAction(private val text: String) : BaseAction() {

    @Inject
    lateinit var clipboardUtil: ClipboardUtil

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun execute(executionContext: ExecutionContext) {
        withContext(Dispatchers.Main) {
            text
                .takeUnlessEmpty()
                ?.let(clipboardUtil::copyToClipboard)
        }
    }
}
