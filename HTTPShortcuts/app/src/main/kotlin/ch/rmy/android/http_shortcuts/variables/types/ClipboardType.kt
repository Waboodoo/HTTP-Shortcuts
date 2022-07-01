package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import ch.rmy.android.framework.utils.ClipboardUtil
import ch.rmy.android.http_shortcuts.dagger.ApplicationComponent
import ch.rmy.android.http_shortcuts.data.models.VariableModel
import io.reactivex.Single
import javax.inject.Inject

class ClipboardType : BaseVariableType() {

    @Inject
    lateinit var clipboardUtil: ClipboardUtil

    override fun inject(applicationComponent: ApplicationComponent) {
        applicationComponent.inject(this)
    }

    override fun resolveValue(context: Context, variable: VariableModel) =
        Single.fromCallable {
            clipboardUtil.getFromClipboard()
                ?.toString()
                ?: ""
        }
}
