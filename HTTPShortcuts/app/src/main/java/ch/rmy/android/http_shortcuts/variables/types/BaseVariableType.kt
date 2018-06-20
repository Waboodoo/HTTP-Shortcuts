package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import android.support.v4.app.FragmentManager
import android.text.TextUtils
import ch.rmy.android.http_shortcuts.dialogs.MenuDialogBuilder
import ch.rmy.android.http_shortcuts.realm.models.Variable
import ch.rmy.android.http_shortcuts.utils.mapIf
import ch.rmy.android.http_shortcuts.utils.rejectSafely
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
