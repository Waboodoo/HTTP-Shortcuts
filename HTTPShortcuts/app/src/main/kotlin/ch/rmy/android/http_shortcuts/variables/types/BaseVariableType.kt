package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import androidx.fragment.app.FragmentManager
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.dialogs.DialogBuilder
import ch.rmy.android.http_shortcuts.extensions.cancel
import ch.rmy.android.http_shortcuts.extensions.mapIf
import io.reactivex.SingleEmitter

abstract class BaseVariableType {

    val tag: String
        get() = javaClass.simpleName

    fun getEditorFragment(fragmentManager: FragmentManager): VariableEditorFragment {
        val fragment = fragmentManager.findFragmentByTag(tag)
        if (fragment != null) {
            return fragment as VariableEditorFragment
        }
        return createEditorFragment()
    }

    protected open fun createEditorFragment() = VariableEditorFragment()

    companion object {

        internal fun createDialogBuilder(context: Context, variable: Variable, emitter: SingleEmitter<String>) =
            DialogBuilder(context)
                .mapIf(!variable.title.isNullOrEmpty()) {
                    it.title(variable.title)
                }
                .dismissListener {
                    emitter.cancel()
                }

    }

}
