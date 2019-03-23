package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import android.text.TextUtils
import androidx.fragment.app.FragmentManager
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.extensions.mapIf
import ch.rmy.android.http_shortcuts.extensions.rejectSafely
import ch.rmy.android.http_shortcuts.realm.models.Variable
import org.jdeferred2.Deferred

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

        internal fun createDialogBuilder(context: Context, variable: Variable, deferred: Deferred<String, Unit, Unit>) =
            MenuDialogBuilder(context)
                .mapIf(!TextUtils.isEmpty(variable.title)) {
                    it.title(variable.title)
                }
                .dismissListener {
                    deferred.rejectSafely(Unit)
                }

    }

}
