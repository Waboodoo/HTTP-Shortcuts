package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.activities.BaseFragment
import ch.rmy.android.http_shortcuts.activities.VariableEditorActivity
import ch.rmy.android.http_shortcuts.realm.models.Variable

open class VariableEditorFragment : BaseFragment() {

    override fun onStart() {
        super.onStart()
        (activity as VariableEditorActivity).onFragmentStarted()
    }

    open fun updateViews(variable: Variable) {

    }

    open fun compileIntoVariable(variable: Variable) {

    }

    open fun validate() = true

}
