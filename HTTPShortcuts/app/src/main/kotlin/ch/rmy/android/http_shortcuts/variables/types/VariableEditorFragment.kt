package ch.rmy.android.http_shortcuts.variables.types

import ch.rmy.android.http_shortcuts.activities.BaseFragment
import ch.rmy.android.http_shortcuts.activities.variables.VariableEditorActivity
import ch.rmy.android.http_shortcuts.data.Controller
import ch.rmy.android.http_shortcuts.data.models.Variable
import ch.rmy.android.http_shortcuts.extensions.toLiveData
import ch.rmy.android.http_shortcuts.variables.VariablePlaceholderProvider

open class VariableEditorFragment : BaseFragment() {

    protected val controller by lazy { destroyer.own(Controller()) }
    protected val variablePlaceholderProvider by lazy {
        VariablePlaceholderProvider(controller.getVariables().toLiveData())
    }

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
