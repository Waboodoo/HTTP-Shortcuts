package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ClipboardType : BaseVariableType() {

    @Inject
    lateinit var clipboardUtil: ClipboardUtil

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override suspend fun resolveValue(context: Context, variable: VariableModel) =
        withContext(Dispatchers.Main) {
            clipboardUtil.getFromClipboard()
                ?.toString()
                .orEmpty()
        }
}
