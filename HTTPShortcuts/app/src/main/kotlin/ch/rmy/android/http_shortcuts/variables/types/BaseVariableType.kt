package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import androidx.fragment.app.FragmentManager
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.cancel
import ch.rmy.android.http_shortcuts.extensions.mapIf
import io.reactivex.Single
import io.reactivex.SingleEmitter

abstract class BaseVariableType {

    val tag: String
        get() = javaClass.simpleName

    fun getEditorFragment(fragmentManager: FragmentManager): VariableEditorFragment<*> =
        fragmentManager.findFragmentByTag(tag) as? VariableEditorFragment<*>?
            ?: createEditorFragment()

    protected abstract fun createEditorFragment(): VariableEditorFragment<*>

    abstract fun resolveValue(context: Context, variable: Variable): Single<String>

    companion object {

        internal fun createDialogBuilder(
            context: Context,
            variable: Variable,
            emitter: SingleEmitter<String>,
        ) =
            DialogBuilder(context)
                .mapIf(variable.title.isNotEmpty()) {
                    title(variable.title)
                }
                .dismissListener {
                    emitter.cancel()
                }
    }
}
