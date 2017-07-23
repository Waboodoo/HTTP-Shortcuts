package ch.rmy.android.http_shortcuts.variables.types

import android.content.Context
import android.support.v4.app.FragmentManager
import android.text.TextUtils
import ch.rmy.android.http_shortcuts.realm.models.Variable
import com.afollestad.materialdialogs.MaterialDialog
import org.jdeferred.Deferred

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

    protected open fun createEditorFragment(): VariableEditorFragment {
        return VariableEditorFragment()
    }

    companion object {

        internal fun createDialogBuilder(context: Context, variable: Variable, deferred: Deferred<String, Void, Void>): MaterialDialog.Builder {
            val dialogBuilder = MaterialDialog.Builder(context)
            if (!TextUtils.isEmpty(variable.title)) {
                dialogBuilder.title(variable.title!!)
            }
            dialogBuilder.dismissListener {
                if (deferred.isPending) {
                    deferred.reject(null)
                }
            }
            return dialogBuilder
        }
    }

}
