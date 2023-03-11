package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.models.Variable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ClipboardType : BaseVariableType() {

    @Inject
    lateinit var clipboardUtil: ClipboardUtil

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun resolveValue(variable: Variable) =
        withContext(Dispatchers.Main) {
            clipboardUtil.getFromClipboard()
                ?.toString()
                .orEmpty()
        }
}
