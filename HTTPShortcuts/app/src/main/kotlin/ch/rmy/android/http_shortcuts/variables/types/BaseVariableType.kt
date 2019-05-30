package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import android.text.TextUtils
import androidx.fragment.app.FragmentManager
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
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
            MenuDialogBuilder(context)
                .mapIf(!TextUtils.isEmpty(variable.title)) {
                    it.title(variable.title)
                }
                .dismissListener {
                    emitter.cancel()
                }

    }

}
